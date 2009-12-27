package org.damazio.notifier.notification;

import java.util.Set;

import org.damazio.notifier.NotifierPreferences;

import android.content.Context;
import android.util.Log;

public class Notifier {

  private final Set<NotificationMethod> allMethods;

  public Notifier(Context context, NotifierPreferences preferences) {
    allMethods = NotificationMethods.getAllValidMethods(context, preferences);
  }

  public synchronized void sendNotification(Notification notification) {
    Log.d("RemoteNotifier", "Sending notification: " + notification);

    for (NotificationMethod method : allMethods) {
      method.sendNotification(notification);
    }
  }
}
