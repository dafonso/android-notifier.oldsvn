package org.damazio.notifier.notification;

/**
 * Enum for the type of notification.
 *
 * @author rdamazio
 */
public enum NotificationType {
  /** Phone ringing. */
  RING,
  /** SMS received. */
  SMS,
  /** MMS received. */
  MMS,
  /** Battery status change. */
  BATTERY,
  /** New voicemail */
  VOICEMAIL,
  /** Test notification. */
  PING,
  /** Third-party (user) message. */
  USER;
}
