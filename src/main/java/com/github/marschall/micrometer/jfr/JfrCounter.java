package com.github.marschall.micrometer.jfr;

import io.micrometer.core.instrument.Counter;
import jdk.jfr.Event;

final class JfrCounter extends AbstractJfrMeter<CounterEventFactory> implements Counter {

  private final AtomicDouble value;

  JfrCounter(Id id) {
    super(id, new CounterEventFactory(id));
    this.value = new AtomicDouble();
  }

  Event newEvent(double increment, double value) {
    return this.meterEventFactory.newEvent(this.jfrEventFactory, increment, value);
  }

  @Override
  public void increment(double amount) {
    // REVIEW, should we validate the value is greater than 0 to make sure it is actually
    // monotonically increasing? No other implementations do this
    double currentValue = this.value.add(amount);

    Event event = this.newEvent(amount, currentValue);
    event.commit();
  }

  @Override
  public double count() {
    return this.value.doubleValue();
  }

}
