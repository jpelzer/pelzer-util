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

/**
 * Basically used as a marker inside a Constant file to make sure the system
 * clarifies the underlying info using the {@link ObfuscationManager}. So if
 * you want to have a password field in a constants file, for example, you'd
 * have it be an ObfuscatedString, then refer to it as
 * <code>Constants.PASSWORD.toString()</code>
 */
public class ObfuscatedString {
  private String mangledString = "";

  public ObfuscatedString(String mangledString) {
    this.mangledString = mangledString;
  }

  /**
   * this class stores its data in mangled form, so if you constantly need to
   * access this, you should cache it locally, to avoid repeated clarification
   * runs.
   */
  public String toString() {
    return ObfuscationManager.clarify(mangledString);
  }

  /** Synonym to {@link #toString()} */
  public String clarify() {
    return toString();
  }
}
