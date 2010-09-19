package org.damazio.notifier.notification;

import java.util.Collections;

/**
 * Notification method for sending notifications over USB.
 *
 * @author rdamazio
 */
class UsbNotificationMethod implements NotificationMethod {

  public void sendNotification(Notification notification, Object target,
      NotificationCallback callback) {
    // TODO(rdamazio): Implement
    callback.notificationDone(notification, target, null);
  }

  public String getName() {
    return "usb";
  }

  public boolean isEnabled() {
    return false;
  }

  @Override
  public Iterable<String> getTargets() {
    return Collections.singletonList("usb");
  }
}
