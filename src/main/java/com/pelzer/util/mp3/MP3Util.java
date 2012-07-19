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
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import org.blinkenlights.jid3.ID3Exception;
import org.blinkenlights.jid3.v2.APICID3V2Frame;
import org.blinkenlights.jid3.v2.COMMID3V2Frame;
import org.blinkenlights.jid3.v2.ID3V2_3_0Tag;
import org.blinkenlights.jid3.v2.PRIVID3V2Frame;
import org.blinkenlights.jid3.v2.TCOPTextInformationID3V2Frame;
import org.blinkenlights.jid3.v2.TENCTextInformationID3V2Frame;
import org.blinkenlights.jid3.v2.TPE2TextInformationID3V2Frame;
import org.blinkenlights.jid3.v2.TPOSTextInformationID3V2Frame;
import org.blinkenlights.jid3.v2.USERID3V2Frame;
import org.blinkenlights.jid3.v2.WCOPUrlLinkID3V2Frame;
import org.blinkenlights.jid3.v2.APICID3V2Frame.PictureType;

/**
 * Currently almost-exclusively ID3-related tools relating to MP3. IF YOU USE THIS CLASS, you must
 * add JID3 to your classpath, as its scope is listed as 'provided'.
 */
public final class MP3Util {
  private MP3Util() {
  }

  /**
   * Takes an array of 4 bytes and converts it to a long, the way the ID3 tag expects it to be
   * converted, which is to say that only 7 bits of each byte is used, giving a max value of 2^28.
   */
  public static long bytesToLength(final byte bytes[]) {
    return bytes[0] << 21 | bytes[1] << 14 | bytes[2] << 7 | bytes[3];
  }

  /**
   * Takes the given file, opens the header and reads the ID3v2 header size. The returned value is
   * inclusive of the header preamble, so taking the returned value and skipping exactly that number
   * of bytes will place the read cursor at the correct beginning of the actual MP3 data. If the
   * ID3v2 header is missing or the file is shorter than 10 bytes, returns 0.
   * 
   * @throws IOException
   *           in the case of error reading the file.
   */
  public static long getID3v2Length(File file) throws IOException {
    BufferedInputStream in = new BufferedInputStream(new FileInputStream(file));
    try {
      byte buffer[] = new byte[10];
      if (in.read(buffer) != 10)
        return 0;
      if (buffer[0] != 'I' || buffer[1] != 'D' || buffer[2] != '3')
        return 0;
      return 10 + bytesToLength(new byte[]{buffer[6], buffer[7], buffer[8], buffer[9] });
    } finally {
      in.close();
    }
  }

  public static byte[] createID3Tag(final Metadata metadata) throws ID3Exception, IOException {
    final ID3V2_3_0Tag tagV2 = new ID3V2_3_0Tag();

    if (metadata.artist != null)
      tagV2.setArtist(metadata.artist);
    if (metadata.albumArtist != null)
      tagV2.setTPE2TextInformationFrame(new TPE2TextInformationID3V2Frame(metadata.albumArtist));
    if (metadata.album != null)
      tagV2.setAlbum(metadata.album);
    if (metadata.genre != null)
      tagV2.setGenre(metadata.genre);
    if (metadata.title != null)
      tagV2.setTitle(metadata.title);

    if (metadata.termsOfUse != null)
      tagV2.setUSERFrame(new USERID3V2Frame("eng", metadata.termsOfUse));
    if (metadata.encodedBy != null)
      tagV2.setTENCTextInformationFrame(new TENCTextInformationID3V2Frame(metadata.encodedBy));
    if (metadata.cLine != null && metadata.year != null)
      tagV2.setTCOPTextInformationFrame(new TCOPTextInformationID3V2Frame(metadata.year, metadata.cLine));
    if (metadata.pLine != null)
      tagV2.addCOMMFrame(new COMMID3V2Frame("eng", null, metadata.pLine));
    if (metadata.copyrightLegalURL != null)
      tagV2.setWCOPUrlLinkFrame(new WCOPUrlLinkID3V2Frame(metadata.copyrightLegalURL));
    if (metadata.year != null)
      tagV2.setYear(metadata.year);

    if (metadata.trackNumber != null) {
      if (metadata.totalTracks != null) {
        tagV2.setTrackNumber(metadata.trackNumber, metadata.totalTracks);
      } else {
        tagV2.setTrackNumber(metadata.trackNumber);
      }
    }
    if (metadata.discNumber != null)
      tagV2.setTPOSTextInformationFrame(new TPOSTextInformationID3V2Frame(metadata.discNumber));
    if (metadata.coverArt != null)
      embedCoverArt(tagV2, metadata.coverArt);

    for (final Map.Entry<String, byte[]> entry : metadata.customFields.entrySet())
      tagV2.addPRIVFrame(new PRIVID3V2Frame(entry.getKey(), entry.getValue()));

    // Now write the tag to the array...
    final ByteArrayOutputStream out = new ByteArrayOutputStream(50000);
    tagV2.write(out);
    return out.toByteArray();
  }

  private static void embedCoverArt(final ID3V2_3_0Tag tag, final File coverFile) throws ID3Exception, IOException {
    final ByteArrayOutputStream coverBytes = new ByteArrayOutputStream();
    final InputStream coverIn = new FileInputStream(coverFile);
    try {
      int currentByte;
      while ((currentByte = coverIn.read()) != -1)
        coverBytes.write(currentByte);
      coverBytes.close();

      tag.addAPICFrame(new APICID3V2Frame("image/jpeg", PictureType.FrontCover, "cover", coverBytes.toByteArray()));
    } finally {
      coverIn.close();
      coverBytes.close();
    }
  }

  /**
   * A POJO that stores the info to be encoded into an MP3 file's ID3.
   */
  public static class Metadata {
    private String artist = null;
    private String albumArtist = null;
    private String album = null;
    private String genre = null;
    private String title = null;
    private Integer trackNumber = null;
    private Integer totalTracks = null;
    private Integer discNumber = null;
    private File coverArt = null;
    private Integer year = null;
    private String termsOfUse = null;
    private String cLine = null;
    private String pLine = null;
    private String encodedBy = null;
    private String copyrightLegalURL = null;
    private final Map<String, byte[]> customFields = new HashMap<String, byte[]>();

    /**
     * Custom fields are encoded into the ID3, but probably not readable by most players. The
     * encoded data can be in any format you choose. The key should be unique, such as a controlled
     * domain or email address.
     */
    public void addCustomField(final String key, final byte data[]) {
      this.customFields.put(key, data);
    }

    public String getArtist() {
      return artist;
    }
    
    public String getAlbumArtist() {
      return albumArtist;
    }

    public String getAlbum() {
      return album;
    }

    public String getGenre() {
      return genre;
    }

    public String getTitle() {
      return title;
    }

    public Integer getTrackNumber() {
      return trackNumber;
    }

    public Integer getTotalTracks() {
      return totalTracks;
    }

    public Integer getDiscNumber() {
      return discNumber;
    }

    public File getCoverArt() {
      return coverArt;
    }

    public Integer getYear() {
      return year;
    }

    public String getTermsOfUse() {
      return termsOfUse;
    }

    public String getCLine() {
      return cLine;
    }

    public String getPLine() {
      return pLine;
    }

    public String getEncodedBy() {
      return encodedBy;
    }

    public String getCopyrightLegalURL() {
      return copyrightLegalURL;
    }

    public void setArtist(final String artist) {
      this.artist = artist;
    }
    
    public void setAlbumArtist(String albumArtist) {
      this.albumArtist = albumArtist;
    }

    public void setAlbum(final String album) {
      this.album = album;
    }

    public void setGenre(final String genre) {
      this.genre = genre;
    }

    public void setTitle(final String title) {
      this.title = title;
    }

    public void setTrackNumber(final Integer trackNumber) {
      this.trackNumber = trackNumber;
    }

    public void setTotalTracks(final Integer totalTracks) {
      this.totalTracks = totalTracks;
    }

    public void setDiscNumber(final Integer discNumber) {
      this.discNumber = discNumber;
    }

    public void setCoverArt(final File coverArt) {
      this.coverArt = coverArt;
    }

    public void setYear(final Integer year) {
      this.year = year;
    }

    public void setTermsOfUse(final String termsOfUse) {
      this.termsOfUse = termsOfUse;
    }

    public void setCLine(final String line) {
      cLine = line;
    }

    public void setPLine(final String line) {
      pLine = line;
    }

    public void setEncodedBy(final String encodedBy) {
      this.encodedBy = encodedBy;
    }

    public void setCopyrightLegalURL(final String copyrightLegalURL) {
      this.copyrightLegalURL = copyrightLegalURL;
    }
  }

}
