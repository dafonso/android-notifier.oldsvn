package org.damazio.notifier.notification;

import java.util.HashSet;
import java.util.Set;

import org.damazio.notifier.NotifierPreferences;

import android.content.Context;
import android.os.Build;

/**
 * Factory for notification methods.
 *
 * @author rdamazio
 */
public class NotificationMethods {
  private NotificationMethods() { }

  /**
   * Create and return a set of all valid notification methods for the current
   * environment.
   *
   * @param context the context to get information from
   * @param preferences the preferences for the methods to use
   * @return the set of notification methods
   */
  public static Set<NotificationMethod> getAllValidMethods(
      Context context, NotifierPreferences preferences) {
    HashSet<NotificationMethod> methods = new HashSet<NotificationMethod>();

    // Methods supported in all versions
    methods.add(new WifiNotificationMethod(context, preferences));
    methods.add(new UsbNotificationMethod());

    // Methods supported only in 2.0 and above
    if (isBluetoothMethodSupported()) {
      methods.add(new BluetoothNotificationMethod(preferences));
    }

    return methods;
  }

  /**
   * @return whether the bluetooth method is supported on this device
   */
  public static boolean isBluetoothMethodSupported() {
    return Build.VERSION.SDK_INT >= 5;
  }
}
