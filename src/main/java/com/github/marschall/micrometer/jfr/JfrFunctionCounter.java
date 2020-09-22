package com.github.marschall.micrometer.jfr;

import java.lang.ref.WeakReference;
import java.util.function.ToDoubleFunction;

import io.micrometer.core.instrument.FunctionCounter;
import jdk.jfr.Event;

final class JfrFunctionCounter<T> extends AbstractJfrMeter<FunctionCounterEventFactory> implements FunctionCounter {

  private final WeakReference<T> reference;
  private final ToDoubleFunction<T> countFunction;

  JfrFunctionCounter(Id id, T obj, ToDoubleFunction<T> countFunction) {
    super(id, new FunctionCounterEventFactory(id));
    // all the other code uses a WeakReference
    this.reference = new WeakReference<>(obj);
    this.countFunction = countFunction;
  }

  Event newEvent(double count) {
    return this.meterEventFactory.newEvent(this.jfrEventFactory);
  }

  @Override
  public double count() {
    T obj = this.reference.get();
    double value;
    if (obj != null) {
      value = this.countFunction.applyAsDouble(obj);
    } else {
      value = Double.NaN;
    }
    Event event = this.newEvent(value);
    event.commit();
    return value;
  }

}
