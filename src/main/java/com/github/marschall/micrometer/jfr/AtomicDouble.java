package com.github.marschall.micrometer.jfr;

import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicLongFieldUpdater;

/**
 * A minimal atomic double implementation.
 */
final class AtomicDouble {

  static final AtomicLongFieldUpdater<AtomicDouble> VALUE_UPDATER =
          AtomicLongFieldUpdater.newUpdater(AtomicDouble.class, "value");

  private volatile long value;

  AtomicDouble() {
    this.value = Double.doubleToLongBits(0.0d);
  }

  /**
   * Adds a given value to the existing one and returns the new sum.
   * 
   * @param increment the value to add
   * @return the new value
   */
  double add(double increment) {
    long sum = VALUE_UPDATER.accumulateAndGet(this, Double.doubleToLongBits(increment), (long l1, long l2) -> {
      double d1 = Double.longBitsToDouble(l1);
      double d2 = Double.longBitsToDouble(l2);
      return Double.doubleToLongBits(d1 + d2);
    });
    return Double.longBitsToDouble(sum);
  }

  /**
   * Returns the current value of this {@code AtomicDouble} as a {@code double}.
   * 
   * @see AtomicLong#longValue()
   */
  double doubleValue() {
    return this.value;
  }

}
