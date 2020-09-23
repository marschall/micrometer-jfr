package com.github.marschall.micrometer.jfr;

import java.util.List;

import io.micrometer.core.instrument.Meter.Id;
import jdk.jfr.AnnotationElement;
import jdk.jfr.Description;
import jdk.jfr.EventFactory;
import jdk.jfr.Label;
import jdk.jfr.ValueDescriptor;

final class DistributionSummaryEventFactory extends AbstractMeterEventFactory<JfrDistributionSummaryEvent> {

  DistributionSummaryEventFactory(Id id) {
    super(id);
  }

  @Override
  protected List<ValueDescriptor> getAdditionalValueDescriptors() {
    List<AnnotationElement> amountAnnotations = List.of(
        new AnnotationElement(Label.class, "Amount"),
        new AnnotationElement(Description.class, "Amount for an event being measured."));
    ValueDescriptor amountDescriptor = new ValueDescriptor(double.class, "amount", amountAnnotations);

    return List.of(amountDescriptor);
  }

  @Override
  JfrDistributionSummaryEvent newEmptyEvent(EventFactory eventFactory) {
    return new JfrDistributionSummaryEvent(this.id, eventFactory.newEvent());
  }

}
