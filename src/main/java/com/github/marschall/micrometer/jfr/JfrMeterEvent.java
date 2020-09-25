package com.github.marschall.micrometer.jfr;

import io.micrometer.core.instrument.Measurement;
import io.micrometer.core.instrument.Meter.Id;
import io.micrometer.core.instrument.Meter.Type;
import jdk.jfr.Event;

final class JfrMeterEvent extends AbstractJfrMeterEvent {

  JfrMeterEvent(Id id, Event event) {
    super(id, event);
  }

  void recordValues(Type type, Iterable<Measurement> measurements) {
    int attributeIndex = 0;

    attributeIndex = this.setCommonEventAttributes(attributeIndex);

    this.event.set(attributeIndex++, type.name());
    for (Measurement measurement : measurements) {
      double value = measurement.getValue();
      this.event.set(attributeIndex++, value);
    }
  }

}
