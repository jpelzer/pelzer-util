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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * Wrapper around the unix concept of a process identifier. Only works if you're
 * running on a system that has perl in the path.
 */
public class PID {
  private static Logging.Logger logger = Logging.getLogger(PID.class);
  
  /**
   * @return the pid of the current JVM. Requires that 'perl' be in the path
   *         otherwise returns -1
   */
  public static int getCurrentPID() {
    try {
      Process p = Runtime.getRuntime().exec(new String[] { "perl", "-e", "print getppid()" });
      BufferedReader br = new BufferedReader(new InputStreamReader(p.getInputStream()));
      final int pid = Integer.parseInt(br.readLine());
      p.destroy();
      br.close();
      p = null;
      br = null;
      return pid;
    } catch (final java.io.IOException ex) {
      logger.error("PID Exception, returning -1", ex);
      return -1;
    }
  }
  
  /** @return the parsed int read from the given pid file */
  public static int readPID(final File pidFile) throws IOException, NumberFormatException {
    final BufferedReader in = new BufferedReader(new FileReader(pidFile));
    try {
      final String pidString = in.readLine();
      return Integer.parseInt(pidString + "");
    } finally {
      in.close();
    }
  }
  
  /** Returns true if the given pid is alive. Requires /bin/ps (ie UNIX) */
  public static boolean isPIDAlive(final int pid) throws IOException {
    final String ps = "ps";
    final String command[] = new String[] { ps, "-fp", pid + "" };
    final StringBuffer in = new StringBuffer();
    final Process process = Runtime.getRuntime().exec(command);
    
    InputStream inStream = process.getInputStream();
    InputStream errStream = process.getErrorStream();
    // wait for the process to complete.
    try {
      process.waitFor();
    } catch (final InterruptedException ignored) {
    }
    
    while (inStream.available() > 0) {
      in.append((char) inStream.read());
    }
    while (errStream.available() > 0) {
      in.append((char) errStream.read());
    }
    
    process.destroy();
    inStream.close();
    inStream = null;
    errStream.close();
    errStream = null;
    
    if (in.indexOf(pid + "") > 0)
      return true;
    return false;
  }
  
  public static void main(final String[] args) {
    logger.debug("PID=" + getCurrentPID());
  }
}
