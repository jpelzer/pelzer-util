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

import java.beans.BeanDescriptor;
import java.beans.PersistenceDelegate;
import java.beans.SimpleBeanInfo;
import java.io.Serializable;

/**
 * This is a simple class that can be extended to specify how to persist a class using the
 * {@link java.beans.Encoder} system. Simply extend this class, then implement the
 * {@link #getPersistenceDelegate()} method. If you are trying to persist a class named Foo, you
 * should create a class named FooBeanInfo which extends this class in the same package as Foo.
 */
abstract public class SimplePersistenceBeanInfo extends SimpleBeanInfo implements Serializable {

  private static final long serialVersionUID = -684990225767655443L;

  /**
   * @return the PersistenceDelegate that should be used for the class described by this BeanInfo.
   */
  protected abstract PersistenceDelegate getPersistenceDelegate();

  public BeanDescriptor getBeanDescriptor() {
    BeanDescriptor bd = super.getBeanDescriptor();
    if (bd == null) {
      bd = new BeanDescriptor(Enum.class);
    }
    bd.setValue("persistenceDelegate", getPersistenceDelegate());
    return bd;
  }

}
