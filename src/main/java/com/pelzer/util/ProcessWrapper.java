package com.pelzer.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class ProcessWrapper {
  private Process proc;
  private InputStream     inStream;
  private InputStream     errStream;
  private OutputStream    outStream;

  public ProcessWrapper(Process proc) {
    setProcess(proc);
  }

  public ProcessWrapper(String command[])  {
    try {
      setProcess((Runtime.getRuntime()).exec(command));
    } catch (Exception ex) {
      throw new RuntimeException("Exception while establishing connection:", ex);
    }
  }

  private void setProcess(Process proc) {
    this.proc = proc;
    inStream = proc.getInputStream();
    errStream = proc.getErrorStream();
    outStream = proc.getOutputStream();
  }

  /**
   * @return an array of zero or more bytes containing all available data from the process' input stream.
   */
  public byte[] readIn() throws IOException {
    byte buffer[] = new byte[inStream.available()];
    inStream.read(buffer);
    return buffer;
  }

  /**
   * @return an array of zero or more bytes containing all available data from the process' error stream.
   */
  public byte[] readErr() throws IOException {
    byte buffer[] = new byte[errStream.available()];
    errStream.read(buffer);
    return buffer;
  }

  /** Writes the given buffer to the process' output stream */
  public void write(byte write[]) throws IOException {
    outStream.write(write);
  }

  /** Cleanly kills the process. */
  public void close() {
    proc.destroy();
    try {
      inStream.close();
      outStream.close();
      errStream.close();
    } catch (IOException ignored) {
    }
    inStream = null;
    outStream = null;
    errStream = null;
    proc = null;
  }

  /** @return true if the process is running. */
  public boolean isAlive() {
    if (proc == null)
      return false;
    try {
      proc.exitValue();
      return false; // If we got here, the process has exited
    } catch (IllegalThreadStateException ignored) {
    }
    return true;
  }
}