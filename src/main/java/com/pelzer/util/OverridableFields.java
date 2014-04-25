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

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

import java.io.Serializable;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

/**
 * This class is used to cache properties from the PropertyManager, and should be extended by local
 * Constants classes in various packages. Classes should put a line in their static initializer like
 * this to guarantee the file gets initialized...: new <Classname>().init();
 */
public abstract class OverridableFields implements Serializable{

  private static final long serialVersionUID = -7895760089195995215L;

  /**
   * if set to true, NO constants will be loaded by the {@link #init()} method, leaving the constant
   * file as it was defined in the .java file. This is only useful when used by the property manager
   * to determine which properties overrides are actually active.
   */
  protected static boolean DISABLE_PROPERTY_LOADING = false;

  private Logging.Logger logger = Logging.getLogger(OverridableFields.class);

  /**
   * This method initializes all the fields of this class with constants from the PropertyManager.
   * For arrays, items should be put in the property system as PROPERTY_NAME.n , where n is a
   * 0-index number (0,1,2,3,...)
   */
  protected void init(){
    init(null);
  }

  /**
   * Custom version of 'init()' that allows you to specify your own base domain to pull properties
   * from. Generally you should just use init() and initialize with the name of your constants
   * class.
   */
  protected void init(String domain){
    // get list of public fields
    java.lang.reflect.Field[] fields = this.getClass().getDeclaredFields();
    if(fields.length > 0){
      if(domain == null)
        domain = fields[0].getDeclaringClass().getName();
      // Fix our debugging to use the child classname
      logger = Logging.getLogger(fields[0].getDeclaringClass().getName());
    }
    logger.debug("Beginning initialization from PropertyManager using domain='" + domain + "'");
    PropertyManager.ManagedProperties properties = PropertyManager.getProperties(domain);
    PropertyManager.ManagedProperties secureProperties = PropertyManager.getProperties("_" + domain);
    // Go through all the public fields in this class, and try to fill them from
    // our property class
    logger.debug("field count = " + fields.length);
    for(int i = 0; i < fields.length; i++){
      String fieldName = fields[i].getName();
      Class<?> type = fields[i].getType();
      String value = secureProperties.getProperty(fieldName);
      boolean doSecurely = value != null;
      if(value == null)
        value = properties.getProperty(fieldName);
      boolean isArray = fields[i].getType().isArray();
      try{
        if(isArray){
          if(value == null){
            // We need to grab all the values for the array (.0,.1,.2, etc)
            List<String> values = new ArrayList<String>();
            for(int j = 0; true; j++){
              if(doSecurely)
                value = secureProperties.getProperty(fieldName + "." + j);
              else
                value = properties.getProperty(fieldName + "." + j);
              if(value == null)
                break;
              if(doSecurely)
                logger.info("Setting field '" + fieldName + ":" + type.getComponentType().getName() + "' [" + j + "] to ***PROTECTED***");
              else
                logger.info("Setting field '" + fieldName + ":" + type.getComponentType().getName() + "' [" + j + "] to " + value);
              values.add(value);
            }
            if(values.size() == 0)
              continue;

            fields[i].setAccessible(true);
            fields[i].set(null, getObjectForString(type, values.toArray(new String[values.size()])));
          }else{
            // We have a single property value, try to deserialize gson
            fields[i].setAccessible(true);
            fields[i].set(null, getObjectForString(type, value));
          }
        }else if(value != null){
          if(doSecurely)
            logger.info("Setting field '" + fieldName + ":" + type.getName() + "' to ***PROTECTED***");
          else
            logger.info("Setting field '" + fieldName + ":" + type.getName() + "' to " + value);
          fields[i].setAccessible(true);
          fields[i].set(null, getObjectForString(type, value));
        }else{
          // debug.debug("'" + fieldName + "' does not exist in the properties
          // file. (Domain='" + domain + "') Leaving alone.");
        }
      }catch(Exception ex){
        logger.error("Error setting field '" + fieldName + "'. Check properties file.", ex);
      }
    }
    logger.debug("Init complete");
  }

  /**
   * This method is similar to {@link #getObjectForString(Class, String[])} except that it returns
   * an array of type objects. Expects the type variable to be an array.
   */
  private Object getObjectForString(Class<?> type, String values[]) throws Exception{
    if(values == null || values.length == 0)
      throw new Exception("getObjectForString: values was null or of zero length");
    Object array = Array.newInstance(type.getComponentType(), values.length);
    // Now fill in the values for the empty array we just created.
    for(int i = 0; i < values.length; i++)
      Array.set(array, i, getObjectForString(type, values[i]));
    return array;
  }

  /**
   * @return an object converted from the given value, for instance
   * getObjectForString("boolean","true") would return a Boolean object set to true, and
   * getObjectForString("float","4.56") would return a Float.
   * @throws Exception if there is a problem converting the String to the given type.
   */
  private Object getObjectForString(Class<?> type, String value) throws Exception{
    Class<?> originalType = type;
    if(type.isArray())
      type = type.getComponentType();
    if(type.equals(Float.TYPE))
      return Float.valueOf(value);
    if(type.equals(Double.TYPE))
      return Double.valueOf(value);
    if(type.equals(Integer.TYPE))
      return Integer.valueOf(value);
    if(type.equals(Long.TYPE))
      return Long.valueOf(value);
    if(type.equals(Character.TYPE))
      return value.charAt(0);
    if(type.equals(Boolean.TYPE)){
      String v = value.toUpperCase().trim();
      return v.startsWith("T") || v.startsWith("Y") || v.startsWith("1") || v.startsWith("ON");
    }
    if(type.equals(ObfuscatedString.class))
      return new ObfuscatedString(value);
    if(type.equals(String.class))
      return value;
    // Attempt to deserialized from JSON
    try{
      Gson gson = new Gson();
      return gson.fromJson(value, originalType);
    }catch(JsonSyntaxException ex){
      throw new Exception("Couldn't deserialize json for class type='" + type.toString() + "', value='" + value + "'", ex);
    }
  }

}
