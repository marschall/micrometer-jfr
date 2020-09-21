package com.github.marschall.micrometer.jfr;

import java.util.concurrent.atomic.LongAccumulator;
import java.util.concurrent.atomic.LongAdder;

/**
 * A minimal statistics implementation for {@code long} values.
 */
final class LongStatistics {

  private final LongAdder count;

  private final LongAccumulator max;

  private final LongAdder totalAmount;

  LongStatistics() {
    this.count = new LongAdder();
    this.max = new LongAccumulator(Math::max, Long.MIN_VALUE);
    this.totalAmount = new LongAdder();
  }

  void record(long value) {
    this.count.increment();
    this.max.accumulate(value);
    this.totalAmount.add(value);
  }

  long count() {
    return this.count.longValue();
  }

  long max() {
    return this.max.longValue();
  }

  long totalAmount() {
    return this.totalAmount.longValue();
  }

}
