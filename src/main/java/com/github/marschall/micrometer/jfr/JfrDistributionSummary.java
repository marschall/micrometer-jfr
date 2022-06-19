package com.github.marschall.micrometer.jfr;

import java.util.function.Consumer;
import java.util.function.Function;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.DistributionSummary;
import io.micrometer.core.instrument.FunctionCounter;
import io.micrometer.core.instrument.FunctionTimer;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.LongTaskTimer;
import io.micrometer.core.instrument.Meter;
import io.micrometer.core.instrument.TimeGauge;
import io.micrometer.core.instrument.Timer;
import io.micrometer.core.instrument.distribution.HistogramSnapshot;

final class JfrDistributionSummary extends AbstractJfrMeter<DistributionSummaryEventFactory, JfrDistributionSummaryEvent> implements DistributionSummary {

  private final double scale;
  private final DoubleStatistics statistics;

  JfrDistributionSummary(Id id, double scale) {
    super(id, new DistributionSummaryEventFactory(id));
    this.scale = scale;
    this.statistics = new DoubleStatistics();
  }

  @Override
  public HistogramSnapshot takeSnapshot() {
    return HistogramSnapshot.empty(this.count(), this.totalAmount(), this.max());
  }

  @Override
  public void record(double amount) {
    if (amount >= 0.0d) {
      double value = amount * this.scale;
      this.statistics.record(value);
      JfrDistributionSummaryEvent event = this.newEmptyEvent();
      event.setAmount(value);
      event.commit();
    }
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

  @Override
  public <T> T match(Function<Gauge, T> visitGauge, Function<Counter, T> visitCounter, Function<Timer, T> visitTimer,
      Function<DistributionSummary, T> visitSummary, Function<LongTaskTimer, T> visitLongTaskTimer,
      Function<TimeGauge, T> visitTimeGauge, Function<FunctionCounter, T> visitFunctionCounter,
      Function<FunctionTimer, T> visitFunctionTimer, Function<Meter, T> visitMeter) {
    return visitSummary.apply(this);
  }

  @Override
  public void use(Consumer<Gauge> visitGauge, Consumer<Counter> visitCounter, Consumer<Timer> visitTimer,
      Consumer<DistributionSummary> visitSummary, Consumer<LongTaskTimer> visitLongTaskTimer,
      Consumer<TimeGauge> visitTimeGauge, Consumer<FunctionCounter> visitFunctionCounter,
      Consumer<FunctionTimer> visitFunctionTimer, Consumer<Meter> visitMeter) {
    visitSummary.accept(this);
  }

}
