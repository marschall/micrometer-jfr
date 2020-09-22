package com.github.marschall.micrometer.jfr;

import java.util.concurrent.TimeUnit;

import io.micrometer.core.instrument.Meter.Id;

final class FunctionTimerEventFactory extends AbstractMeterEventFactory {

  FunctionTimerEventFactory(Id id, TimeUnit baseTimeUnit) {
    super(id, baseTimeUnit);
  }

}
