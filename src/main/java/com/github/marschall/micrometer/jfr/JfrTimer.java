package com.github.marschall.micrometer.jfr;

import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

import io.micrometer.core.instrument.Timer;
import io.micrometer.core.instrument.distribution.DistributionStatisticConfig;
import io.micrometer.core.instrument.distribution.HistogramSnapshot;
import io.micrometer.core.instrument.distribution.pause.PauseDetector;

final class JfrTimer implements Timer {

  private final Id id;
  private final DistributionStatisticConfig distributionStatisticConfig;
  private final PauseDetector pauseDetector;

  JfrTimer(Id id, DistributionStatisticConfig distributionStatisticConfig, PauseDetector pauseDetector) {
    this.id = id;
    this.distributionStatisticConfig = distributionStatisticConfig;
    this.pauseDetector = pauseDetector;
  }

  @Override
  public Id getId() {
    return this.id;
  }

  @Override
  public HistogramSnapshot takeSnapshot() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public void record(long amount, TimeUnit unit) {
    // TODO Auto-generated method stub

  }

  @Override
  public <T> T record(Supplier<T> f) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public <T> T recordCallable(Callable<T> f) throws Exception {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public void record(Runnable f) {
    // TODO Auto-generated method stub

  }

  @Override
  public long count() {
    // TODO Auto-generated method stub
    return 0;
  }

  @Override
  public double totalTime(TimeUnit unit) {
    // TODO Auto-generated method stub
    return 0;
  }

  @Override
  public double max(TimeUnit unit) {
    // TODO Auto-generated method stub
    return 0;
  }

  @Override
  public TimeUnit baseTimeUnit() {
    // TODO Auto-generated method stub
    return null;
  }

}
