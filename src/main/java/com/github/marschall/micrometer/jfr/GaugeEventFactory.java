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

final class GaugeEventFactory extends AbstractMeterEventFactory {

  GaugeEventFactory(Id id) {
    super(id);
  }

  @Override
  protected List<ValueDescriptor> getAdditionalValueDescriptors() {
    List<AnnotationElement> valueAnnotations = new ArrayList<>(3);
    valueAnnotations.add(new AnnotationElement(Label.class, "Value"));
    valueAnnotations.add(new AnnotationElement(Description.class, "The current value of the gauge."));
    Optional<AnnotationElement> baseUnitAnnotation = BaseUnitUtils.mapToAnnotationElement(id.getBaseUnit());
    if (baseUnitAnnotation.isPresent()) {
      valueAnnotations.add(baseUnitAnnotation.get());
    }
   ValueDescriptor valueDescriptor = new ValueDescriptor(double.class, "value", valueAnnotations);

    return List.of(valueDescriptor);
  }

  Event newEvent(EventFactory eventFactory, double value) {
    Event event = this.newEmptyEvent(eventFactory);
    int attributeIndex = 0;

    attributeIndex = this.setCommonEventAttributes(event, attributeIndex);
    event.set(attributeIndex++, value);

    return event;
  }

}
