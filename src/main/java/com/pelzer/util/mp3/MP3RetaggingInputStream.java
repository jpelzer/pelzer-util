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

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.blinkenlights.jid3.ID3Exception;

import com.pelzer.util.Logging;
import com.pelzer.util.mp3.MP3Util.Metadata;

/**
 * This class takes the given file or stream, strips ID3 information from it using
 * {@link MP3AudioOnlyInputStream} and then prepends its own replacement metadata to the stream. IF
 * YOU USE THIS CLASS, you must add JID3 to your classpath, as its scope is listed as 'provided'.
 */
public class MP3RetaggingInputStream extends InputStream {
  private static Logging.Logger log = Logging.getLogger(MP3RetaggingInputStream.class);
  private final MP3AudioOnlyInputStream in;
  private byte tagArray[] = new byte[0];
  private int tagArrayIndex = 0;

  public static void main(final String[] args) throws Exception {
    try {
      final String inName = "C:\\Documents and Settings\\Jason Pelzer\\My Documents\\My Music\\eol Trio\\EAR TO THE GROUND_ FRANCE\\eol Trio - EAR TO THE GROUND_ FRANCE - 11 - Hybrid Marmalade.mp3.nometa.mp3";
      final String outName = inName + ".changed.mp3";
      final String coverArt = "C:\\Documents and Settings\\Jason Pelzer\\My Documents\\My Music\\Dolly Parton\\Little Blossoms\\Folder.jpg";

      final Metadata metadata = new Metadata();
      metadata.setArtist("Jason Pelzer");
      metadata.setCoverArt(new File(coverArt));

      final InputStream in = new MP3RetaggingInputStream(new File(inName), metadata);
      final OutputStream out = new BufferedOutputStream(new FileOutputStream(outName));

      for (int current = in.read(); current != -1; current = in.read()) {
        out.write(current);
      }
      in.close();
      out.close();
    } catch (final Throwable ex) {
      log.error("Exception", ex);
    }
  }

  @Override
  public int read() throws IOException {
    if (tagArray != null) {
      if (tagArrayIndex < tagArray.length) {
        // Haven't reached the end of the tag yet...
        tagArrayIndex++;
        return tagArray[tagArrayIndex - 1] & 0xFF;
      }
      // If we're here, we're done with the tag. Free up the memory.
      tagArray = null;
    }
    return in.read();
  }

  /** Takes the given stream, strips it of its existing header and prepends the given byte array. */
  public MP3RetaggingInputStream(final InputStream in, byte[] newID3ByteArray) throws IOException {
    this.in = new MP3AudioOnlyInputStream(in);
    this.tagArray = newID3ByteArray;
  }

  /** Takes the given file, strips it of its existing header and prepends the given byte array. */
  public MP3RetaggingInputStream(final File source, byte[] newID3ByteArray) throws IOException {
    this(new FileInputStream(source), newID3ByteArray);
  }

  /**
   * Computes the new header and then calls
   * {@link MP3RetaggingInputStream#MP3RetaggingInputStream(InputStream, byte[])}
   */
  public MP3RetaggingInputStream(final InputStream in, final Metadata metadata) throws ID3Exception, IOException {
    this(in, MP3Util.createID3Tag(metadata));
  }

  /**
   * Computes the new header and then calls
   * {@link MP3RetaggingInputStream#MP3RetaggingInputStream(InputStream, byte[])}
   */
  public MP3RetaggingInputStream(final File source, final Metadata metadata) throws ID3Exception, IOException {
    this(new FileInputStream(source), MP3Util.createID3Tag(metadata));
  }

  @Override
  public void close() throws IOException {
    in.close();
    super.close();
  }

}
