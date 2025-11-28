package com.github.marschall.micrometer.jfr;

import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.function.ToDoubleFunction;
import java.util.function.ToLongFunction;

import org.jspecify.annotations.Nullable;

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
import io.micrometer.core.instrument.TimeGauge;
import io.micrometer.core.instrument.Timer;
import io.micrometer.core.instrument.distribution.DistributionStatisticConfig;
import io.micrometer.core.instrument.distribution.pause.PauseDetector;

/**
 * A Micrometer meter registry that generates JFR events for metrics.
 */
public final class JfrMeterRegistry extends MeterRegistry {

  private final JfrConfig configuration;

  private final LongStatisticsFactory longStatisticsFactory;

  /**
   * Constructor using the given clock and configuration.
   *
   * @param clock the clock to use, not {@code null}
   * @param configuration the configuration to use, not {@code configuration}
   */
  JfrMeterRegistry(Clock clock, JfrConfig configuration) {
    super(clock);
    Objects.requireNonNull(clock, "clock");
    Objects.requireNonNull(configuration, "configuration");
    this.configuration = configuration;
    this.longStatisticsFactory = LongStatisticsFactory.newInstance(configuration.statisticsMode());
  }

  /**
   * Default constructor using the system clock and the default configuration.
   */
  public JfrMeterRegistry() {
    this(Clock.SYSTEM, JfrConfig.DEFAULT);
  }

  /**
   * Constructor using the given configuration.
   *
   * @param configuration the configuration to use, not {@code configuration}
   */
  public JfrMeterRegistry(JfrConfig configuration) {
    this(Clock.SYSTEM, configuration);
  }

  @Override
  protected <T> Gauge newGauge(Id id, @Nullable T obj, ToDoubleFunction<T> valueFunction) {
    Gauge gauge = new JfrGauge<>(id, obj, valueFunction);
    gauge.value(); // record the initial value
    return gauge;
  }

  @Override
  protected <T> TimeGauge newTimeGauge(Id id, T obj, TimeUnit valueFunctionUnit, ToDoubleFunction<T> valueFunction) {
    TimeGauge gauge = new JfrTimeGauge<>(id, obj, valueFunctionUnit, valueFunction);
    gauge.value(); // record the initial value
    return gauge;
  }

  @Override
  protected Counter newCounter(Id id) {
    return new JfrCounter(id);
  }

  @Override
  protected Timer newTimer(Id id, DistributionStatisticConfig distributionStatisticConfig, PauseDetector pauseDetector) {
    return new JfrTimer(id, distributionStatisticConfig, this.getBaseTimeUnit(), this.clock, this.longStatisticsFactory);
  }

  @Override
  protected LongTaskTimer newLongTaskTimer(Id id, DistributionStatisticConfig distributionStatisticConfig) {
    return new JfrLongTaskTimer(id, this.getBaseTimeUnit(), this.clock, this.longStatisticsFactory);
  }

  @Override
  @Deprecated
  protected LongTaskTimer newLongTaskTimer(Id id) {
    return this.newLongTaskTimer(id, defaultHistogramConfig());
  }

  @Override
  protected DistributionSummary newDistributionSummary(Id id, DistributionStatisticConfig distributionStatisticConfig, double scale) {
    return new JfrDistributionSummary(id, scale);
  }

  @Override
  protected Meter newMeter(Id id, Type type, Iterable<Measurement> measurements) {
    return new JfrMeter(id, type, measurements, this.getBaseTimeUnit());
  }

  @Override
  protected <T> FunctionTimer newFunctionTimer(Id id, T obj,
          ToLongFunction<T> countFunction,
          ToDoubleFunction<T> totalTimeFunction,
          TimeUnit totalTimeFunctionUnit) {
    JfrFunctionTimer<T> functionTimer = new JfrFunctionTimer<>(id, obj, countFunction, totalTimeFunction, totalTimeFunctionUnit, this.getBaseTimeUnit());
    functionTimer.recordEvent(); // record the initial value
    return functionTimer;
  }

  @Override
  protected <T> FunctionCounter newFunctionCounter(Id id, T obj, ToDoubleFunction<T> countFunction) {
    FunctionCounter functionCounter = new JfrFunctionCounter<>(id, obj, countFunction);
    functionCounter.count(); // record the initial value
    return functionCounter;
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
