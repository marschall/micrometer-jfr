package com.github.marschall.micrometer.jfr;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class AtomicDoubleTests {

  private AtomicDouble atomicDouble;

  @BeforeEach
  void setUp() {
    this.atomicDouble = new AtomicDouble();
  }

  @Test
  void add() {
    assertEquals(0.0d, this.atomicDouble.add(0.0d), 0.000001d);
    assertEquals(1.0d, this.atomicDouble.add(1.0d), 0.000001d);
    assertEquals(2.0d, this.atomicDouble.add(1.0d), 0.000001d);
  }
  
  @Test
  void doubleValue() {
    assertEquals(1.0d, this.atomicDouble.add(1.0d), 0.000001d);
    assertEquals(1.0d, this.atomicDouble.doubleValue(), 0.000001d);
  }

}
