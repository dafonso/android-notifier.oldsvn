package org.damazio.notifier.notification;

import java.util.Set;

import org.damazio.notifier.NotifierConstants;
import org.damazio.notifier.NotifierPreferences;

import android.content.Context;
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
  public synchronized void sendNotification(Notification notification) {
    if (!isNotificationEnabled(notification)) {
      return;
    }

    Log.d(NotifierConstants.LOG_TAG, "Sending notification: " + notification);
    for (NotificationMethod method : allMethods) {
      method.sendNotification(notification);
    }
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
      case PING:
        return true;
      default:
        return false;
    }
  }
}
