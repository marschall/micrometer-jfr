package com.github.marschall.micrometer.jfr;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import io.micrometer.core.instrument.Meter.Id;
import io.micrometer.core.instrument.Tag;
import io.micrometer.core.lang.Nullable;
import jdk.jfr.AnnotationElement;
import jdk.jfr.Category;
import jdk.jfr.Description;
import jdk.jfr.Event;
import jdk.jfr.EventFactory;
import jdk.jfr.Label;
import jdk.jfr.Name;
import jdk.jfr.StackTrace;
import jdk.jfr.ValueDescriptor;

/**
 * Support class for generating an {@link EventFactory} and setting attributes of events.
 *
 */
abstract class AbstractMeterEventFactory {

  protected final Id id;

  @Nullable
  protected final TimeUnit baseTimeUnit;

  AbstractMeterEventFactory(Id id) {
    this(id, null);
  }

  AbstractMeterEventFactory(Id id, @Nullable TimeUnit baseTimeUnit) {
    this.id = id;
    this.baseTimeUnit = baseTimeUnit;
  }

  Event newEvent(EventFactory eventFactory) {
    Event event = this.newEmptyEvent(eventFactory);

    int attributeIndex = 0;

    attributeIndex = this.setCommonEventAttributes(event, attributeIndex);

    return event;
  }

  Event newEmptyEvent(EventFactory eventFactory) {
    return eventFactory.newEvent();
  }

  EventFactory newEventFactory() {

    List<ValueDescriptor> fields = new ArrayList<>();

    fields.add(this.getTypeValueDescriptor());

    String baseIdUnit = this.id.getBaseUnit();
    if (baseIdUnit != null) {
      fields.add(this.getBaseUnitValueDescriptor());
    }

    fields.addAll(this.getTagValueDescriptors());

    fields.addAll(this.getAdditionalValueDescriptors());

    List<AnnotationElement> eventAnnotations = this.getEventAnnotations();

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

  private List<ValueDescriptor> getTagValueDescriptors() {
    List<ValueDescriptor> fields = new ArrayList<>();
    for (Tag tag : this.id.getTagsAsIterable()) {
      List<AnnotationElement> tagAnnotations = List.of();
      ValueDescriptor valueDescriptor = new ValueDescriptor(String.class, tag.getKey(), tagAnnotations);
      fields.add(valueDescriptor);
    }
    return fields;
  }

  private List<AnnotationElement> getEventAnnotations() {
    String[] category = { "Micrometer" };
    List<AnnotationElement> eventAnnotations = new ArrayList<>();
    eventAnnotations.add(new AnnotationElement(Name.class, this.id.getName()));
    eventAnnotations.add(new AnnotationElement(Description.class, this.id.getDescription()));
    eventAnnotations.add(new AnnotationElement(Category.class, category));
    eventAnnotations.add(new AnnotationElement(StackTrace.class, false));
    return eventAnnotations;
  }

  protected List<ValueDescriptor> getAdditionalValueDescriptors() {
    return List.of();
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

}
