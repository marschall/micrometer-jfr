package com.github.marschall.micrometer.jfr;

import java.util.concurrent.atomic.DoubleAdder;

import io.micrometer.core.instrument.Counter;
import jdk.jfr.Event;

final class JfrCounter extends AbstractJfrMeter implements Counter {

  private final DoubleAdder adder;

  JfrCounter(Id id) {
    super(id);
    this.adder = new DoubleAdder();
  }

  @Override
  public void increment(double amount) {
    // REVIEW, should we validate the value is greater than 0 to make sure it is actually
    // monotonically increasing? No other implementations do this
    this.adder.add(amount);

    Event event = this.newEvent();
    // TODO register value
    // TODO register increment
    event.commit();
  }

  @Override
  public double count() {
    return this.adder.doubleValue();
  }

}
