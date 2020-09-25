package com.github.marschall.micrometer.jfr;

import java.util.List;

import io.micrometer.core.instrument.Meter.Id;
import jdk.jfr.AnnotationElement;
import jdk.jfr.Description;
import jdk.jfr.EventFactory;
import jdk.jfr.Label;
import jdk.jfr.ValueDescriptor;

final class FunctionCounterEventFactory extends AbstractMeterEventFactory<JfrFunctionCounterEvent> {

  FunctionCounterEventFactory(Id id) {
    super(id);
  }

  @Override
  protected List<ValueDescriptor> getAdditionalValueDescriptors() {
    List<AnnotationElement> countAnnotations = List.of(
        new AnnotationElement(Label.class, "Count"),
        new AnnotationElement(Description.class, "The cumulative count since this counter was created."));
    ValueDescriptor incrementDescriptor = new ValueDescriptor(double.class, "count", countAnnotations);

    return List.of(incrementDescriptor);
  }

  @Override
  JfrFunctionCounterEvent newEmptyEvent(EventFactory eventFactory) {
    return new JfrFunctionCounterEvent(this.id, eventFactory.newEvent());
  }

}
