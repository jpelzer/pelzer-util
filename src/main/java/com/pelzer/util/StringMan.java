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

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.Collator;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.StringTokenizer;

/**
 * Utility class for string manipulation.
 */
public class StringMan {
  /**
   * Returns an empty string if input is null, otherwise returns the original input string
   * untouched.
   * 
   * @param input
   *          String to be evaluated
   */
  public static String getNullAsEmptyString(String input) {
    return (input == null) ? "" : input;
  }

  /**
   * Returns the specified string value if input is null, otherwise returns the original input
   * string untouched.
   * 
   * @param input
   *          String to be evaluated
   * @param value
   *          String to substitute if input is null
   */
  public static String getNullAsValue(String input, String value) {
    return (input == null) ? value : input;
  }

  /** Replaces first occurence of a String with another string */
  public static String replaceFirst(String source, String find, String replace) {
    return fastReplaceFirst(source, find, replace);
  }

  /** @return true if s is null or all whitespace. */
  public static boolean isEmpty(String s) {
    return (s == null || "".equals(s.trim()));
  }

  /**
   * @return a substring up to the first occurence of '\n'. If '\n' is not found, returns the
   *         original string. If s is null, throws a NullPointerException
   */
  public static String getFirstLine(String s) {
    int j = -1;
    if (!isEmpty(s) && (j = s.indexOf('\n')) != -1) {
      return s.substring(0, j);
    }
    return s;
  }

  /**
   * @return what you'd see in the console by doing ex.printStackTrace()
   */
  public static String getStackTrace(Throwable ex) {
    StringWriter sw = new StringWriter();
    PrintWriter pw = new PrintWriter(sw);
    ex.printStackTrace(pw);
    try {
      sw.close();
    } catch (IOException ioex) {
    }
    return sw.getBuffer().toString();
  }
  
  /**
   * Returns true if the string is 'TRUE' or 'YES' or '1', case insensitive.
   * False for null, empty, etc.
   */
  public static boolean isStringTrue(String in) {
    if (in == null)
      return false;
    return in.equalsIgnoreCase("TRUE") || in.equalsIgnoreCase("YES") || in.equals("1");
  }

  /**
   * Chops a string off after a certain number of characters. If the string is shorter than the
   * length, then the string is returned untouched. nulls are returned as null.
   * 
   * @param source
   * @param length
   */
  public static String chop(String source, int length) {
    try {
      return source.substring(0, length);
    } catch (StringIndexOutOfBoundsException e) {
      return source;
    } catch (NullPointerException e) {
      return null;
    }
  }

  /**
   * Replaces all occurences of a String with another string, using the fastest method we could
   * devise. Runs about twice as fast as the java.util.regex replace method.
   */
  public static String replace(String haystack, String needle, String replacement) {
    if (haystack == null || needle == null || replacement == null || needle.length() == 0) {
      return haystack;
    }
    StringBuilder buf = new StringBuilder();
    int start = 0, end = 0;
    int needleLength = needle.length();
    while ((end = haystack.indexOf(needle, start)) != -1) {
      buf.append(haystack.substring(start, end)).append(replacement);
      start = end + needleLength;
    }
    if (start < haystack.length())
      buf.append(haystack.substring(start));
    return buf.toString();
  }

  /**
   * Same behavior as {@link #replace(String, String, String)} but cycles through the replacements,
   * so replace("xxx","x","1","2","3") would return "123". If you don't give enough replacements,
   * needles will be left in the resulting string. If you don't have enough instances of needle to
   * fit all replacements, no foul, you just won't see those replacements.
   */
  public static String replace(String haystack, String needle, String... replacements) {
    if (haystack == null || needle == null || replacements == null || replacements.length == 0 || needle.length() == 0) {
      return haystack;
    }
    StringBuilder buf = new StringBuilder();
    int start = 0, end = 0;
    int needleLength = needle.length();
    int replacementIndex = 0;
    while ((end = haystack.indexOf(needle, start)) != -1 && replacementIndex < replacements.length) {
      buf.append(haystack.substring(start, end)).append(replacements[replacementIndex++]);
      start = end + needleLength;
    }
    if (start < haystack.length())
      buf.append(haystack.substring(start));
    return buf.toString();
  }

  private static String fastReplaceFirst(String haystack, String needle, String replacement) {
    if (haystack == null || needle == null || replacement == null || needle.length() == 0) {
      return haystack;
    }
    StringBuilder buf = new StringBuilder(haystack.length());
    int start = 0, end = 0;
    if ((end = haystack.indexOf(needle, start)) != -1) {
      buf.append(haystack.substring(start, end)).append(replacement);
      start = end + needle.length();
    }
    buf.append(haystack.substring(start));
    return buf.toString();
  }

  /**
   * Makes a string xml-safe, converting &<>"' to their &token; equivalents, and converting all
   * other non-safe chars to their &#000; version.
   */
  public static String escapeForXML(String inString) {
    if (inString == null)
      return null;
    String outString = inString;
    outString = replace(outString, "&", "&amp;");
    outString = replace(outString, "<", "&lt;");
    outString = replace(outString, ">", "&gt;");
    outString = replace(outString, "\"", "&quot;");
    outString = replace(outString, "'", "&apos;");
    // We'll escape all chars not in this set (Safe)
    String validChars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz1234567890 &#;-_=+:,.?/";
    for (int i = 0; i < outString.length(); i++) {
      char c = outString.charAt(i);
      if (validChars.indexOf(c) == -1) {
        // Escape the character
        outString = replace(outString, "" + c, "&#" + ((int) c) + ";");
      }
    }
    return outString;
  }

  /**
   * Takes the inString and capitalizes every leter that occurs after a non-alpha character.<br>
   * "I like pizza" becomes "I Like Pizza"<br>
   * "DON'T EAT THAT" becomes "Don'T Eat That"
   */
  public static String toInitialCaps(String inString) {
    String lowerString = inString.toLowerCase();
    String outString = "";
    String alphaChars = "abcdefghijklmnopqrstuvwxyz";
    boolean doUpper = true;
    for (int i = 0; i < lowerString.length(); i++) {
      if (doUpper)
        outString += ("" + lowerString.charAt(i)).toUpperCase();
      else
        outString += lowerString.charAt(i);
      if (alphaChars.indexOf(lowerString.charAt(i)) >= 0)
        doUpper = false;
      if (lowerString.charAt(i) == ' ')
        doUpper = true;
    }
    return outString;
  }

  /** "foo" becomes "foo " */
  public static String padLeftJustified(String string, int length) {
    return padLeftJustified(string, length, " ");
  }

  /** @see #padLeftJustified(String, int) */
  public static String padLeftJustified(String string, int length, String padString) {
    for (int i = 0; i < length; i++)
      string = string + padString;
    return string.substring(0, length);
  }

  /** "foo" becomes " foo" */
  public static String padRightJustified(String string, int length) {
    return padRightJustified(string, length, " ");
  }

  public static String padRightJustified(String string, int length, String padString) {
    for (int i = 0; i < length; i++)
      string = padString + string;
    return string.substring(string.length() - length);
  }

  /**
   * Takes a string encoded in hex (00-FF), two chars per byte, and returns an array of bytes.
   * array.length will be hex.length/2 after hex has been run through {@link #decompressHexString}
   */
  public static byte[] decodeStringToBytes(String hex) {
    hex = decompressHexString(hex);
    int stringLength = hex.length();
    byte[] b = new byte[stringLength / 2];
    for (int i = 0, j = 0; i < stringLength; i += 2, j++) {
      int high = charToNibble(hex.charAt(i));
      int low = charToNibble(hex.charAt(i + 1));
      b[j] = (byte) ((high << 4) | low);
    }
    return b;
  }

  /** Strips all characters c satisfying Character.isWhitespace(c). */
  public static String stripWhitespace(String input) {
    StringBuilder output = new StringBuilder();
    for (int i = 0; i < input.length(); i++) {
      char current = input.charAt(i);
      if (!Character.isWhitespace(current))
        output.append(current);
    }
    return output.toString();
  }

  /**
   * Strips all non A-Z/0-9 characters from the input string. Spaces are left intact.
   */
  public static String stripNonAlphaNumericCharacters(String input) {
    StringBuilder output = new StringBuilder();
    for (int i = 0; i < input.length(); i++) {
      char current = input.charAt(i);
      if ("abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789 ".indexOf(current) >= 0)
        output.append(current);
    }
    return output.toString();
  }

  /**
   * Strips everything except A-Z a-z 0-9 @ . characters from the input string. Spaces are left
   * intact.
   */
  public static String stripNonEmailCharacters(String input) {
    StringBuilder output = new StringBuilder();
    for (int i = 0; i < input.length(); i++) {
      char current = input.charAt(i);
      if ("@._-abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789 ".indexOf(current) >= 0)
        output.append(current);
    }
    return output.toString();
  }

  private static int charToNibble(char c) {
    if ('0' <= c && c <= '9')
      return c - '0';
    else if ('a' <= c && c <= 'f')
      return c - 'a' + 0xa;
    else if ('A' <= c && c <= 'F')
      return c - 'A' + 0xa;
    else
      throw new IllegalArgumentException("Invalid hex character: " + c);
  }

  /**
   * Takes a byte array and returns a hex string (00-FF) encoded two chars per byte.
   */
  public static String encodeBytesToString(byte bytes[]) {
    StringBuilder string = new StringBuilder(bytes.length * 2);
    for (int i = 0; i < bytes.length; i++) {
      // look up high nibble char
      string.append(hexChar[(bytes[i] & 0xf0) >>> 4]);
      // look up low nibble char
      string.append(hexChar[bytes[i] & 0x0f]);
    }
    return string.toString();
  }

  // table to convert a nibble to a hex char.
  static char[] hexChar = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'};

  /**
   * Very simplistic compression method that encodes repeating characters into a smaller character,
   * so '0000000000' becomes '^0j' where the '^' escapes the next two characters, '0' is the
   * repeating character, and j encodes 10 repetitions.
   */
  public static String compressHexString(String inString) {
    String in = inString + "^"; // Add this onto the end since the last group of
    // characters is always dropped... This carat
    // will be dropped from the output string.
    StringBuilder out = new StringBuilder();
    char lastChar = ' ';
    int count = 0;
    for (int i = 0; i < in.length(); i++) {
      char thisChar = in.charAt(i);
      if (thisChar == lastChar && count < 35) { // Maybe we're starting a string
        // of repeating chars...
        count++;
      } else {
        if (count > 3) {
          out.append('^');
          out.append(lastChar);
          out.append("abcdefghijklmnopqrstuvwxyz01234567890".charAt(count));
        } else {
          for (int j = 0; j < count; j++)
            out.append(lastChar);
        }
        count = 1;
        lastChar = thisChar;
      }
    }
    String outString = out.toString();
    return outString;
  }

  /** Decompresses a string encoded by {@link #compressHexString} */
  public static String decompressHexString(String in) {
    StringBuilder out = new StringBuilder();
    for (int i = 0; i < in.length(); i++) {
      char thisChar = in.charAt(i);
      if (thisChar != '^') {
        out.append(thisChar);
      } else {
        thisChar = in.charAt(i + 1);
        int count = "abcdefghijklmnopqrstuvwxyz01234567890".indexOf(in.charAt(i + 2));
        for (int j = 0; j < count; j++)
          out.append(thisChar);
        i += 2;
      }
    }
    return out.toString();
  }

  /**
   * Converts a string with possible high-ascii values into their ascii7 equiv., for instance 'é'
   * maps to 'e'. All low ascii characters are left untouched. Any character above 255 is converted
   * to a space.
   */
  public static String getUS7ASCIIEquiv(String convertString) {
    if (convertString == null)
      return null;

    StringBuilder convertedString = new StringBuilder();
    String upperConvertString = convertString.toUpperCase();

    Collator collator = Collator.getInstance(Locale.US);
    collator.setStrength(Collator.PRIMARY);

    String chars = "abcdefghijklmnopqrstuvwxyz";
    for (int i = 0; i < convertString.length(); i++) {
      char currentChar = convertString.charAt(i);
      boolean isUppercase = currentChar == upperConvertString.charAt(i);
      char mappedChar = ' ';
      if (currentChar < 128) {
        // Within low-ascii, leave it alone.
        mappedChar = currentChar;
      } else if (currentChar < 256) {
        for (int j = 0; j < chars.length(); j++) {
          if (collator.compare(currentChar + "", chars.charAt(j) + "") == 0) {
            if (isUppercase)
              mappedChar = Character.toUpperCase(chars.charAt(j));
            else
              mappedChar = chars.charAt(j);
            break;
          }
        }
      } else {
        // Out of our mapping range. It just becomes a space
      }
      convertedString.append(mappedChar);
    }
    return convertedString.toString();
  }

  /**
   * Concats elements in array between <tt>fromIndex</tt>, inclusive, to <tt>toIndex</tt>,
   * inclusive, inserting <tt>separator</tt> between them.
   */
  public static String concat(String[] array, String separator, int fromIndex, int toIndex) {
    StringBuilder buf = new StringBuilder();
    for (int i = fromIndex; i <= toIndex; i++) {
      if (buf.length() > 0)
        buf.append(separator);
      buf.append(array[i]);
    }
    return buf.toString();
  }

  /**
   * Splits string into an array using StringTokenizer's default delimeter set which is *
   * <code>"&nbsp;&#92;t&#92;n&#92;r&#92;f"</code>: the space character, the tab character, the
   * newline character, the carriage-return character, and the form-feed character.
   * 
   * @see java.util.StringTokenizer
   */
  public static String[] tokenize(String s) {
    List<String> tokens = new ArrayList<String>();
    StringTokenizer toker = new StringTokenizer(s);
    while (toker.hasMoreTokens())
      tokens.add(toker.nextToken());
    return tokens.toArray(new String[tokens.size()]);
  }

  /**
   * Splits string into an array using the given delimiter
   * 
   * @see java.util.StringTokenizer
   */
  public static String[] tokenize(String s, String delimiter) {
    List<String> tokens = new ArrayList<String>();
    StringTokenizer toker = new StringTokenizer(s, delimiter);
    while (toker.hasMoreTokens())
      tokens.add(toker.nextToken());
    return tokens.toArray(new String[tokens.size()]);
  }

  /**
   * Converts a string array into a single string with values delimited by the specified delimiter.
   */
  public static String detokenize(String[] values, String delimiter) {
    return concat(values, delimiter, 0, values.length - 1);
  }

  /** null-safe string equality test. If both s1 and s2 are null, returns true. */
  public static boolean areStringsEqual(String s1, String s2) {
    return s1 != null && s1.equals(s2) || s1 == null && s2 == null;
  }

  /**
   * Takes an array of Strings with long values and returns an array of longs, or throws a
   * {@link NumberFormatException} if there's a problem.
   */
  public static long[] parseLongs(String[] array) {
    long[] result = new long[array.length];
    for (int i = 0; i < array.length; i++)
      result[i] = Long.parseLong(array[i]);
    return result;
  }

  /**
   * Takes an array of Strings with integer values and returns an array of ints, or throws a
   * {@link NumberFormatException} if there's a problem.
   */
  public static int[] parseInts(String[] array) {
    int[] result = new int[array.length];
    for (int i = 0; i < array.length; i++)
      result[i] = Integer.parseInt(array[i]);
    return result;
  }

  private StringMan() {
  }

}
