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

import java.beans.XMLDecoder;
import java.beans.XMLEncoder;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

import junit.framework.TestCase;

import com.pelzer.util.Logging;
import com.pelzer.util.Timecode;
import com.pelzer.util.Timecode.TimecodeException;
import com.pelzer.util.Timecode.Type;

public class TimecodeTest extends TestCase {
  public TimecodeTest(String name) {
    super(name);
  }

  public void testToFramesCD() {
    Timecode s = new Timecode(Type.TYPE_AUDIO_CD);
    s.setSeconds(1);
    s.normalize();
    assertEquals(75, s.toFrames(), 0);
  }

  /**
   * Tests that the class succussfully encodes using the java.beans.Encoder api.
   */
  public void testXMLEncoding() {
    Logging.Logger logger = Logging.getLogger(this);
    Timecode s1 = new Timecode(Type.TYPE_AUDIO_CD);
    s1.setCode(345343);
    ByteArrayOutputStream xml = new ByteArrayOutputStream();
    XMLEncoder xmlEnc = new XMLEncoder(xml);
    xmlEnc.writeObject(s1);
    xmlEnc.close();
    logger.debug("XML=" + xml.toString());
    XMLDecoder xmlDec = new XMLDecoder(new ByteArrayInputStream(xml.toByteArray()));
    Timecode s2 = (Timecode) xmlDec.readObject();
    assertEquals(s1, s2);
  }

  public void testToFramesNTSC() {
    Timecode s = new Timecode(Type.TYPE_VIDEO_NTSC);
    s.setSeconds(1);
    s.normalize();
    assertEquals(29.97F, s.toFrames(), 0);
  }

  public void testCD() throws TimecodeException {
    assertEquals(0F, new Timecode(0).toSeconds(), 0);
    assertEquals(1F, new Timecode("00:00:00:75").toSeconds(), 0);
  }

  public void testNTSCVideo() {
    assertEquals(0F, new Timecode(0, Type.TYPE_VIDEO_NTSC).toSeconds(), 0);
    assertEquals(29.97F, new Timecode(1F, Type.TYPE_VIDEO_NTSC).toFrames(), 0.001);
  }

  public void testToMMSS() {
    Timecode s = new Timecode(0);
    assertEquals("00:00", s.toMMSS());
    s.setSeconds(59);
    assertEquals("00:59", s.toMMSS());
    s.setSeconds(60);
    s.normalize();
    assertEquals("01:00", s.toMMSS());
    s.clear();
    s.setHours(1);
    s.setMinutes(3);
    s.setSeconds(5);
    s.setFrames(7);
    s.normalize();
    assertEquals("63-05", s.toMMSS("-"));
    assertEquals("6305", s.toMMSS(""));
  }

  public void testToHHMMSS() {
    Timecode s = new Timecode(0);
    assertEquals("00:00:00", s.toHHMMSS());
    s.setSeconds(59);
    assertEquals("00:00:59", s.toHHMMSS());
    s.setSeconds(60);
    s.normalize();
    assertEquals("00:01:00", s.toHHMMSS());
    s.clear();
    s.setHours(1);
    s.setMinutes(3);
    s.setSeconds(5);
    s.setFrames(7);
    s.normalize();
    assertEquals("01^_^03^_^05", s.toHHMMSS("^_^"));
    assertEquals("01-03-05", s.toHHMMSS("-"));
    assertEquals("010305", s.toHHMMSS(""));
  }

  public void testToIntSeconds() {
    Timecode s = new Timecode(0);
    assertEquals(0, s.toIntSeconds());
    s.setSeconds(59);
    assertEquals(59, s.toIntSeconds());
    s.setSeconds(60);
    s.normalize();
    assertEquals(60, s.toIntSeconds());
    s.clear();
    assertEquals(0, s.toIntSeconds());
    s.setHours(1);
    s.setMinutes(3);
    s.setSeconds(5);
    s.setFrames(7);
    s.normalize();
    assertEquals(3785, s.toIntSeconds());
  }

  public void testTypeGetInstance() {
    assertEquals(Type.getInstance(29.97f), Type.TYPE_VIDEO_NTSC);
    assertEquals(Type.getInstance(75.0f), Type.TYPE_AUDIO_CD);
    assertEquals(Type.getInstance(24.000F), Type.TYPE_VIDEO_FILM);
    assertEquals(Type.getInstance(25.000f), Type.TYPE_VIDEO_PAL);
    // Just for giggles, do the opposite.
    assertNotSame(Type.getInstance(29.97f), Type.TYPE_VIDEO_FILM);
  }

}