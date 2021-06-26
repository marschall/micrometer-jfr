package com.github.marschall.micrometer.jfr;

import static java.util.stream.Collectors.toList;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Path;
import java.time.Duration;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.ToDoubleFunction;
import java.util.function.ToLongFunction;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

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
import io.micrometer.core.instrument.TimeGauge;
import io.micrometer.core.instrument.Timer;
import io.micrometer.core.instrument.binder.BaseUnits;
import jdk.jfr.EventType;
import jdk.jfr.Recording;
import jdk.jfr.consumer.RecordedEvent;
import jdk.jfr.consumer.RecordingFile;

@TestMethodOrder(OrderAnnotation.class)
class JfrMeterRegistryTests {

  private static final String METRICS_PREFIX = "marschall.jfr.";

  private static volatile JfrMeterRegistry jfrRegistry;

  private static volatile Recording recording;

  static final Path RECORDING_LOCATION = Path.of("target", JfrMeterRegistryTests.class.getSimpleName() + ".jfr");

  @BeforeAll
  static void beforeAll() throws IOException {
    startRecording();
    addRegistry();
  }

  private static void startRecording() throws IOException {
    recording = new Recording();
    recording.enable("marschall.jfr.counter");
    recording.enable("marschall.jfr.distributionSummary");
    recording.enable("marschall.jfr.functionCounter").withPeriod(Duration.ofMillis(10L));
    recording.enable("marschall.jfr.functionTimer").withPeriod(Duration.ofMillis(10L));
    recording.enable("marschall.jfr.gauge").withPeriod(Duration.ofMillis(10L));
    recording.enable("marschall.jfr.longTaskTimer");
    recording.enable("marschall.jfr.meter").withPeriod(Duration.ofMillis(10L));
    recording.enable("marschall.jfr.timer");
    recording.enable("marschall.jfr.timer.sample");
    recording.enable("org.junit.TestExecution");
    recording.enable("org.junit.TestPlan");
    recording.setMaxSize(1L * 1024L * 1024L);
    recording.setToDisk(true);
    recording.setDestination(RECORDING_LOCATION);
    recording.start();
  }

  private static void addRegistry() {
    jfrRegistry = new JfrMeterRegistry();
    Metrics.addRegistry(jfrRegistry);
  }

  @AfterAll
  static void afterAll() throws InterruptedException, IOException {
    removeRegistry();
    stopRecording();
  }

  private static void removeRegistry() throws InterruptedException, IOException {
    Thread.sleep(500L);
    Metrics.removeRegistry(jfrRegistry);
    jfrRegistry.close();
  }

  private static void stopRecording() throws IOException {
    recording.close();
    Set<String> eventNames = new HashSet<>();
    try (RecordingFile recordingFile = new RecordingFile(RECORDING_LOCATION)) {
      while (recordingFile.hasMoreEvents()) {
        RecordedEvent event = recordingFile.readEvent();
        EventType eventType = event.getEventType();
        eventNames.add(eventType.getName());
      }
    }
    assertFalse(eventNames.isEmpty());
    assertTrue(eventNames.contains("marschall.jfr.counter"));
    assertTrue(eventNames.contains("marschall.jfr.distributionSummary"));
    assertTrue(eventNames.contains("marschall.jfr.functionCounter"));
    assertTrue(eventNames.contains("marschall.jfr.functionTimer"));
    assertTrue(eventNames.contains("marschall.jfr.gauge"));
    assertTrue(eventNames.contains("marschall.jfr.timeGauge"));
    assertTrue(eventNames.contains("marschall.jfr.longTaskTimer"));
    assertTrue(eventNames.contains("marschall.jfr.meter"));
    assertTrue(eventNames.contains("marschall.jfr.timer"));
    assertTrue(eventNames.contains("marschall.jfr.timer.sample"));
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
  void createTimeGauge() {
    AtomicLong valueHolder = new AtomicLong();
    TimeGauge timeGauge = createTimeGauge("timeGauge", "Time Gauge", valueHolder, AtomicLong::doubleValue, TimeUnit.MILLISECONDS,
            Tag.of("name", "cleanup"), Tag.of("status", "ok"));

    valueHolder.set(1_234L);
    double value = timeGauge.value();
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
    FunctionTimer functionTimer = createFunctionTimer("functionTimer", "Function Timer",
            countAndTime, CountAndTime::getCount, CountAndTime::getTotalTime, TimeUnit.MINUTES,
            Tag.of("name", "cleanup"), Tag.of("status", "ok"));

    countAndTime.count = 1234L;
    countAndTime.totalTime = 1234.0d;

    functionTimer.mean(TimeUnit.MILLISECONDS);
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

  private static <T> TimeGauge createTimeGauge(String name, String description, T obj, ToDoubleFunction<T> valueFunction, TimeUnit baseUnit, Tag... tags) {
    return TimeGauge.builder(METRICS_PREFIX + name, obj, baseUnit, valueFunction)
            .description(description)
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
