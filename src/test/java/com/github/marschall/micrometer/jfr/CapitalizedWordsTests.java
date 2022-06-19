package com.github.marschall.micrometer.jfr;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import io.micrometer.core.instrument.config.NamingConvention;

class CapitalizedWordsTests {

  private NamingConvention convention;

  @BeforeEach
  void setUp() {
    this.convention = CapitalizedWords.IINSTANCE;
  }

  @Test
  void tagKeys() {
    assertEquals("Job Name", this.convention.tagKey("job.name"));
    assertEquals("Status", this.convention.tagKey("status"));
  }

}
