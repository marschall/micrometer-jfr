package com.github.marschall.micrometer.jfr;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import io.micrometer.core.instrument.Measurement;
import io.micrometer.core.instrument.Meter.Id;
import io.micrometer.core.instrument.Statistic;
import jdk.jfr.AnnotationElement;
import jdk.jfr.Description;
import jdk.jfr.EventFactory;
import jdk.jfr.Label;
import jdk.jfr.Timespan;
import jdk.jfr.ValueDescriptor;

final class MeterEventFactory extends AbstractMeterEventFactory<JfrMeterEvent> {

  private final Iterable<Measurement> measurements;

  MeterEventFactory(Id id, TimeUnit baseTimeUnit, Iterable<Measurement> measurements) {
    super(id, baseTimeUnit);
    this.measurements = measurements;
  }

  @Override
  protected List<ValueDescriptor> getAdditionalValueDescriptors() {
    List<ValueDescriptor> valueDescriptors = new ArrayList<>();
    
    List<AnnotationElement> typeAnnotations = List.of(
      new AnnotationElement(Label.class, "Type"),
      new AnnotationElement(Description.class, "What kind of meter this is."));
    ValueDescriptor typeDescriptor = new ValueDescriptor(String.class, "type", typeAnnotations);
    valueDescriptors.add(typeDescriptor);

    for (Measurement measurement : this.measurements) {
      Statistic statistic = measurement.getStatistic();
      ValueDescriptor statisticDescriptor;
      switch (statistic) {
        case TOTAL_TIME:
          List<AnnotationElement> totalTimeAnnotations = List.of(
              new AnnotationElement(Label.class, "Total Time"),
              new AnnotationElement(Description.class, "The total time of all occurrences of the timed event metered in " + this.baseTimeUnit),
              new AnnotationElement(Timespan.class, this.getTimespanOfBaseTimeUnit()));
          statisticDescriptor = new ValueDescriptor(double.class, "totalTime", totalTimeAnnotations);
        case DURATION:
          List<AnnotationElement> durationAnnotations = List.of(
              new AnnotationElement(Label.class, "Metered Duration"),
              new AnnotationElement(Description.class, "Duration in " + this.baseTimeUnit),
              new AnnotationElement(Timespan.class, this.getTimespanOfBaseTimeUnit()));
          // "duration" causes an error
          statisticDescriptor = new ValueDescriptor(double.class, "meteredDuration", durationAnnotations);
        default:
          List<AnnotationElement> statisticAnnotations = List.of(
              new AnnotationElement(Label.class, statistic.getTagValueRepresentation()));
          statisticDescriptor = new ValueDescriptor(double.class, statistic.getTagValueRepresentation(), statisticAnnotations);
      }
      valueDescriptors.add(statisticDescriptor);
    }

    return valueDescriptors;
  }
  
  @Override
  JfrMeterEvent newEmptyEvent(EventFactory eventFactory) {
    return new JfrMeterEvent(this.id, eventFactory.newEvent());
  }

}
