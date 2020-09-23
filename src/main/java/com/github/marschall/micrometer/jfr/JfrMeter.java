package com.github.marschall.micrometer.jfr;

import io.micrometer.core.instrument.Measurement;
import io.micrometer.core.instrument.Meter;

final class JfrMeter extends AbstractJfrMeter<MeterEventFactory, JfrMeterEvent> implements Meter {

  private final Type type;
  private final Iterable<Measurement> measurements;

  public JfrMeter(Id id, Type type, Iterable<Measurement> measurements) {
    super(id, new MeterEventFactory(id));
    this.type = type;
    this.measurements = measurements;
  }

  @Override
  public Iterable<Measurement> measure() {
    JfrMeterEvent event = this.newEmptyEvent();
    // TODO handle measurements
    // TODO record type?
    // who the type different from the type of the id?
    event.commit();
    return this.measurements;
  }

}
