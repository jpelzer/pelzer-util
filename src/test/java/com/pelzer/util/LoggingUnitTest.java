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

import junit.framework.TestCase;

import org.apache.log4j.Level;

/**
 * This test doesn't actually test much, it just logs a few things, but as long as it doesn't throw
 * an exception, it passes. That should probably be changed.
 */
public class LoggingUnitTest extends TestCase {
  private static Logging.Logger logger = null;

  public LoggingUnitTest(String name) {
    super(name);
  }

  public void testStartup() {
    logger = Logging.getLogger(this);
    logger.debug("Testing DEBUG.");
    logger.info("Testing INFO.");
    logger.warn("Testing WARN.");
    logger.error("Testing ERROR.");
    logger.fatal("Testing FATAL.");
    logger.error("Testing exception", new RuntimeException("Yippee!!!"));
    logger.debug("Back to normal.");
  }

  public void testTemplates(){
    logger = Logging.getLogger(this);
    logger.debug("I am a {}","test");
    logger.debug("Testing exception ({})",new RuntimeException("Expected exception!"),"foo");
  }
  
  public void testLog4J(){
    org.apache.log4j.Logger log4j =  org.apache.log4j.Logger.getLogger(LoggingUnitTest.class);
    log4j.log(Level.INFO, "Testing Log4J INFO.");
    log4j =  org.apache.log4j.Logger.getLogger(LoggingUnitTest.class.getName()+"#testLog4J()");
    log4j.log(Level.DEBUG, "Testing Log4J DEBUG.");
  }

  public void testLocalProperty() throws InterruptedException{
    class LoggerThread extends Thread{
      private Logging.Logger log = Logging.getLogger(this);

      @Override
      public void run(){
        Logging.setLocalProperty("I'm a property: " + getName());
        for(int i = 0; i < 5; i++){
          log.debug("I'm alive");
          Absorb.sleep(250);
        }
      }
    }


    Thread threads[] = new Thread[] {new LoggerThread(),new LoggerThread(), new LoggerThread()};
    for(Thread thread:threads)
      thread.start();
    for(Thread thread:threads)
      thread.join();
  }
}
