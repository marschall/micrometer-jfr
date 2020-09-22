package com.github.marschall.micrometer.jfr;

import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

import io.micrometer.core.instrument.Clock;
import io.micrometer.core.instrument.Timer;
import io.micrometer.core.instrument.distribution.DistributionStatisticConfig;
import io.micrometer.core.instrument.distribution.HistogramSnapshot;
import io.micrometer.core.instrument.distribution.pause.PauseDetector;
import io.micrometer.core.instrument.util.TimeUtils;
import jdk.jfr.Event;

final class JfrTimer extends AbstractJfrMeter<TimerEventFactory> implements Timer {

  private final DistributionStatisticConfig distributionStatisticConfig;
  private final PauseDetector pauseDetector;
  private final TimeUnit baseTimeUnit;
  private final LongStatistics statistics;
  private final Clock clock;

  JfrTimer(Id id, DistributionStatisticConfig distributionStatisticConfig, PauseDetector pauseDetector, TimeUnit baseTimeUnit, Clock clock) {
    super(id, new TimerEventFactory(id, baseTimeUnit));
    this.distributionStatisticConfig = distributionStatisticConfig;
    this.pauseDetector = pauseDetector;
    this.baseTimeUnit = baseTimeUnit;
    this.clock = clock;
    this.statistics = new LongStatistics();
  }

  Event newEvent(long duration) {
    return this.meterEventFactory.newEvent(this.jfrEventFactory, duration);
  }

  private void setEventAttributes(long duration, Event event) {
    this.meterEventFactory.setEventAttributes(duration, event);
  }

  @Override
  public HistogramSnapshot takeSnapshot() {
    return HistogramSnapshot.empty(this.count(), this.totalTime(this.baseTimeUnit()), this.max(this.baseTimeUnit()));
  }

  @Override
  public void record(long amount, TimeUnit unit) {
    long duration = this.baseTimeUnit().convert(amount, unit);

    Event event = this.newEvent(duration);
    event.commit();

    this.statistics.record(duration);
  }

  @Override
  public <T> T record(Supplier<T> f) {
    Event event = this.newEmptyEvent();
    long start = this.clock.monotonicTime();
    event.begin();
    try {
      return f.get();
    } finally {
      event.end();
      long end = this.clock.monotonicTime();
      long duration = end - start;
      this.setEventAttributes(duration, event);
      event.commit();
    }
  }

  @Override
  public <T> T recordCallable(Callable<T> f) throws Exception {

    Event event = this.newEmptyEvent();
    long start = this.clock.monotonicTime();
    event.begin();
    try {
      return f.call();
    } finally {
      event.end();
      long end = this.clock.monotonicTime();
      long duration = end - start;
      this.setEventAttributes(duration, event);
      event.commit();
    }
  }

  @Override
  public void record(Runnable f) {
    Event event = this.newEmptyEvent();
    long start = this.clock.monotonicTime();
    event.begin();
    try {
      f.run();
    } finally {
      event.end();
      long end = this.clock.monotonicTime();
      long duration = end - start;
      this.setEventAttributes(duration, event);
      event.commit();
    }
  }

  @Override
  public long count() {
    return this.statistics.count();
  }

  @Override
  public double totalTime(TimeUnit destinationUnit) {
    long totalAmount = this.statistics.totalAmount();
    return TimeUtils.convert(totalAmount, this.baseTimeUnit(), destinationUnit);
  }

  @Override
  public double max(TimeUnit destinationUnit) {
    long max = this.statistics.max();
    return TimeUtils.convert(max, this.baseTimeUnit(), destinationUnit);
  }

  @Override
  public TimeUnit baseTimeUnit() {
    return this.baseTimeUnit;
  }

}
