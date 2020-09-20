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

    fields.add(this.getTypeValueDescriptor());

    String baseUnit = id.getBaseUnit();
    if (baseUnit != null) {
      fields.add(this.getBaseUnitValueDescriptor());
    }

    fields.addAll(this.getTagValueDescriptors(id));

    fields.addAll(this.getAdditionalValueDescriptors());

    List<AnnotationElement> eventAnnotations = this.getEventAnnotations(id);

    return EventFactory.create(eventAnnotations, fields);
  }

  private ValueDescriptor getTypeValueDescriptor() {
    List<AnnotationElement> typeAnnotations = List.of(
            new AnnotationElement(Label.class, "Type"),
            new AnnotationElement(Description.class, "The meter's type"));
    return new ValueDescriptor(String.class, "type", typeAnnotations);
  }

  private ValueDescriptor getBaseUnitValueDescriptor() {
    List<AnnotationElement> baseUnitAnnotations = List.of(
            new AnnotationElement(Label.class, "Base Unit"),
            new AnnotationElement(Description.class, "The base unit of measurement for this meter."));
    return new ValueDescriptor(String.class, "baseUnit", baseUnitAnnotations);
  }

  private List<ValueDescriptor> getTagValueDescriptors(Id id) {
    List<ValueDescriptor> fields = new ArrayList<>();
    for (Tag tag : id.getTagsAsIterable()) {
      List<AnnotationElement> tagAnnotations = List.of();
      ValueDescriptor valueDescriptor = new ValueDescriptor(String.class, tag.getKey(), tagAnnotations);
      fields.add(valueDescriptor);
    }
    return fields;
  }

  private List<AnnotationElement> getEventAnnotations(Id id) {
    String[] category = { "Micrometer" };
    List<AnnotationElement> eventAnnotations = new ArrayList<>();
    eventAnnotations.add(new AnnotationElement(Name.class, id.getName()));
    eventAnnotations.add(new AnnotationElement(Description.class, id.getDescription()));
    eventAnnotations.add(new AnnotationElement(Category.class, category));
    eventAnnotations.add(new AnnotationElement(StackTrace.class, false));
    return eventAnnotations;
  }

  protected List<ValueDescriptor> getAdditionalValueDescriptors() {
    return List.of();
  }


  Event newEvent() {
    Event event = this.newEmptyEvent();

    int attributeIndex = 0;

    attributeIndex = this.setCommonEventAttributes(event, attributeIndex);

    return event;
  }

  Event newEmptyEvent() {
    return this.eventFactory.newEvent();
  }

  int setCommonEventAttributes(Event event, int attributeIndex) {
    attributeIndex = this.setType(event, attributeIndex);

    attributeIndex = this.setBaseUnit(event, attributeIndex);

    attributeIndex = this.setTags(event, attributeIndex);

    return attributeIndex;
  }

  int setType(Event event, int attributeIndex) {
    event.set(attributeIndex, this.id.getType().name());
    return attributeIndex += 1;
  }

  private int setBaseUnit(Event event, int attributeIndex) {
    String baseUnit = this.id.getBaseUnit();
    if (baseUnit != null) {
      event.set(attributeIndex, baseUnit);
      return attributeIndex += 1;
    } else {
      return attributeIndex;
    }
  }



  private int setTags(Event event, int attributeIndex) {
    for (Tag tag : this.id.getTagsAsIterable()) {
      event.set(attributeIndex++, tag.getValue());
    }
    return attributeIndex;
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
