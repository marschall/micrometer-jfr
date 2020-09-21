package com.github.marschall.micrometer.jfr;

import java.util.concurrent.atomic.DoubleAccumulator;
import java.util.concurrent.atomic.DoubleAdder;
import java.util.concurrent.atomic.LongAdder;

/**
 * A minimal statistics implementation for {@code double} values.
 */
final class DoubleStatistics {

  private final LongAdder count;

  private final DoubleAccumulator max;

  private final DoubleAdder totalAmount;

  DoubleStatistics() {
    this.count = new LongAdder();
    this.max = new DoubleAccumulator(Math::max, Double.MIN_VALUE);
    this.totalAmount = new DoubleAdder();
  }

  void record(double value) {
    this.count.increment();
    this.max.accumulate(value);
    this.totalAmount.add(value);
  }

  long count() {
    return this.count.longValue();
  }

  double max() {
    return this.max.doubleValue();
  }

  double totalAmount() {
    return this.totalAmount.doubleValue();
  }

}
