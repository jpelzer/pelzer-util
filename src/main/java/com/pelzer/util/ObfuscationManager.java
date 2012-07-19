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
 * Some people might have named this thing the EncryptionManager or something like that, but since
 * this really just slows people down from figuring out passwords and such, it's just obfuscation.
 */
public class ObfuscationManager {
  private static final String SUPER_SECRET_MANGLE_STRING = "0hMyG05hPL3@53D0n'TCr@cKTH15.!!!!111oneoneone";

  private ObfuscationManager() {
  }

  /** Takes each argument and outputs the obfuscated version of it. */
  public static void main(String[] args) {
    for (int i = 0; i < args.length; i++)
      System.out.println("'" + args[i] + "' becomes '" + obfuscate(args[i]) + "'");
  }

  /**
   * Takes a string and returns an obfuscated string with no more than 2x a many characters as the
   * original. Only works in the DEV environment.
   */
  public static String obfuscate(String plaintext) {
    String blargtext = StringMan.encodeBytesToString(mangleArray(plaintext.getBytes()));
    blargtext = StringMan.compressHexString(blargtext);
    return blargtext;
  }

  /** Opposite of {@link #obfuscate(String)} */
  public static String clarify(String blargtext) {
    if (blargtext == null)
      return null;
    byte blargBytes[] = decodeStringToBytes(blargtext);
    String plaintext = new String(mangleArray(blargBytes));
    return plaintext;
  }

  /** Inlined from StringManipulator due to static init circular references, called by clarify */
  private static byte[] decodeStringToBytes(String hex) {
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

  /** Inlined from StringManipulator due to static init circular references, called by clarify */
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

  /** Inlined from StringManipulator due to static init circular references, called by clarify */
  private static String decompressHexString(String in) {
    StringBuffer out = new StringBuffer();
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
   * Takes a given byte array and mangles it by XOR'ing it with a secret garbage hash. It is
   * reversible, so calling mangleArray(mangleArray(blah)) will return the original array.
   * 
   * @return the mangled array (Same size as the input array)
   */
  private static byte[] mangleArray(byte plainBytes[]) {
    if (plainBytes == null)
      return null;
    byte mangled[] = new byte[plainBytes.length];
    byte hash[] = SUPER_SECRET_MANGLE_STRING.getBytes();
    for (int i = 0; i < plainBytes.length; i++) {
      byte current = (byte) (plainBytes[i] ^ hash[i % hash.length]);
      mangled[i] = current;
    }
    return mangled;
  }

}
