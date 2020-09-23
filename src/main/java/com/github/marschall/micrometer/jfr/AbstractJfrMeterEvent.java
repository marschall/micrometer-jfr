package com.github.marschall.micrometer.jfr;

import io.micrometer.core.instrument.Meter.Id;
import io.micrometer.core.instrument.Tag;
import jdk.jfr.Event;

abstract class AbstractJfrMeterEvent {

  private final Id id;

  protected final Event event;

  AbstractJfrMeterEvent(Id id, Event event) {
    this.id = id;
    this.event = event;
  }

  protected int setCommonEventAttributes(int attributeIndex) {
    attributeIndex = this.setType(attributeIndex);

    attributeIndex = this.setBaseUnit(attributeIndex);

    attributeIndex = this.setTags(attributeIndex);

    return attributeIndex;
  }

  protected int setType(int attributeIndex) {
    this.event.set(attributeIndex, this.id.getType().name());
    return attributeIndex += 1;
  }

  private int setBaseUnit(int attributeIndex) {
    String baseUnit = this.id.getBaseUnit();
    if (baseUnit != null) {
      this.event.set(attributeIndex, baseUnit);
      return attributeIndex += 1;
    } else {
      return attributeIndex;
    }
  }

  private int setTags(int attributeIndex) {
    for (Tag tag : this.id.getTagsAsIterable()) {
      this.event.set(attributeIndex++, tag.getValue());
    }
    return attributeIndex;
  }

  void begin() {
    this.event.begin();
  }

  void end() {
    this.event.end();
  }

  void commit() {
    this.event.commit();
  }

}
