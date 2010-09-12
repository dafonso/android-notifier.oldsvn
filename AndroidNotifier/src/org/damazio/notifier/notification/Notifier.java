package org.damazio.notifier.notification;

import java.util.Set;

import org.damazio.notifier.NotifierConstants;
import org.damazio.notifier.NotifierPreferences;
import org.damazio.notifier.notification.NotificationMethod.NotificationCallback;

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

    Log.d(NotifierConstants.LOG_TAG, "Sending notification: " + notification);
    for (final NotificationMethod method : allMethods) {
      // Skip the method if disabled
      if (!method.isEnabled()) {
        continue;
      }

      Iterable<String> targets = method.getTargets();
      for (final String target : targets) {
        // Start a new thread with a looper to send the notification in
        new Thread("Notification " + method.getName() + " for " + target) {
          public void run() {
            runNotificationThread(method, notification, target);
          }
        }.start();
      }
    }
  }

  /**
   * Sets up the current thread to send a notification (by starting a looper),
   * then send it.
   */
  private void runNotificationThread(NotificationMethod method, Notification notification,
      String target) {
    Looper.prepare();
    final Looper looper = Looper.myLooper();
    method.sendNotification(notification, target, new NotificationCallback() {
      public void notificationDone(
          Notification notification, String target, Throwable failureReason) {
        looper.quit();
      }
    });
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
      default:
        return false;
    }
  }
}
