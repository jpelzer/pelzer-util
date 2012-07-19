package com.pelzer.util;

import java.util.concurrent.TimeUnit;

import junit.framework.TestCase;

public class AbsorbTest extends TestCase {
  public void testSleep() {
    long time = System.currentTimeMillis();
    Absorb.sleep(TimeUnit.SECONDS, 1);
    assertTrue("We didn't sleep at least 1 second", time + 1000 < System.currentTimeMillis());
  }

  public void testIgnore() {
    Absorb.ignore(new RuntimeException("Ignore me!"));
  }

  public void testRethrow() {
    try {
      Absorb.rethrow(new Throwable("I'm an exception!"));
      fail("Exception should have been thrown.");
    } catch (Throwable expected) {
      assertTrue("Thrown exception wasn't a runtime exception!", expected instanceof RuntimeException);
      assertEquals("I'm an exception!", expected.getCause().getMessage());
    }
  }

}
