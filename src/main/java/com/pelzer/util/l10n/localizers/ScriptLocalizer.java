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

/** Used to localize based on ISO 15924 script codes (English would be 'latn' for Latin) */
public class ScriptLocalizer extends Localizer {
  private static final long serialVersionUID = 1L;
  /** Unknown (#unknown), a custom script code */
  public static final ScriptLocalizer DEFAULT = new ScriptLocalizer("#unknown", "");
  /** Matches any ScriptLocalizer (not in .equals(), but when the Localizable class is searching) */
  public static final ScriptLocalizer ANY = new ScriptLocalizer(WILDCARD_IDENTIFIER, "");

  /** Always lowercases the script, should be something like 'latn' */
  public ScriptLocalizer(String script, String description) {
    super(script.toLowerCase(), description);
  }

  /** Always lowercases the script, should be something like 'latn' */
  public ScriptLocalizer(String script, String description, String nativeDescription) {
    super(script.toLowerCase(), description, nativeDescription);
  }

}