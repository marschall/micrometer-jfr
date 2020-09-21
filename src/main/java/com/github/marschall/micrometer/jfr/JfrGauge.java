package com.github.marschall.micrometer.jfr;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.function.ToDoubleFunction;

import io.micrometer.core.instrument.Gauge;
import jdk.jfr.AnnotationElement;
import jdk.jfr.Description;
import jdk.jfr.Event;
import jdk.jfr.Label;
import jdk.jfr.ValueDescriptor;

final class JfrGauge<T> extends AbstractJfrMeter implements Gauge {

  private final WeakReference<T> reference;
  private final ToDoubleFunction<T> valueFunction;

  JfrGauge(Id id, T obj, ToDoubleFunction<T> valueFunction) {
    super(id);
    // all the other code uses a WeakReference
    this.reference = new WeakReference<>(obj);
    this.valueFunction = valueFunction;
  }

  @Override
  protected List<ValueDescriptor> getAdditionalValueDescriptors(Id id, TimeUnit baseTimeUnit) {
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

  Event newEvent(double value) {
    Event event = this.newEmptyEvent();
    int attributeIndex = 0;

    attributeIndex = this.setCommonEventAttributes(event, attributeIndex);
    event.set(attributeIndex++, value);

    return event;
  }

  @Override
  public double value() {
    T obj = this.reference.get();
    double value;
    if (this.reference != null) {
      value = this.valueFunction.applyAsDouble(obj);
    } else {
      value = Double.NaN;
    }
    Event event = this.newEvent(value);
    event.commit();
    return value;
  }

}
