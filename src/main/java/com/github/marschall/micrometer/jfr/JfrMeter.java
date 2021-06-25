package com.github.marschall.micrometer.jfr;

import java.util.concurrent.TimeUnit;
import java.util.function.Function;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.DistributionSummary;
import io.micrometer.core.instrument.FunctionCounter;
import io.micrometer.core.instrument.FunctionTimer;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.LongTaskTimer;
import io.micrometer.core.instrument.Measurement;
import io.micrometer.core.instrument.Meter;
import io.micrometer.core.instrument.TimeGauge;
import io.micrometer.core.instrument.Timer;

final class JfrMeter extends AbstractJfrMeter<MeterEventFactory, JfrMeterEvent> implements Meter {

  private final Type type;
  private final Iterable<Measurement> measurements;
  private final Runnable hook;

  JfrMeter(Id id, Type type, Iterable<Measurement> measurements, TimeUnit baseTimeUnit) {
    super(id, new MeterEventFactory(id, baseTimeUnit, measurements));
    this.type = type;
    this.measurements = measurements;
    this.hook = () -> this.recordEvent();
    this.meterEventFactory.registerPeriodicEvent(this.jfrEventFactory, this.hook);
  }

  @Override
  public void close() {
    this.meterEventFactory.unregisterPeriodicEvent(this.hook);
    super.close();
  }

  private void recordEvent() {
    JfrMeterEvent event = this.newEmptyEvent();
    event.recordValues(this.type, this.measurements);
    event.commit();
  }

  @Override
  public Iterable<Measurement> measure() {
    return this.measurements;
  }
  
  @Override
  public <T> T match(Function<Gauge, T> visitGauge, Function<Counter, T> visitCounter, Function<Timer, T> visitTimer,
      Function<DistributionSummary, T> visitSummary, Function<LongTaskTimer, T> visitLongTaskTimer,
      Function<TimeGauge, T> visitTimeGauge, Function<FunctionCounter, T> visitFunctionCounter,
      Function<FunctionTimer, T> visitFunctionTimer, Function<Meter, T> visitMeter) {
    return visitMeter.apply(this);
  }

}
