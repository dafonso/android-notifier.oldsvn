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
     * Indicates that the given notification's sending is done (either successfully or with a
     * failure).
     *
     * @param failureReason the exception that caused the failure, if there was one
     */
    void notificationDone(Notification notification, String target, Throwable failureReason);
  }

  /**
   * Sends a notification to the desktop application.
   * This should never be called if {@link #isEnabled} returns false.
   *
   * @param notification the notification to send
   * @param callback callback which is called
   */
  void sendNotification(Notification notification, String target, NotificationCallback callback);

  /**
   * Returns a set of targets that notifications should be sent to.
   * {@link #sendNotification} will be called once (in parallel) for each target returned.
   */
  Iterable<String> getTargets();

  /**
   * @return the name of this notification method
   */
  String getName();

  /**
   * @return whether this method is enabled and should be called
   */
  boolean isEnabled();
}
