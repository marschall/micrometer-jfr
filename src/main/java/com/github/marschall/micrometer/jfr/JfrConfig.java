package com.github.marschall.micrometer.jfr;

import static io.micrometer.core.instrument.config.MeterRegistryConfigValidator.check;
import static io.micrometer.core.instrument.config.MeterRegistryConfigValidator.checkAll;
import static io.micrometer.core.instrument.config.validate.PropertyValidator.getEnum;

import io.micrometer.core.instrument.LongTaskTimer;
import io.micrometer.core.instrument.Timer;
import io.micrometer.core.instrument.config.MeterRegistryConfig;
import io.micrometer.core.instrument.config.validate.Validated;

/**
 * Configuration for {@link JfrMeterRegistry}.
 */
public interface JfrConfig extends MeterRegistryConfig {

  /**
   * Default configuration.
   */
  JfrConfig DEFAULT = k -> null;

  @Override
  default String prefix() {
    return "jfr";
  }

  /**
   * How the statistics for {@link Timer} and {@link LongTaskTimer} should
   * be optimized. 
   * 
   * @return the optimization mode
   */
  default StatisticsMode statisticsMode() {
      return getEnum(this, StatisticsMode.class, "statisticsMode").orElse(StatisticsMode.MEMORY);
  }

  @Override
  default Validated<?> validate() {
      return checkAll(this, check("mode", JfrConfig::statisticsMode));
  }

}
