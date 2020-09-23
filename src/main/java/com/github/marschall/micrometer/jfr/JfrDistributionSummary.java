package com.github.marschall.micrometer.jfr;

import io.micrometer.core.instrument.DistributionSummary;
import io.micrometer.core.instrument.distribution.DistributionStatisticConfig;
import io.micrometer.core.instrument.distribution.HistogramSnapshot;

final class JfrDistributionSummary extends AbstractJfrMeter<DistributionSummaryEventFactory, JfrDistributionSummaryEvent> implements DistributionSummary {

  private final DistributionStatisticConfig distributionStatisticConfig;
  private final double scale;
  private final DoubleStatistics statistics;

  JfrDistributionSummary(Id id, DistributionStatisticConfig distributionStatisticConfig, double scale) {
    super(id, new DistributionSummaryEventFactory(id));
    this.distributionStatisticConfig = distributionStatisticConfig;
    this.scale = scale;
    this.statistics = new DoubleStatistics();
  }

  @Override
  public HistogramSnapshot takeSnapshot() {
    return HistogramSnapshot.empty(this.count(), this.totalAmount(), this.max());
  }

  @Override
  public void record(double amount) {
    double value = amount * this.scale;
    this.statistics.record(value);

    JfrDistributionSummaryEvent event = this.newEmptyEvent();
    event.setAmount(value);
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
