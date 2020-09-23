package com.github.marschall.micrometer.jfr;

import io.micrometer.core.instrument.Meter.Id;
import jdk.jfr.EventFactory;

final class MeterEventFactory extends AbstractMeterEventFactory<JfrMeterEvent> {

  MeterEventFactory(Id id) {
    super(id);
  }

  @Override
  JfrMeterEvent newEmptyEvent(EventFactory eventFactory) {
    return new JfrMeterEvent(this.id, eventFactory.newEvent());
  }

}
