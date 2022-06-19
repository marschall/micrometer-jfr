package com.github.marschall.micrometer.jfr;

@FunctionalInterface
interface LongStatisticsFactory {

  LongStatistics newLongStatistics();

  static LongStatisticsFactory newInstance(StatisticsMode mode) {
    switch (mode) {
    case NONE:
      return () -> NullLongStatistics.INSTANCE;
    case MEMORY:
      return FieldUpdaterLongStatistics::new;
    case CONCURRENCY:
      return AtomicLongStatistics::new;
    default:
      throw new IncompatibleClassChangeError("unknown enum value: " + mode);
    }
  }


}
