package org.damazio.notifier.notification;

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
 *
 * @author rdamazio
 */
public class Encryption {

  private static final String ENCRYPTION_KEY_TYPE = "AES";
  private static final String ENCRYPTION_ALGORITHM = "AES/CBC/PKCS7Padding";

  private final SecretKeySpec keySpec;
  private final byte[] iv;

  public Encryption(String passphrase) {
    byte[] passPhraseBytes;
    try {
      passPhraseBytes = passphrase.getBytes("UTF8");
    } catch (UnsupportedEncodingException e) {
      throw new IllegalArgumentException(e);
    }

    byte[] keyBytes = doMD5(passPhraseBytes);
    iv = doMD5(keyBytes);
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

  private byte[] doMD5(byte[] data) {
    try {
      MessageDigest md = MessageDigest.getInstance("MD5");
      md.update(data);
      return md.digest();
    } catch (NoSuchAlgorithmException e) {
      Log.e(NotifierConstants.LOG_TAG, "Algorithm not available", e);
      return null;
    }
  }
}
