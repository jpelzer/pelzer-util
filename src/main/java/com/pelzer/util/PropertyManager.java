/**
 * Copyright 2007 Jason Pelzer.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package com.pelzer.util;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.Writer;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.StringTokenizer;

/**
 * This class is used to condense and maintain the myriad .property files used
 * by the system. It provides a layer of abstraction which allows us to migrate
 * from *many* files, down to one, or even to pull our properties from a
 * database, with little or no code modifications on dependent classes.
 */
public class PropertyManager {
  private static java.util.logging.Logger logger            = java.util.logging.Logger.getLogger(PropertyManager.class.getName());
  private static String                   hostname          = "UNKNOWN";
  static final String                     KEY_BUILD_NUMBER  = "build.number";

  private static PropertyManager          singletonInstance = null;

  static {
    logger.setLevel(Logging.Priority.ERROR.getLevel());

    if (StringMan.isStringTrue(System.getProperty("pelzer.mute")) || StringMan.isStringTrue(System.getProperty("PELZER_MUTE")))
      logger.setLevel(Logging.Priority.FATAL.getLevel());

    singletonInstance = new PropertyManager("PropertyManager.properties", null);
    // Now try to set up the hostname
    try {
      hostname = java.net.InetAddress.getLocalHost().getHostName().toUpperCase();
      if (hostname.indexOf(".") > -1) {
        // We only want the first part of the hostname...
        // HOSTNAME.THESE.PARTS.IGNORED
        hostname = hostname.substring(0, hostname.indexOf("."));
      }
      logger.info("PropertyManager has determined HOSTNAME='" + hostname + "'");
    }
    catch (final Exception ex) {
      logger.log(Logging.Priority.ERROR.getLevel(), "Exception getting hostname during PropertyManager static init.", ex);
    }
  }

  public static String getBuildNumber() {
    return getProperty(KEY_BUILD_NUMBER);
  }

  public static String getHostname() {
    return hostname;
  }

  public synchronized static ManagedProperties getProperties(final String namespace) {
    return new PropertyManager.ManagedProperties(namespace);
  }

  public synchronized static String getProperty(final String key) {
    return getProperty("", key, null);
  }

  public synchronized static String getProperty(final String namespace, final String key) {
    return getProperty(namespace, key, null);
  }

  public synchronized static String getProperty(final String namespace, final String key, final String defaultValue) {
    return singletonInstance._getProperty(namespace, key, defaultValue);
  }

  public synchronized static String getLocalizedProperty(final String key) {
    return getLocalizedProperty("", key, null);
  }

  public synchronized static String getLocalizedProperty(final String namespace, final String key) {
    return getLocalizedProperty(namespace, key, null);
  }

  /**
   * @return the underlying singleton the PM is relying on. Only used for unit
   *         testing.
   */
  static PropertyManager getSingletonInstance() {
    return singletonInstance;
  }

  /**
   * Allows retrieval of localized properties set up for SPECIFIC hosts. Calling
   * getLocalizedProperty("foo.bar.BLAH") on the machine 'wintermute' is
   * equivilant to doing getProperty("WINTERMUTE.foo.bar.BLAH"). Machine names
   * are always uppercase, and never have sub-domains (ie WINTERMUTE and not
   * WINTERMUTE.PELZER.COM)
   */
  public synchronized static String getLocalizedProperty(final String namespace, final String key, final String defaultValue) {
    if (namespace == null || namespace.equals(""))
      return getProperty("", hostname + "." + key, defaultValue);
    return getProperty(hostname + "." + namespace, key, defaultValue);
  }

  synchronized static void setDefaultEnvironment(final String defaultEnvironment) {
    if (singletonInstance.defaultEnvironment != null && !singletonInstance.defaultEnvironment.equals(defaultEnvironment)) {
      logger.warning("**********************************************************************");
      logger.warning("The default environment has been overridden via setDefaultEnvironment.");
      logger.warning("It is generally preferable to set the environment via the following:");
      logger.warning("java -Dpelzer.environment=FOO");
      logger.warning("");
      logger.warning("Old environment=" + singletonInstance.defaultEnvironment);
      logger.warning("New environment=" + defaultEnvironment);
      logger.warning("**********************************************************************");
    }
    singletonInstance.defaultEnvironment = defaultEnvironment;
    singletonInstance.defaultSearchEnvironments = null;
  }

  public synchronized static String getEnvironment() {
    return singletonInstance.getDefaultEnvironment();
  }

  private static Boolean IS_DEV  = null;
  private static Boolean IS_TEST = null;
  private static Boolean IS_PROD = null;

  /**
   * Called the first time {@link #isDEV()}, {@link #isTEST()} or
   * {@link #isPROD()} is called, to cache the values. NOT called during static
   * init because we want to support early-stage code reseting the environment
   * if needed.
   */
  private static synchronized void initDEVTESTPROD() {
    if (IS_DEV != null)
      return;
    IS_DEV = false;
    IS_TEST = false;
    IS_PROD = false;
    final List<String> envs = singletonInstance.getSearchEnvironments();
    for (final String env : envs) {
      if (env.equalsIgnoreCase("DEV")) {
        IS_DEV = true;
      }
    }
    for (final String env : envs) {
      if (env.equalsIgnoreCase("TEST")) {
        IS_DEV = false;
        IS_TEST = true;
      }
    }
    for (final String env : envs) {
      if (env.equalsIgnoreCase("PROD")) {
        IS_DEV = false;
        IS_TEST = false;
        IS_PROD = true;
      }
    }

  }

  /** @return true if the environment is DEV */
  public static boolean isDEV() {
    if (IS_DEV == null) {
      initDEVTESTPROD();
    }
    return IS_DEV;
  }

  /** @return true if the environment is TEST */
  public static boolean isTEST() {
    if (IS_TEST == null) {
      initDEVTESTPROD();
    }
    return IS_TEST;
  }

  /** @return true if the environment is PROD */
  public static boolean isPROD() {
    if (IS_PROD == null) {
      initDEVTESTPROD();
    }
    return IS_PROD;
  }

  public static List<Map.Entry<String, String>> getAllProperties(final String environment) {
    final String oldEnvironment = PropertyManager.getEnvironment();
    try {
      PropertyManager.setDefaultEnvironment(environment);
      final List<Map.Entry<String, String>> out = new ArrayList<Map.Entry<String, String>>();
      for (final Map.Entry<Object, Object> entry : singletonInstance.allProperties.entrySet()) {
        final String key = stripEnvironmentPrefix(entry.getKey().toString());
        final String value = PropertyManager.getProperty(key);
        out.add(new PropertyEntry(key, value));
      }
      return out;
    }
    finally {
      PropertyManager.setDefaultEnvironment(oldEnvironment);
    }
  }

  /**
   * Looks at the prefix of the key up to the first period. If it is UPPERCASE,
   * strips it off then checks to see the PM has a value for that key in the
   * current environment. If null, returns the original key, if there's a value,
   * returns the key with the prefix removed. If there is not an uppercase
   * prefix, returns the original key.
   */
  private static String stripEnvironmentPrefix(final String key) {

    if (key.indexOf('.') < 0)
      return key;
    final String prefix = key.substring(0, key.indexOf('.'));
    if (!prefix.toUpperCase().equals(prefix))
      return key;
    final String newKey = key.substring(prefix.length() + 1);
    final String value = PropertyManager.getProperty(newKey);
    if (value == null)
      return key;
    return newKey;
  }

  private static class PropertyEntry implements Map.Entry<String, String> {
    private final String key;
    private final String value;

    public PropertyEntry(final String key, final String value) {
      this.key = key;
      this.value = value;
    }

    public String getKey() {
      return key;
    }

    public String getValue() {
      return value;
    }

    public String setValue(final String value) {
      throw new RuntimeException("setValue is not implemented.");
    }

  }

  // --------------------------------------------------------------
  private String           defaultEnvironment  = null;
  private final String     environmentFilename = "PropertyManager.environment.properties";
  private final Properties allProperties       = new Properties();

  private PropertyManager(final String basePropertyFile, final String defaultEnvironment) {
    logger.warning("PropertyManager beginning construction. basePropertyFile='" + basePropertyFile + "'");
    this.defaultEnvironment = defaultEnvironment;
    this.defaultEnvironment = getDefaultEnvironment();

    // First, init the properties
    loadProperties(basePropertyFile, 0);

    // Now we need to load any overrides
    try {
      loadOverrides();
    }
    catch (final Exception ex) {
      logger.log(Logging.Priority.ERROR.getLevel(), "Exception during loadOverrides. Ignoring.", ex);
    }
    logger.warning("PropertyManager finished construction. basePropertyFile='" + basePropertyFile + "'");
    EnvironmentManager.markInitialized();
  }

  /**
   * Opens an inputstream (wrapped in a buffer) for the given filename using
   * several mechanisms, the first being the classpath, then looking in the same
   * directory as the PropertyManager (this mode will only load fall-through
   * files that are included in the Pelzer.util jar)
   */
  private List<InputStream> getInputStreamsForFile(final String filename) throws IOException {
    // First try using the PropertyManager's class loader
    Enumeration<URL> urls = PropertyManager.class.getClassLoader().getResources(filename);
    final List<InputStream> ioStreams = new ArrayList<InputStream>();

    while (urls.hasMoreElements()) {
      ioStreams.add(urls.nextElement().openStream());
    }
    if (ioStreams.size() > 0)
      return ioStreams;

    // Second try, use the thread context classloader
    urls = Thread.currentThread().getContextClassLoader().getResources(filename);
    while (urls.hasMoreElements()) {
      ioStreams.add(urls.nextElement().openConnection().getInputStream());
    }
    if (ioStreams.size() > 0)
      return ioStreams;

    // That didn't work, try again... (This way only works for single items)
    final URL filePathURL = PropertyManager.class.getResource(filename);
    if (filePathURL != null) {
      ioStreams.add(filePathURL.openConnection().getInputStream());
      return ioStreams;
    }

    ioStreams.add(new ClassPathResource(filename).getInputStream());
    return ioStreams;
  }

  /**
   * Loads the given property file, plus any files that are referenced by
   * #include statements
   */
  private void loadProperties(final String filename, final int depth) {
    logger.warning("Loading file '" + filename + "'");
    final Properties properties = new Properties();
    try {
      // Load our properties file.
      for (final InputStream io : getInputStreamsForFile(filename)) {
        properties.load(io);
        logger.info("Property file loaded successfully (" + filename + ")");
        io.close();
      }
    }
    catch (final java.io.FileNotFoundException ex) {
      logger.warning("Property file not found (" + filename + ")");
    }
    catch (final java.io.IOException ex) {
      logger.warning("IOException loading file: " + ex.getMessage());
      logger.log(Logging.Priority.FATAL.getLevel(), "Error occured while loading properties file (" + filename + ")", ex);
    }
    allProperties.putAll(properties);
    final String includeFiles[] = readIncludeFiles(filename, depth);
    for (final String includeFile : includeFiles) {
      loadProperties(includeFile, depth + 1);
    }
  }

  /**
   * Reads the property file looking for '#include' statements
   */
  private String[] readIncludeFiles(final String filename, final int depth) {
    logger.info("In readIncludeFiles() for filename='" + filename + "' depth=" + depth);
    if (depth > 10) { // 10 seems like a nice limit... If we're this deep, we're
      // probably in an infinite loop anyway.
      logger.log(Logging.Priority.ERROR.getLevel(), "readIncludeFiles(): Too deep to continue including (depth=" + depth + "). Ending this branch at: " + filename);
      return new String[0];
    }
    final List<String> filenames = new ArrayList<String>();
    java.io.InputStream in = null;
    try {
      final List<InputStream> ioStreams = getInputStreamsForFile(filename);

      for (int i = 0; i < ioStreams.size(); i++) {
        try {
          in = ioStreams.get(i);

          int currentChar = in.read();
          String currentLine = "";
          while (currentChar != -1) {
            if (currentChar == '\r' || currentChar == '\n') {
              if (currentLine.indexOf("#include ") == 0) {
                final String includeFile = replaceVariables(currentLine.substring(9));
                if (includeFile.equals("false")) {
                  logger.info("Found '#include false': Cancelling include processing for this file.");
                  return new String[0];
                }
                logger.info("Found: " + currentLine + ", adding include of '" + includeFile + "' from file='" + filename + "'");
                filenames.add(includeFile);
              }
              currentLine = "";
            } else {
              final byte temp[] = new byte[1];
              temp[0] = (byte) currentChar;
              currentLine += new String(temp, 0, 1);
            }
            currentChar = in.read();
          }
        }
        catch (final java.io.IOException ex) {
          logger.log(Logging.Priority.ERROR.getLevel(), "Unable to read property file to parse include files (" + filename + "): ", ex);
        }
        finally {
          try {
            if (in != null) {
              in.close();
            }
          }
          catch (final java.io.IOException ignored) {
          }
        }
      }
    }
    catch (final java.io.IOException ex) {
      logger.log(Logging.Priority.ERROR.getLevel(), "Unable to read property file to parse include files (" + filename + "): ", ex);
    }
    return filenames.toArray(new String[filenames.size()]);
  }

  /**
   * Replaces variables like {ENVIRONMENT} with their correct values, also
   * handles clarifying obfuscated strings in the form
   * KEY=[[[xxxxxxxxxxxxxxxx]]]
   */
  private String replaceVariables(String inString) {
    if (inString == null || inString == "")
      return inString;
    String outString = inString;

    // Replace $ENVIRONMENT with the default environment
    outString = stringReplace(outString, "{ENVIRONMENT}", defaultEnvironment);

    // Go through the string and try to replace {...} escaped keys
    String currentToken = "";
    String replacedString = "";
    boolean inToken = false;
    for (int i = 0; i < outString.length(); i++) {
      final char currentChar = outString.charAt(i);
      if (!inToken && currentChar != '{') {
        replacedString = replacedString + currentChar;
      } else if (inToken && currentChar == '{') {
        replacedString += "{" + currentToken;
        currentToken = "";
      } else if (inToken && currentChar != '}') {
        currentToken = currentToken + currentChar;
      } else if (inToken && currentChar == '}') {
        // Try to find a replacement
        inToken = false;
        String replacement = null;
        if (singletonInstance != null) {
          replacement = PropertyManager.getProperty("", currentToken, null);
        }
        if (replacement != null) {
          replacedString += replacement;
        } else {
          replacedString += "{" + currentToken + "}";
        }
        currentToken = "";
      } else if (!inToken && currentChar == '{') {
        inToken = true;
        currentToken = "";
      }
    }
    if (!currentToken.equals("")) {
      replacedString += "{" + currentToken;
    }
    outString = replacedString;

    // If in == out, then we've done as many replacements as we can, we're done
    if (outString.equals(inString)) {
      // Finally, check to see if the string should be clarified
      if (inString.startsWith("[[[") && inString.endsWith("]]]")) {
        // Clarify...
        final String obfuscatedText = inString.substring(3, inString.length() - 3);
        inString = ObfuscationManager.clarify(obfuscatedText);
      }
      return inString;
    }
    return replaceVariables(outString);
  }

  /** Replaces a first occurence of a String with another string */
  private String stringReplace(final String source, final String find, final String replace) {
    if (source == null || find == null || replace == null)
      return source;
    final int index = source.indexOf(find);
    if (index == -1)
      return source; // no occurence, don't do anything;
    if (index == 0)
      return replace + source.substring(find.length());
    return source.substring(0, index) + replace + source.substring(index + find.length());
  }

  /**
   * Called by the PropertyManager on init, overridden keys are loaded from the
   * database or elsewhere and placed into our allProperties object, as though
   * they have been loaded already from our files. trings should *not* have ENV
   * (DEV, TEST, etc) prepended to them already). Currently only pulling from
   * the environment, first from System.getenv(), then from
   * System.getProperties()
   */
  private void loadOverrides() {
    loadOverridesFromEnv();
    loadOverridesFromCommandLine();
  }

  private void loadOverridesFromEnv() {
    final Map<String, String> env = System.getenv();
    for (final String key : env.keySet()) {
      if (!isEnvKeyInteresting(key)) {
        continue;
      }
      final String value = env.get(key);
      final boolean doSecurely = key.startsWith("_");
      if (doSecurely) {
        logger.warning("Pulling override from System.getenv(): '" + key + "'=***PROTECTED***");
      } else {
        logger.warning("Pulling override from System.getenv(): '" + key + "'='" + value + "'");
      }
      setOverride(key, value);
    }
  }

  /**
   * Decide if a key from System.getenv() is interesting for overrides, so it is
   * not all uppercase, not in the disallowed list, and has the right prefix.
   */
  boolean isEnvKeyInteresting(final String key) {
    final List<String> disallowedKeys = Arrays.asList("windir", "SystemDrive", "CommonProgramFiles", "ComSpec", "SystemRoot", "Path", "ProgramFiles");

    if (key == null || key.length() == 0)
      return false;
    // No all-uppercase keys
    if (key.toUpperCase().equals(key))
      return false;
    // No keys in the list
    if (disallowedKeys.contains(key))
      return false;

    return true;
  }

  /**
   * Loads overrides from System.getProperties(), mostly for things passed in on
   * the command line using the -Dkey=value scheme.
   */
  private void loadOverridesFromCommandLine() {
    final List<String> disallowedKeys = Arrays.asList("java.version", "java.vendorjava.vendor.url", "java.home", "java.vm.specification.version", "java.vm.specification.vendor", "java.vm.specification.name", "java.vm.version",
        "java.vm.vendor", "java.vm.name", "java.specification.version", "java.specification.vendor", "java.specification.name", "java.class.version", "java.class.path", "java.library.path", "java.io.tmpdir", "java.compiler",
        "java.ext.dirs", "os.name", "os.arch", "os.version", "file.separator", "path.separator", "line.separator", "user.name", "user.home", "user.dir", "java.runtime.name", "sun.boot.library.path", "java.vendor.url", "file.encoding.pkg",
        "sun.java.launcher", "user.country", "sun.os.patch.level", "java.runtime.version", "java.awt.graphicsenv", "java.endorsed.dirs", "user.variant", "sun.jnu.encoding", "sun.management.compiler", "user.timezone", "java.awt.printerjob",
        "file.encoding", "sun.arch.data.model", "user.language", "awt.toolkit", "java.vm.info", "sun.boot.class.path", "java.vendor", "java.vendor.url.bug", "sun.io.unicode.encoding", "sun.cpu.endian", "sun.desktop", "sun.cpu.isalist");

    final Properties sysProperties = System.getProperties();
    for (final Object obj : sysProperties.keySet()) {
      final String key = (String) obj;
      // We don't override for the default defined system keys.
      if (disallowedKeys.contains(key)) {
        continue;
      }
      final String value = sysProperties.getProperty(key);
      final boolean doSecurely = key.startsWith("_");
      if (doSecurely) {
        logger.warning("Pulling override from System.getProperty: '" + key + "'=***PROTECTED***");
      } else {
        logger.warning("Pulling override from System.getProperty: '" + key + "'='" + value + "'");
      }
      setOverride(key, value);
    }
  }

  /**
   * Sets a property manually: You'd generally only call this method from a
   * unit-test, to make sure a certain property was set correctly...
   */
  public static void override(final String key, final String value) {
    singletonInstance.setOverride(key, value);
  }

  /** Replaces whatever was in the allProperties object, and clears the cache. */
  private void setOverride(final String key, final String value) {
    final boolean doSecurely = key.startsWith("_");
    final String fullKey = defaultEnvironment + "." + key;
    if (value != null) {
      final String oldValue = _getProperty("", key, null);
      if (value.equals(oldValue)) {
        // Same value, not really an override.
        if (doSecurely) {
          logger.warning("*** IGNORED  *** Ignoring redundant override " + fullKey + "=***PROTECTED***");
        } else {
          logger.warning("*** IGNORED  *** Ignoring redundant override " + fullKey + "='" + value + "'");
        }
      } else {
        logger.warning("*** OVERRIDE *** Setting override " + fullKey);
        if (doSecurely) {
          logger.warning("                 New value=***PROTECTED***");
          logger.warning("                 Old value='" + oldValue + "'");
        } else {
          logger.warning("                 New value='" + value + "'");
          logger.warning("                 Old value='" + oldValue + "'");
        }
        allProperties.setProperty(fullKey, value);
      }
    }
  }

  private String _getProperty(final String namespace, final String key, final String defaultValue) {
    return _getProperty(namespace, key, defaultValue, getSearchEnvironments());
  }

  private String _getProperty(final String namespace, final String key, final String defaultValue, final List<String> searchEnvironments) {
    if (searchEnvironments == null || searchEnvironments.size() == 0 || searchEnvironments.get(0) == null) {
      logger.info("Failed search in namespace='" + namespace + "' for key='" + key + "'");
      return defaultValue;
    }
    // "namespace.key" or just "key" if nameplace is blank
    final String namespaceKey = namespace + (namespace != null && namespace != "" ? "." : "") + key;
    String returnValue = null;

    final String environment = searchEnvironments.get(0);
    final String envNamespaceKey = environment + (!environment.equals("") ? "." : "") + namespaceKey;
    // We don't have it cached. Look for env.nameplace.key in our mainProperties
    // file...
    returnValue = allProperties.getProperty(envNamespaceKey);
    if (returnValue != null) {
      logger.info("Found value '" + returnValue + "' for key '" + envNamespaceKey + "'");
      returnValue = replaceVariables(returnValue);
      return returnValue;
    }
    // Didn't find a value...
    searchEnvironments.remove(0);
    return _getProperty(namespace, key, defaultValue, searchEnvironments);
  }

  private List<String> defaultSearchEnvironments = null;

  /**
   * @return a List of environments that should be searched when finding
   *         properties, for instance {"JPELZER","DEV",""} This method
   *         guarantees that the last element in this list will always be ""
   *         (the default environment.
   */
  private List<String> getSearchEnvironments() {
    if (defaultSearchEnvironments != null)
      return new ArrayList<String>(defaultSearchEnvironments);
    defaultSearchEnvironments = new ArrayList<String>();
    defaultSearchEnvironments.add(getDefaultEnvironment());
    // Now find if the default env has a fallback list
    final String fallbacks = allProperties.getProperty(getDefaultEnvironment() + ".ENVIRONMENTS");
    if (fallbacks != null) {
      final StringTokenizer tokens = new StringTokenizer(fallbacks, " ,\t");
      while (tokens.hasMoreTokens()) {
        final String token = tokens.nextToken();
        if (!defaultSearchEnvironments.contains(token)) {
          defaultSearchEnvironments.add(token);
        }
      }
    }
    defaultSearchEnvironments.add("");
    return getSearchEnvironments();
  }

  private String getDefaultEnvironment() {
    if (defaultEnvironment != null)
      return defaultEnvironment;

    String envOverride = System.getProperty("pelzer.environment");
    if (envOverride == null) {
      envOverride = System.getProperty("PELZER_ENVIRONMENT");
    }
    if (isAcceptableEnvironment(envOverride)) {
      defaultEnvironment = envOverride.toUpperCase();
      logger.warning("Default environment determined to be '" + defaultEnvironment + "' from system property 'pelzer.environment'");
      return defaultEnvironment;
    }
    String returnValue = "ERROR";
    final java.util.Properties properties = new java.util.Properties();
    java.io.InputStream io = null;
    try {
      io = getInputStreamsForFile(environmentFilename).get(0);
      properties.load(io);
      io.close();
      returnValue = properties.getProperty("environment");
    }
    catch (final java.io.IOException ex) {
      logger.log(Logging.Priority.ERROR.getLevel(), "Unable to load environment property file '" + environmentFilename + "'... This is a Bad Thing!!!");
    }

    defaultEnvironment = returnValue;
    logger.warning("Default environment determined to be '" + defaultEnvironment + "' from property file '" + environmentFilename + "'");
    return defaultEnvironment;

  }

  /**
   * @return true if the passed in environment is not null, empty, and contains
   *         no non-alphanumeric chars.
   */
  boolean isAcceptableEnvironment(String testEnvironment) {
    if (testEnvironment == null || "".equals(testEnvironment))
      return false;
    final String originalEnvironment = testEnvironment;
    testEnvironment = StringMan.stripNonAlphaNumericCharacters(testEnvironment);
    testEnvironment = StringMan.stripWhitespace(testEnvironment);
    if (!originalEnvironment.equals(testEnvironment)) {
      logger.log(Logging.Priority.ERROR.getLevel(), "pelzer.environment was set to '" + originalEnvironment + "', but this is not an acceptable environment. IGNORING.");
      return false;
    }
    return true;
  }

  /**
   * This class emulates a java.util.Properties object, but doesn't quite
   * support all its methods. So we don't extend Properties, we just have
   * similar method signatures.
   */
  public static class ManagedProperties {
    private String namespace = "";

    /**
     * Pass in a namespace, such as "com.pelzer.util" and then call
     * getProperty("BLAH") is the same as calling
     * PropertyManager.getProperty("com.pelzer.util","BLAH").
     */
    private ManagedProperties(final String namespace) {
      this.namespace = namespace;
    }

    public String getProperty(final String key) {
      return this.getProperty(key, null);
    }

    public String getProperty(final String key, final String defaultValue) {
      return PropertyManager.getProperty(namespace, key, defaultValue);
    }

  }

  /**
   * Performs preprocessing on the PropertyManager.properties file, performing
   * #include processing, obfuscation, etc.
   */
  public static class PropertyProcessor {
    public static void main(final String[] args) {
      if (args.length < 3) {
        printUsageAndExit();
      }

      Logging.mute();
      try {
        if (args[0].equals("INCLUDES")) {
          try {
            processIncludes(args[1], args[2]);
          }
          catch (final IOException ex) {
            ex.printStackTrace();
            System.exit(-1);
          }
        } else if (args[0].equals("ENVIRONMENTS")) {
          if (args.length < 4) {
            printUsageAndExit();
          }
          try {
            processEnvironments(args[1], args[2], args[3]);
          }
          catch (final IOException ex) {
            ex.printStackTrace();
            System.exit(-1);
          }
        } else {
          printUsageAndExit();
          System.exit(-1);
        }
      }
      catch (final Exception ex) {
        System.out.println("Unexpected exception during processing: ");
        ex.printStackTrace();
        System.exit(-1);
      }
      System.out.println("Done.");
      System.exit(0);
    }

    private static void printUsageAndExit() {
      System.out.println("Usage:");
      System.out.println("  PropertyProcessor {INCLUDES|ENVIRONMENTS} {source property file} {target property file} {environment}");
      System.out.println("     INCLUDES:     Reads the source property file, follows and #include directives, and generates");
      System.out.println("                   a concatenated target property file. If the build target is anything other than");
      System.out.println("                   DEV, all properties are obfuscated as well. 'environment' is ignored");
      System.out.println("");
      System.out.println("     ENVIRONMENTS: Reads the source property file, processing #env #vne directives and generating a");
      System.out.println("                   trimmed-down target property file. Environment corresponds to the comma-separated");
      System.out.println("                   tags after the #env tags.");
      System.exit(-2);
    }

    private static void processEnvironments(final String sourceProps, final String targetProps, final String environment) throws IOException {
      System.out.println("Processing #env statements from '" + sourceProps + "' to '" + targetProps + "'");
      final BufferedReader reader = new BufferedReader(new FileReader(sourceProps));
      final FileWriter writer = new FileWriter(targetProps);
      // Begin processing...

      String line = null;
      // depth is the count of how many #env tags we've seen
      // envDepth incremented every time we see an #env, decremented when we see
      // a #vne
      int envDepth = 0;
      // includeDepth incremented every time we see an #env that matches our
      // environment, decremented when we see a #vne if the depth is > 0
      int includeDepth = 0;
      int lineNumber = 0;
      while ((line = reader.readLine()) != null) {
        lineNumber++;
        if (line.startsWith("#env ")) {
          // Found the beginning of an env...
          envDepth++;
          // Decide if it matches us...
          if (includeDepth > 0) {
            // If we're already inside a matching #env, all contained #env tags
            // match
            includeDepth++;
          } else {
            // Gotta look through the comma-separated list...
            final StringTokenizer envs = new StringTokenizer(line.substring(4), ",");
            while (envs.hasMoreTokens()) {
              final String env = envs.nextToken().trim();
              if (env.equalsIgnoreCase("ALL") || env.equalsIgnoreCase(environment)) {
                // we found a matching environment...
                includeDepth++;
                break;
              }
            }
          }
        } else if (line.startsWith("#vne")) {
          envDepth--;
          if (includeDepth > 0) {
            includeDepth--;
          }
          if (envDepth < 0) {
            System.out.println("Found an unexpected #vne on line " + lineNumber);
            envDepth = 0;
          }
        } else if (includeDepth > 0) {
          writer.write(line + "\n");
        }
      }

      reader.close();
      writer.close();
    }

    /**
     * Reads the sourceProps file, processing the #include directives and
     * creating a new file, targetProps. TargetProps will be overwritten.
     * #include directive files are loaded from the same directory as the source
     * file.
     */
    private static void processIncludes(final String sourceProps, final String targetProps) throws IOException {
      System.out.println("Processing #include statements from '" + sourceProps + "' to '" + targetProps + "'");
      final Writer writer = new BufferedWriter(new FileWriter(targetProps));
      writer.write("#include false\n");
      processIncludes(sourceProps, writer);
      writer.close();
    }

    /**
     * Called by {@link #processIncludes(String, String)} to handle the
     * appending of included files onto the already opened writer object.
     */
    private static void processIncludes(final String sourceProps, final Writer writer) throws IOException {
      BufferedReader reader;
      if (sourceProps == null || sourceProps.endsWith("false"))
        return;
      try {
        reader = new BufferedReader(new FileReader(sourceProps));
      }
      catch (final IOException ex) {
        System.out.println("IOException while opening file for reading... Skipping this file: " + ex.getMessage());
        return;
        // ex.printStackTrace();
      }
      String line = null;
      writer.write("#### Beginning of file '" + sourceProps + "' ####\n");
      while ((line = reader.readLine()) != null) {
        if (line.startsWith("#include")) {
          final String includeFile = line.substring(9);
          final String sourceDir = new File(sourceProps).getParent();
          processIncludes(new File(sourceDir, includeFile).getPath(), writer);
          writer.write("#### Returning to file '" + sourceProps + "' ####\n");
        } else {
          final int equalsIndex = line.indexOf("=");
          if (line.startsWith("#") || equalsIndex < 0) {
            // It's either a comment, directive, or a malformed line with no '='
            // sign
            writer.write(line + "\n");
          } else {
            // We'll obfuscate in all environments other than DEV
            if (!PropertyManager.isDEV()) {
              // We know that this isn't a comment and it does contain an '='
              // sign
              final String key = line.substring(0, equalsIndex);
              final String value = line.substring(equalsIndex + 1);
              writer.write(key + "=" + obfuscatePropertyValue(value) + "\n");
            } else {
              writer.write(line + "\n");
            }
          }
        }
      }
      reader.close();
      writer.write("#### End of file '" + sourceProps + "' ####\n");
    }
  }

  /**
   * Takes the given value and obfuscates it as well as it can. It ignores
   * {reference} directives, returning any value that contains a '{' character
   * unchanged. Other values are passed through the ObfuscationManager to hide
   * their values.
   */
  private static String obfuscatePropertyValue(final String value) {
    if (value == null || value.indexOf('{') > -1)
      return value;
    return "[[[" + ObfuscationManager.obfuscate(value) + "]]]";
  }

}