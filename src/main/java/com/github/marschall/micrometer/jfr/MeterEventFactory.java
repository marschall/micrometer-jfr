package com.github.marschall.micrometer.jfr;

import io.micrometer.core.instrument.Meter.Id;

final class MeterEventFactory extends AbstractMeterEventFactory {

  MeterEventFactory(Id id) {
    super(id);
  }

}
