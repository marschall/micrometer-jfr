package com.github.marschall.micrometer.jfr;

final class NullLongStatistics implements LongStatistics {
  
  static final LongStatistics INSTANCE = new NullLongStatistics();

  @Override
  public long totalAmount() {
    return 0L;
  }

  @Override
  public long max() {
    return 0L;
  }

  @Override
  public long count() {
    return 0L;
  }

  @Override
  public void record(long value) {
    // intentionally left empty
  }

}
