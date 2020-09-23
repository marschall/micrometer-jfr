package com.github.marschall.micrometer.jfr;

import io.micrometer.core.instrument.Meter.Id;
import jdk.jfr.Event;

final class JfrMeterEvent extends AbstractJfrMeterEvent {

  JfrMeterEvent(Id id, Event event) {
    super(id, event);
  }

  void setValues() {
    int attributeIndex = 0;

    attributeIndex = this.setCommonEventAttributes(attributeIndex);
  }

}
