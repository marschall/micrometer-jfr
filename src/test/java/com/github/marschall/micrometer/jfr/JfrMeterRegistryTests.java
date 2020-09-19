package com.github.marschall.micrometer.jfr;

import java.time.Duration;
import java.util.List;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import io.micrometer.core.instrument.LongTaskTimer;
import io.micrometer.core.instrument.Metrics;
import io.micrometer.core.instrument.Tag;
import io.micrometer.core.instrument.Timer;

class JfrMeterRegistryTests {

  private static final String METRICS_PREFIX = "marschall.jfr.";

  private static JfrMeterRegistry jfrRegistry;

  @BeforeAll
  static void addRegistry() {
    jfrRegistry = new JfrMeterRegistry();
    Metrics.addRegistry(jfrRegistry);
  }

  @BeforeAll
  static void removeRegistry() {
    Metrics.removeRegistry(jfrRegistry);
  }

  @Test
  void createTimer() {
    Timer timer = createTimer("job", "Job duration",
            Tag.of("name", "cleanup"),
            Tag.of("status", "ok")
            );
    timer.record(Duration.ofMinutes(1L));
  }

  @Test
  void createLongTaskTimer() throws Exception {
    LongTaskTimer longTaskTimer = createLongTaskTimer("job.active", "Active jobs");

    LongTaskTimer.Sample longTaskTimerSample = longTaskTimer.start();

    Timer.Sample timerSample = createTimerSample();

    Thread.sleep(500L);

    timerSample.stop(createTimer("job", "Job duration",
            Tag.of("name", "cleanup"),
            Tag.of("status", "ok")
        ));

    Thread.sleep(500L);

    longTaskTimerSample.stop();
  }

  private static Timer createTimer(String name, String description, Tag... tags) {
    return Timer.builder(METRICS_PREFIX + name)
            .description(description)
            .tags(List.of(tags))
            .register(Metrics.globalRegistry);
  }

  private static Timer.Sample createTimerSample() {
    return Timer.start(Metrics.globalRegistry);
  }

  private static LongTaskTimer createLongTaskTimer(String name, String description, Tag... tags) {
    return LongTaskTimer.builder(METRICS_PREFIX + name)
            .description(description)
            .tags(List.of(tags))
            .register(Metrics.globalRegistry);
  }

}
