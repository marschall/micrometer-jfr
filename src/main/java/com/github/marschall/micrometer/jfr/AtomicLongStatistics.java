package com.github.marschall.micrometer.jfr;

import java.util.concurrent.atomic.LongAccumulator;
import java.util.concurrent.atomic.LongAdder;

/**
 * A minimal statistics implementation for {@code long} values using
 * {@code java.util.concurrent.atomic} types consuming more memory
 * but offering better concurrency.
 */
final class AtomicLongStatistics implements LongStatistics {

  private final LongAdder count;

  private final LongAccumulator max;

  private final LongAdder totalAmount;

  AtomicLongStatistics() {
    this.count = new LongAdder();
    this.max = new LongAccumulator(Math::max, Long.MIN_VALUE);
    this.totalAmount = new LongAdder();
  }

  @Override
  public void record(long value) {
    this.count.increment();
    this.max.accumulate(value);
    this.totalAmount.add(value);
  }

  @Override
  public long count() {
    return this.count.longValue();
  }

  @Override
  public long max() {
    return this.max.longValue();
  }

  @Override
  public long totalAmount() {
    return this.totalAmount.longValue();
  }

}
