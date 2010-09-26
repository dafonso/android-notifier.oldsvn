/*
 * Copyright 2010 Rodrigo Damazio <rodrigo@damazio.org>
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * 
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR
 * IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 * IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
 * NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
 * THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.damazio.notifier.util;

import java.io.UnsupportedEncodingException;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import org.damazio.notifier.NotifierConstants;

import android.util.Log;

/**
 * Helper class for encrypting and decrypting payloads using arbitrary string passphrases.
 * This requires converting the passphrase into a byte array key.
 * This class also includes utilities for encoding that byte array in a string-safe way
 * for storage.
 *
 * @author rdamazio
 */
public class Encryption {

  private static final String ENCRYPTION_KEY_TYPE = "AES";
  private static final String ENCRYPTION_ALGORITHM = "AES/CBC/PKCS7Padding";

  private final SecretKeySpec keySpec;
  private final byte[] iv;

  /**
   * Converts a user-entered pass phrase into a hashed binary value which is
   * used as the encryption key.
   * @param hashAlgorithm 
   */
  public static byte[] passPhraseToKey(String passphrase, String hashAlgorithm, int numHashes) {
    if (numHashes < 1) {
      throw new IllegalArgumentException("Need a positive hash count");
    }

    byte[] passPhraseBytes;
    try {
      passPhraseBytes = passphrase.getBytes("UTF8");
    } catch (UnsupportedEncodingException e) {
      throw new IllegalArgumentException(e);
    }

    // Hash it multiple times to keep the paranoid people happy :)
    byte[] keyBytes = passPhraseBytes;
    for (int i = 0; i < numHashes; i++) {
      keyBytes = doDigest(keyBytes, hashAlgorithm);
    }

    return keyBytes;
  }

  public Encryption(byte[] keyBytes) {
    // Use an MD5 to generate an arbitrary initialization vector
    iv = doDigest(keyBytes, "MD5");
    keySpec = new SecretKeySpec(keyBytes, ENCRYPTION_KEY_TYPE);
  }

  public byte[] encrypt(byte[] unencrypted) throws GeneralSecurityException {
    return doCipher(unencrypted, Cipher.ENCRYPT_MODE);
  }

  public byte[] decrypt(byte[] encrypted) throws GeneralSecurityException {
    return doCipher(encrypted, Cipher.DECRYPT_MODE);
  }

  private byte[] doCipher(byte[] original, int mode) throws GeneralSecurityException {
    Cipher cipher = Cipher.getInstance(ENCRYPTION_ALGORITHM);
    cipher.init(mode, keySpec, new IvParameterSpec(iv));
    return cipher.doFinal(original);
  }

  private static byte[] doDigest(byte[] data, String algorithm) {
    try {
      MessageDigest md = MessageDigest.getInstance(algorithm);
      md.update(data);
      return md.digest();
    } catch (NoSuchAlgorithmException e) {
      Log.e(NotifierConstants.LOG_TAG, "Algorithm not available", e);
      throw new IllegalArgumentException(e);
    }
  }
}
