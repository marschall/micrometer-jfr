
/**
 * A Micrometer meter registry that generates JFR events.
 */
module micrometer.jfr {

  exports com.github.marschall.micrometer.jfr;

  requires static micrometer.commons;
  requires micrometer.core;
  requires jdk.jfr;

}