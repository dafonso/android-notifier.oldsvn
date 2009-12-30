package org.damazio.notifier.notification;

import android.content.Context;

/**
 * Data object which represents a notification.
 *
 * @author rdamazio
 */
public class Notification {

  private final String deviceId;
  private final String notificationId;
  private final NotificationType type;
  private final String contents;

  public Notification(Context context, NotificationType type, String contents) {
    this.deviceId = DeviceIdProvider.getDeviceId(context);
    this.notificationId = notificationIdFor(deviceId, System.currentTimeMillis(), type, contents);
    this.type = type;
    this.contents = contents;
  }

  /**
   * @return the type of notification
   */
  public NotificationType getType() {
    return type;
  }

  @Override
  public String toString() {
    StringBuilder messageBuilder = new StringBuilder();
    messageBuilder.append(deviceId);
    messageBuilder.append('/');
    messageBuilder.append(notificationId);
    messageBuilder.append('/');
    messageBuilder.append(type);
    messageBuilder.append("/");
    if (contents != null) {
      messageBuilder.append(contents);
    }
    return messageBuilder.toString();
  }

  /**
   * Builds a notification ID so that this notification is uniquely identified.
   *
   * @param deviceId the ID of this device
   * @param timestamp the timestamp when the notification was created
   * @param type the type of notification
   * @param contents the contents of the notification
   * @return a unique notification ID
   */
  private static String notificationIdFor(String deviceId, long timestamp, NotificationType type,
      String contents) {
    long hashCode = deviceId.hashCode();
    hashCode = hashCode * 31 + timestamp;
    hashCode = hashCode * 31 + type.hashCode();
    if (contents != null) {
      hashCode = hashCode * 31 + contents.hashCode();
    }
    return Long.toHexString(hashCode);
  }
}
