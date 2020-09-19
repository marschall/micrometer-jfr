package com.github.marschall.micrometer.jfr;

import java.util.ArrayList;
import java.util.List;

import io.micrometer.core.instrument.Meter;
import io.micrometer.core.instrument.Tag;
import jdk.jfr.AnnotationElement;
import jdk.jfr.Category;
import jdk.jfr.Description;
import jdk.jfr.Event;
import jdk.jfr.EventFactory;
import jdk.jfr.Label;
import jdk.jfr.Name;
import jdk.jfr.StackTrace;
import jdk.jfr.ValueDescriptor;

abstract class AbstractJfrMeter implements Meter {

  private final Id id;
  private final EventFactory eventFactory;

  AbstractJfrMeter(Id id) {
    this.id = id;
    this.eventFactory = this.newEventFactory(id);
  }

  private EventFactory newEventFactory(Id id) {

    List<ValueDescriptor> fields = new ArrayList<>();

    List<AnnotationElement> typeAnnotations = List.of(new AnnotationElement(Label.class, "Type"),
            new AnnotationElement(Description.class, "The meter's type"));
    ValueDescriptor typeDescriptor = new ValueDescriptor(String.class, "type", typeAnnotations);
    fields.add(typeDescriptor);

    // base unit
    String baseUnit = id.getBaseUnit();
    if (baseUnit != null) {
      List<AnnotationElement> baseUnitAnnotations = List.of(new AnnotationElement(Label.class, "Base Unit"),
              new AnnotationElement(Description.class, "The base unit of measurement for this meter."));
      ValueDescriptor baseUnitDescriptor = new ValueDescriptor(String.class, "baseUnit", baseUnitAnnotations);
      fields.add(baseUnitDescriptor);
    }

    // tags
    for (Tag tag : id.getTagsAsIterable()) {
      List<AnnotationElement> tagAnnotations = List.of();
      ValueDescriptor valueDescriptor = new ValueDescriptor(String.class, tag.getKey(), tagAnnotations);
      fields.add(valueDescriptor);
    }

    String[] category = { "Micrometer" };
    List<AnnotationElement> eventAnnotations = new ArrayList<>();
    eventAnnotations.add(new AnnotationElement(Name.class, id.getName()));
//    eventAnnotations.add(new AnnotationElement(Label.class, "Hello World"));
    eventAnnotations.add(new AnnotationElement(Description.class, id.getDescription()));
    eventAnnotations.add(new AnnotationElement(Category.class, category));
    eventAnnotations.add(new AnnotationElement(StackTrace.class, false));

    return EventFactory.create(eventAnnotations, fields);
  }

  Event newEvent() {
    Event event = this.eventFactory.newEvent();

    int attributeIndex = 0;

    // type
    event.set(attributeIndex++, this.id.getType().name());

    // base unit
    String baseUnit = this.id.getBaseUnit();
    if (baseUnit != null) {
      event.set(attributeIndex++, baseUnit);
    }

    // base tags
    for (Tag tag : this.id.getTagsAsIterable()) {
      event.set(attributeIndex++, tag.getValue());
    }
    return event;
  }

  @Override
  public Id getId() {
    return this.id;
  }

  @Override
  public void close() {
    this.eventFactory.unregister();
  }

  @Override
  public String toString() {
    return this.getClass().getSimpleName() + '(' + this.getId() + ')';
  }

}
