package org.damazio.notifier.notification;

/**
 * Notification method for sending notifications over USB.
 *
 * @author rdamazio
 */
public class UsbNotificationMethod implements NotificationMethod {

  public void sendNotification(Notification notification) {
    // TODO(rdamazio): Implement
  }

  public String getName() {
    return "usb";
  }
}
