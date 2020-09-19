package com.github.marschall.micrometer.jfr;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.LongAdder;

import io.micrometer.core.instrument.LongTaskTimer;
import io.micrometer.core.instrument.distribution.DistributionStatisticConfig;
import io.micrometer.core.instrument.distribution.HistogramSnapshot;
import io.micrometer.core.instrument.util.TimeUtils;
import jdk.jfr.Event;

final class JfrLongTaskTimer extends AbstractJfrMeter implements LongTaskTimer {

  private final DistributionStatisticConfig distributionStatisticConfig;
  private final TimeUnit baseTimeUnit;
  private final LongStatistics statistics;
  private final LongAdder activeTasks;

  JfrLongTaskTimer(Id id, DistributionStatisticConfig distributionStatisticConfig, TimeUnit baseTimeUnit) {
    super(id);
    this.distributionStatisticConfig = distributionStatisticConfig;
    this.baseTimeUnit = baseTimeUnit;
    this.statistics = new LongStatistics();
    this.activeTasks = new LongAdder();
  }

  @Override
  public HistogramSnapshot takeSnapshot() {
    return HistogramSnapshot.empty(this.statistics.count(), this.duration(this.baseTimeUnit()), this.max(this.baseTimeUnit()));
  }

  @Override
  public Sample start() {
    this.activeTasks.increment();
    Event event = this.newEvent();
    return new JfrSample(event);
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

    JfrSample(Event event) {
      this.event = event;
    }

    @Override
    public long stop() {
      this.event.end();
      this.event.commit();
      JfrLongTaskTimer.this.decrementActiveTasks();
      // TODO Auto-generated method stub
      return 0;
    }

    @Override
    public double duration(TimeUnit unit) {
      // TODO Auto-generated method stub
      return 0;
    }

  }

}
