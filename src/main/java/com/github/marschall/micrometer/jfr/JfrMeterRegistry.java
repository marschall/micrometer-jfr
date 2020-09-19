package com.github.marschall.micrometer.jfr;

import java.util.concurrent.TimeUnit;
import java.util.function.ToDoubleFunction;
import java.util.function.ToLongFunction;

import io.micrometer.core.instrument.Clock;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.DistributionSummary;
import io.micrometer.core.instrument.FunctionCounter;
import io.micrometer.core.instrument.FunctionTimer;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.LongTaskTimer;
import io.micrometer.core.instrument.Measurement;
import io.micrometer.core.instrument.Meter;
import io.micrometer.core.instrument.Meter.Id;
import io.micrometer.core.instrument.Meter.Type;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import io.micrometer.core.instrument.distribution.DistributionStatisticConfig;
import io.micrometer.core.instrument.distribution.pause.PauseDetector;

public final class JfrMeterRegistry extends MeterRegistry {

  public JfrMeterRegistry(Clock clock) {
    super(clock);
  }

  public JfrMeterRegistry() {
    this(Clock.SYSTEM);
  }

  @Override
  protected <T> Gauge newGauge(Id id, T obj, ToDoubleFunction<T> valueFunction) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  protected Counter newCounter(Id id) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  protected Timer newTimer(Id id,
          DistributionStatisticConfig distributionStatisticConfig,
          PauseDetector pauseDetector) {
    return new JfrTimer(id, distributionStatisticConfig, pauseDetector);
  }

  @Override
  protected LongTaskTimer newLongTaskTimer(Id id, DistributionStatisticConfig distributionStatisticConfig) {
    return new JfrLongTaskTimer(id, distributionStatisticConfig);
  }

  @Override
  protected LongTaskTimer newLongTaskTimer(Id id) {
    return this.newLongTaskTimer(id, null);
  }

  @Override
  protected DistributionSummary newDistributionSummary(Id id,
          DistributionStatisticConfig distributionStatisticConfig,
          double scale) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  protected Meter newMeter(Id id, Type type, Iterable<Measurement> measurements) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  protected <T> FunctionTimer newFunctionTimer(Id id, T obj,
          ToLongFunction<T> countFunction,
          ToDoubleFunction<T> totalTimeFunction,
          TimeUnit totalTimeFunctionUnit) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  protected <T> FunctionCounter newFunctionCounter(Id id, T obj, ToDoubleFunction<T> countFunction) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  protected TimeUnit getBaseTimeUnit() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  protected DistributionStatisticConfig defaultHistogramConfig() {
    // TODO Auto-generated method stub
    return null;
  }

}
