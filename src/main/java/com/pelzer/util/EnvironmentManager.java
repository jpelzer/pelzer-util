package com.pelzer.util;


/**
 * Very simple wrapper class that pulls a few of the PropertyManager methods out
 * into a more logical location, for instance {@link #getBuildNumber()} or
 * {@link #getHostname()}, but ultimately delegates the work back to the
 * PropertyManager. The {@link #setDefaultEnvironment(String)} method is
 * slightly smarter, as it sets a system property to override the default
 * environment, and then calls
 * {@link PropertyManager#setDefaultEnvironment(String)}. This will result in a
 * very clean initialization of a static main method class into a particular
 * environment, <b>if</b> the following conditions are met:
 * <ol>
 * <li>The class does not initialize any static variables that reference the
 * pelzer-util system (ie a static logger instance)
 * <li>The class calls {@link #setDefaultEnvironment(String)} first, before any
 * pelzer-util method have been called (ie before any pelzer-util static
 * initialization has happened)
 * </ol>
 */
public class EnvironmentManager {
  private static boolean initialized = false;

  /**
   * Called by the {@link PropertyManager} after it has completed singleton
   * initialization.
   */
  static void markInitialized() {
    initialized = true;
  }

  /**
   * @return true if the PropertyManager and Logging systems are fully
   *         initialized (ie, they've completed loading all property files and
   *         system properties.)
   */
  public static boolean isInitialized() {
    return initialized;
  }

  /** Overrides and resets the PropertyManager to a new environment. */
  public static void setDefaultEnvironment(String environment) {
    System.setProperty("pelzer.environment", environment);
    Logging.getLogger(EnvironmentManager.class);
    PropertyManager.setDefaultEnvironment(environment);
  }

  /**
   * @return The current default environment that the {@link PropertyManager} is
   *         running against.
   */
  public static String getEnvironment() {
    return PropertyManager.getEnvironment();
  }

  /** Pulls 'build.number' from PropertyManager.version.properties. */
  public static String getBuildNumber() {
    return PropertyManager.getBuildNumber();
  }

  /**
   * The first part of the hostname of this box, so for 'foo.bar.com', would
   * return 'foo'.
   */
  public static String getHostname() {
    return PropertyManager.getHostname();
  }
}
