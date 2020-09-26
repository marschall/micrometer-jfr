package com.github.marschall.micrometer.jfr;

import static java.util.stream.Collectors.toList;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.ToDoubleFunction;
import java.util.function.ToLongFunction;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.DistributionSummary;
import io.micrometer.core.instrument.FunctionCounter;
import io.micrometer.core.instrument.FunctionTimer;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.LongTaskTimer;
import io.micrometer.core.instrument.Measurement;
import io.micrometer.core.instrument.Meter;
import io.micrometer.core.instrument.Meter.Type;
import io.micrometer.core.instrument.Metrics;
import io.micrometer.core.instrument.Statistic;
import io.micrometer.core.instrument.Tag;
import io.micrometer.core.instrument.Timer;
import io.micrometer.core.instrument.binder.BaseUnits;

class JfrMeterRegistryTests {

  private static final String METRICS_PREFIX = "marschall.jfr.";

  private static JfrMeterRegistry jfrRegistry;

  @BeforeAll
  static void addRegistry() {
    jfrRegistry = new JfrMeterRegistry();
    Metrics.addRegistry(jfrRegistry);
  }

  @AfterAll
  static void removeRegistry() throws InterruptedException {
    Thread.sleep(500L);
    Metrics.removeRegistry(jfrRegistry);
    jfrRegistry.close();
  }

  @Test
  void createTimer() throws Exception {
    Timer timer = createTimer("timer", "Timer",
        Tag.of("name", "cleanup"),
        Tag.of("status", "ok")
        );

    timer.record(Duration.ofMinutes(1L));
    timer.record(2L, TimeUnit.MINUTES);
    timer.record(() -> {
      try {
        Thread.sleep(5L);
      } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
        return null;
      }
      return null;
    });
    timer.record(() -> {
      try {
        Thread.sleep(5L);
      } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
      }
    });
    timer.recordCallable(() -> {
      Thread.sleep(5L);
      return null;
    });
  }

  @Test
  void testCreateTimerSample() throws Exception {

    Timer.Sample timerSample = createTimerSample();

    Timer timer = createTimer("timer.sample", "Timer Sample",
        Tag.of("name", "cleanup"), Tag.of("status", "ok"));

    Thread.sleep(5L);

    timerSample.stop(timer);
  }

  @Test
  void createCounter() {
    Counter counter = createCounter("counter", "Counter",
        Tag.of("name", "cleanup"), Tag.of("status", "ok"));

    counter.increment();
    counter.increment();
  }

  @Test
  void createMeter() {
    //@formatter:off
    List<Measurement> measurements = Arrays.stream(Statistic.values())
                                           .map(statistic -> new Measurement(() -> 1.0d, statistic))
                                           .collect(toList());
    //@formatter:on
    Meter meter = createMeter("meter", "Meter", Type.GAUGE, measurements,
            Tag.of("name", "cleanup"), Tag.of("status", "ok"));

    meter.measure();
  }

  @Test
  void createDistributionSummary() {
    DistributionSummary distributionSummary = createDistributionSummary("distributionSummary", "Distribution Summary",
        Tag.of("name", "cleanup"), Tag.of("status", "ok"));

    distributionSummary.record(55.0d);
  }

  @Test
  void createGauge() {
    AtomicLong valueHolder = new AtomicLong();
    Gauge gauge = createGauge("gauge", "Gauge", valueHolder, AtomicLong::doubleValue, BaseUnits.BYTES,
        Tag.of("name", "cleanup"), Tag.of("status", "ok"));

    valueHolder.set(1_234_567L);
    double value = gauge.value();
    assertTrue(value > 0.0d);
  }

  @Test
  void createFunctionCounter() {
    AtomicLong count = new AtomicLong();
    FunctionCounter functionCounter = createFunctionCounter("functionCounter", "Function Counter", count, AtomicLong::doubleValue,
        Tag.of("name", "cleanup"), Tag.of("status", "ok"));

    count.set(20);
    functionCounter.count();
    count.set(40);
  }

  @Test
  void createFunctionTimer() {
    class CountAndTime {

      volatile long count;
      volatile double totalTime;

      long getCount() {
        return this.count;
      }
      double getTotalTime() {
        return this.totalTime;
      }

    }
    CountAndTime countAndTime = new CountAndTime();
    createFunctionTimer("functionTimer", "Function Timer",
            countAndTime, CountAndTime::getCount, CountAndTime::getTotalTime, TimeUnit.MINUTES,
            Tag.of("name", "cleanup"), Tag.of("status", "ok"));

    countAndTime.count = 1234L;
    countAndTime.totalTime = 1234.0d;
  }

  @Test
  void createLongTaskTimer() throws Exception {
    LongTaskTimer longTaskTimer = createLongTaskTimer("longTaskTimer", "Long Task Timer");

    LongTaskTimer.Sample longTaskTimerSample = longTaskTimer.start();

    Thread.sleep(500L);

    longTaskTimerSample.stop();
  }

  private static Timer createTimer(String name, String description, Tag... tags) {
    return Timer.builder(METRICS_PREFIX + name)
        .description(description)
        .tags(List.of(tags))
        .register(Metrics.globalRegistry);
  }

  private static Meter createMeter(String name, String description, Type type, Iterable<Measurement> measurements, Tag... tags) {
    return Meter.builder(METRICS_PREFIX + name, type, measurements)
        .description(description)
        .tags(List.of(tags))
        .register(Metrics.globalRegistry);
  }

  private static Counter createCounter(String name, String description, Tag... tags) {
    return Counter.builder(METRICS_PREFIX + name)
        .description(description)
        .tags(List.of(tags))
        .register(Metrics.globalRegistry);
  }

  private static <T> Gauge createGauge(String name, String description, T obj, ToDoubleFunction<T> valueFunction, String baseUnit, Tag... tags) {
    return Gauge.builder(METRICS_PREFIX + name, obj, valueFunction)
        .description(description)
        .baseUnit(baseUnit)
        .tags(List.of(tags))
        .register(Metrics.globalRegistry);
  }

  private static DistributionSummary createDistributionSummary(String name, String description, Tag... tags) {
    return DistributionSummary.builder(METRICS_PREFIX + name)
        .description(description)
        .tags(List.of(tags))
        .register(Metrics.globalRegistry);
  }

  private static <T> FunctionCounter createFunctionCounter(String name, String description, T obj, ToDoubleFunction<T> valueFunction, Tag... tags) {
    return FunctionCounter.builder(METRICS_PREFIX + name, obj, valueFunction)
        .description(description)
        .tags(List.of(tags))
        .register(Metrics.globalRegistry);
  }

  private static <T> FunctionTimer createFunctionTimer(String name, String description, T obj, ToLongFunction<T> countFunction,
          ToDoubleFunction<T> totalTimeFunction,
          TimeUnit totalTimeFunctionUnit, Tag... tags) {
    return FunctionTimer.builder(METRICS_PREFIX + name, obj, countFunction, totalTimeFunction, totalTimeFunctionUnit)
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
