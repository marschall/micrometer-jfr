package com.github.marschall.micrometer.jfr;

import java.util.Optional;

import io.micrometer.core.instrument.binder.BaseUnits;
import jdk.jfr.AnnotationElement;
import jdk.jfr.DataAmount;
import jdk.jfr.Timespan;
import jdk.jfr.Percentage;

final class BaseUnitsUtil {

  private BaseUnitsUtil() {
    throw new AssertionError("not instantiable");
  }

  static Optional<AnnotationElement> mapToAnnotationElement(String baseUnit) {
    if (baseUnit == null) {
      return Optional.empty();
    }
    switch (baseUnit) {
      case BaseUnits.BYTES:
        return Optional.of(new AnnotationElement(DataAmount.class, DataAmount.BYTES));
      case BaseUnits.MILLISECONDS:
        return Optional.of(new AnnotationElement(Timespan.class, Timespan.MILLISECONDS));
      case BaseUnits.PERCENT:
        return Optional.of(new AnnotationElement(Percentage.class));
      default:
        return Optional.empty();
    }
  }

}
