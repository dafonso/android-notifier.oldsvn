package org.damazio.notifier.notification;

import java.io.UnsupportedEncodingException;
import java.security.GeneralSecurityException;
import java.util.Set;

import org.damazio.notifier.NotifierConstants;
import org.damazio.notifier.NotifierPreferences;
import org.damazio.notifier.notification.NotificationMethod.NotificationCallback;
import org.damazio.notifier.util.Encryption;

import android.content.Context;
import android.os.Looper;
import android.util.Log;

/**
 * Notification manager, which distributes relevant notifications to all
 * notification methods.
 *
 * @author rdamazio
 */
public class Notifier {

  private static final byte DELIMITER_BYTE = 0;
  private final Set<NotificationMethod> allMethods;
  private final NotifierPreferences preferences;

  public Notifier(Context context, NotifierPreferences preferences) {
    this.preferences = preferences;
    allMethods = NotificationMethods.getAllValidMethods(context, preferences);
  }

  /**
   * Send a notification through all enable notification methods, if the
   * notification type is enabled.
   * If the notification type is not enabled, or there are no enabled
   * notification methods, this is a no-op.
   *
   * @param notification the notification to send
   */
  public void sendNotification(final Notification notification) {
    if (!isNotificationEnabled(notification)) {
      return;
    }

    // Serialize the notification
    Log.d(NotifierConstants.LOG_TAG, "Sending notification: " + notification);
    final byte[] payload = serializeNotification(notification);
    if (payload == null) {
      return;
    }

    // Ping notifications can only be sent from the UI
    final boolean isForeground = notification.getType() == NotificationType.PING;

    for (final NotificationMethod method : allMethods) {
      // Skip the method if disabled
      if (!method.isEnabled()) {
        continue;
      }

      Iterable<?> targets = method.getTargets();
      for (final Object target : targets) {
        // Start a new thread with a looper to send the notification in
        new Thread("Notification " + method.getName() + " for " + target) {
          public void run() {
            runNotificationThread(method, payload, target, isForeground);
          }
        }.start();
      }
    }
  }

  /**
   * Serializes the notification into a byte array, applying all necessary transformations.
   *
   * @param notification the notification to serialize
   * @return the serialized version
   */
  private byte[] serializeNotification(Notification notification) {
    byte[] payload;
    try {
      payload = notification.toString().getBytes("UTF8");
    } catch (UnsupportedEncodingException e) {
      Log.e(NotifierConstants.LOG_TAG, "Unable to serialize message", e);
      return null;
    }
    payload = addDelimiter(payload);

    // Encrypt the payload if requested
    payload = maybeEncrypt(payload);

    return payload;
  }

  /**
   * Encrypts the given payload if the configuration has requested it.
   *
   * @param payload the payload to encrypt
   * @return the encrypted payload
   */
  private byte[] maybeEncrypt(byte[] payload) {
    if (!preferences.isEncryptionEnabled()) {
      return payload;
    }

    byte[] encryptionKey = preferences.getEncryptionKey();
    if (encryptionKey == null) {
      Log.w(NotifierConstants.LOG_TAG, "No encryption key specified");
      return payload;
    }

    Encryption encryption = new Encryption(encryptionKey);
    try {
      return encryption.encrypt(payload);
    } catch (GeneralSecurityException e) {
      Log.e(NotifierConstants.LOG_TAG, "Unable to encrypt payload", e);
    }

    return payload;
  }

  /**
   * Adds a final delimiter to the message so that its end can be easily
   * detected.
   *
   * @param messageBytes the original message bytes
   * @return message bytes with the delimiter
   */
  private byte[] addDelimiter(byte[] messageBytes) {
    byte[] result = new byte[messageBytes.length  + 1];
    System.arraycopy(messageBytes, 0, result, 0, messageBytes.length);
    result[messageBytes.length] = DELIMITER_BYTE;
    return result;
  }

  /**
   * Sets up the current thread to send a notification (by starting a looper),
   * then send it.
   */
  private void runNotificationThread(NotificationMethod method, byte[] payload,
      Object target, boolean isForeground) {
    Looper.prepare();
    final Looper looper = Looper.myLooper();
    method.sendNotification(payload, target, new NotificationCallback() {
      public void notificationDone(Object target, Throwable failureReason) {
        looper.quit();
      }
    }, isForeground);
    Looper.loop();
  }

  /**
   * Tells whether the given notification should be sent.
   */
  private boolean isNotificationEnabled(Notification notification) {
    switch (notification.getType()) {
      case RING:
        return preferences.isRingEventEnabled();
      case SMS:
        return preferences.isSmsEventEnabled();
      case MMS:
        return preferences.isMmsEventEnabled();
      case BATTERY:
        return preferences.isBatteryEventEnabled();
      case VOICEMAIL:
        return preferences.isVoicemailEventEnabled();
      case PING:
        return true;
      case USER:
          return preferences.isUserEventEnabled();
      default:
        return false;
    }
  }
}
