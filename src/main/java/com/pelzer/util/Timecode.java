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

/**
 * Offers methods to act on timecodes, SMPTE or otherwise. Has special support for audio timecodes,
 * which look like SMPTE but have 75 frames/sec. This class does some nice things, such as when you
 * pass in "00:00:75:00" as a timecode, it normalizes the data to "00:01:15:00". If you pass in a
 * code with a fifth parameter, that parameter is assumed to be samples. Any non-numeric character
 * can be used as a delimiter, so both "00|01|12|34" and "00a01b12c34" are equivalent.
 * {@link #toSamples()} returns the total number of samples that this code represents, and is the
 * most accurate representation with which to do arithmetic on.
 */
public class Timecode implements java.io.Serializable, Cloneable {
  private static final long serialVersionUID = 1L;

  private int hours = 0;
  private int minutes = 0;
  private int seconds = 0;
  private int frames = 0;
  private float framesPerSecond = 75;
  private long samples = -1;
  private float samplesPerSecond = 44100;
  private boolean useSamples = true;

  /**
   * Sets this timecodes samplesPerSecond and framesPerSecond based on the given type
   */
  private void setType(Type type) {
    setSamplesPerSecond(type.getSamplesPerSecond());
    setFramesPerSecond(type.getFramesPerSecond());
    setUseSamples(type.usesSamples());
  }

  /**
   * Creates a new blank CD timecode set at 00:00:00:00
   * 
   * @deprecated This is here for XMLEncode/Decode only. Use
   *             {@link #Timecode(com.pelzer.util.Timecode.Type)} instead.
   */
  public Timecode() {
    this(Type.TYPE_AUDIO_CD);
  }

  /** Creates a new blank timecode of the given type set at 00:00:00:00 */
  public Timecode(Type type) {
    setType(type);
    setCode(0);
  }

  /** Creates a new CD timecode based on a string in the form HH:MM:SS:FF */
  public Timecode(String timecode) throws Timecode.TimecodeException {
    this(timecode, Type.TYPE_AUDIO_CD);
  }

  /**
   * Creates a new timecode of the given type based on a string in the form HH:MM:SS:FF
   */
  public Timecode(String timecode, Type type) throws Timecode.TimecodeException {
    setType(type);
    setCode(timecode);
  }

  /**
   * Creates a new CD timecode based on seconds using default seconds-&gt;frames rate from
   * UploadConstants
   */
  public Timecode(float seconds) {
    this(seconds, Type.TYPE_AUDIO_CD);
  }

  /** Creates a new timecode of the given type based on seconds. */
  public Timecode(float seconds, Type type) {
    setType(type);
    setCode(seconds);
  }

  public void clear() {
    setHours(0);
    setMinutes(0);
    setSeconds(0);
    setFrames(0);
    setSamples(-1);
  }

  public void setHours(int value) {
    hours = value;
  }

  public int getHours() {
    return hours;
  }

  public void setMinutes(int value) {
    minutes = value;
  }

  public int getMinutes() {
    return minutes;
  }

  public void setSeconds(int value) {
    seconds = value;
  }

  public int getSeconds() {
    return seconds;
  }

  public void setFrames(int value) {
    frames = value;
  }

  public int getFrames() {
    return frames;
  }

  public void setFramesPerSecond(float value) {
    framesPerSecond = value;
  }

  public float getFramesPerSecond() {
    return framesPerSecond;
  }

  public void setSamples(long value) {
    samples = value;
  }

  public long getSamples() {
    return samples;
  }

  public void setSamplesPerSecond(float value) {
    samplesPerSecond = value;
  }

  public float getSamplesPerSecond() {
    return samplesPerSecond;
  }

  public boolean useSamples() {
    return useSamples;
  }

  /**
   * If true, samples per second will be used in computing fractions of frames. If false, frames is
   * the smallest unit.
   */
  public void setUseSamples(boolean b) {
    useSamples = b;
  }

  /** Sets the object based on a string in the form HH:MM:SS:FF */
  public void setCode(String timecode) throws Timecode.TimecodeException {
    clear();
    setHours(getToken(timecode, 0));
    setMinutes(getToken(timecode, 1));
    setSeconds(getToken(timecode, 2));
    setFrames(getToken(timecode, 3));
    if (useSamples()) {
      try {
        setSamples(getToken(timecode, 4));
        // If we got here, we're parsing a AES31-formatted string... Set our
        // frames per sec to 30 instead of 75
        setFramesPerSecond(30);
      } catch (Timecode.TimecodeException ignored) {
      } // If this fails, it just means the code didn't have samples appended
    }
    // fix badly formed values : 00:00:60:00 will become 00:01:00:00, etc.
    normalize();
  }

  /**
   * Sets the object up based on seconds... Will be an approximation as the code will default to
   * 1/75 second accuracy or that set by <code>setFramesPerSecond()</code>.)
   */
  public void setCode(float seconds) {
    setCode((double) seconds);
  }

  @SuppressWarnings("all")
  public void setCode(double seconds) {
    clear();
    setSamples((long) (seconds * (double) getSamplesPerSecond()));
    normalize();
  }

  /**
   * Set the code based on frames, framesPerSecond defaults to 75. For example, setCode(100) would
   * be '00:00:01:25'
   */
  public void setCode(int frames) {
    clear();
    setFrames(frames);
    normalize();
  }

  /**
   * Goes through the system and sanitizes badly formed values : 00:00:60:00 will become
   * 00:01:00:00, etc.
   */
  @SuppressWarnings("all")
  public void normalize() {
    // Start on samples
    int samplesPerFrame = (int) ((float) getSamplesPerSecond() / (float) getFramesPerSecond());
    setFrames(getFrames() + (int) (getSamples() * (1.0f / samplesPerFrame)));
    setSamples(getSamples() % samplesPerFrame);

    setSeconds(getSeconds() + (int) ((float) getFrames() / getFramesPerSecond()));
    setFrames((int) (getFrames() % getFramesPerSecond()));

    while (getSeconds() >= 60) {
      setSeconds(getSeconds() - 60);
      setMinutes(getMinutes() + 1);
    }
    while (getMinutes() >= 60) {
      setMinutes(getMinutes() - 60);
      setHours(getHours() + 1);
    }
  }

  public String getCode() {
    String outString = "";
    outString += (getHours() < 10 ? "0" + getHours() : "" + getHours()) + ":";
    outString += (getMinutes() < 10 ? "0" + getMinutes() : "" + getMinutes()) + ":";
    outString += (getSeconds() < 10 ? "0" + getSeconds() : "" + getSeconds()) + ":";
    outString += (getFrames() < 10 ? "0" + getFrames() : "" + getFrames());
    return outString;
  }

  public String getCodeWithSamples() {
    String outString = "";
    outString += (getHours() < 10 ? "0" + getHours() : "" + getHours()) + "|";
    outString += (getMinutes() < 10 ? "0" + getMinutes() : "" + getMinutes()) + "|";
    outString += (getSeconds() < 10 ? "0" + getSeconds() : "" + getSeconds()) + "|";
    outString += (getFrames() < 10 ? "0" + getFrames() : "" + getFrames()) + "|";
    if (getSamples() < 0) {
      outString += "0000";
    } else {
      String _samples = "000" + getSamples();
      outString += _samples.substring(_samples.length() - 4);
    }
    return outString;
  }

  public float toSeconds() {
    return (float) toDoubleSeconds();
  }

  public double toDoubleSeconds() {
    double value = 0;
    value += getHours() * 3600;
    value += getMinutes() * 60;
    value += getSeconds();
    value += getFrames() * (1.0 / getFramesPerSecond());
    if (useSamples() && getSamples() > -1)
      value += getSamples() * (1.0 / getSamplesPerSecond());
    return value;
  }

  /**
   * @return the total number of frames this object represents, so for a 1 second ntsc video timecode,
   *         this would return 29.97.
   */
  public double toFrames() {
    double value = 0;
    value += getHours() * 3600 * getFramesPerSecond();
    value += getMinutes() * 60 * getFramesPerSecond();
    value += getSeconds() * getFramesPerSecond();
    value += getFrames();
    if (getSamples() > -1)
      value += getSamples() / (getSamplesPerSecond() / getFramesPerSecond());
    return value;
  }

  /**
   * @return the total number of samples this object represents, so for a 1 second timecode, this would
   *         return 44100.
   */
  public long toSamples() {
    long value = 0;
    if (getSamples() > -1)
      value += getSamples();
    value += getFrames() * (getSamplesPerSecond() / getFramesPerSecond());
    value += getSeconds() * getSamplesPerSecond();
    value += getMinutes() * 60 * getSamplesPerSecond();
    value += getHours() * 60 * 60 * getSamplesPerSecond();
    return value;
  }

  public String toString() {
    return getCode();
  }

  /**
   * Hours are added to the minutes and minutes are added to the seconds value so seconds could be
   * larger than 60. Units smaller than seconds are ignored completely.
   * 
   * @return the total time in seconds.
   */
  public int toIntSeconds() {
    int _minutes = getHours() * 60;
    _minutes += getMinutes();
    int _seconds = _minutes * 60;
    _seconds += getSeconds();
    return _seconds;
  }

  /**
   * Hours are added to the minutes value so minutes could be larger than 60. Units smaller than
   * seconds are ignored completely. For example the HH:MM:SS:FF string "01:03:05:07" would return
   * "63:05"
   * 
   * @param delimiter
   * @return the total time in mm:ss format where ":" is specified by the delimiter parameter.
   */
  public String toMMSS(String delimiter) {
    String outString = "";
    int _minutes = getHours() * 60;
    _minutes += getMinutes();
    outString += (_minutes < 10 ? "0" + _minutes : "" + _minutes) + delimiter;
    outString += (getSeconds() < 10 ? "0" + getSeconds() : "" + getSeconds());
    return outString;
  }

  /**
   * Uses the default ":" delimiter
   * 
   * @return the code as "MM:SS"
   */
  public String toMMSS() {
    return toMMSS(":");
  }

  /**
   * Uses the default ":" delimiter
   * 
   * @return the code as "HH:MM:SS"
   */
  public String toHHMMSS() {
    return toHHMMSS(":");
  }

  /**
   * Units smaller than seconds are ignored completely. For example the HH:MM:SS:FF string
   * "01:03:05:07" would return "01:03:05"
   * 
   * @param delimiter
   * @return the total time in hh:mm:ss format where ":" is specified by the delimiter parameter.
   */
  public String toHHMMSS(String delimiter) {
    String outString = "";
    outString += (getHours() < 10 ? "0" + getHours() : "" + getHours()) + delimiter;
    outString += (getMinutes() < 10 ? "0" + getMinutes() : "" + getMinutes()) + delimiter;
    outString += (getSeconds() < 10 ? "0" + getSeconds() : "" + getSeconds());
    return outString;
  }

  /**
   * Breaks a string on any non-numeric character and returns the index token, zero indexed
   */
  private int getToken(String inString, int index) throws Timecode.TimecodeException {
    inString = inString.trim();
    String valid = "0123456789";
    String token = "";
    int count = 0;
    for (int i = 0; i < inString.length(); i++) {
      char current = inString.charAt(i);
      if (valid.indexOf(current) > -1) {
        token += current;
      } else {
        count++;
        if (count > index)
          break; // Found the token.
        token = ""; // Start reading the next token
      }
    }
    if (count < index || token.equals(""))
      throw new Timecode.TimecodeException("Malformed timecode '" + inString + "', can't get index=" + index);
    try {
      return Integer.parseInt(token);
    } catch (NumberFormatException ex) {
      throw new Timecode.TimecodeException("Malformed timecode '" + inString + "', '" + token + "' is not an integer");
    }
  }

  public boolean equals(Object obj) {
    return this.getCode().equals(obj.toString());
  }

  public class TimecodeException extends Exception {
    private static final long serialVersionUID = 1L;

    public TimecodeException(String message) {
      super(message);
    }
  }

  public static class Type {
    /** 29.97 frames/sec, 29,970 samples/sec */
    public static final Type TYPE_VIDEO_NTSC = new Type(29.970F);
    /** 25 frames/sec, 25,000 samples/sec */
    public static final Type TYPE_VIDEO_PAL = new Type(25.000F);
    /** 24 frames/sec, 24,000 samples/sec */
    public static final Type TYPE_VIDEO_FILM = new Type(24.000F);
    /** 75 frames/sec, 44,100 samples/sec */
    public static final Type TYPE_AUDIO_CD = new Type(75.000F, 44100.000F);

    private float framesPerSecond;
    private float samplesPerSecond;
    private boolean usesSamples = false;

    private Type(float framesPerSecond, float samplesPerSecond) {
      this.framesPerSecond = framesPerSecond;
      this.samplesPerSecond = samplesPerSecond;
      this.usesSamples = true;
    }

    private Type(float framesPerSecond) {
      this.framesPerSecond = framesPerSecond;
      this.samplesPerSecond = framesPerSecond * 1000;
    }

    public float getFramesPerSecond() {
      return framesPerSecond;
    }

    public float getSamplesPerSecond() {
      return samplesPerSecond;
    }

    public boolean usesSamples() {
      return usesSamples;
    }

    /**
     * Takes framesPerSecond and attempts to map it to one of our predefined TYPE_ objects. For
     * instance, getInstance(29.970F) would return TYPE_VIDEO_NTSC.
     * 
     * @throws NumberFormatException
     *           If this method is unable tomatch the fps to a constance object.
     */
    public static Type getInstance(float framesPerSecond) throws NumberFormatException {
      float maxDelta = .031f;
      if (Math.abs(framesPerSecond - TYPE_VIDEO_NTSC.getFramesPerSecond()) < maxDelta)
        return TYPE_VIDEO_NTSC;
      if (Math.abs(framesPerSecond - TYPE_VIDEO_PAL.getFramesPerSecond()) < maxDelta)
        return TYPE_VIDEO_PAL;
      if (Math.abs(framesPerSecond - TYPE_VIDEO_FILM.getFramesPerSecond()) < maxDelta)
        return TYPE_VIDEO_FILM;
      if (Math.abs(framesPerSecond - TYPE_AUDIO_CD.getFramesPerSecond()) < maxDelta)
        return TYPE_AUDIO_CD;
      throw new NumberFormatException("Cannot map " + framesPerSecond + " frames/sec to a defined timecode format.");
    }
  }

}
