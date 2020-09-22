package com.github.marschall.micrometer.jfr;

import java.util.List;

import io.micrometer.core.instrument.Meter.Id;
import jdk.jfr.AnnotationElement;
import jdk.jfr.Description;
import jdk.jfr.Event;
import jdk.jfr.EventFactory;
import jdk.jfr.Label;
import jdk.jfr.ValueDescriptor;

final class FunctionCounterEventFactory extends AbstractMeterEventFactory {

  FunctionCounterEventFactory(Id id) {
    super(id);
  }

  @Override
  protected List<ValueDescriptor> getAdditionalValueDescriptors() {
    List<AnnotationElement> incrementAnnotations = List.of(
        new AnnotationElement(Label.class, "Count"),
        new AnnotationElement(Description.class, "The cumulative count since this counter was created."));
    ValueDescriptor incrementDescriptor = new ValueDescriptor(double.class, "count", incrementAnnotations);

    return List.of(incrementDescriptor);
  }

  Event newEvent(EventFactory eventFactory, double count) {
    Event event = this.newEmptyEvent(eventFactory);
    int attributeIndex = 0;

    attributeIndex = this.setCommonEventAttributes(event, attributeIndex);
    event.set(attributeIndex++, count);

    return event;
  }


}
