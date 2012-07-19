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
package com.pelzer.util.l10n.localizers;

/** Used to localize based on ISO 639-2 3-letter language codes (English would be 'eng') */
public class LanguageLocalizer extends Localizer {
  private static final long serialVersionUID = 1L;
  /** Unknown (#unknown), a custom language code */
  public static final LanguageLocalizer DEFAULT = new LanguageLocalizer("#unknown", "");
  /** Matches any LanguageLocalizer (not in .equals(), but when the Localizable class is searching) */
  public static final LanguageLocalizer ANY = new LanguageLocalizer(WILDCARD_IDENTIFIER, "");

  /** Always lowercases the language, should be something like 'eng' */
  public LanguageLocalizer(String language, String description) {
    super(language.toLowerCase(), description);
  }

  /** Always lowercases the language, should be something like 'eng' */
  public LanguageLocalizer(String language, String description, String nativeDescription) {
    super(language.toLowerCase(), description, nativeDescription);
  }
}