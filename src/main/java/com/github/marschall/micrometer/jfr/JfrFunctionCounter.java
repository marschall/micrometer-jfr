package com.github.marschall.micrometer.jfr;

import java.lang.ref.WeakReference;
import java.util.function.ToDoubleFunction;

import io.micrometer.core.instrument.FunctionCounter;

final class JfrFunctionCounter<T> extends AbstractJfrMeter<FunctionCounterEventFactory, JfrFunctionCounterEvent> implements FunctionCounter {

  private final WeakReference<T> reference;
  private final ToDoubleFunction<T> countFunction;
  private final Runnable hook;

  JfrFunctionCounter(Id id, T obj, ToDoubleFunction<T> countFunction) {
    super(id, new FunctionCounterEventFactory(id));
    // all the other code uses a WeakReference
    this.reference = new WeakReference<>(obj);
    this.countFunction = countFunction;
    this.hook = () -> this.count();
    this.meterEventFactory.registerPeriodicEvent(this.jfrEventFactory, this.hook);
  }

  @Override
  public void close() {
    this.meterEventFactory.unregisterPeriodicEvent(this.hook);
    super.close();
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
    JfrFunctionCounterEvent event = this.newEmptyEvent();
    event.setCount(value);
    event.commit();
    return value;
  }

}
