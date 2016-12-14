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

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * Utility class to absorb exceptions in certain cases such as sleeping and in
 * catch blocks.
 */
public class Absorb{
  private static final Logging.Logger ignore = Logging.getLogger(Absorb.class.getName() + ".ignore");
  private static final Set<IgnoredExceptionListener> ignoredExceptionListeners = new HashSet<IgnoredExceptionListener>();

  /**
   * Sleeps in exactly the same way as calling
   * <code>timeUnit.sleep(units)</code>, but absorbs any InterruptedException
   * and exits normally.
   */
  public static void sleep(final TimeUnit timeUnit, final long units){
    try{
      timeUnit.sleep(units);
    }catch(final InterruptedException ignored){
      ignore(ignored);
    }
  }

  /** Shortcut to {@link Absorb#sleep(TimeUnit, long)} */
  public static void sleep(final long milliseconds){
    sleep(TimeUnit.MILLISECONDS, milliseconds);
  }

  /**
   * Not an exactly perfect name, but basically takes the given exception and
   * logs it under this class's logger, which is silenced by default. Useful for
   * making sure you don't have empty <code>catch</code> clauses, but you don't have to write a bunch of boilerplate
   * to do something with the tossed exception.
   */
  public static void ignore(final Throwable ignored){
    ignore.error("Ignored exception", ignored);
    for(final IgnoredExceptionListener listener : ignoredExceptionListeners){
      listener.handle(ignored);
    }
  }

  public static synchronized void addIgnoredExceptionListener(final IgnoredExceptionListener listener){
    ignoredExceptionListeners.add(listener);
  }

  public static synchronized void removeIgnoredExceptionListener(final IgnoredExceptionListener listener){
    ignoredExceptionListeners.remove(listener);
  }

  public static interface IgnoredExceptionListener{
    void handle(Throwable ignored);
  }

  /**
   * Wraps the passed exception and then rethrows it wrapped in a runtime
   * exception.
   */
  public static void rethrow(final Throwable ex){
    throw new RuntimeException("Rethrowing as RTE: ", ex);
  }
}
