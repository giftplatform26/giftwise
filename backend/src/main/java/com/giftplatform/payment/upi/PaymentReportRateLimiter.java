package com.giftplatform.payment.upi;

import java.time.Clock;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

/**
 * In-process fixed-window brake for public payment endpoints (start, app response, report, screenshot).
 * Multi-instance production deployments still need a shared limiter at the edge.
 */
@Component
public class PaymentReportRateLimiter {
    public enum Action {
        START(30), RESPONSE(30), REPORT(12), EVIDENCE(6);
        final int limit;
        Action(int limit) { this.limit = limit; }
    }
    private static final long WINDOW_MILLIS = 15 * 60 * 1000L;
    private static final int MAX_KEYS = 50_000;
    private final Clock clock;
    private final Map<String, long[]> buckets = new HashMap<>();

    public PaymentReportRateLimiter() { this(Clock.systemUTC()); }
    PaymentReportRateLimiter(Clock clock) { this.clock = clock; }

    /** Counts this request and throws 429 once the action's limit for this client and event is exceeded. */
    public synchronized void check(Action action, String client, Object event) {
        long now = clock.millis();
        String key = action + "|" + (client == null ? "unknown" : client) + "|" + event;
        long[] bucket = buckets.get(key);
        if (bucket == null || now >= bucket[1]) {
            if (buckets.size() >= MAX_KEYS) evict(now);
            bucket = new long[]{0, now + WINDOW_MILLIS};
            buckets.put(key, bucket);
        }
        if (++bucket[0] > action.limit)
            throw new ResponseStatusException(HttpStatus.TOO_MANY_REQUESTS, "Too many payment requests. Please wait a few minutes.");
    }

    private void evict(long now) {
        Iterator<Map.Entry<String, long[]>> it = buckets.entrySet().iterator();
        while (it.hasNext()) if (now >= it.next().getValue()[1]) it.remove();
        if (buckets.size() >= MAX_KEYS) buckets.remove(buckets.keySet().iterator().next());
    }
}
