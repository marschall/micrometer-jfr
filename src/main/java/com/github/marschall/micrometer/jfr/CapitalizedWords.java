package com.github.marschall.micrometer.jfr;

import io.micrometer.core.instrument.Meter.Type;
import io.micrometer.core.instrument.config.NamingConvention;
import io.micrometer.core.lang.Nullable;

final class CapitalizedWords implements NamingConvention {

  static final NamingConvention IINSTANCE = new CapitalizedWords();

  @Override
  public String name(String name, Type type, @Nullable String baseUnit) {
    if (name.isEmpty()) {
      return name;
    }

    String[] parts = name.split("\\.");
    StringBuilder conventionName = new StringBuilder(name.length());
    for (int i = 0; i < parts.length; i++) {
      if (i > 0) {
        conventionName.append(' ');
      }
      String part = parts[i];
      char firstChar = part.charAt(0);
      if (Character.isLowerCase(firstChar)) {
        conventionName.append(Character.toUpperCase(firstChar));
        conventionName.append(part, 1, part.length());
      } else {
        conventionName.append(part);
      }
    }

    name.charAt(0);
    return conventionName.toString();
  }

  @Override
  public String tagKey(String key) {
    return this.name(key, null, null);
  }
}