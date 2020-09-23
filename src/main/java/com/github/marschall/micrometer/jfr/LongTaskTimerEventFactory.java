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
    List<AnnotationElement> amountAnnotations = List.of(
        new AnnotationElement(Label.class, "Duration"),
        new AnnotationElement(Description.class, "Duration in " + this.baseTimeUnit),
        new AnnotationElement(Timespan.class, TimeUnitUtils.mapTimeUnitToTimespan(this.baseTimeUnit)));
    // "duration" causes an error
    ValueDescriptor amountDescriptor = new ValueDescriptor(long.class, "meteredDuration", amountAnnotations);

    return List.of(amountDescriptor);
  }

  @Override
  JfrLongTaskTimerEvent newEmptyEvent(EventFactory eventFactory) {
    return new JfrLongTaskTimerEvent(this.id, eventFactory.newEvent());
  }

}
