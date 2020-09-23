package com.github.marschall.micrometer.jfr;

import io.micrometer.core.instrument.Counter;

final class JfrCounter extends AbstractJfrMeter<CounterEventFactory, JfrCounterEvent> implements Counter {

  private final AtomicDouble value;

  JfrCounter(Id id) {
    super(id, new CounterEventFactory(id));
    this.value = new AtomicDouble();
  }

  @Override
  public void increment(double amount) {
    // REVIEW, should we validate the value is greater than 0 to make sure it is actually
    // monotonically increasing? No other implementations do this
    double currentValue = this.value.add(amount);

    JfrCounterEvent event = this.newEmptyEvent();
    event.setValues(amount, currentValue);
    event.commit();
  }

  @Override
  public double count() {
    return this.value.doubleValue();
  }

}
