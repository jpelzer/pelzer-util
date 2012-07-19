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

/** There is no built-in mechanism for stopping threads in Java, so this class is a simple 
 * fix for the issue. Any thread that you need another thread to be able to signal to 
 * shut down should have a run() method that looks something like this:
 * 
 *  <pre>
 *  run(){
 *  	while(!die){
 *  		doSomething();
 *  		sleep(...);
 *  	}
 *  }</pre>
 *  
 *  Another thread can then request the killable thread shut down gracefully by doing the following:
 *  
 *  <pre>
 *  killableThread.die = true;
 *  killableThread.waitFor();
 *  </pre>
 */
public abstract class KillableThread extends Thread {
  /** Implementing classes should watch this value, and terminate gracefully if it ever becomes true. */
  public volatile boolean die = false;
}
