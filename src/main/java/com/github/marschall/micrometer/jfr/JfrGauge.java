package com.github.marschall.micrometer.jfr;

import java.lang.ref.WeakReference;
import java.util.function.ToDoubleFunction;

import io.micrometer.core.instrument.Gauge;
import jdk.jfr.Event;
import jdk.jfr.EventFactory;
import jdk.jfr.FlightRecorder;

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
  }

  @Override
  protected void registerPriodicEvent(EventFactory eventFactory) {
    Event event = eventFactory.newEvent();
    Class<? extends Event> eventClass = event.getClass();
    FlightRecorder.addPeriodicEvent(eventClass, this.hook);
  }

  @Override
  public void close() {
    FlightRecorder.removePeriodicEvent(this.hook);
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

}
