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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import com.pelzer.util.l10n.localizers.LanguageLocalizer;
import com.pelzer.util.l10n.localizers.Localizer;
import com.pelzer.util.l10n.localizers.ScriptLocalizer;
import com.pelzer.util.l10n.localizers.TerritoryLocalizer;

/** Wraps the passed-in localizer arrays so that we can use them as keys for lookup. */
public class Locale implements Serializable {

  private static final long serialVersionUID = 3343918375907956703L;

  /**
   * Most non-locale aware systems will both populate and pull based on this special locale. If this
   * empty locale is passed into the {@link Localizable#get(Locale[])} method, anywhere in the list,
   * if there are no direct matches, this will match the {@link Localizable#getDefaultLocale()} and
   * hence the get method may return the same value as {@link Localizable#getDefault()}
   */
  public static final Locale NONE = new Locale();

  protected Localizer localizers[];

  public Locale(Localizer... localizers) {
    this.localizers = localizers;
  }

  public Locale(String language, String territory, String script) {
    List<Localizer> localizersList = new ArrayList<Localizer>();
    if (language != null)
      localizersList.add(new LanguageLocalizer(language, ""));
    if (territory != null)
      localizersList.add(new TerritoryLocalizer(territory, ""));
    if (script != null)
      localizersList.add(new ScriptLocalizer(script, ""));

    this.localizers = localizersList.toArray(new Localizer[localizersList.size()]);
  }

  public Locale(java.util.Locale locale) {
    this(locale.getLanguage(), locale.getCountry(), locale.getVariant());
  }

  public Localizer[] getLocalizers() {
    return localizers;
  }

  /** @return The hashcode of the first localizer, or zero if no localizers are specified. */
  @Override
  public int hashCode() {
    if (localizers.length > 0 && localizers[0] != null)
      return localizers[0].hashCode();
    return 0;
  }

  /**
   * @return true if the passed in object wraps Localizers with the same values as this one
   *         specified in the same order.
   */
  @Override
  public boolean equals(Object obj) {
    if (obj instanceof Locale) {
      Locale mlw = (Locale) obj;
      Localizer mlwLocalizers[] = mlw.getLocalizers();
      if (localizers.length != mlwLocalizers.length)
        return false;
      for (int i = 0; i < localizers.length; i++) {
        if (localizers[i] == null && mlwLocalizers[i] != null)
          return false;
        if (!localizers[i].equals(mlwLocalizers[i]))
          return false;
      }
      return true;
    }
    return false;
  }
}