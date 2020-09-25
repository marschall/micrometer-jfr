package com.github.marschall.micrometer.jfr;

import java.util.List;
import java.util.concurrent.TimeUnit;

import io.micrometer.core.instrument.Meter.Id;
import jdk.jfr.AnnotationElement;
import jdk.jfr.Description;
import jdk.jfr.EventFactory;
import jdk.jfr.Label;
import jdk.jfr.Timespan;
import jdk.jfr.ValueDescriptor;

final class LongTaskTimerEventFactory extends AbstractMeterEventFactory<JfrLongTaskTimerEvent> {

  LongTaskTimerEventFactory(Id id, TimeUnit baseTimeUnit) {
    super(id, baseTimeUnit);
  }

  @Override
  protected List<ValueDescriptor> getAdditionalValueDescriptors() {
    List<AnnotationElement> durationAnnotations = List.of(
        new AnnotationElement(Label.class, "Metered Duration"),
        new AnnotationElement(Description.class, "Duration in " + this.baseTimeUnit),
        new AnnotationElement(Timespan.class, this.getTimespanOfBaseTimeUnit()));
    // "duration" causes an error
    ValueDescriptor durationDescriptor = new ValueDescriptor(long.class, "meteredDuration", durationAnnotations);

    return List.of(durationDescriptor);
  }

  @Override
  JfrLongTaskTimerEvent newEmptyEvent(EventFactory eventFactory) {
    return new JfrLongTaskTimerEvent(this.id, eventFactory.newEvent());
  }

}
