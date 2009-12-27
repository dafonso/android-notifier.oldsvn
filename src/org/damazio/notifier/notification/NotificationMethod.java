package org.damazio.notifier.notification;

public interface NotificationMethod {

  /**
   * Sends a notification to the desktop application.
   * @param notification TODO
   */
  void sendNotification(Notification notification);

}
