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

import java.io.Serializable;

/** Wrapper for language or territory. */
public abstract class Localizer implements Comparable<Localizer>, Serializable {
  private static final long serialVersionUID = 1L;
  protected static final String WILDCARD_IDENTIFIER = "*";
  /**
   * The underlying id for an instance in persistent storage. Not settable by anything other than
   * hibernate, and your own system will need to map this correctly for your storage system.
   */
  protected int id = 0;
  private String nativeDescription;
  private String identifier = null;
  private String description = null;

  /** @return true if this localizer has been set to be a wildcard, ie: always matches */
  public boolean isWildCard() {
    return WILDCARD_IDENTIFIER.equals(identifier);
  }

  /**
   * Identifier is the unique id that the ISO (or other standards body) has assigned this localizer,
   * description is the english description, native description is the locale-specific description,
   * or null if it's not available or the same.
   */
  public Localizer(String identifier, String description) {
    this(identifier, description, null);
  }

  public Localizer(String identifier, String description, String nativeDescription) {
    this.identifier = identifier;
    this.description = description;
    this.nativeDescription = nativeDescription;
  }

  /**
   * A shorted code, usually very short based on various ISO codes for language, territory or
   * script. This is the main payload for a Localizer instance.
   */
  public String getIdentifier() {
    return identifier;
  }

  /** Human-readable description of the identifier. */
  public String getDescription() {
    return description;
  }

  /**
   * @return the native description for a given Localizer, for instance Deutschland for Germany, or
   *         the english description if the native description is null.
   */
  public String getNativeDescription() {
    return nativeDescription == null ? description : nativeDescription;
  }

  public int compareTo(Localizer localizer) {
    return identifier.compareTo(localizer.getIdentifier());
  }

  @Override
  public int hashCode() {
    return this.identifier.hashCode();
  }

  @Override
  public boolean equals(Object obj) {
    if (obj instanceof Localizer) {
      return compareTo((Localizer) obj) == 0;
    }
    return false;
  }

}