package com.github.marschall.micrometer.jfr;

import java.util.concurrent.TimeUnit;

import io.micrometer.core.instrument.LongTaskTimer;
import io.micrometer.core.instrument.distribution.DistributionStatisticConfig;
import io.micrometer.core.instrument.distribution.HistogramSnapshot;

final class JfrLongTaskTimer implements LongTaskTimer {

  private final Id id;
  private final DistributionStatisticConfig distributionStatisticConfig;

  JfrLongTaskTimer(Id id, DistributionStatisticConfig distributionStatisticConfig) {
    this.id = id;
    this.distributionStatisticConfig = distributionStatisticConfig;
  }

  @Override
  public Id getId() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public HistogramSnapshot takeSnapshot() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public Sample start() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public double duration(TimeUnit unit) {
    // TODO Auto-generated method stub
    return 0;
  }

  @Override
  public int activeTasks() {
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
