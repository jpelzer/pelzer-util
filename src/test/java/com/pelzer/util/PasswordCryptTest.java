package com.pelzer.util;

import junit.framework.TestCase;

public class PasswordCryptTest extends TestCase {
  public void testGetPasswordHash() {
    final String salt = "bar";
    
    assertEquals("86F7E437FAA5A7FCE15D1DDCB9EAEAEA377667B8", PasswordCrypt.computePasswordHash("", "a"));
    assertEquals("10A3BDB1D33AA289C3CBE23006CD85CD24FA20A0", PasswordCrypt.computePasswordHash("test password", salt));
    
  }
  
  public void testGetPasswordReturnsSaltOnEmptyPassword() {
    assertEquals("86F7E437FAA5A7FCE15D1DDCB9EAEAEA377667B8", PasswordCrypt.computePasswordHash("", "a"));
  }
  
  public void testStringToSalt() {
    assertEquals("DA39A3EE5E6B4B0D3255BFEF95601890AFD80709", StringMan.encodeBytesToString(PasswordCrypt.stringToSalt("")));
    assertEquals("86F7E437FAA5A7FCE15D1DDCB9EAEAEA377667B8", StringMan.encodeBytesToString(PasswordCrypt.stringToSalt("a")));
  }
}
