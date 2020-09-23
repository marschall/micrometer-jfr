package com.github.marschall.micrometer.jfr;

import io.micrometer.core.instrument.Meter.Id;
import jdk.jfr.Event;

final class JfrDistributionSummaryEvent extends AbstractJfrMeterEvent {

  JfrDistributionSummaryEvent(Id id, Event event) {
    super(id, event);
  }

  void setAmount(double amount) {
    int attributeIndex = 0;

    attributeIndex = this.setCommonEventAttributes(attributeIndex);
    event.set(attributeIndex++, amount);
  }

}
