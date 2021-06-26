package com.github.marschall.micrometer.jfr;

import java.util.List;
import java.util.concurrent.TimeUnit;

import io.micrometer.core.instrument.Meter.Id;
import jdk.jfr.AnnotationElement;
import jdk.jfr.Description;
import jdk.jfr.EventFactory;
import jdk.jfr.Label;
import jdk.jfr.Timespan;
import jdk.jfr.ValueDescriptor;

final class TimerGaugeEventFactory extends AbstractMeterEventFactory<JfrTimerGaugeEvent> {

  TimerGaugeEventFactory(Id id, TimeUnit baseTimeUnit) {
    super(id, baseTimeUnit);
  }

  @Override
  protected List<ValueDescriptor> getAdditionalValueDescriptors() {
    List<AnnotationElement> valueAnnotations = List.of(
            new AnnotationElement(Label.class, "Value"),
            new AnnotationElement(Description.class, "The current value of the gauge in " + this.baseTimeUnit),
            new AnnotationElement(Timespan.class, this.getTimespanOfBaseTimeUnit()));
    ValueDescriptor valueDescriptor = new ValueDescriptor(double.class, "value", valueAnnotations);

    return List.of(valueDescriptor);
  }

  @Override
  JfrTimerGaugeEvent newEmptyEvent(EventFactory eventFactory) {
    return new JfrTimerGaugeEvent(this.id, eventFactory.newEvent());
  }

}
