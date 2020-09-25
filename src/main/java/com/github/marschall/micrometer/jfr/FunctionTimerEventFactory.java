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

final class FunctionTimerEventFactory extends AbstractMeterEventFactory<JfrFunctionTimerEvent> {

  FunctionTimerEventFactory(Id id, TimeUnit baseTimeUnit) {
    super(id, baseTimeUnit);
  }

  @Override
  protected List<ValueDescriptor> getAdditionalValueDescriptors() {
    List<AnnotationElement> totalTimeAnnotations = List.of(
        new AnnotationElement(Label.class, "Total Time"),
        new AnnotationElement(Description.class, "The total time of all occurrences of the timed event metered in " + this.baseTimeUnit),
        new AnnotationElement(Timespan.class, this.getTimespanOfBaseTimeUnit()));
    ValueDescriptor totalTimeDescriptor = new ValueDescriptor(long.class, "totalTime", totalTimeAnnotations);

    List<AnnotationElement> countAnnotations = List.of(
        new AnnotationElement(Label.class, "Count"),
        new AnnotationElement(Description.class, "The total number of occurrences of the timed event."));
    ValueDescriptor countDescriptor = new ValueDescriptor(double.class, "count", countAnnotations);

    return List.of(totalTimeDescriptor, countDescriptor);
  }

  @Override
  JfrFunctionTimerEvent newEmptyEvent(EventFactory eventFactory) {
    return new JfrFunctionTimerEvent(this.id, eventFactory.newEvent());
  }

}
