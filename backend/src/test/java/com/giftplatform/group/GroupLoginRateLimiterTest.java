package com.giftplatform.group;

import org.junit.jupiter.api.Test;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import static org.junit.jupiter.api.Assertions.*;

class GroupLoginRateLimiterTest {
  @Test void unknownUserAttemptsAreLimited() {
    var l = new GroupLoginRateLimiter();
    for (int i=0; i<10; i++) { assertTrue(l.allowed("ip|event|missing", "ip")); l.failed("ip|event|missing", "ip"); }
    assertFalse(l.allowed("ip|event|missing", "ip"));
    assertTrue(l.allowed("ip|event|other", "ip"));
  }
  @Test void successfulLoginClearsUsernameBucket() {
    var l = new GroupLoginRateLimiter();
    for (int i=0; i<10; i++) l.failed("ip|event|alice", "ip");
    assertFalse(l.allowed("ip|event|alice", "ip"));
    l.succeeded("ip|event|alice");
    assertTrue(l.allowed("ip|event|alice", "ip"));
  }
  @Test void windowExpires() {
    var now = new java.util.concurrent.atomic.AtomicLong(Instant.parse("2026-01-01T00:00:00Z").toEpochMilli());
    Clock clock = new Clock() {
      public ZoneOffset getZone() { return ZoneOffset.UTC; }
      public Clock withZone(java.time.ZoneId zone) { return this; }
      public Instant instant() { return Instant.ofEpochMilli(now.get()); }
    };
    var limiter = new GroupLoginRateLimiter(clock);
    for (int i=0; i<10; i++) limiter.failed("ip|event|alice", "ip");
    assertFalse(limiter.allowed("ip|event|alice", "ip"));
    now.addAndGet(16 * 60 * 1000L);
    assertTrue(limiter.allowed("ip|event|alice", "ip"));
  }
  @Test void concurrentFailuresDoNotLoseCounts() throws Exception {
    var l = new GroupLoginRateLimiter();
    var pool = Executors.newFixedThreadPool(8);
    var gate = new CountDownLatch(1);
    try {
      var tasks = new java.util.ArrayList<java.util.concurrent.Future<?>>();
      for (int i=0; i<40; i++) tasks.add(pool.submit(() -> { try { gate.await(); } catch (InterruptedException e) { Thread.currentThread().interrupt(); return; } l.failed("ip|event|alice", "ip"); }));
      gate.countDown();
      for (var task : tasks) task.get(10, TimeUnit.SECONDS);
      assertFalse(l.allowed("ip|event|alice", "ip"));
    } finally { pool.shutdownNow(); }
  }
}
