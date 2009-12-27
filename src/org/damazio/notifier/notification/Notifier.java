package org.damazio.notifier.notification;

import java.util.Set;

import org.damazio.notifier.NotifierPreferences;

import android.content.Context;
import android.util.Log;

public class Notifier {

  private final Set<NotificationMethod> allMethods;
  private final NotifierPreferences preferences;

  public Notifier(Context context, NotifierPreferences preferences) {
    this.preferences = preferences;
    allMethods = NotificationMethods.getAllValidMethods(context, preferences);
  }

  public synchronized void sendNotification(Notification notification) {
    if (!isNotificationEnabled(notification)) {
      return;
    }

    Log.d("RemoteNotifier", "Sending notification: " + notification);
    for (NotificationMethod method : allMethods) {
      method.sendNotification(notification);
    }
  }

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
