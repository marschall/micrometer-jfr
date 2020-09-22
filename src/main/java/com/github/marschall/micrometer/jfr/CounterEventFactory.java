package com.github.marschall.micrometer.jfr;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import io.micrometer.core.instrument.Meter.Id;
import jdk.jfr.AnnotationElement;
import jdk.jfr.Description;
import jdk.jfr.Event;
import jdk.jfr.EventFactory;
import jdk.jfr.Label;
import jdk.jfr.ValueDescriptor;

final class CounterEventFactory extends AbstractMeterEventFactory {

  CounterEventFactory(Id id) {
    super(id);
  }

  @Override
  protected List<ValueDescriptor> getAdditionalValueDescriptors() {
    List<AnnotationElement> incrementAnnotations = new ArrayList<>(3);
    incrementAnnotations.add(new AnnotationElement(Label.class, "Increment"));
    incrementAnnotations.add(new AnnotationElement(Description.class, "Amount to add to the counter."));
    Optional<AnnotationElement> baseUnitAnnotation = this.getAnnotationElementOfBaseUnit();
    if (baseUnitAnnotation.isPresent()) {
      incrementAnnotations.add(baseUnitAnnotation.get());
    }
    ValueDescriptor incrementDescriptor = new ValueDescriptor(double.class, "increment", incrementAnnotations);

    List<AnnotationElement> valueAnnotations = new ArrayList<>(3);
    valueAnnotations.add(new AnnotationElement(Label.class, "Value"));
    valueAnnotations.add(new AnnotationElement(Description.class, "The current value of the counter."));
    if (baseUnitAnnotation.isPresent()) {
      valueAnnotations.add(baseUnitAnnotation.get());
    }
    ValueDescriptor valueDescriptor = new ValueDescriptor(double.class, "value", valueAnnotations);

    return List.of(incrementDescriptor, valueDescriptor);
  }

  Event newEvent(EventFactory eventFactory, double increment, double value) {
    Event event = this.newEmptyEvent(eventFactory);
    int attributeIndex = 0;

    attributeIndex = this.setCommonEventAttributes(event, attributeIndex);

    event.set(attributeIndex++, increment);
    event.set(attributeIndex++, value);

    return event;
  }

}
