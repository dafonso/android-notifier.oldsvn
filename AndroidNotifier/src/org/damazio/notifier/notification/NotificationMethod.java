package org.damazio.notifier.notification;

public interface NotificationMethod {

  /**
   * @return whether this notification method is enabled in the preferences
   */
  boolean isEnabled();

  /**
   * Sends a notification to the desktop application.
   * @param notification TODO
   */
  void sendNotification(Notification notification);

}
