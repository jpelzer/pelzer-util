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
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import junit.framework.TestCase;

import com.pelzer.util.Logging;
import com.pelzer.util.XMLPrettyPrintOutputStream;

public class PrettyPrintTest extends TestCase {
  private Logging.Logger logger = Logging.getLogger(this);

  public PrettyPrintTest(String name) {
    super(name);
  }

  public void testStaticPretty() {
    logger.debug("\n" + XMLPrettyPrintOutputStream.getPrettyPrinted("<foo><dee><bar/><blah>Text</blah></dee></foo>"));
    logger.debug("\n" + XMLPrettyPrintOutputStream.getPrettyPrinted("I'm a test."));
    logger.debug("\n"
        + XMLPrettyPrintOutputStream
            .getPrettyPrinted("<foo>\n<bar this=\"that\"\nthat=\"this\"><dee><doo>dope</doo><ddd></ddd><crud><foam>white</foam></crud></dee>\n\n\n</bar><mac>I'm\nan\nexample</mac></foo>"));
  }

  /**
   * Not a real test, just runs and tries to load that file, which is
   * transient. You can point it elsewhere if you want.
   */
  public void xtestStreaming() {
    File file = new File("c:/metadata.xml");
    if (file.exists()) {
      ByteArrayOutputStream prettyxml = new ByteArrayOutputStream();
      XMLPrettyPrintOutputStream out = new XMLPrettyPrintOutputStream(prettyxml);
      try {
        FileInputStream in = new FileInputStream(file);
        int read;
        while ((read = in.read()) > -1)
          out.write(read);
        out.close();
        in.close();
      } catch (IOException ex) {
        logger.error("IOException reading file...", ex);
      }
      logger.debug("\n" + prettyxml.toString());
    }
  }
}