package com.github.marschall.micrometer.jfr;

import java.lang.ref.WeakReference;
import java.util.function.ToDoubleFunction;

import io.micrometer.core.instrument.Gauge;

abstract class AbstractJfrGauge<T, F extends AbstractMeterEventFactory<E>, E extends AbstractJfrMeterEvent & DoubleValueEvent> extends AbstractJfrMeter<F, E> implements Gauge {

  final WeakReference<T> reference;
  final ToDoubleFunction<T> valueFunction;
  private final Runnable hook;

  AbstractJfrGauge(Id id, T obj, ToDoubleFunction<T> valueFunction, F meterEventFactory) {
    super(id, meterEventFactory);
    // all the other code uses a WeakReference
    this.reference = new WeakReference<>(obj);
    this.valueFunction = valueFunction;
    this.hook = this::value;
    this.meterEventFactory.registerPeriodicEvent(this.jfrEventFactory, this.hook);
  }

  @Override
  public double value() {
    T obj = this.reference.get();
    double value;
    if (obj != null) {
      value = this.valueFunction.applyAsDouble(obj);
    } else {
      value = Double.NaN;
    }
    E event = this.newEmptyEvent();
    event.setValue(value);
    event.commit();
    return value;
  }

  @Override
  public void close() {
    this.meterEventFactory.unregisterPeriodicEvent(this.hook);
    super.close();
  }

}
