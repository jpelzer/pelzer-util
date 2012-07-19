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

import java.util.concurrent.Semaphore;

/**
 * A wrapper around the concurrent.Semaphore object, which returns an actual
 * permit object when acquire() is called, which can then be used to release the
 * lock, and has safety finalizers that ensure that an acquired permit will
 * eventually be released during garbage collection if a thread crash or other
 * bug breaks the code before semaphore release.
 */
public class SafeSemaphore {
  private Semaphore semaphore;

  public SafeSemaphore(int initialPermits) {
    semaphore = new Semaphore(initialPermits, true);
  }

  /** Wait until a permit is available, and take one. */
  public Permit acquire() throws java.lang.InterruptedException {
    semaphore.acquire();
    return new Permit();
  }

  /** Same as {@link #acquire()}, but won't throw an InterruptedException. */
  public Permit acquireUninterruptibly() {
    semaphore.acquireUninterruptibly();
    return new Permit();
  }

  /** Release a permit. Really, the same as calling permit.release(). */
  public void release(Permit permit) {
    permit.release();
  }

  public class Permit {
    private boolean isReleased = false;

    private Permit() {
    }

    /**
     * If this permit is still valid, calls release() on the parent semaphore.
     * Otherwise does nothing.
     */
    public void release() {
      if (isReleased)
        return;
      semaphore.release();
      isReleased = true;
    }

    /** Makes sure this permit is released. */
    protected void finalize() throws Throwable {
      release();
    }
  }
}
