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
package com.pelzer.util.l10n;

import java.beans.XMLDecoder;
import java.beans.XMLEncoder;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

import junit.framework.TestCase;

import com.pelzer.util.Logging;
import com.pelzer.util.l10n.localizers.LanguageLocalizer;
import com.pelzer.util.l10n.localizers.TerritoryLocalizer;

public class LocalizableTest extends TestCase {
  public LocalizableTest(String name) {
    super(name);
  }

  public void testDefault() {
    Locale en = new Locale(new LanguageLocalizer("en", "English"));
    Locale enUS = new Locale(new LanguageLocalizer("en", "English"), new TerritoryLocalizer("US", "United States"));
    Locale spUS = new Locale(new LanguageLocalizer("sp", "Spanish"), new TerritoryLocalizer("US", "United States"));
    Locale frFR = new Locale(new LanguageLocalizer("fr", "French"), new TerritoryLocalizer("FR", "France"));
    Locale enFR = new Locale(new LanguageLocalizer("en", "English"), new TerritoryLocalizer("FR", "France"));
    Locale deDE = new Locale(new LanguageLocalizer("de", "German"), new TerritoryLocalizer("DE", "Germany"));
    Locale fooBAR = new Locale(new LanguageLocalizer("foo", "Foo"), new TerritoryLocalizer("BAR", "Bar"));

    {
      // Simple
      Localizable<String> ls1 = new Localizable<String>("12345", new Locale(new LanguageLocalizer("chi", "Chinese")));
      ls1.set("56789", new Locale(new LanguageLocalizer("eng", "English")));
      assertEquals("12345", ls1.getDefault());
      assertEquals("56789", ls1.exactGet(new Locale(new LanguageLocalizer("eng", "English"))));
      assertEquals("12345", ls1.get(new Locale(new LanguageLocalizer("abc", "ABC")), new Locale(new LanguageLocalizer("def", "DEF")), Locale.NONE));
      assertEquals(null, ls1.get(new Locale(new LanguageLocalizer("abc", "ABC")), new Locale(new LanguageLocalizer("def", "DEF"))));
      assertEquals("56789", ls1.get(new Locale(new LanguageLocalizer("abc", "ABC")), new Locale(new LanguageLocalizer("eng", "English"))));
    }

    {
      // Complex?
      Localizable<String> ls1 = new Localizable<String>("I'm the default (I'm enUS).", enUS);
      ls1.set("I'm en", en);
      ls1.set("I'm frFR", frFR);
      ls1.set("I'm deDE", deDE);
      ls1.set("I'm spUS", spUS);

      // Test basic defaulting logic, using keys that we either know exist, or we know don't
      assertEquals("I'm en", ls1.get(en));
      assertEquals("I'm en", ls1.get(en, fooBAR));
      assertEquals("I'm en", ls1.get(fooBAR, en));
      assertEquals("I'm frFR", ls1.get(fooBAR, frFR, en));
      assertEquals("I'm the default (I'm enUS).", ls1.get(fooBAR, Locale.NONE));
      assertEquals("I'm the default (I'm enUS).", ls1.get(enUS));
      assertEquals("I'm the default (I'm enUS).", ls1.getDefault());
      // Now test using wildcards
      assertEquals("I'm frFR", ls1.get(new Locale(new LanguageLocalizer("fr", "French"), TerritoryLocalizer.ANY)));
      assertEquals("I'm frFR", ls1.get(new Locale(new LanguageLocalizer("fr", "French"))));
      assertEquals("I'm frFR", ls1.get(new Locale(new TerritoryLocalizer("FR", "France"))));
      //assertEquals("I'm the default (I'm enUS).", ls1.get(new Locale(new TerritoryLocalizer("US", "United States"))));
    }

    // Test territory-only
    {
      Localizable<String> ld = new Localizable<String>();
      ld.set("US", enUS);
      ld.set("FR", enFR);
      assertEquals("FR", ld.get(new Locale(new TerritoryLocalizer("FR", "France"))));
      assertEquals("US", ld.get(new Locale(new TerritoryLocalizer("US", "United States"))));
    }
  }

  public void testXMLEncodingAndDecoding() {
    Logging.Logger logger = Logging.getLogger(this);
    Locale en = new Locale(new LanguageLocalizer("en", "English"));
    Locale enUS = new Locale(new LanguageLocalizer("en", "English"), new TerritoryLocalizer("US", "United States"));
    Locale spUS = new Locale(new LanguageLocalizer("sp", "Spanish"), new TerritoryLocalizer("US", "United States"));
    Locale frFR = new Locale(new LanguageLocalizer("fr", "French"), new TerritoryLocalizer("FR", "France"));
    Locale deDE = new Locale(new LanguageLocalizer("de", "German"), new TerritoryLocalizer("DE", "Germany"));
    Locale fooBAR = new Locale(new LanguageLocalizer("foo", "Foo"), new TerritoryLocalizer("BAR", "Bar"));

    Localizable<String> ls1 = new Localizable<String>("I'm the default (I'm enUS).", enUS);
    ls1.set("I'm en", en);
    ls1.set("I'm frFR", frFR);
    ls1.set("I'm deDE", deDE);
    ls1.set("I'm spUS", spUS);

    ByteArrayOutputStream xml = new ByteArrayOutputStream();
    XMLEncoder xmlEnc = new XMLEncoder(xml);
    xmlEnc.writeObject(ls1);
    xmlEnc.close();
    logger.debug("XML=" + xml.toString());
    XMLDecoder xmlDec = new XMLDecoder(new ByteArrayInputStream(xml.toByteArray()));
    Localizable<?> ls2 = (Localizable<?>) xmlDec.readObject();

    assertEquals(ls1.getDefault(), ls2.getDefault());
    assertEquals(ls1.get(frFR), ls2.get(frFR));
    assertEquals(ls1.get(fooBAR), ls2.get(fooBAR));
    assertEquals(ls1.get(deDE), ls2.get(deDE));
    assertEquals(ls1.get(spUS), ls2.get(spUS));
  }
}
