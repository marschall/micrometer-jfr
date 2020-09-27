package com.github.marschall.micrometer.jfr;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.Test;

import jdk.jfr.Timespan;

class AbstractMeterEventFactoryTests {

  @Test
  void supportedTimeUnits() {
    assertEquals(Timespan.NANOSECONDS, AbstractMeterEventFactory.mapTimeUnitToTimespan(TimeUnit.NANOSECONDS));
    assertEquals(Timespan.MICROSECONDS, AbstractMeterEventFactory.mapTimeUnitToTimespan(TimeUnit.MICROSECONDS));
    assertEquals(Timespan.MILLISECONDS, AbstractMeterEventFactory.mapTimeUnitToTimespan(TimeUnit.MILLISECONDS));
    assertEquals(Timespan.SECONDS, AbstractMeterEventFactory.mapTimeUnitToTimespan(TimeUnit.SECONDS));
  }

  @Test
  void unsupporteTimeUnits() {
    assertThrows(IllegalArgumentException.class, () -> AbstractMeterEventFactory.mapTimeUnitToTimespan(TimeUnit.MINUTES));
    assertThrows(IllegalArgumentException.class, () -> AbstractMeterEventFactory.mapTimeUnitToTimespan(TimeUnit.HOURS));
    assertThrows(IllegalArgumentException.class, () -> AbstractMeterEventFactory.mapTimeUnitToTimespan(TimeUnit.DAYS));
  }

  @Test
  void tagKeys() {
    assertEquals("Job Name", AbstractMeterEventFactory.CAPITALIZED_WORDS.tagKey("job.name"));
  }

}
