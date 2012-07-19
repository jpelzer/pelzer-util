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

import com.pelzer.util.ObfuscationManager;

import junit.framework.TestCase;

public class ObfuscationManagerTest extends TestCase {
  public ObfuscationManagerTest(String string) {
    super(string);
  }

  public void testObfuscate() {
    String start = "I'm a happy string.";
    String end = ObfuscationManager.obfuscate(start);
    assertEquals("794F205926105D09203C4A604647365900407A", end);
    String clear = ObfuscationManager.clarify(end);
    assertEquals(start, clear);
  }
  public void testClarify() {
    String start = "I'm a happy string.";
    String clear = ObfuscationManager.clarify("794F205926105D09203C4A604647365900407A");
    assertEquals(start, clear);
  }
}