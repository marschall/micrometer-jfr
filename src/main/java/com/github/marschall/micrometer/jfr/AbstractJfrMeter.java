package com.github.marschall.micrometer.jfr;

import io.micrometer.core.instrument.Meter;
import io.micrometer.core.instrument.util.MeterEquivalence;
import jdk.jfr.EventFactory;

abstract class AbstractJfrMeter<F extends AbstractMeterEventFactory<E>, E extends AbstractJfrMeterEvent> implements Meter {

  private final Id id;
  protected final F meterEventFactory;
  protected final EventFactory jfrEventFactory;

  AbstractJfrMeter(Id id, F meterEventFactory) {
    this.id = id;
    this.meterEventFactory = meterEventFactory;
    this.jfrEventFactory = this.meterEventFactory.newEventFactory();
  }

  E newEmptyEvent() {
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
