package com.pelzer.util;

import junit.framework.TestCase;

public class EnvironmentManagerTest extends TestCase {

  public void testGetSet() {
    // Make sure PropertyManager has started at least once:
    PropertyManager.getBuildNumber();
    String original = EnvironmentManager.getEnvironment();
    EnvironmentManager.setDefaultEnvironment("ABC123");
    assertEquals("ABC123", EnvironmentManager.getEnvironment());
    // Return to normal
    EnvironmentManager.setDefaultEnvironment(original);
    assertEquals(original, EnvironmentManager.getEnvironment());
  }

  public static void main(String[] args) {
    EnvironmentManager.setDefaultEnvironment("FOO");
    Logging.Logger log = Logging.getLogger(EnvironmentManagerTest.class);
    log.debug("Environment={}", EnvironmentManager.getEnvironment());
    EnvironmentManager.setDefaultEnvironment("FOO2");
    log.debug("Environment={}", EnvironmentManager.getEnvironment());
  }
}
