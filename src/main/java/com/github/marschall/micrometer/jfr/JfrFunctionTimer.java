package com.github.marschall.micrometer.jfr;

import java.lang.ref.WeakReference;
import java.util.concurrent.TimeUnit;
import java.util.function.ToDoubleFunction;
import java.util.function.ToLongFunction;

import io.micrometer.core.instrument.FunctionTimer;
import io.micrometer.core.instrument.util.TimeUtils;
import jdk.jfr.Event;
import jdk.jfr.EventFactory;
import jdk.jfr.FlightRecorder;

final class JfrFunctionTimer<T> extends AbstractJfrMeter<FunctionTimerEventFactory, JfrFunctionTimerEvent> implements FunctionTimer {

  private final WeakReference<T> reference;
  private final ToLongFunction<T> countFunction;
  private final ToDoubleFunction<T> totalTimeFunction;
  private final TimeUnit totalTimeFunctionUnit;
  private final TimeUnit baseTimeUnit;
  private final Runnable hook;

  JfrFunctionTimer(Id id, T obj,
          ToLongFunction<T> countFunction,
          ToDoubleFunction<T> totalTimeFunction,
          TimeUnit totalTimeFunctionUnit,
          TimeUnit baseTimeUnit) {
    super(id, new FunctionTimerEventFactory(id, baseTimeUnit));
    this.reference = new WeakReference<>(obj);
    this.countFunction = countFunction;
    this.totalTimeFunction = totalTimeFunction;
    this.totalTimeFunctionUnit = totalTimeFunctionUnit;
    this.baseTimeUnit = baseTimeUnit;
    this.hook = () -> this.recordEvent();
  }

  @Override
  protected void registerPriodicEvent(EventFactory eventFactory) {
    Event event = eventFactory.newEvent();
    Class<? extends Event> eventClass = event.getClass();
    FlightRecorder.addPeriodicEvent(eventClass, this.hook);
  }

  @Override
  public void close() {
    FlightRecorder.removePeriodicEvent(this.hook);
    super.close();
  }
  
  void recordEvent() {
    double totalTime = this.totalTime(this.baseTimeUnit());
    double count = this.count();
    
    JfrFunctionTimerEvent event = this.newEmptyEvent();
    event.setTotalTimeAndCount(totalTime, count);
  }

  @Override
  public double count() {
    T obj = this.reference.get();
    if (obj != null) {
      return this.countFunction.applyAsLong(obj);
    } else {
      return Double.NaN;
    }
  }

  @Override
  public double totalTime(TimeUnit destinationUnit) {
    T obj = this.reference.get();
    if (obj != null) {
      double totalTime = this.totalTimeFunction.applyAsDouble(obj);
      return TimeUtils.convert(totalTime, this.totalTimeFunctionUnit, destinationUnit);
    } else {
      return Double.NaN;
    }
  }

  @Override
  public TimeUnit baseTimeUnit() {
    return this.baseTimeUnit;
  }

}
