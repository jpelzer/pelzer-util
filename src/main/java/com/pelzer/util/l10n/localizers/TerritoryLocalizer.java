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

/**
 * Used to localize based on two character ISO 3166 territory codes (United States would be 'US'),
 * or 'WW' (which is not an official code)
 */
public class TerritoryLocalizer extends Localizer {
  private static final long serialVersionUID = 1L;
  /** Worldwide (WW) */
  public static final TerritoryLocalizer WW = new TerritoryLocalizer("WW", "World");
  /** Matches any TerritoryLocalizer (not in .equals(), but when the Localizable class is searching) */
  public static final TerritoryLocalizer ANY = new TerritoryLocalizer(WILDCARD_IDENTIFIER, "");

  /** Always capitalizes the territory, should be something like 'US' or 'WW' */
  public TerritoryLocalizer(String territory, String description) {
    super(territory.toUpperCase(), description);
  }

  /** Always capitalizes the territory, should be something like 'US' or 'WW' */
  public TerritoryLocalizer(String territory, String description, String nativeDescription) {
    super(territory.toUpperCase(), description, nativeDescription);
  }

}