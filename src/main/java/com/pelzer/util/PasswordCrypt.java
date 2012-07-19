package com.pelzer.util;

import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

public class PasswordCrypt {
  
  /**
   * Converts the salt into a byte array using {@link #stringToSalt(String)} and
   * then calls {@link #computePasswordHash(String, byte[])}
   */
  public static String computePasswordHash(final String password, final String salt) {
    return computePasswordHash(password, stringToSalt(salt));
  }
  
  /**
   * Uses PBKDF2WithHmacSHA1 to hash the password using the given salt,
   * returning the has in encoded hexadecimal form. If the password is empty,
   * returns the salt.
   */
  public static String computePasswordHash(final String password, final byte[] salt) {
    if (StringMan.isEmpty(password))
      return (StringMan.encodeBytesToString(salt));
    final KeySpec spec = new PBEKeySpec(password.toCharArray(), salt, 2048, 160);
    SecretKeyFactory f;
    try {
      f = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
      final byte[] hash = f.generateSecret(spec).getEncoded();
      return StringMan.encodeBytesToString(hash);
    } catch (final NoSuchAlgorithmException ex) {
      throw new RuntimeException("Missing encryption algorithm:", ex);
    } catch (final InvalidKeySpecException ex) {
      throw new RuntimeException("Key spec is incorrect.", ex);
    }
  }
  
  private static final SecureRandom random = new SecureRandom();
  
  /**
   * Generates a random byte array to be used as a salt for
   * {@link #computePasswordHash(String, byte[])}
   */
  public static byte[] generateRandomSalt() {
    return stringToSalt(new BigInteger(130, random).toString(32));
  }
  
  /** Uses SHA-1 to hash the given string and returns the byte array. */
  public static byte[] stringToSalt(final String string) {
    try {
      final MessageDigest digest = MessageDigest.getInstance("SHA-1");
      digest.reset();
      return digest.digest(string.getBytes("UTF-8"));
    } catch (final NoSuchAlgorithmException ex) {
      throw new RuntimeException(ex);
    } catch (final UnsupportedEncodingException ex) {
      throw new RuntimeException(ex);
    }
  }
}
