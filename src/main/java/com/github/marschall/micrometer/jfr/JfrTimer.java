package com.github.marschall.micrometer.jfr;

import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

import io.micrometer.core.instrument.Timer;
import io.micrometer.core.instrument.distribution.DistributionStatisticConfig;
import io.micrometer.core.instrument.distribution.HistogramSnapshot;
import io.micrometer.core.instrument.distribution.pause.PauseDetector;
import io.micrometer.core.instrument.util.TimeUtils;
import jdk.jfr.Event;

final class JfrTimer extends AbstractJfrMeter implements Timer {

  private final DistributionStatisticConfig distributionStatisticConfig;
  private final PauseDetector pauseDetector;
  private final TimeUnit baseTimeUnit;
  private final LongStatistics statistics;

  JfrTimer(Id id, DistributionStatisticConfig distributionStatisticConfig, PauseDetector pauseDetector, TimeUnit baseTimeUnit) {
    super(id);
    this.distributionStatisticConfig = distributionStatisticConfig;
    this.pauseDetector = pauseDetector;
    this.baseTimeUnit = baseTimeUnit;
    this.statistics = new LongStatistics();
  }

  @Override
  public HistogramSnapshot takeSnapshot() {
    return HistogramSnapshot.empty(this.count(), this.totalTime(this.baseTimeUnit()), this.max(this.baseTimeUnit()));
  }

  @Override
  public void record(long amount, TimeUnit unit) {
    Event event = this.newEvent();
    long value = this.baseTimeUnit().convert(amount, unit);
    this.statistics.record(value);
    // TODO set duration
    event.commit();
  }

  @Override
  public <T> T record(Supplier<T> f) {
    Event event = this.newEvent();
    event.begin();
    try {
      return f.get();
    } finally {
      event.end();
      event.commit();
    }
  }

  @Override
  public <T> T recordCallable(Callable<T> f) throws Exception {

    Event event = this.newEvent();
    event.begin();
    try {
      return f.call();
    } finally {
      event.end();
      event.commit();
    }
  }

  @Override
  public void record(Runnable f) {
    Event event = this.newEvent();
    event.begin();
    try {
      f.run();
    } finally {
      event.end();
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
