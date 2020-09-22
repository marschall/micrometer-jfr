package com.github.marschall.micrometer.jfr;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.LongAdder;

import io.micrometer.core.instrument.Clock;
import io.micrometer.core.instrument.LongTaskTimer;
import io.micrometer.core.instrument.distribution.DistributionStatisticConfig;
import io.micrometer.core.instrument.distribution.HistogramSnapshot;
import io.micrometer.core.instrument.util.TimeUtils;
import jdk.jfr.Event;

final class JfrLongTaskTimer extends AbstractJfrMeter<LongTaskTimerEventFactory> implements LongTaskTimer {

  private final DistributionStatisticConfig distributionStatisticConfig;
  private final TimeUnit baseTimeUnit;
  private final LongStatistics statistics;
  private final LongAdder activeTasks;
  private final Clock clock;

  JfrLongTaskTimer(Id id, DistributionStatisticConfig distributionStatisticConfig, TimeUnit baseTimeUnit, Clock clock) {
    super(id, new LongTaskTimerEventFactory(id, baseTimeUnit));
    this.distributionStatisticConfig = distributionStatisticConfig;
    this.baseTimeUnit = baseTimeUnit;
    this.clock = clock;
    this.statistics = new LongStatistics();
    this.activeTasks = new LongAdder();
  }

  Event newEvent(long duration) {
    return this.meterEventFactory.newEvent(this.jfrEventFactory, duration);
  }

  void setEventAttributes(long duration, Event event) {
    this.meterEventFactory.setEventAttributes(duration, event);
  }

  @Override
  public HistogramSnapshot takeSnapshot() {
    return HistogramSnapshot.empty(this.statistics.count(), this.duration(this.baseTimeUnit()), this.max(this.baseTimeUnit()));
  }

  @Override
  public Sample start() {
    this.activeTasks.increment();
    Event event = this.newEvent();
    long start = this.clock.monotonicTime();
    return new JfrSample(event, start);
  }

  void decrementActiveTasks() {
    this.activeTasks.decrement();
  }

  @Override
  public double duration(TimeUnit destinationUnit) {
    long totalAmount = this.statistics.totalAmount();
    return TimeUtils.convert(totalAmount, this.baseTimeUnit(), destinationUnit);
  }

  @Override
  public int activeTasks() {
    return this.activeTasks.intValue();
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

  final class JfrSample extends Sample {

    private final Event event;
    private final long start;
    private long duration;

    JfrSample(Event event, long start) {
      this.event = event;
      this.start = start;
    }

    @Override
    public long stop() {
      this.event.end();

      long end = JfrLongTaskTimer.this.clock.monotonicTime();
      this.duration = end - start;
      JfrLongTaskTimer.this.setEventAttributes(this.duration, this.event);
      this.event.commit();

      JfrLongTaskTimer.this.decrementActiveTasks();
      return TimeUnit.NANOSECONDS.convert(this.duration, JfrLongTaskTimer.this.baseTimeUnit);
    }

    @Override
    public double duration(TimeUnit unit) {
      return TimeUtils.convert(duration, JfrLongTaskTimer.this.baseTimeUnit, unit);
    }

  }

}
