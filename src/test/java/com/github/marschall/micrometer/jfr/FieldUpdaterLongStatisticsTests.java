package com.github.marschall.micrometer.jfr;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class FieldUpdaterLongStatisticsTests {

  private LongStatistics statistics;

  @BeforeEach
  void setUp() {
    this.statistics = new FieldUpdaterLongStatistics();
  }

  @Test
  void test() {
    this.statistics.record(1L);
    this.statistics.record(42L);

    assertEquals(2L, this.statistics.count());
    assertEquals(42L, this.statistics.max());
    assertEquals(43L, this.statistics.totalAmount());
  }

}
