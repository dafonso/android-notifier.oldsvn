package org.damazio.notifier.notification;

/**
 * Interface that defines a method of sending notifications.
 *
 * @author rdamazio
 */
public interface NotificationMethod {

  /**
   * Sends a notification to the desktop application.
   * @param notification the notification to send
   */
  void sendNotification(Notification notification);

  /**
   * @return the name of this notification method
   */
  String getName();
}
