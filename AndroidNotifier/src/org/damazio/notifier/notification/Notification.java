package org.damazio.notifier.notification;

import android.content.Context;

/**
 * Data object which represents a notification.
 *
 * @author rdamazio
 */
public class Notification {

  private static final String PROTOCOL_VERSION = "v2";
  private final String deviceId;
  private final String notificationId;
  private final NotificationType type;
  private final String data;
  private final String description;

  public Notification(Context context, NotificationType type, String data, String description) {
    this.deviceId = DeviceIdProvider.getDeviceId(context);
    this.notificationId =
        notificationIdFor(deviceId, System.currentTimeMillis(), type, data, description);
    this.type = type;
    this.data = data;
    this.description = description;
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
    messageBuilder.append(PROTOCOL_VERSION);
    messageBuilder.append('/');
    messageBuilder.append(deviceId);
    messageBuilder.append('/');
    messageBuilder.append(notificationId);
    messageBuilder.append('/');
    messageBuilder.append(type);
    messageBuilder.append('/');
    if (data != null) {
      messageBuilder.append(data);
    }
    messageBuilder.append("/");
    if (description != null) {
      messageBuilder.append(description);
    }
    return messageBuilder.toString();
  }

  /**
   * Builds a notification ID so that this notification is uniquely identified.
   *
   * @param deviceId the ID of this device
   * @param timestamp the timestamp when the notification was created
   * @param type the type of notification
   * @param data the machine-readable data for the notification
   * @param description the human-readable description of the notification
   * @return a unique notification ID
   */
  private static String notificationIdFor(String deviceId, long timestamp, NotificationType type,
      String data, String description) {
    long hashCode = deviceId.hashCode();
    hashCode = hashCode * 31 + timestamp;
    hashCode = hashCode * 31 + type.hashCode();
    if (data != null) {
      hashCode = hashCode * 31 + data.hashCode();
    }
    if (description != null) {
      hashCode = hashCode * 31 + description.hashCode();
    }
    return Long.toHexString(hashCode);
  }
}
