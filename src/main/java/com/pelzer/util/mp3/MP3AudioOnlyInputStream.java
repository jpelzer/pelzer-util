/**
 * Copyright 2009 Jason Pelzer.
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
package com.pelzer.util.mp3;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.LinkedList;
import java.util.ListIterator;

/**
 * This class takes the given stream or file and strips the ID3v2.x tags from it, returning only the
 * audio content. If you do not pass in a {@link BufferedInputStream}, this class will wrap your
 * passed in stream.
 */
public class MP3AudioOnlyInputStream extends InputStream {
  private final BufferedInputStream in;
  private final LinkedList<Integer> buffer = new LinkedList<Integer>();
  private long bytesRead = 0;

  public static void main(final String[] args) throws Exception {
    final String inName = "C:\\Documents and Settings\\Jason Pelzer\\My Documents\\My Music\\eol Trio\\EAR TO THE GROUND_ FRANCE\\eol Trio - EAR TO THE GROUND_ FRANCE - 11 - Hybrid Marmalade.mp3";
    final String outName = inName + ".nometa.mp3";
    final InputStream in = new MP3AudioOnlyInputStream(new File(inName));
    final OutputStream out = new BufferedOutputStream(new FileOutputStream(outName));

    for (int current = in.read(); current != -1; current = in.read()) {
      out.write(current);
    }
    in.close();
    out.close();
  }

  @Override
  public int read() throws IOException {
    if (bytesRead == 0) {
      // First read, let's see if the ID3 tag is there at all...
      if (bufferedPeek(0) == 'I' && bufferedPeek(1) == 'D' && bufferedPeek(2) == '3') {
        // Found the header, let's figure out how large the ID3 header is so we can skip ahead...
        final long length = MP3Util.bytesToLength(new byte[]{(byte) bufferedPeek(6), (byte) bufferedPeek(7), (byte) bufferedPeek(8), (byte) bufferedPeek(9) });
        for (int i = 0; i < 10 + length; i++)
          bufferedRead();
      }
    }
    // We know we've stripped off any ID3v2.x tags from the beginning, now start looking for v1.1
    final int current = bufferedRead();
    if (current == 'T' && bufferedPeek(0) == 'A' && bufferedPeek(1) == 'G' && bufferedPeek(128) == -1) {
      // We've found the ID3v1.x header and the end of the file.
      return -1;
    }
    return current;
  }

  /**
   * @return the next character off the stream, from the buffer if it's full, or directly from the
   *         stream if the buffer is empty.
   */
  private int bufferedRead() throws IOException {
    bytesRead++;
    if (buffer.size() > 0)
      return buffer.remove();
    return in.read();
  }

  /**
   * Reads throw the buffer up to the given index, filling the buffer from the underlying stream if
   * necessary. Returns -1 if the underlying stream terminates before the given offset can be
   * reached.
   */
  private int bufferedPeek(final int offset) throws IOException {
    if (buffer.size() > offset) {
      // We've got enough of the file in memory already, we just pull from the buffer
      final ListIterator<Integer> iterator = buffer.listIterator(offset);
      return iterator.next();
    }
    // We don't have enough in the buffer to peek... Read until we do, then recurse.
    while (buffer.size() <= offset) {
      final int current = in.read();
      if (current < 0)
        return -1;
      buffer.add(current);
    }
    // Buffer should now be full enough to answer the request.
    return bufferedPeek(offset);
  }

  public MP3AudioOnlyInputStream(final File inFile) throws FileNotFoundException {
    this(new FileInputStream(inFile));
  }

  public MP3AudioOnlyInputStream(final InputStream in) {
    if (in instanceof BufferedInputStream)
      this.in = (BufferedInputStream) in;
    else
      this.in = new BufferedInputStream(in);
  }

  @Override
  public void close() throws IOException {
    in.close();
    super.close();
  }

}
