package com.github.marschall.micrometer.jfr;

import io.micrometer.core.instrument.Meter.Id;
import jdk.jfr.Event;

final class JfrFunctionTimerEvent extends AbstractJfrMeterEvent {

  JfrFunctionTimerEvent(Id id, Event event) {
    super(id, event);
  }

  void setDuration(long duration) {
    int attributeIndex = 0;

    attributeIndex = this.setCommonEventAttributes(attributeIndex);
    event.set(attributeIndex++, duration);
  }

}
