package com.github.marschall.micrometer.jfr;

import java.lang.ref.WeakReference;
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

final class JfrGauge<T> extends AbstractJfrMeter<GaugeEventFactory, JfrGaugeEvent> implements Gauge {

  private final WeakReference<T> reference;
  private final ToDoubleFunction<T> valueFunction;
  private final Runnable hook;

  JfrGauge(Id id, T obj, ToDoubleFunction<T> valueFunction) {
    super(id, new GaugeEventFactory(id));
    // all the other code uses a WeakReference
    this.reference = new WeakReference<>(obj);
    this.valueFunction = valueFunction;
    this.hook = () -> this.value();
    this.meterEventFactory.registerPeriodicEvent(this.jfrEventFactory, this.hook);
  }

  @Override
  public void close() {
    this.meterEventFactory.unregisterPeriodicEvent(this.hook);
    super.close();
  }

  @Override
  public double value() {
    T obj = this.reference.get();
    double value;
    if (this.reference != null) {
      value = this.valueFunction.applyAsDouble(obj);
    } else {
      value = Double.NaN;
    }
    JfrGaugeEvent event = this.newEmptyEvent();
    event.setValue(value);
    event.commit();
    return value;
  }
  
  @Override
  public <X> X match(Function<Gauge, X> visitGauge, Function<Counter, X> visitCounter, Function<Timer, X> visitTimer,
      Function<DistributionSummary, X> visitSummary, Function<LongTaskTimer, X> visitLongTaskTimer,
      Function<TimeGauge, X> visitTimeGauge, Function<FunctionCounter, X> visitFunctionCounter,
      Function<FunctionTimer, X> visitFunctionTimer, Function<Meter, X> visitMeter) {
    return visitGauge.apply(this);
  }

}
