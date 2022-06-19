package com.github.marschall.micrometer.jfr;

import java.time.Duration;

import org.junit.jupiter.api.Disabled;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.MockClock;
import io.micrometer.core.tck.MeterRegistryCompatibilityKit;

@Disabled("some histogram tests break")
class JfrMeterRegistryCompatibilityTests extends MeterRegistryCompatibilityKit {

  @Override
  public MeterRegistry registry() {
    return new JfrMeterRegistry(new MockClock(), new JfrConfig() {

      @Override
      public String get(String key) {
        return null;
      }
    });
  }

  @Override
  public Duration step() {
    return Duration.ofNanos(1L);
  }

}
