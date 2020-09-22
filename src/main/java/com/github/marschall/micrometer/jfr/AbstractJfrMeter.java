package com.github.marschall.micrometer.jfr;

import io.micrometer.core.instrument.Measurement;
import io.micrometer.core.instrument.Meter;
import io.micrometer.core.instrument.util.MeterEquivalence;
import jdk.jfr.Event;
import jdk.jfr.EventFactory;

abstract class AbstractJfrMeter<F extends AbstractMeterEventFactory> implements Meter {

  private final Id id;
  protected final F meterEventFactory;
  protected final EventFactory jfrEventFactory;

  AbstractJfrMeter(Id id, F meterEventFactory) {
    this.id = id;
    this.meterEventFactory = meterEventFactory;
    this.jfrEventFactory = this.meterEventFactory.newEventFactory();
  }

  Event newEvent() {
    return this.meterEventFactory.newEvent(this.jfrEventFactory);
  }
  
  Event newEmptyEvent() {
    return this.meterEventFactory.newEmptyEvent(this.jfrEventFactory);
  }

  @Override
  public Id getId() {
    return this.id;
  }

  @Override
  public void close() {
    this.jfrEventFactory.unregister();
  }

  @Override
  public String toString() {
    return this.getClass().getSimpleName() + '(' + this.getId() + ')';
  }

  @Override
  public Iterable<Measurement> measure() {
    // TODO Auto-generated method stub
    return null;
  }
  
  @Override
  public boolean equals(Object o) {
    if (o == this) {
      return true;
    }
    if (!(o instanceof Meter)) {
      return false;
    }
    return MeterEquivalence.equals(this, o);
  }

  @Override
  public int hashCode() {
    return MeterEquivalence.hashCode(this);
  }

}
