package com.github.marschall.micrometer.jfr;

import java.util.concurrent.TimeUnit;

import io.micrometer.core.instrument.Meter.Id;
import jdk.jfr.EventFactory;

final class FunctionTimerEventFactory extends AbstractMeterEventFactory<JfrFunctionTimerEvent> {

  FunctionTimerEventFactory(Id id, TimeUnit baseTimeUnit) {
    super(id, baseTimeUnit);
  }

  @Override
  JfrFunctionTimerEvent newEmptyEvent(EventFactory eventFactory) {
    return new JfrFunctionTimerEvent(this.id, eventFactory.newEvent());
  }

}
