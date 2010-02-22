package org.damazio.notifier.notification;

/**
 * Interface that defines a method of sending notifications.
 *
 * @author rdamazio
 */
interface NotificationMethod {
  
  /**
   * Callback to be called after a notification is sent, or its sending
   * fails permanently.
   */
  interface NotificationCallback {
    /**
     * Indicates that the given notification was sent successfully.
     */
    void notificationSent(Notification notification);

    /**
     * Indicates that the given notification could not be sent.
     *
     * @param reason the exception that caused the failure, if there was one
     */
    void notificationFailed(Notification notification, Throwable reason);
  }

  /**
   * Sends a notification to the desktop application.
   * This should never be called if {@link #isEnabled} returns false.
   *
   * @param notification the notification to send
   * @param callback callback which is called
   */
  void sendNotification(Notification notification, NotificationCallback callback);

  /**
   * @return the name of this notification method
   */
  String getName();

  /**
   * @return whether this method is enabled and should be called
   */
  boolean isEnabled();
}
