package com.github.marschall.micrometer.jfr;

interface LongStatistics {

  long totalAmount();

  long max();

  long count();

  void record(long value);

}