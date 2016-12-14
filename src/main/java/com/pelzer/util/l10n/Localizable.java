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
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import com.pelzer.util.l10n.localizers.Localizer;

/**
 * This class wraps multiple versions of an object (E) based on a localizer, and then has some
 * defaulting logic for grabbing a single entry back out.
 */
public class Localizable<E> implements Map<Locale, E>, Serializable {
  private static final long serialVersionUID = -4713649222248430617L;
  private Locale defaultLocale = null;
  private Map<Locale, E> values = new HashMap<Locale, E>();

  public Localizable() {
  }

  /**
   * Instantiates, and then calls {@link Localizable#setDefault(Object, Locale)}
   * 
   * @param defaultValue
   *          can be null, and subsequent calls to getDefault will return null
   * @throws NullPointerException
   *           if locale is null.
   */
  public Localizable(E defaultValue, Locale locale) {
    setDefault(defaultValue, locale);
  }

  /**
   * @return an array of the locales that this object has been localized for, including the default
   *         one as the last element of the array. They can then be used to call
   *         {@link #exactGet(Locale)} to return absolute values
   */
  public Locale[] getAllLocales() {
    Vector<Locale> locales = new Vector<Locale>();
    for (Locale locale : values.keySet()) {
      if (!locale.equals(defaultLocale))
        locales.add(locale);
    }
    locales.add(defaultLocale);
    return locales.toArray(new Locale[locales.size()]);
  }

  /**
   * Adds or overrides a value to the internal hash.
   * 
   * @param value
   *          If set to null, removes the value from the internal hash (the local will no longer
   *          show up in {@link #getAllLocales()}
   * @throws NullPointerException
   *           if locale is null.
   * @return The value previously associated with the locale, or null if none.
   */
  public E set(E value, Locale locale) {
    if (defaultLocale == null)
      defaultLocale = locale;
    if (value != null)
      return values.put(locale, value);
    values.remove(locale);
    return null;
  }

  /**
   * Overrides the default locale value.
   * 
   * @param value
   *          can be null, and subsequent calls to getDefault will return null
   * @throws NullPointerException
   *           if locale is null.
   */
  public void setDefault(E value, Locale locale) {
    set(value, locale);
    this.defaultLocale = locale;
  }

  /**
   * @return the String set during construction or later by a call to
   *         {@link Localizable#setDefault(Object, Locale)}
   */
  public E getDefault() {
    return exactGet(defaultLocale);
  }

  /** @return the value stored for the given wrapper, or null if no exact match found. */
  public E exactGet(Locale locale) {
    return values.get(locale);
  }

  /**
   * @return true if there is an EXACT match for the given wrapper, false if the system would need
   *         to use defaulting logic to satisfy the request.
   */
  public boolean hasExactMatch(Locale locale) {
    return exactGet(locale) != null;
  }

  /**
   * @return the best String that can be returned based on the passed-in preferred locales. If one
   *         of the locales matches a stored key, either using absolute equality or fuzzy matching
   *         (out of order/wildcard), that entry will be returned. If no exact or fuzzy matches
   *         occur, and one of the preferred locales was {@link Locale#NONE}, then
   *         {@link #getDefault()} will be returned. A null is possible if no matches are found, and
   *         Locale.NONE is not one of the preferred locales.
   */
  public E get(Locale... preferredLocales) {
    boolean foundNONE = false;
    // Try to get an exactly-matching value
    for (Locale locale : preferredLocales) {
      if (Locale.NONE.equals(locale))
        foundNONE = true;
      E value = exactGet(locale);
      if (value != null)
        return value;
    }
    // Didn't find an exact-match... Try wildcards/fuzzy matching
    E value = getBest(preferredLocales);
    if (value != null)
      return value;

    // No fuzzy matches, return the default
    if (foundNONE)
      return exactGet(defaultLocale);
    return null;
  }

  /**
   * Goes through the list of preferred wrappers, and returns the value stored the hashtable with
   * the 'most good' matching key, or null if there are no matches.
   */
  private E getBest(Locale... preferredLocales) {
    long bestGoodness = 0;
    Locale bestKey = null;
    for (Locale locale : preferredLocales) {
      for (Locale key : values.keySet()) {
        long goodness = computeGoodness(locale, key);
        if (goodness > bestGoodness)
          bestKey = key;
      }
    }
    if (bestKey != null)
      return exactGet(bestKey);
    return null;
  }

  /** @return the default localizer set during construction or {@link Localizable#setDefault(Object, Locale)} */
  public Locale getDefaultLocale() {
    return defaultLocale;
  }

  /**
   * Takes two input wrappers and returns a 'goodness' factor, a reflection of how well the two
   * wrappers match each other. The searchWrapper is a wrapper that may contain 'ANY' localizers,
   * which will match any localizer in the comparisonWrapper that shares the same classtype (these
   * matches are slightly less good than a .equals() match, but still very good)
   * 
   * @return a goodness factor, less than zero if the search wrapper can a localizer that doesn't
   *         match ANY localizers in the comparison wrapper.
   */
  private long computeGoodness(Locale searchLocale, Locale comparisonLocale) {
    Localizer searchLocalizers[] = searchLocale.getLocalizers();
    Localizer comparisonLocalizers[] = comparisonLocale.getLocalizers();
    long goodness = 0;
    for (Localizer searchLocalizer : searchLocalizers) {
      boolean foundMatch = false;
      for (Localizer comparisonLocalizer : comparisonLocalizers) {
        if (comparisonLocalizer.getClass().isInstance(searchLocalizer)) {
          if (searchLocalizer.isWildCard()) {
            // Wildcard match, add goodness...
            goodness += 4;
            foundMatch = true;
          } else if (comparisonLocalizer.equals(searchLocalizer)) {
            // Exact match, add goodness...
            goodness += 5;
            foundMatch = true;
          }
        }
      }
      if (!foundMatch) {
        // searchLocalizer not found at all in comparisonLocalizers, so return -1
        return -1;
      }
    }
    return goodness;
  }

  /**
   * Since this class is actually just wrapping an underlying hashmap, for hibernate support we need
   * to have direct access to this value... The only time this should be used is during
   * (de)serialization.
   * 
   * @see #setWrappedMap(Map)
   */
  public Map<Locale, E> getWrappedMap() {
    return values;
  }

  /** @see #getWrappedMap() */
  public void setWrappedMap(Map<Locale, E> values) {
    this.values = values;
  }

  public void clear() {
    values.clear();
  }

  public boolean containsKey(Object key) {
    if (!(key instanceof Locale))
      return false;
    return hasExactMatch((Locale) key);
  }

  public boolean containsValue(Object value) {
    return values.containsValue(value);
  }

  public Set<java.util.Map.Entry<Locale, E>> entrySet() {
    return values.entrySet();
  }

  public E get(Object key) {
    if (key instanceof Locale)
      return get(new Locale[] { (Locale) key });
    return get(new Locale[0]);
  }

  /** Localizable can never be empty. */
  public boolean isEmpty() {
    return false;
  }

  public Set<Locale> keySet() {
    return values.keySet();
  }

  public E put(Locale locale, E value) {
    return set(value, locale);
  }

  public void putAll(Map<? extends Locale, ? extends E> t) {
    values.putAll(t);
  }

  public E remove(Object key) {
    if (key instanceof Locale)
      return set(null, (Locale) key);
    return null;
  }

  public int size() {
    return values.size();
  }

  public Collection<E> values() {
    return values.values();
  }
}
