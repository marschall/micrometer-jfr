package com.github.marschall.micrometer.jfr;

import io.micrometer.core.instrument.Meter.Id;
import jdk.jfr.Event;

final class JfrTimerGaugeEvent extends AbstractJfrMeterEvent implements DoubleValueEvent {

  JfrTimerGaugeEvent(Id id, Event event) {
    super(id, event);
  }

  @Override
  public void setValue(double duration) {
    int attributeIndex = 0;

    attributeIndex = this.setCommonEventAttributes(attributeIndex);
    event.set(attributeIndex++, duration);
  }

}
