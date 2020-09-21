package com.github.marschall.micrometer.jfr;

import java.util.List;
import java.util.concurrent.TimeUnit;

import io.micrometer.core.instrument.DistributionSummary;
import io.micrometer.core.instrument.distribution.DistributionStatisticConfig;
import io.micrometer.core.instrument.distribution.HistogramSnapshot;
import jdk.jfr.AnnotationElement;
import jdk.jfr.Description;
import jdk.jfr.Event;
import jdk.jfr.Label;
import jdk.jfr.ValueDescriptor;

final class JfrDistributionSummary extends AbstractJfrMeter implements DistributionSummary {

  private final DistributionStatisticConfig distributionStatisticConfig;
  private final double scale;
  private final DoubleStatistics statistics;

  JfrDistributionSummary(Id id, DistributionStatisticConfig distributionStatisticConfig, double scale) {
    super(id);
    this.distributionStatisticConfig = distributionStatisticConfig;
    this.scale = scale;
    this.statistics = new DoubleStatistics();
  }

  @Override
  protected List<ValueDescriptor> getAdditionalValueDescriptors(TimeUnit baseTimeUnit) {
    List<AnnotationElement> amountAnnotations = List.of(
            new AnnotationElement(Label.class, "Amount"),
            new AnnotationElement(Description.class, "Amount for an event being measured."));
    ValueDescriptor amountDescriptor = new ValueDescriptor(double.class, "amount", amountAnnotations);

    return List.of(amountDescriptor);
  }

  Event newEvent(double amount) {
   Event event = this.newEmptyEvent();
    int attributeIndex = 0;

    attributeIndex = this.setCommonEventAttributes(event, attributeIndex);
    event.set(attributeIndex++, amount);

    return event;
  }

  @Override
  public HistogramSnapshot takeSnapshot() {
    return HistogramSnapshot.empty(this.count(), this.totalAmount(), this.max());
  }

  @Override
  public void record(double amount) {
    double value = amount * this.scale;
    this.statistics.record(value);

    Event event = this.newEvent(value);
    event.commit();
  }

  @Override
  public long count() {
    return this.statistics.count();
  }

  @Override
  public double totalAmount() {
    return this.statistics.totalAmount();
  }

  @Override
  public double max() {
    return this.statistics.max();
  }

}
