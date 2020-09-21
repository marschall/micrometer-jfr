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
- Generates a JFR event for every metered value.

