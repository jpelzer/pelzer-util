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

import java.beans.Encoder;
import java.beans.Expression;
import java.beans.PersistenceDelegate;
import java.beans.Statement;

import com.pelzer.util.SimplePersistenceBeanInfo;

/** Specifies how to serialize a Localizable object using {@link java.beans.XMLEncoder} */
public class LocalizableBeanInfo extends SimplePersistenceBeanInfo {
  private static final long serialVersionUID = -6366802866344117694L;

  @Override
  protected PersistenceDelegate getPersistenceDelegate() {
    return new LocalizablePersistenceDelegate();
  }

  public static class LocalizablePersistenceDelegate extends PersistenceDelegate {

    protected Expression instantiate(Object oldInstance, Encoder out) {
      Localizable<?> localizable = (Localizable<?>) oldInstance;
      return new Expression(oldInstance, oldInstance.getClass(), "new", new Object[] { localizable.getDefault(), localizable.getDefaultLocale() });
    }

    protected void initialize(Class<?> type, Object oldInstance, Object newInstance, Encoder out) {
      Localizable<?> localizable = (Localizable<?>) oldInstance;

      for (Locale locale : localizable.getAllLocales())
        out.writeStatement(new Statement(oldInstance, "set", new Object[] { localizable.exactGet(locale), locale }));
    }
  }
}
