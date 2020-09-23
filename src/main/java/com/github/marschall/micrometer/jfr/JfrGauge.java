package com.github.marschall.micrometer.jfr;

import java.lang.ref.WeakReference;
import java.util.function.ToDoubleFunction;

import io.micrometer.core.instrument.Gauge;

final class JfrGauge<T> extends AbstractJfrMeter<GaugeEventFactory, JfrGaugeEvent> implements Gauge {

  private final WeakReference<T> reference;
  private final ToDoubleFunction<T> valueFunction;

  JfrGauge(Id id, T obj, ToDoubleFunction<T> valueFunction) {
    super(id, new GaugeEventFactory(id));
    // all the other code uses a WeakReference
    this.reference = new WeakReference<>(obj);
    this.valueFunction = valueFunction;
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

}
