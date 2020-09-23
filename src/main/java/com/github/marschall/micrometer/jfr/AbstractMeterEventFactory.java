package com.github.marschall.micrometer.jfr;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import io.micrometer.core.instrument.Meter.Id;
import io.micrometer.core.instrument.Tag;
import io.micrometer.core.instrument.binder.BaseUnits;
import io.micrometer.core.lang.Nullable;
import jdk.jfr.AnnotationElement;
import jdk.jfr.Category;
import jdk.jfr.DataAmount;
import jdk.jfr.Description;
import jdk.jfr.EventFactory;
import jdk.jfr.Label;
import jdk.jfr.Name;
import jdk.jfr.Percentage;
import jdk.jfr.StackTrace;
import jdk.jfr.Timespan;
import jdk.jfr.ValueDescriptor;

/**
 * Support class for generating an {@link EventFactory} and setting attributes of
 * event definitions.
 */
abstract class AbstractMeterEventFactory<E extends AbstractJfrMeterEvent> {

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

  abstract E newEmptyEvent(EventFactory eventFactory);

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

  Optional<AnnotationElement> getAnnotationElementOfBaseUnit() {
    return mapToAnnotationElement(this.id.getBaseUnit());
  }

  String getTimespanOfBaseTimeUnit() {
    return mapTimeUnitToTimespan(this.baseTimeUnit);
  }

  static String mapTimeUnitToTimespan(TimeUnit timeUnit) {
    switch (timeUnit) {
    case NANOSECONDS:
      return Timespan.NANOSECONDS;
    case MICROSECONDS:
      return Timespan.MICROSECONDS;
    case MILLISECONDS:
      return Timespan.MILLISECONDS;
    case SECONDS:
      return Timespan.SECONDS;
    default:
      throw new IllegalArgumentException("unsupporte time unit");
    }
  }

  private static Optional<AnnotationElement> mapToAnnotationElement(String baseUnit) {
    if (baseUnit == null) {
      return Optional.empty();
    }
    switch (baseUnit) {
    case BaseUnits.BYTES:
      return Optional.of(new AnnotationElement(DataAmount.class, DataAmount.BYTES));
    case BaseUnits.MILLISECONDS:
      return Optional.of(new AnnotationElement(Timespan.class, Timespan.MILLISECONDS));
    case BaseUnits.PERCENT:
      return Optional.of(new AnnotationElement(Percentage.class));
    default:
      return Optional.empty();
    }
  }

}
