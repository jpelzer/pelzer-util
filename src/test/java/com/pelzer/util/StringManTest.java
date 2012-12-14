/**
 * Copyright 2007 Jason Pelzer.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); 
 * you may not use this file except in compliance with the License. 
 * You may obtain a copy of the License at 
 * 
 * http://www.apache.org/licenses/LICENSE-2.0 
 * 
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an "AS IS" BASIS, 
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
 * See the License for the specific language governing permissions and 
 * limitations under the License.
 */
package com.pelzer.util;

import junit.framework.TestCase;

public class StringManTest extends TestCase {
  private Logging.Logger logger = Logging.getLogger(this);

  public StringManTest(String name) {
    super(name);
  }

  public void testASCII7() {
    assertEquals("Beyonce", StringMan.getUS7ASCIIEquiv("Beyonc" + ((char) 233)));
  }

  public void testStripNonAlphaChars() {
    assertEquals("I dont like pizza", StringMan.stripNonAlphaNumericCharacters("I don't like pizza."));
  }

  public void testStripWhitespace() {
    assertEquals("DeBock", StringMan.stripWhitespace("De\t\r\n   Bock"));
  }

  public void stripNonEmailCharacters() {
    assertEquals("I like beer.", StringMan.stripNonAlphaNumericCharacters("I like beer."));
    assertEquals("paul@example.com smoove@example.com", StringMan.stripNonAlphaNumericCharacters("paul@exa#$%$%^/mple.com,  smoove@example.com;"));
  }

  public void testReplace() {
    assertEquals("testtesttest", StringMan.replace("testtesttest", "test", "test"));
    assertEquals("testTESTtest", StringMan.replace("testtesttest", "ttestt", "tTESTt"));
    assertEquals("%20%20%20-%20%20%20", StringMan.replace("%%%-%%%", "%", "%20"));
    assertEquals("testTESTtest", StringMan.replace("test(JWP)test", "(JWP)", "TEST"));
    // Make sure <> replacements work
    assertEquals("testTESTtest", StringMan.replace("test<programmer name=\"jason\">test", "<programmer name=\"jason\">", "TEST"));
    // Make sure . isn't a wildcard
    assertEquals("testTESTtest", StringMan.replace("testTESTtest", ".ES.", "XXXX"));
    // Make sure & isn't a strange char.
    assertEquals("testTESTtest", StringMan.replace("test&test", "&", "TEST"));
    // Make sure \ isn't a strange char.
    assertEquals("testTESTtest", StringMan.replace("test\\test", "\\", "TEST"));
    // Test some strange strings
    assertEquals("testTESTtest", StringMan.replace("test&\\test", "&\\", "TEST"));
  }

  public void testReplaceWithRotatingReplacements() {
    assertEquals("123", StringMan.replace("xxx", "x", "1", "2", "3"));
    assertEquals("123x", StringMan.replace("xxxx", "x", "1", "2", "3"));
    assertEquals("123xxx", StringMan.replace("xxxxxx", "x", "1", "2", "3"));
    assertEquals("12", StringMan.replace("xx", "x", "1", "2", "3"));
    assertEquals("This is a test.", StringMan.replace("{} {} {} {}.", "{}", "This", "is", "a", "test"));
    assertEquals("This is a test", StringMan.replace("{} {} {} {}", "{}", "This", "is", "a", "test"));
  }

  public void testReplaceFirst() {
    assertEquals("abc!!?def??gef", StringMan.replaceFirst("abc??def??gef", "?", "!!"));
    assertEquals("12345", StringMan.replaceFirst("12345__", "__", ""));
    assertEquals("12345", StringMan.replaceFirst("__12345", "__", ""));
    assertEquals("12345__", StringMan.replaceFirst("__12345__", "__", ""));
    assertEquals("12345", StringMan.replaceFirst("12345", "?", "!"));
  }

  public void testEncodeBytesToString() {
    assertEquals("00017F80FF", StringMan.encodeBytesToString(new byte[]{0, 1, 127, -128, -1 }));
  }

  public void testPadLeftJustified() {
    assertEquals("1234####", StringMan.padLeftJustified("1234", 8, "#"));
  }

  public void testPadRightJustified() {
    assertEquals("####1234", StringMan.padRightJustified("1234", 8, "#"));
  }

  public void testEscapeForXML() {
    logger.debug(StringMan.escapeForXML("Beyonc\u00e9 AT&T isn't \"better\" than <html> "));
    assertEquals("Beyonc&#233; AT&amp;T isn&apos;t &quot;better&quot; than &lt;html&gt; ", StringMan.escapeForXML("Beyonc\u00e9 AT&T isn't \"better\" than <html> "));
    assertEquals("&#13;&#10;&#9;&#92;", StringMan.escapeForXML("\r\n\t\\"));
  }

  public void testAreStringsEqual() {
    assertTrue(StringMan.areStringsEqual(null, null));
    assertTrue(StringMan.areStringsEqual("abc", "abc"));
    assertFalse(StringMan.areStringsEqual("abc", null));
    assertFalse(StringMan.areStringsEqual(null, "abc"));
  }
}