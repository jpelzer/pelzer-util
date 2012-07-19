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

import junit.framework.TestCase;

public class OverridableFieldsTest extends TestCase {
  private final Logging.Logger logger = Logging.getLogger(this);
  
  static {
    PropertyManager.override("com.pelzer.util.UnitOverridableFields.STRING", "I'm a string");
    PropertyManager.override("com.pelzer.util.UnitOverridableFields.INT", "12345");
    
    PropertyManager.override("com.pelzer.util.UnitOverridableFields.STRING_ARRAY.0", "blah1");
    PropertyManager.override("com.pelzer.util.UnitOverridableFields.STRING_ARRAY.1", "blah2");
    PropertyManager.override("com.pelzer.util.UnitOverridableFields.STRING_ARRAY.2", "blah3");
    PropertyManager.override("com.pelzer.util.UnitOverridableFields.STRING_ARRAY.3", "blah4");
    PropertyManager.override("com.pelzer.util.UnitOverridableFields.STRING_ARRAY.4", "blah5");
    
    PropertyManager.override("com.pelzer.util.UnitOverridableFields.INT_ARRAY.0", "42");
    PropertyManager.override("com.pelzer.util.UnitOverridableFields.INT_ARRAY.1", "12");
    PropertyManager.override("com.pelzer.util.UnitOverridableFields.INT_ARRAY.3", "I should never be parsed since there is no INT_ARRAY.2");
    
    PropertyManager.override("com.pelzer.util.UnitOverridableFields.OBFUSCATED_STRING", ObfuscationManager.obfuscate(UnitOverridableFields.PLAINTEXT_STRING));
    
    PropertyManager.override("_com.pelzer.util.UnitOverridableFields.PROTECTED_STRING", "You should never see this in the log!!! ERROR!!!");
  }
  
  public OverridableFieldsTest(final String name) {
    super(name);
  }
  
  public void testStrings() {
    assertEquals("I'm a string", UnitOverridableFields.STRING);
  }
  
  public void testObfuscatedStrings() {
    if (PropertyManager.isDEV()) {
      assertEquals(UnitOverridableFields.PLAINTEXT_STRING, UnitOverridableFields.OBFUSCATED_STRING.clarify());
    } else {
      logger.warn("testObfuscatedStrings(): Obfuscation is disabled outside of DEV. Skipping test.");
    }
  }
  
  public void testInts() {
    assertEquals(12345, UnitOverridableFields.INT);
  }
  
  public void testStringArrays() {
    assertEquals(5, UnitOverridableFields.STRING_ARRAY.length);
    assertEquals("blah1", UnitOverridableFields.STRING_ARRAY[0]);
  }
  
  public void testIntArrays() {
    final int intArray[] = UnitOverridableFields.getIntArray();
    assertEquals(2, intArray.length);
    assertEquals(42, intArray[0]);
    assertEquals(12, intArray[1]);
  }
}