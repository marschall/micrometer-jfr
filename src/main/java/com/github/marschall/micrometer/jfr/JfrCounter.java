package com.github.marschall.micrometer.jfr;

import java.util.List;

import io.micrometer.core.instrument.Counter;
import jdk.jfr.AnnotationElement;
import jdk.jfr.Description;
import jdk.jfr.Event;
import jdk.jfr.Label;
import jdk.jfr.ValueDescriptor;

final class JfrCounter extends AbstractJfrMeter implements Counter {

  private final AtomicDouble value;

  JfrCounter(Id id) {
    super(id);
    this.value = new AtomicDouble();
  }

  @Override
  protected List<ValueDescriptor> getAdditionalValueDescriptors() {
    List<AnnotationElement> incrementAnnotations = List.of(
            new AnnotationElement(Label.class, "Increment"),
            new AnnotationElement(Description.class, "Amount to add to the counter."));
    ValueDescriptor incrementDescriptor = new ValueDescriptor(double.class, "increment", incrementAnnotations);

    List<AnnotationElement> valueAnnotations = List.of(
            new AnnotationElement(Label.class, "Value"),
            new AnnotationElement(Description.class, "The current value of the counter."));
   ValueDescriptor valueDescriptor = new ValueDescriptor(double.class, "value", valueAnnotations);

    return List.of(incrementDescriptor, valueDescriptor);
  }

  Event newEvent(double increment, double value) {
    Event event = this.newEmptyEvent();
    int attributeIndex = 0;

    attributeIndex = this.setCommonEventAttributes(event, attributeIndex);
    event.set(attributeIndex++, increment);
    event.set(attributeIndex++, value);

    return event;
  }

  @Override
  public void increment(double amount) {
    // REVIEW, should we validate the value is greater than 0 to make sure it is actually
    // monotonically increasing? No other implementations do this
    double currentValue = this.value.add(amount);

    Event event = this.newEvent(amount, currentValue);
    event.commit();
  }

  @Override
  public double count() {
    return this.value.getValue();
  }

}
