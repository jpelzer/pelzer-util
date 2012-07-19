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

import junit.framework.TestCase;

import com.pelzer.util.l10n.localizers.LanguageLocalizer;
import com.pelzer.util.l10n.localizers.TerritoryLocalizer;

public class LocaleTest extends TestCase {
  public LocaleTest(String name) {
    super(name);
  }

  public void testHashCodeAndEqualsWork() {
    Locale en = new Locale(new LanguageLocalizer("en", "English"));
    Locale enUS = new Locale(new LanguageLocalizer("en", "English"), new TerritoryLocalizer("US", "United States"));
    Locale enUS2 = new Locale(new LanguageLocalizer("en", "English"), new TerritoryLocalizer("US", "United States"));
    Locale frFR = new Locale(new LanguageLocalizer("fr", "French"), new TerritoryLocalizer("FR", "France"));
    assertEquals(enUS.hashCode(), enUS2.hashCode());
    assertTrue(enUS.equals(enUS2));
    assertFalse(en.equals(enUS));
    assertFalse(enUS.equals(en));
    assertFalse(frFR.equals(en));

  }
}