package com.github.marschall.micrometer.jfr;

import java.util.concurrent.TimeUnit;

import jdk.jfr.Timespan;

final class TimeUnitUtils {

  private TimeUnitUtils() {
    throw new AssertionError("not instantiable");
  }

  static String mapTimeUnitToTimespan(TimeUnit timeUnit) {
    switch (timeUnit) {
    case NANOSECONDS:
      return Timespan.NANOSECONDS;
    case MICROSECONDS:
      return Timespan.MICROSECONDS;
    case MILLISECONDS:
      return Timespan.MILLISECONDS;
    case SECONDS:
      return Timespan.SECONDS;
    default:
      throw new IllegalArgumentException("unsupporte time unit");
    }
  }

}
