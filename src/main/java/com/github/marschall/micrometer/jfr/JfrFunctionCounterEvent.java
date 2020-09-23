package com.github.marschall.micrometer.jfr;

import io.micrometer.core.instrument.Meter.Id;
import jdk.jfr.Event;

final class JfrFunctionCounterEvent extends AbstractJfrMeterEvent {

  JfrFunctionCounterEvent(Id id, Event event) {
    super(id, event);
  }

  void setCount(double amount) {
    int attributeIndex = 0;

    attributeIndex = this.setCommonEventAttributes(attributeIndex);
    event.set(attributeIndex++, amount);
  }

}
