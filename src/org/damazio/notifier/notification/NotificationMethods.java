package org.damazio.notifier.notification;

import java.util.HashSet;
import java.util.Set;

import org.damazio.notifier.NotifierPreferences;

import android.content.Context;
import android.os.Build;

public class NotificationMethods {
  private NotificationMethods() { }

  public static Set<NotificationMethod> getAllValidMethods(
      Context context, NotifierPreferences preferences) {
    HashSet<NotificationMethod> methods = new HashSet<NotificationMethod>();

    // Methods supported in all versions
    methods.add(new WifiNotificationMethod(context, preferences));
    methods.add(new UsbNotificationMethod());

    // Methods supported only in 2.0 and above
    int sdkVersion = Integer.parseInt(Build.VERSION.SDK);
    if (sdkVersion >= 5) {
      methods.add(new BluetoothNotificationMethod(preferences));
    }

    return methods;
  }
}
