package com.github.marschall.micrometer.jfr;

import io.micrometer.core.instrument.Meter.Id;
import jdk.jfr.Event;

final class JfrCounterEvent extends AbstractJfrMeterEvent {

  JfrCounterEvent(Id id, Event event) {
    super(id, event);
  }

  void setValues(double increment, double value) {
    int attributeIndex = 0;

    attributeIndex = this.setCommonEventAttributes(attributeIndex);

    event.set(attributeIndex++, increment);
    event.set(attributeIndex++, value);

  }

}
