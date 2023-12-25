package com.github.marschall.micrometer.jfr;

import static java.util.concurrent.TimeUnit.NANOSECONDS;

import java.time.Duration;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.DoubleSupplier;
import java.util.function.Function;
import java.util.function.IntSupplier;
import java.util.function.LongSupplier;
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
import io.micrometer.core.instrument.util.TimeUtils;

final class JfrTimer extends AbstractJfrMeter<TimerEventFactory, JfrTimerEvent> implements Timer {

  private final TimeUnit baseTimeUnit;
  private final LongStatistics statistics;
  private final Clock clock;

  JfrTimer(Id id, DistributionStatisticConfig distributionStatisticConfig, TimeUnit baseTimeUnit, Clock clock,
      LongStatisticsFactory longStatisticsFactory) {
    super(id, new TimerEventFactory(id, baseTimeUnit));
    this.baseTimeUnit = baseTimeUnit;
    this.clock = clock;
    this.statistics = longStatisticsFactory.newLongStatistics();
  }

  @Override
  public HistogramSnapshot takeSnapshot() {
    return HistogramSnapshot.empty(this.count(), this.totalTime(this.baseTimeUnit()), this.max(this.baseTimeUnit()));
  }

  @Override
  public void record(long amount, TimeUnit unit) {
    if (amount < 0L) {
      return;
    }
    long units = this.baseTimeUnit().convert(amount, unit);

    JfrTimerEvent event = this.newEmptyEvent();
    event.setDuration(units);
    event.commit();

    this.statistics.record(units);
  }

  @Override
  public void record(Duration duration) {
    if (duration.isNegative()) {
      return;
    }
    long units = this.baseTimeUnit().convert(duration);

    JfrTimerEvent event = this.newEmptyEvent();
    event.setDuration(units);
    event.commit();

    this.statistics.record(units);
  }

  @Override
  public <T> T record(Supplier<T> f) {
    JfrTimerEvent event = this.newEmptyEvent();
    long start = this.clock.monotonicTime();
    event.begin();
    try {
      return f.get();
    } finally {
      recordDuration(start, event);
    }
  }

  private void recordDuration(long start, JfrTimerEvent event) {
    event.end();
    long end = this.clock.monotonicTime();
    long duration = end - start;
    this.statistics.record(this.baseTimeUnit().convert(duration, NANOSECONDS));
    event.setDuration(duration);
    event.commit();
  }

  @Override
  public boolean record(BooleanSupplier f) {
    JfrTimerEvent event = this.newEmptyEvent();
    long start = this.clock.monotonicTime();
    event.begin();
    try {
      return f.getAsBoolean();
    } finally {
      recordDuration(start, event);
    }
  }

  @Override
  public int record(IntSupplier f) {
    JfrTimerEvent event = this.newEmptyEvent();
    long start = this.clock.monotonicTime();
    event.begin();
    try {
      return f.getAsInt();
    } finally {
      recordDuration(start, event);
    }
  }

  @Override
  public long record(LongSupplier f) {
    JfrTimerEvent event = this.newEmptyEvent();
    long start = this.clock.monotonicTime();
    event.begin();
    try {
      return f.getAsLong();
    } finally {
      recordDuration(start, event);
    }
  }

  @Override
  public double record(DoubleSupplier f) {
    JfrTimerEvent event = this.newEmptyEvent();
    long start = this.clock.monotonicTime();
    event.begin();
    try {
      return f.getAsDouble();
    } finally {
      recordDuration(start, event);
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
      recordDuration(start, event);
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
      recordDuration(start, event);
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
  public double mean(TimeUnit unit) {
    long count = this.statistics.count();
    if (count == 0L) {
      return 0L;
    }
    return totalTime(unit) / count;
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

  @Override
  public void use(Consumer<Gauge> visitGauge, Consumer<Counter> visitCounter, Consumer<Timer> visitTimer,
      Consumer<DistributionSummary> visitSummary, Consumer<LongTaskTimer> visitLongTaskTimer,
      Consumer<TimeGauge> visitTimeGauge, Consumer<FunctionCounter> visitFunctionCounter,
      Consumer<FunctionTimer> visitFunctionTimer, Consumer<Meter> visitMeter) {
    visitMeter.accept(this);
  }

}
