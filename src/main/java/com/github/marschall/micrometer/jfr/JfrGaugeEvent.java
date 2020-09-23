package com.github.marschall.micrometer.jfr;

import io.micrometer.core.instrument.Meter.Id;
import jdk.jfr.Event;

final class JfrGaugeEvent extends AbstractJfrMeterEvent {

  JfrGaugeEvent(Id id, Event event) {
    super(id, event);
  }

  void setValue(double value) {
    int attributeIndex = 0;

    attributeIndex = this.setCommonEventAttributes(attributeIndex);
    event.set(attributeIndex++, value);
  }

}
