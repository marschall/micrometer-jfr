package com.github.marschall.micrometer.jfr;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.time.Duration;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tags;
import io.micrometer.core.instrument.binder.cache.CacheMeterBinder;
import io.micrometer.core.instrument.binder.cache.CaffeineCacheMetrics;

/**
 * Regression test for <a href="https://github.com/marschall/micrometer-jfr/issues/1">Support . in tag value descriptors</a>.
 *
 * @see <a href="https://bugs.openjdk.org/browse/JDK-8272515">JDK-8272515</a>
 */
class CacheRegerssionTests {

  private MeterRegistry meterRegistry;
  private LoadingCache<String, String> cache;

  @BeforeEach
  void setUp() {
    meterRegistry = new JfrMeterRegistry();
    cache = Caffeine.newBuilder()
            .maximumSize(100)
            .recordStats()
            .expireAfterWrite(Duration.ofSeconds(5))
            .refreshAfterWrite(Duration.ofSeconds(1))
            .build(key -> key + "_value");
  }

  @AfterEach
  void tearDown() {
    this.cache.cleanUp();
  }

  @Test
  void monitor() {
    assertEquals("key1_value", this.cache.get("key1"));
    LoadingCache<String, String> monitored = CaffeineCacheMetrics.monitor(this.meterRegistry, this.cache, "junit-test-cache", "cache.manager", "junit");
    assertNotNull(monitored);
  }

  @Test
  void bindTo() {
    assertEquals("key2_value", this.cache.get("key2"));
    CacheMeterBinder<LoadingCache<String, String>> binder = new CaffeineCacheMetrics<>( this.cache, "junit-test-cache", Tags.of("cache.manager", "junit"));
    binder.bindTo(this.meterRegistry);
  }

}
