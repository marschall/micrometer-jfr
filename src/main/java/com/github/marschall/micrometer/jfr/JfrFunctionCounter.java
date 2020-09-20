package com.github.marschall.micrometer.jfr;

import java.lang.ref.WeakReference;
import java.util.List;
import java.util.function.ToDoubleFunction;

import io.micrometer.core.instrument.FunctionCounter;
import jdk.jfr.AnnotationElement;
import jdk.jfr.Description;
import jdk.jfr.Event;
import jdk.jfr.Label;
import jdk.jfr.ValueDescriptor;

final class JfrFunctionCounter<T> extends AbstractJfrMeter implements FunctionCounter {

  private final WeakReference<T> reference;
  private final ToDoubleFunction<T> countFunction;

  JfrFunctionCounter(Id id, T obj, ToDoubleFunction<T> countFunction) {
    super(id);
    // all the other code uses a WeakReference
    this.reference = new WeakReference<>(obj);
    this.countFunction = countFunction;
  }

  @Override
  protected List<ValueDescriptor> getAdditionalValueDescriptors() {
    List<AnnotationElement> incrementAnnotations = List.of(
            new AnnotationElement(Label.class, "Count"),
            new AnnotationElement(Description.class, "The cumulative count since this counter was created."));
    ValueDescriptor incrementDescriptor = new ValueDescriptor(double.class, "count", incrementAnnotations);

    return List.of(incrementDescriptor);
  }

  Event newEvent(double count) {
    Event event = this.newEmptyEvent();
    int attributeIndex = 0;

    attributeIndex = this.setCommonEventAttributes(event, attributeIndex);
    event.set(attributeIndex++, count);

    return event;
  }

  @Override
  public double count() {
    T obj = this.reference.get();
    double value;
    if (obj != null) {
      value = this.countFunction.applyAsDouble(obj);
    } else {
      value = Double.NaN;
    }
    Event event = this.newEvent(value);
    event.commit();
    return value;
  }

}
