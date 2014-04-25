/**
 * Copyright 2007 Jason Pelzer.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package com.pelzer.util;

/**
 * Simple class used to set up the OverridableFieldsTest
 */
public class UnitOverridableFields extends OverridableFields{
  private static final long serialVersionUID = 1L;
  public static String STRING;
  public static int INT;
  public static String STRING_ARRAY[];
  public static UnitEnum[] ENUM_LIST;
  public static UnitPojo POJO;
  public final static String PLAINTEXT_STRING = "Tee hee hee, let's see if this works!";
  public static ObfuscatedString OBFUSCATED_STRING;

  private static int INT_ARRAY[];

  public static int[] getIntArray(){
    return INT_ARRAY;
  }

  static{
    new UnitOverridableFields().init();
  }
}
