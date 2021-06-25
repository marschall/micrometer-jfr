package com.github.marschall.micrometer.jfr;

import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.function.Supplier;

import io.micrometer.core.instrument.Clock;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.DistributionSummary;
import io.micrometer.core.instrument.FunctionCounter;
import io.micrometer.core.instrument.FunctionTimer;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.LongTaskTimer;
import io.micrometer.core.instrument.Meter;
import io.micrometer.core.instrument.TimeGauge;
import io.micrometer.core.instrument.Timer;
import io.micrometer.core.instrument.distribution.DistributionStatisticConfig;
import io.micrometer.core.instrument.distribution.HistogramSnapshot;
import io.micrometer.core.instrument.distribution.pause.PauseDetector;
import io.micrometer.core.instrument.util.TimeUtils;

final class JfrTimer extends AbstractJfrMeter<TimerEventFactory, JfrTimerEvent> implements Timer {

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

  @Override
  public HistogramSnapshot takeSnapshot() {
    return HistogramSnapshot.empty(this.count(), this.totalTime(this.baseTimeUnit()), this.max(this.baseTimeUnit()));
  }

  @Override
  public void record(long amount, TimeUnit unit) {
    long duration = this.baseTimeUnit().convert(amount, unit);

    JfrTimerEvent event = this.newEmptyEvent();
    event.setDuration(duration);
    event.commit();

    this.statistics.record(duration);
  }

  @Override
  public <T> T record(Supplier<T> f) {
    JfrTimerEvent event = this.newEmptyEvent();
    long start = this.clock.monotonicTime();
    event.begin();
    try {
      return f.get();
    } finally {
      event.end();
      long end = this.clock.monotonicTime();
      long duration = end - start;
      event.setDuration(duration);
      event.commit();
    }
  }

  @Override
  public <T> T recordCallable(Callable<T> f) throws Exception {

    JfrTimerEvent event = this.newEmptyEvent();
    long start = this.clock.monotonicTime();
    event.begin();
    try {
      return f.call();
    } finally {
      event.end();
      long end = this.clock.monotonicTime();
      long duration = end - start;
      event.setDuration(duration);
      event.commit();
    }
  }

  @Override
  public void record(Runnable f) {
    JfrTimerEvent event = this.newEmptyEvent();
    long start = this.clock.monotonicTime();
    event.begin();
    try {
      f.run();
    } finally {
      event.end();
      long end = this.clock.monotonicTime();
      long duration = end - start;
      event.setDuration(duration);
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

  @Override
  public <T> T match(Function<Gauge, T> visitGauge, Function<Counter, T> visitCounter, Function<Timer, T> visitTimer,
      Function<DistributionSummary, T> visitSummary, Function<LongTaskTimer, T> visitLongTaskTimer,
      Function<TimeGauge, T> visitTimeGauge, Function<FunctionCounter, T> visitFunctionCounter,
      Function<FunctionTimer, T> visitFunctionTimer, Function<Meter, T> visitMeter) {
    return visitMeter.apply(this);
  }

}
