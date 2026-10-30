package com.giftplatform.group;

import java.time.Clock;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/** In-process abuse brake. Production multi-instance deployments require a shared limiter at the edge. */
final class GroupLoginRateLimiter {
  private static final long WINDOW_MILLIS = 15 * 60 * 1000L;
  private static final int USER_IP_LIMIT = 10;
  private static final int IP_LIMIT = 100;
  private static final int MAX_KEYS = 20000;
  private final Clock clock;
  private final Map<String, Bucket> buckets = new HashMap<>();
  private record Bucket(int count, long expiresAt) {}
  GroupLoginRateLimiter() { this(Clock.systemUTC()); }
  GroupLoginRateLimiter(Clock clock) { this.clock = clock; }

  synchronized boolean allowed(String userIpKey, String ipKey) {
    long now = clock.millis();
    return count("user:" + userIpKey, now) < USER_IP_LIMIT && count("ip:" + ipKey, now) < IP_LIMIT;
  }

  synchronized void failed(String userIpKey, String ipKey) {
    long now = clock.millis();
    increment("user:" + userIpKey, now);
    increment("ip:" + ipKey, now);
  }

  synchronized void succeeded(String userIpKey) { buckets.remove("user:" + userIpKey); }

  private int count(String key, long now) {
    Bucket b = buckets.get(key);
    if (b == null) return 0;
    if (now >= b.expiresAt()) { buckets.remove(key); return 0; }
    return b.count();
  }

  private void increment(String key, long now) {
    int current = count(key, now);
    if (current == 0 && buckets.size() >= MAX_KEYS) {
      Iterator<Map.Entry<String, Bucket>> it = buckets.entrySet().iterator();
      while (it.hasNext()) if (now >= it.next().getValue().expiresAt()) it.remove();
      // Bounded memory: if still full, drop one existing entry; external edge limits are still required.
      if (buckets.size() >= MAX_KEYS) buckets.remove(buckets.keySet().iterator().next());
    }
    Bucket old = buckets.get(key);
    long expires = current == 0 ? now + WINDOW_MILLIS : old.expiresAt();
    buckets.put(key, new Bucket(current + 1, expires));
  }
}
