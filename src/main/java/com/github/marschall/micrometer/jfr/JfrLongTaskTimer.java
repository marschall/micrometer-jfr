package com.github.marschall.micrometer.jfr;

import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.LongAdder;
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
import io.micrometer.core.instrument.Meter.Id;
import io.micrometer.core.instrument.TimeGauge;
import io.micrometer.core.instrument.Timer;
import io.micrometer.core.instrument.distribution.HistogramSnapshot;
import io.micrometer.core.instrument.util.TimeUtils;

final class JfrLongTaskTimer extends AbstractJfrMeter<LongTaskTimerEventFactory, JfrLongTaskTimerEvent> implements LongTaskTimer {

  private final TimeUnit baseTimeUnit;
  private final LongStatistics statistics;
  private final LongAdder activeTasks;
  private final Clock clock;

  JfrLongTaskTimer(Id id, TimeUnit baseTimeUnit, Clock clock, LongStatisticsFactory longStatisticsFactory) {
    super(id, new LongTaskTimerEventFactory(id, baseTimeUnit));
    this.baseTimeUnit = baseTimeUnit;
    this.clock = clock;
    this.statistics = longStatisticsFactory.newLongStatistics();
    this.activeTasks = new LongAdder();
  }

  private long endEvent(JfrLongTaskTimerEvent event, long start) {
    event.end();
    long duration = this.clock.monotonicTime() - start;
    event.setDuration(duration);
    this.decrementActiveTasks();
    this.statistics.record(duration);
    event.commit();
    return duration;
  }

  // override to avoid Sample allocation
  @Override
  public boolean record(BooleanSupplier f) {
    JfrLongTaskTimerEvent event = this.newEmptyEvent();
    long start = this.clock.monotonicTime();
    event.begin();
    try {
      return f.getAsBoolean();
    } finally {
      endEvent(event, start);
    }
  }

  @Override
  public double record(DoubleSupplier f) {
    JfrLongTaskTimerEvent event = this.newEmptyEvent();
    long start = this.clock.monotonicTime();
    event.begin();
    try {
      return f.getAsDouble();
    } finally {
      endEvent(event, start);
    }
  }

  @Override
  public int record(IntSupplier f) {
    JfrLongTaskTimerEvent event = this.newEmptyEvent();
    long start = this.clock.monotonicTime();
    event.begin();
    try {
      return f.getAsInt();
    } finally {
      endEvent(event, start);
    }
  }

  @Override
  public long record(LongSupplier f) {
    JfrLongTaskTimerEvent event = this.newEmptyEvent();
    long start = this.clock.monotonicTime();
    event.begin();
    try {
      return f.getAsLong();
    } finally {
      endEvent(event, start);
    }
  }

  @Override
  public void record(Runnable f) {
    JfrLongTaskTimerEvent event = this.newEmptyEvent();
    long start = this.clock.monotonicTime();
    event.begin();
    try {
      f.run();
    } finally {
      endEvent(event, start);
    }
  }

  @Override
  public <T> T record(Supplier<T> f) {
    JfrLongTaskTimerEvent event = this.newEmptyEvent();
    long start = this.clock.monotonicTime();
    event.begin();
    try {
      return f.get();
    } finally {
      endEvent(event, start);
    }
  }

  @Override
  public <T> T recordCallable(Callable<T> f) throws Exception {
    JfrLongTaskTimerEvent event = this.newEmptyEvent();
    long start = this.clock.monotonicTime();
    event.begin();
    try {
      return f.call();
    } finally {
      endEvent(event, start);
    }
  }

  @Override
  public HistogramSnapshot takeSnapshot() {
    return HistogramSnapshot.empty(this.statistics.count(), this.duration(this.baseTimeUnit()), this.max(this.baseTimeUnit()));
  }

  @Override
  public Sample start() {
    this.activeTasks.increment();
    JfrLongTaskTimerEvent event = this.newEmptyEvent();
    long start = this.clock.monotonicTime();
    event.begin();
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

  @Override
  public <T> T match(Function<Gauge, T> visitGauge, Function<Counter, T> visitCounter, Function<Timer, T> visitTimer,
      Function<DistributionSummary, T> visitSummary, Function<LongTaskTimer, T> visitLongTaskTimer,
      Function<TimeGauge, T> visitTimeGauge, Function<FunctionCounter, T> visitFunctionCounter,
      Function<FunctionTimer, T> visitFunctionTimer, Function<Meter, T> visitMeter) {
    return visitLongTaskTimer.apply(this);
  }

  @Override
  public void use(Consumer<Gauge> visitGauge, Consumer<Counter> visitCounter, Consumer<Timer> visitTimer,
      Consumer<DistributionSummary> visitSummary, Consumer<LongTaskTimer> visitLongTaskTimer,
      Consumer<TimeGauge> visitTimeGauge, Consumer<FunctionCounter> visitFunctionCounter,
      Consumer<FunctionTimer> visitFunctionTimer, Consumer<Meter> visitMeter) {
    visitLongTaskTimer.accept(this);
  }

  final class JfrSample extends Sample {

    private final JfrLongTaskTimerEvent event;
    private final long start;
    private long duration;

    JfrSample(JfrLongTaskTimerEvent event, long start) {
      this.event = event;
      this.start = start;
    }

    @Override
    public long stop() {
      this.duration = JfrLongTaskTimer.this.endEvent(this.event, this.start);

      // is already in nanoseconds, no need to convert
      return this.duration;
    }

    @Override
    public double duration(TimeUnit unit) {
      return unit.convert(this.duration, TimeUnit.NANOSECONDS);
    }

  }

}
