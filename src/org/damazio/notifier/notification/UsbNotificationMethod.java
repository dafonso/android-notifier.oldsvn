package org.damazio.notifier.notification;

/**
 * Notification method for sending notifications over USB.
 *
 * @author rdamazio
 */
class UsbNotificationMethod implements NotificationMethod {

  public void sendNotification(Notification notification, NotificationCallback callback) {
    // TODO(rdamazio): Implement
    callback.notificationFailed(notification, null);
  }

  public String getName() {
    return "usb";
  }

  public boolean isEnabled() {
    return false;
  }
}
