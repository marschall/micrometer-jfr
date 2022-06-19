package com.github.marschall.micrometer.jfr;

import io.micrometer.core.instrument.LongTaskTimer;
import io.micrometer.core.instrument.Timer;

/**
 * How the statistics for {@link Timer} and {@link LongTaskTimer} should
 * be optimized.
 */
public enum StatisticsMode {

  /**
   * Do not implement statistics for {@link Timer} and {@link LongTaskTimer}.
   * <p>
   * While this gives you the best performance and lowest overhead this
   * will have impact the following methods which will no longer return
   * meaningful results.
   * <ul>
   *  <li>{@link Timer#takeSnapshot()} </li>
   *  <li>{@link Timer#totalTime(java.util.concurrent.TimeUnit)} </li>
   *  <li>{@link Timer#max(java.util.concurrent.TimeUnit)}</li>
   *  <li>{@link LongTaskTimer#takeSnapshot()} </li>
   *  <li>{@link LongTaskTimer#max(java.util.concurrent.TimeUnit)}</li>
   * </ul>
   */
  NONE,

  /**
   * Optimize for memory usage.
   */
  MEMORY,

  /**
   * Optimize for concurrency.
   */
  CONCURRENCY;

}
