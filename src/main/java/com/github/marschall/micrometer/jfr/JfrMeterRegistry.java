package com.github.marschall.micrometer.jfr;

import java.util.Objects;
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
import io.micrometer.core.lang.Nullable;

/**
 * A Micrometer meter registry that generates JFR events for metrics.
 */
public final class JfrMeterRegistry extends MeterRegistry {

  /**
   * Default constructor using the given clock.
   *
   * @param clock the clock to use, not {@code null}
   */
  public JfrMeterRegistry(Clock clock) {
    super(clock);
    Objects.requireNonNull(clock, "clock");
  }

  /**
   * Default constructor using the system clock.
   */
  public JfrMeterRegistry() {
    this(Clock.SYSTEM);
  }

  @Override
  protected <T> Gauge newGauge(Id id, @Nullable T obj, ToDoubleFunction<T> valueFunction) {
    return new JfrGauge<>(id, obj, valueFunction);
  }

  @Override
  protected Counter newCounter(Id id) {
    return new JfrCounter(id);
  }

  @Override
  protected Timer newTimer(Id id, DistributionStatisticConfig distributionStatisticConfig, PauseDetector pauseDetector) {
    return new JfrTimer(id, distributionStatisticConfig, pauseDetector, this.getBaseTimeUnit());
  }

  @Override
  protected LongTaskTimer newLongTaskTimer(Id id, DistributionStatisticConfig distributionStatisticConfig) {
    return new JfrLongTaskTimer(id, distributionStatisticConfig, this.getBaseTimeUnit());
  }

  @Override
  @Deprecated
  protected LongTaskTimer newLongTaskTimer(Id id) {
    return this.newLongTaskTimer(id, null);
  }

  @Override
  protected DistributionSummary newDistributionSummary(Id id, DistributionStatisticConfig distributionStatisticConfig, double scale) {
    return new JfrDistributionSummary(id, distributionStatisticConfig, scale);
  }

  @Override
  protected Meter newMeter(Id id, Type type, Iterable<Measurement> measurements) {
    return new JfrMeter(id, type, measurements);
  }

  @Override
  protected <T> FunctionTimer newFunctionTimer(Id id, T obj,
          ToLongFunction<T> countFunction,
          ToDoubleFunction<T> totalTimeFunction,
          TimeUnit totalTimeFunctionUnit) {
    return new JfrFunctionTimer<>(id, obj, countFunction, totalTimeFunction, totalTimeFunctionUnit, this.getBaseTimeUnit());
  }

  @Override
  protected <T> FunctionCounter newFunctionCounter(Id id, T obj, ToDoubleFunction<T> countFunction) {
    return new JfrFunctionCounter<>(id, obj, countFunction);
  }

  @Override
  protected TimeUnit getBaseTimeUnit() {
    return TimeUnit.NANOSECONDS;
  }

  @Override
  protected DistributionStatisticConfig defaultHistogramConfig() {
    return DistributionStatisticConfig.NONE;
  }

  @Override
  public String toString() {
    return "JFR MeterRegistry";
  }

}
