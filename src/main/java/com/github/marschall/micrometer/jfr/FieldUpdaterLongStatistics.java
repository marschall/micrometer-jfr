package com.github.marschall.micrometer.jfr;

import java.util.concurrent.atomic.AtomicLongFieldUpdater;

/**
 * A minimal statistics implementation for {@code long} values using
 * {@code AtomicLongFieldUpdater} types consuming less memory
 * but offering worse concurrency.
 */
final class FieldUpdaterLongStatistics implements LongStatistics {

  static final AtomicLongFieldUpdater<FieldUpdaterLongStatistics> COUNT_UPDATER =
          AtomicLongFieldUpdater.newUpdater(FieldUpdaterLongStatistics.class, "count");

  static final AtomicLongFieldUpdater<FieldUpdaterLongStatistics> MAX_UPDATER =
          AtomicLongFieldUpdater.newUpdater(FieldUpdaterLongStatistics.class, "max");

  static final AtomicLongFieldUpdater<FieldUpdaterLongStatistics> TOTALAMOUNT_UPDATER =
          AtomicLongFieldUpdater.newUpdater(FieldUpdaterLongStatistics.class, "totalAmount");

  private volatile long count;

  private volatile long max;

  private volatile long totalAmount;

  FieldUpdaterLongStatistics() {
    this.count = 0L;
    this.max = Long.MIN_VALUE;
    this.totalAmount = 0L;
  }

  @Override
  public long count() {
    return this.count;
  }

  @Override
  public long totalAmount() {
    return this.totalAmount;
  }

  @Override
  public long max() {
    return this.max;
  }

  @Override
  public void record(long value) {
    COUNT_UPDATER.incrementAndGet(this);
    MAX_UPDATER.getAndAccumulate(this, value, Long::max);
    TOTALAMOUNT_UPDATER.addAndGet(this, value);
  }

}
