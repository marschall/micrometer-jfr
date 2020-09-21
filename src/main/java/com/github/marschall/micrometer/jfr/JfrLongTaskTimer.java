package com.github.marschall.micrometer.jfr;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.LongAdder;

import io.micrometer.core.instrument.Clock;
import io.micrometer.core.instrument.LongTaskTimer;
import io.micrometer.core.instrument.distribution.DistributionStatisticConfig;
import io.micrometer.core.instrument.distribution.HistogramSnapshot;
import io.micrometer.core.instrument.util.TimeUtils;
import jdk.jfr.AnnotationElement;
import jdk.jfr.Description;
import jdk.jfr.Event;
import jdk.jfr.Label;
import jdk.jfr.Timespan;
import jdk.jfr.ValueDescriptor;

final class JfrLongTaskTimer extends AbstractJfrMeter implements LongTaskTimer {

  private final DistributionStatisticConfig distributionStatisticConfig;
  private final TimeUnit baseTimeUnit;
  private final LongStatistics statistics;
  private final LongAdder activeTasks;
  private final Clock clock;

  JfrLongTaskTimer(Id id, DistributionStatisticConfig distributionStatisticConfig, TimeUnit baseTimeUnit, Clock clock) {
    super(id, baseTimeUnit);
    this.distributionStatisticConfig = distributionStatisticConfig;
    this.baseTimeUnit = baseTimeUnit;
    this.clock = clock;
    this.statistics = new LongStatistics();
    this.activeTasks = new LongAdder();
  }

  @Override
  protected List<ValueDescriptor> getAdditionalValueDescriptors(TimeUnit baseTimeUnit) {
    List<AnnotationElement> amountAnnotations = List.of(
        new AnnotationElement(Label.class, "Duration"),
        new AnnotationElement(Description.class, "Duration in " + baseTimeUnit),
        new AnnotationElement(Timespan.class, TimeUnitUtils.mapTimeUnitToTimespan(baseTimeUnit)));
    ValueDescriptor amountDescriptor = new ValueDescriptor(long.class, "duration", amountAnnotations);

    return List.of(amountDescriptor);
  }

  Event newEvent(long duration) {
    Event event = this.newEmptyEvent();

    this.setEventAttributes(duration, event);

    return event;
  }

  private void setEventAttributes(long duration, Event event) {
    int attributeIndex = 0;
    attributeIndex = this.setCommonEventAttributes(event, attributeIndex);
    event.set(attributeIndex++, duration);
  }

  @Override
  public HistogramSnapshot takeSnapshot() {
    return HistogramSnapshot.empty(this.statistics.count(), this.duration(this.baseTimeUnit()), this.max(this.baseTimeUnit()));
  }

  @Override
  public Sample start() {
    this.activeTasks.increment();
    Event event = this.newEvent();
    long start = this.clock.monotonicTime();
    return new JfrSample(event, start);
  }

  void decrementActiveTasks() {
    this.activeTasks.decrement();
  }

  @Override
  public double duration(TimeUnit destinationUnit) {
    long totalAmount = this.statistics.totalAmount();
    return TimeUtils.convert(totalAmount, this.baseTimeUnit(), destinationUnit);
  }

  @Override
  public int activeTasks() {
    return this.activeTasks.intValue();
  }

  @Override
  public double max(TimeUnit destinationUnit) {
    long max = this.statistics.max();
    return TimeUtils.convert(max, this.baseTimeUnit(), destinationUnit);
  }

  @Override
  public TimeUnit baseTimeUnit() {
    return this.baseTimeUnit;
  }

  final class JfrSample extends Sample {

    private final Event event;
    private final long start;
    private long duration;

    JfrSample(Event event, long start) {
      this.event = event;
      this.start = start;
    }

    @Override
    public long stop() {
      this.event.end();

      long end = JfrLongTaskTimer.this.clock.monotonicTime();
      this.duration = end - start;
      JfrLongTaskTimer.this.setEventAttributes(this.duration, this.event);
      this.event.commit();

      JfrLongTaskTimer.this.decrementActiveTasks();
      return TimeUnit.NANOSECONDS.convert(this.duration, JfrLongTaskTimer.this.baseTimeUnit);
    }

    @Override
    public double duration(TimeUnit unit) {
      return TimeUtils.convert(duration, JfrLongTaskTimer.this.baseTimeUnit, unit);
    }

  }

}
