/*
 * Copyright 2010 Rodrigo Damazio <rodrigo@damazio.org>
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * 
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR
 * IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 * IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
 * NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
 * THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.damazio.notifier.notification;

import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;

/**
 * Data object which represents a notification.
 *
 * @author rdamazio
 */
public class Notification implements Parcelable {

  private static final String PROTOCOL_VERSION = "v2";
  private final String deviceId;
  private final String notificationId;
  private final NotificationType type;
  private final String data;
  private final String description;

  private Notification(String deviceId, String notificationId, NotificationType type,
      String data, String description) {
    this.deviceId = deviceId;
    this.notificationId = notificationId;
    this.type = type;
    this.data = data;
    this.description = description;
  }

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

  @Override
  public int describeContents() {
    return 0;
  }

  @Override
  public void writeToParcel(Parcel dest, int flags) {
    // By saving the timestamp, we ensure device ID and notification ID are
    // consistently rebuilt
    dest.writeString(deviceId);
    dest.writeString(notificationId);
    dest.writeInt(type.ordinal());
    dest.writeString(data);
    dest.writeString(description);
  }

  public static final Creator<Notification> CREATOR = new Creator<Notification>() {
    @Override
    public Notification[] newArray(int size) {
      return new Notification[size];
    }

    @Override
    public Notification createFromParcel(Parcel source) {
      String deviceId = source.readString();
      String notificationId = source.readString();
      NotificationType type = NotificationType.values()[source.readInt()];
      String data = source.readString();
      String description = source.readString();
      return new Notification(deviceId, notificationId, type, data, description);
    }
  };
}
