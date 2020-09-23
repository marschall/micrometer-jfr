package com.github.marschall.micrometer.jfr;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.Test;

import jdk.jfr.Timespan;

class TimeUnitUtilsTests {

  @Test
  void supportedTimeUnits() {
    assertEquals(Timespan.NANOSECONDS, TimeUnitUtils.mapTimeUnitToTimespan(TimeUnit.NANOSECONDS));
    assertEquals(Timespan.MICROSECONDS, TimeUnitUtils.mapTimeUnitToTimespan(TimeUnit.MICROSECONDS));
    assertEquals(Timespan.MILLISECONDS, TimeUnitUtils.mapTimeUnitToTimespan(TimeUnit.MILLISECONDS));
    assertEquals(Timespan.SECONDS, TimeUnitUtils.mapTimeUnitToTimespan(TimeUnit.SECONDS));
  }
  
  @Test
  void unsupporteTimeUnits() {
    assertThrows(IllegalArgumentException.class, () -> TimeUnitUtils.mapTimeUnitToTimespan(TimeUnit.MINUTES));
    assertThrows(IllegalArgumentException.class, () -> TimeUnitUtils.mapTimeUnitToTimespan(TimeUnit.HOURS));
    assertThrows(IllegalArgumentException.class, () -> TimeUnitUtils.mapTimeUnitToTimespan(TimeUnit.DAYS));
  }

}
