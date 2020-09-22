Micrometer JFR
==============

A [Micrometer](https://micrometer.io/) meter registry that generates [JFR](https://openjdk.java.net/jeps/328) events

Usage
-----

```java
Metrics.addRegistry(new JfrMeterRegistry());
```

Features
--------

- Converts tags (key value pairs) into JFR event attributes.
- Basic mapping from some `io.micrometer.core.instrument.binder.BaseUnits` to `jdk.jfr.ContentType`.
- Maps `Id#getName()` and `Id#getDescription()` to `@Name` and `@Description`.
- Stacktraces are disabled for all events for reduced overhead.
- Generates a JFR event for every metered value.


Issues
------

- Meters that should be polled currently aren't, eg. the count of `FunctionCounter` is currently not reported.
