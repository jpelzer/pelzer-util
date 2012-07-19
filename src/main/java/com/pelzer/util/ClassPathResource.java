/**
 * Copyright 2010 Jason Pelzer.
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

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

/**
 * Simple class to load files from the classpath. Used primarily by the PropertyManager to find
 * properties files.
 */
public class ClassPathResource {
  private final String path;

  private ClassLoader classLoader;

  public ClassPathResource(String path) {
    this(path, (ClassLoader) null);
  }

  public ClassPathResource(String path, ClassLoader classLoader) {
    if (path.startsWith("/"))
      path = path.substring(1);
    this.path = path;
    this.classLoader = (classLoader != null ? classLoader : getDefaultClassLoader());
  }

  public InputStream getInputStream() throws IOException {
    InputStream is = this.classLoader.getResourceAsStream(this.path);

    if (is == null) {
      throw new FileNotFoundException(path + " cannot be opened because it does not exist");
    }

    return is;
  }

  public static ClassLoader getDefaultClassLoader() {
    ClassLoader cl = null;
    try {
      cl = Thread.currentThread().getContextClassLoader();
    } catch (Throwable ex) {
      // logger.debug("Cannot access thread context ClassLoader - falling back to system class
      // loader", ex);
    }

    if (cl == null) {
      cl = ClassPathResource.class.getClassLoader();
    }
    return cl;
  }
}
