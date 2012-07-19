package com.pelzer.util;

import java.util.Map;

import junit.framework.TestCase;

public class PropertyManagerTest extends TestCase {
  private final Logging.Logger log = Logging.getLogger(this);

  public PropertyManagerTest(String name) {
    super(name);
  }

  public void testMakeSureEnvironmentIsNotNullOrError() {
    assertNotNull(PropertyManager.getEnvironment());
    assertNotSame("null", PropertyManager.getEnvironment());
    assertNotSame("", PropertyManager.getEnvironment());
    assertNotSame("ERROR", PropertyManager.getEnvironment());
    Logging.getLogger(this).debug("ENV=" + PropertyManager.getEnvironment());
  }

  public void testOverrides() {
    PropertyManager.override("testOverrides", "foo");
    assertEquals("foo", PropertyManager.getProperty("testOverrides"));
  }

  public void testIsDEV() {
    String oldEnvironment = PropertyManager.getEnvironment();
    try {
      PropertyManager.setDefaultEnvironment("IS_DEV_CASCADE");
      assertTrue(PropertyManager.isDEV());
      assertFalse(PropertyManager.isTEST());
    } finally {
      PropertyManager.setDefaultEnvironment(oldEnvironment);
    }

  }

  public void testGetAllProperties() {
    // Search the DEV env
    boolean foundDefault = false;
    for (Map.Entry<String, String> entry : PropertyManager.getAllProperties("DEV")) {
      log.debug("Key=[{}] value=[{}]", entry.getKey(), entry.getValue());
      if (entry.getKey().equals("com.pelzer.util.PropertyManagerTest.unit1.value") && entry.getValue().equals("default!"))
        foundDefault = true;
    }
    assertTrue(foundDefault);

    // Search the FOO env
    boolean foundFoo = false;
    for (Map.Entry<String, String> entry : PropertyManager.getAllProperties("FOO")) {
      log.debug("Key=[{}] value=[{}]", entry.getKey(), entry.getValue());
      if (entry.getKey().equals("com.pelzer.util.PropertyManagerTest.unit1.value") && entry.getValue().equals("foo!"))
        foundFoo = true;
    }
    assertTrue(foundFoo);
  }

  public void testIsEnvKeyInteresting() {
    PropertyManager pm = PropertyManager.getSingletonInstance();
    assertTrue(pm.isEnvKeyInteresting("foo"));
    assertTrue(pm.isEnvKeyInteresting("fOO"));
    assertTrue(pm.isEnvKeyInteresting("Foo"));
    assertFalse(pm.isEnvKeyInteresting("FOO"));
    assertFalse(pm.isEnvKeyInteresting("F1"));
  }

  public void testNullValue() {
    assertNull(PropertyManager.getProperty("testDefaultValue", "foo"));
    assertNotNull(PropertyManager.getProperty("testDefaultValue", "foo", "Not null"));
  }

  public void testCascadingEnvironments() {
    final String baseProperty = "com.pelzer.util.PropertyManagerTest.unit1.value";
    String originalDefaultEnvironment = PropertyManager.getEnvironment();
    try {
      PropertyManager.setDefaultEnvironment("XXX");
      assertEquals("default!", PropertyManager.getProperty(baseProperty));
      PropertyManager.setDefaultEnvironment("FOO");
      assertEquals("foo!", PropertyManager.getProperty(baseProperty));
      PropertyManager.setDefaultEnvironment("BAR");
      assertEquals("bar!", PropertyManager.getProperty(baseProperty));

      PropertyManager.setDefaultEnvironment("CASCADE_UNIT_TEST");
      assertEquals("foo!", PropertyManager.getProperty(baseProperty));
    } finally {
      PropertyManager.setDefaultEnvironment(originalDefaultEnvironment);
    }
  }

  public void testGetBuildNumber() {
    assertEquals(PropertyManager.getProperty(PropertyManager.KEY_BUILD_NUMBER), PropertyManager.getBuildNumber());
  }

  public void testEnvironmentVariableOverrides() {
    // Right now we just make sure that the default ones are filtered out.
    assertNull(PropertyManager.getProperty("java.version"));
  }

  public void testAcceptableEnvironments() {
    PropertyManager pm = PropertyManager.getSingletonInstance();
    assertFalse(pm.isAcceptableEnvironment(null));
    assertFalse(pm.isAcceptableEnvironment(""));
    assertFalse(pm.isAcceptableEnvironment(" "));
    assertFalse(pm.isAcceptableEnvironment("$"));
    assertFalse(pm.isAcceptableEnvironment("A-B"));
    assertFalse(pm.isAcceptableEnvironment("TEST "));

    assertTrue(pm.isAcceptableEnvironment("TEST"));
    assertTrue(pm.isAcceptableEnvironment("test"));
  }
}
