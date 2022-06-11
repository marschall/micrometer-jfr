package com.github.marschall.micrometer.jfr;

import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.ToDoubleFunction;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.DistributionSummary;
import io.micrometer.core.instrument.FunctionCounter;
import io.micrometer.core.instrument.FunctionTimer;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.LongTaskTimer;
import io.micrometer.core.instrument.Meter;
import io.micrometer.core.instrument.TimeGauge;
import io.micrometer.core.instrument.Timer;

final class JfrTimeGauge<T> extends AbstractJfrGauge<T, TimerGaugeEventFactory, JfrTimerGaugeEvent> implements TimeGauge {

  private final TimeUnit valueFunctionUnit;

  JfrTimeGauge(Id id, T obj, TimeUnit valueFunctionUnit, ToDoubleFunction<T> valueFunction) {
    super(id, obj, valueFunction, new TimerGaugeEventFactory(id, valueFunctionUnit));
    this.valueFunctionUnit = valueFunctionUnit;
  }

  @Override
  public TimeUnit baseTimeUnit() {
    return this.valueFunctionUnit;
  }

  @Override
  public <X> X match(Function<Gauge, X> visitGauge, Function<Counter, X> visitCounter, Function<Timer, X> visitTimer,
          Function<DistributionSummary, X> visitSummary, Function<LongTaskTimer, X> visitLongTaskTimer,
          Function<TimeGauge, X> visitTimeGauge, Function<FunctionCounter, X> visitFunctionCounter,
          Function<FunctionTimer, X> visitFunctionTimer, Function<Meter, X> visitMeter) {
    return visitTimeGauge.apply(this);
  }

  @Override
  public void use(Consumer<Gauge> visitGauge, Consumer<Counter> visitCounter, Consumer<Timer> visitTimer,
      Consumer<DistributionSummary> visitSummary, Consumer<LongTaskTimer> visitLongTaskTimer,
      Consumer<TimeGauge> visitTimeGauge, Consumer<FunctionCounter> visitFunctionCounter,
      Consumer<FunctionTimer> visitFunctionTimer, Consumer<Meter> visitMeter) {
    visitTimeGauge.accept(this);
  }

}
