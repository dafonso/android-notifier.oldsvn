package org.damazio.notifier.notification;

import android.content.Context;

public class Notification {

  private final String deviceId;
  private final long notificationId;
  private final NotificationType type;
  private final String contents;

  public Notification(Context context, NotificationType type, String contents) {
    this.deviceId = DeviceIdProvider.getDeviceId(context);
    this.notificationId = notificationIdFor(deviceId, System.currentTimeMillis(), type, contents);
    this.type = type;
    this.contents = contents;
  }

  public NotificationType getType() {
    return type;
  }

  public String getContents() {
    return contents;
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

  private static long notificationIdFor(String deviceId, long timestamp, NotificationType type,
      String contents) {
    long hashCode = deviceId.hashCode();
    hashCode = hashCode * 31 + timestamp;
    hashCode = hashCode * 31 + type.hashCode();
    if (contents != null) {
      hashCode = hashCode * 31 + contents.hashCode();
    }
    return hashCode;
  }
}
