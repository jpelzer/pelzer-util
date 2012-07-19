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
package com.pelzer.util.mp3;

import junit.framework.TestCase;

public class MP3UtilTest extends TestCase {
  public MP3UtilTest(final String name) {
    super(name);
  }

  public void testBytesToLength() {
    assertEquals(0, MP3Util.bytesToLength(new byte[]{0, 0, 0, 0 }));
    assertEquals(1, MP3Util.bytesToLength(new byte[]{0, 0, 0, 1 }));
    assertEquals(128, MP3Util.bytesToLength(new byte[]{0, 0, 1, 0 }));
    assertEquals(268435455, MP3Util.bytesToLength(new byte[]{127, 127, 127, 127 }));
    assertEquals(43282, MP3Util.bytesToLength(new byte[]{0x00, 0x02, 0x52, 0x12 }));
    assertEquals(17833, MP3Util.bytesToLength(new byte[]{0x00, 0x01, 0x0b, 0x29 }));
  }

}