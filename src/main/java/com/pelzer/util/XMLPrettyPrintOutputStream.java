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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;


/**
 * As you pass in non-pretty xml, this will write out 'pretty' xml as best it
 * can, but without validating the underlying XML in any way (as using the
 * xerces code would require). This class may result in munged XML if whitespace
 * was significant to it in some way. In general, this class reads an entire
 * line of the incoming text, then parses it when a newline or stream close
 * command comes in. If the line is empty (only whitespace) then the line is
 * dropped. If the line starts with a '&lt;' (after removing whitespace) then the
 * system indents that line based on its perceived current depth.
 */
public class XMLPrettyPrintOutputStream extends OutputStream {
  private OutputStream target;
  private static boolean disabled = false;

  public XMLPrettyPrintOutputStream(OutputStream target) {
    this.target = target;
  }

  private boolean dropWhiteSpaceOnlyLines = true;
  private int indentIncrement = 2;
  private char indentChar = ' ';

  private int indentLevel = 0;
  private StringBuffer currentLine = new StringBuffer();

  private void increaseIndent() {
    // debug.debug("Increment");
    indentLevel += indentIncrement;
  }

  private void decreaseIndent() {
    if (indentLevel - indentIncrement < 0)
      indentLevel = 0;
    else
      indentLevel -= indentIncrement;
    // debug.debug("Decrement " + indentLevel);
  }

  private String getIndentPrefix() {
    StringBuffer spaces = new StringBuffer(indentLevel);
    for (int i = 0; i < indentLevel; i++)
      spaces.append(indentChar);
    return spaces.toString();
  }

  private boolean byteIsALineTerminator(int b) {
    return b == '\n' || b == '\r';
  }

  @Override
  public void write(int b) throws IOException {
    if (disabled)
      target.write(b);
    else if (byteIsALineTerminator(b)) {
      writeLineToTarget(processLine(currentLine.toString()));
      currentLine = new StringBuffer();
    } else {
      currentLine.append((char) b);
    }
  }

  @Override
  public void write(byte[] b) throws IOException {
    write(b, 0, b.length);
  }

  @Override
  public void write(byte[] b, int off, int len) throws IOException {
    for (int i = off; i < len; i++)
      write(b[i]);
  }

  /** Adds a line return if the line is 1 or more chars long and writes it out. */
  private void writeLineToTarget(String line) throws IOException {
    for (int i = 0; i < line.length(); i++)
      target.write(line.charAt(i));
  }

  /**
   * Takes the line, processes it and returns the result, which may have been
   * split to multiple lines. Does not clear the currentLine variable.
   */
  private String processLine(String line) {
    if (dropWhiteSpaceOnlyLines && isLineOnlyWhiteSpace(line))
      return "";
    StringBuffer fixedLines = new StringBuffer();
    StringBuffer currLine = new StringBuffer();
    line = line.trim();

    boolean isEndTag = false;
    boolean inLeadingWhitespace = true;
    char lastChar = '\n';
    for (int i = 0; i < line.length(); i++) {
      char current = line.charAt(i);

      if (inLeadingWhitespace)
        if (isCharWhitespace(current))
          continue;
      inLeadingWhitespace = false;

      if (current == '<') {
        isEndTag = false;
      }
      if (current == '>' && lastChar == '/')
        isEndTag = true;

      if (current == '/' && lastChar == '<') {
        isEndTag = true;
      }

      currLine.append(current);
      boolean isEndOfLine = i + 1 == line.length();
      if (current == '>' || isEndOfLine) {
        if (isEndTag && lastChar != '/')
          decreaseIndent();
        if (isNextNonWhitespaceCharacterABracket(line, i + 1) || isEndOfLine) {
          boolean isNonNullEntry = (isEndOfLine || (i < line.length() + 3 && (line.charAt(i + 1) != '<' || line.charAt(i + 2) != '/')));
          if (!isNonNullEntry || (fixedLines.length() > 1 && fixedLines.charAt(fixedLines.length() - 1) == '\n'))
            fixedLines.append(getIndentPrefix());
          fixedLines.append(currLine);
          if (isEndTag || isNonNullEntry)
            fixedLines.append("\n");
          inLeadingWhitespace = true;
          currLine = new StringBuffer();
        } else {
          inLeadingWhitespace = false;
        }
        if (!isEndTag && current == '>' && lastChar != '/')
          increaseIndent();

        isEndTag = false;
      }
      lastChar = current;
    }

    return fixedLines.toString();
  }

  /**
   * Takes the given line and starting index and finds the first non-whitespace
   * character... If it is a bracket, it returns true, otherwise false.
   */
  boolean isNextNonWhitespaceCharacterABracket(String line, int startingIndex) {
    for (int i = startingIndex; i < line.length(); i++) {
      char current = line.charAt(i);
      if (current == '<')
        return true;
      if (!isCharWhitespace(current))
        return false;
    }
    return false;
  }

  private boolean isCharWhitespace(char character) {
    return character == '\n' || character == '\r' || character == '\t' || character == ' ';
  }

  boolean isLineOnlyWhiteSpace(String line) {
    return line.replaceAll("[\n\r\t ]", "").equals("");
  }

  /** Flushes the target */
  @Override
  public void flush() throws IOException {
    target.flush();
  }

  /** Closes the underlying target stream. */
  @Override
  public void close() throws IOException {
    writeLineToTarget(processLine(currentLine.toString()));
    target.close();
  }

  /**
   * Takes a given XML as a string and does a pretty print. Internally creates
   * streams, so if you already have a stream, it'll be more efficient to use
   * this class as it was designed. This is mostly used for testing.
   */
  public static String getPrettyPrinted(String xml) {
    ByteArrayOutputStream prettyxml = new ByteArrayOutputStream();
    XMLPrettyPrintOutputStream out = new XMLPrettyPrintOutputStream(prettyxml);
    try {
      for (int i = 0; i < xml.length(); i++)
        out.write(xml.charAt(i));
      out.close();
    } catch (IOException ex) {
      throw new RuntimeException("IOException while writing in-memory? WTF?", ex);
    }
    return prettyxml.toString();
  }

}
