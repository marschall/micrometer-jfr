package com.github.marschall.micrometer.jfr;

import java.lang.ref.WeakReference;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.ToDoubleFunction;
import java.util.function.ToLongFunction;

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
import io.micrometer.core.instrument.util.TimeUtils;

final class JfrFunctionTimer<T> extends AbstractJfrMeter<FunctionTimerEventFactory, JfrFunctionTimerEvent> implements FunctionTimer {

  private final WeakReference<T> reference;
  private final ToLongFunction<T> countFunction;
  private final ToDoubleFunction<T> totalTimeFunction;
  private final TimeUnit totalTimeFunctionUnit;
  private final TimeUnit baseTimeUnit;
  private final Runnable hook;

  JfrFunctionTimer(Id id, T obj,
          ToLongFunction<T> countFunction,
          ToDoubleFunction<T> totalTimeFunction,
          TimeUnit totalTimeFunctionUnit,
          TimeUnit baseTimeUnit) {
    super(id, new FunctionTimerEventFactory(id, baseTimeUnit));
    this.reference = new WeakReference<>(obj);
    this.countFunction = countFunction;
    this.totalTimeFunction = totalTimeFunction;
    this.totalTimeFunctionUnit = totalTimeFunctionUnit;
    this.baseTimeUnit = baseTimeUnit;
    this.hook = () -> this.recordEvent();
    this.meterEventFactory.registerPeriodicEvent(this.jfrEventFactory, this.hook);
  }

  @Override
  public void close() {
    this.meterEventFactory.unregisterPeriodicEvent(this.hook);
    super.close();
  }

  void recordEvent() {
    double totalTime = this.totalTime(this.baseTimeUnit());
    double count = this.count();

    JfrFunctionTimerEvent event = this.newEmptyEvent();
    event.setTotalTimeAndCount(totalTime, count);
    event.commit();
  }

  @Override
  public double count() {
    T obj = this.reference.get();
    if (obj != null) {
      return this.countFunction.applyAsLong(obj);
    } else {
      return Double.NaN;
    }
  }

  @Override
  public double totalTime(TimeUnit destinationUnit) {
    T obj = this.reference.get();
    if (obj != null) {
      double totalTime = this.totalTimeFunction.applyAsDouble(obj);
      return TimeUtils.convert(totalTime, this.totalTimeFunctionUnit, destinationUnit);
    } else {
      return Double.NaN;
    }
  }

  @Override
  public double mean(TimeUnit destinationUnit) {
    T obj = this.reference.get();
    if (obj != null) {
      double count = this.countFunction.applyAsLong(obj);
      if (count == 0.0d) {
        return 0.0d;
      }
      double totalTime = this.totalTimeFunction.applyAsDouble(obj);
      double totalTimeInDestinationUnit = TimeUtils.convert(totalTime, this.totalTimeFunctionUnit, destinationUnit);
      return totalTimeInDestinationUnit / count;
    } else {
      return 0.0d;
    }
  }

  @Override
  public TimeUnit baseTimeUnit() {
    return this.baseTimeUnit;
  }

  @Override
  public <X> X match(Function<Gauge, X> visitGauge, Function<Counter, X> visitCounter, Function<Timer, X> visitTimer,
      Function<DistributionSummary, X> visitSummary, Function<LongTaskTimer, X> visitLongTaskTimer,
      Function<TimeGauge, X> visitTimeGauge, Function<FunctionCounter, X> visitFunctionCounter,
      Function<FunctionTimer, X> visitFunctionTimer, Function<Meter, X> visitMeter) {
    return visitFunctionTimer.apply(this);
  }

  @Override
  public void use(Consumer<Gauge> visitGauge, Consumer<Counter> visitCounter, Consumer<Timer> visitTimer,
      Consumer<DistributionSummary> visitSummary, Consumer<LongTaskTimer> visitLongTaskTimer,
      Consumer<TimeGauge> visitTimeGauge, Consumer<FunctionCounter> visitFunctionCounter,
      Consumer<FunctionTimer> visitFunctionTimer, Consumer<Meter> visitMeter) {
    visitFunctionTimer.accept(this);
  }

}
