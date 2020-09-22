package com.github.marschall.micrometer.jfr;

import java.lang.ref.WeakReference;
import java.util.function.ToDoubleFunction;

import io.micrometer.core.instrument.Gauge;
import jdk.jfr.Event;

final class JfrGauge<T> extends AbstractJfrMeter<GaugeEventFactory> implements Gauge {

  private final WeakReference<T> reference;
  private final ToDoubleFunction<T> valueFunction;

  JfrGauge(Id id, T obj, ToDoubleFunction<T> valueFunction) {
    super(id, new GaugeEventFactory(id));
    // all the other code uses a WeakReference
    this.reference = new WeakReference<>(obj);
    this.valueFunction = valueFunction;
  }

  Event newEvent(double value) {
    return this.meterEventFactory.newEvent(this.jfrEventFactory, value);
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
    Event event = this.newEvent(value);
    event.commit();
    return value;
  }

}
