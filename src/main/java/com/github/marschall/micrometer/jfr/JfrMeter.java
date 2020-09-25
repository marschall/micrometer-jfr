package com.github.marschall.micrometer.jfr;

import java.util.concurrent.TimeUnit;

import io.micrometer.core.instrument.Measurement;
import io.micrometer.core.instrument.Meter;

final class JfrMeter extends AbstractJfrMeter<MeterEventFactory, JfrMeterEvent> implements Meter {

  private final Type type;
  private final Iterable<Measurement> measurements;
  private final Runnable hook;

  JfrMeter(Id id, Type type, Iterable<Measurement> measurements, TimeUnit baseTimeUnit) {
    super(id, new MeterEventFactory(id, baseTimeUnit, measurements));
    this.type = type;
    this.measurements = measurements;
    this.hook = () -> this.recordEvent();
    this.meterEventFactory.registerPeriodicEvent(this.jfrEventFactory, this.hook);
  }

  @Override
  public void close() {
    this.meterEventFactory.unregisterPeriodicEvent(this.hook);
    super.close();
  }

  private void recordEvent() {
    JfrMeterEvent event = this.newEmptyEvent();
    event.recordValues(this.type, this.measurements);
    event.commit();
  }

  @Override
  public Iterable<Measurement> measure() {
    return this.measurements;
  }

}
