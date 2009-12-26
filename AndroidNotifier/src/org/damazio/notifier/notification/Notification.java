package org.damazio.notifier.notification;

import android.content.Context;

public class Notification {

  private final NotificationType type;
  private final String contents;
  private final Context context;

  public Notification(Context context, NotificationType type, String contents) {
    this.context = context;
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
    messageBuilder.append(DeviceIdProvider.getDeviceId(context));
    messageBuilder.append('/');
    messageBuilder.append(type);
    if (contents != null) {
      messageBuilder.append("/");
      messageBuilder.append(contents);
    }
    return messageBuilder.toString();
  }
}
