Micrometer JFR [![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.github.marschall/micrometer-jfr/badge.svg)](https://maven-badges.herokuapp.com/maven-central/com.github.marschall/micrometer-jfr) [![Javadocs](https://www.javadoc.io/badge/com.github.marschall/micrometer-jfr.svg)](https://www.javadoc.io/doc/com.github.marschall/micrometer-jfr) [![Build Status](https://travis-ci.org/marschall/micrometer-jfr.svg?branch=master)](https://travis-ci.org/marschall/micrometer-jfr)
==============

A [Micrometer](https://micrometer.io/) meter registry that generates [JFR](https://openjdk.java.net/jeps/328) events

Usage
-----

```java
Metrics.addRegistry(new JfrMeterRegistry());
```

```xml
<dependency>
  <groupId>com.github.marschall</groupId>
  <artifactId>micrometer-jfr</artifactId>
  <version>0.3.0</version>
</dependency>
```

![Image of Spring Batch Micrometer JFR events](https://github.com/marschall/micrometer-jfr/raw/master/src/main/javadoc/screenshot.png)

Features
--------

- Converts tags (key value pairs) into JFR event attributes.
- Basic mapping from some `io.micrometer.core.instrument.binder.BaseUnits` to `jdk.jfr.ContentType`.
- Maps `Id#getName()` and `Id#getDescription()` to `@Name` and `@Description`.
- Stacktraces are disabled for all events for reduced overhead.
- Generates a JFR event for every metered value.
- A custom naming convention us used to translate tag names to capitalized words.

Limitations
-----------

- The built in duration of most events is 0 instead a new attribute "Metered Duration" is added.


Configuration
-------------

The frequency of polled meters like `FunctionCounter` have to be configured in a `.jfc` file.

```xml
<event name="my.fcounter">
  <setting name="enabled">true</setting>
  <setting name="period">5 s</setting>
</event>
```
