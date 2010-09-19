package org.damazio.notifier.service;

import org.damazio.notifier.NotifierConstants;
import org.damazio.notifier.notification.Notification;
import org.damazio.notifier.notification.NotificationType;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

/**
 * Receiver for user-defined notifications.
 *
 * @author Maarten 'MrSnowflake' Krijn
 */
class UserReceiver extends BroadcastReceiver {
  /**
   * The intent action to use when sending broadcasts.
   */
  static final String ACTION = "org.damazio.notifier.service.UserReceiver.USER_MESSAGE";

  /**
   * The intent extra to set for the notification's title.
   * Either this or {@link #EXTRA_DESCRIPTION} (or both) must be set.
   */
  static final String EXTRA_TITLE = "title";

  /**
   * The intent extra to set for the notification's description.
   * Either this or {@link #EXTRA_TITLE} (or both) must be set.
   */
  static final String EXTRA_DESCRIPTION = "description";

  private final NotificationService service;

  public UserReceiver(NotificationService service) {
    this.service = service;
  }

  @Override
  public void onReceive(Context context, Intent intent) {
    if (!intent.getAction().equals(ACTION)) {
      Log.e(NotifierConstants.LOG_TAG,
          "Wrong intent received by user receiver - " + intent.getAction());
      return;
    }

    String message = null;
    String title = null;

    // Try to read extras from intent
    Bundle extras = intent.getExtras();
    if (extras != null) {
      message = extras.getString(EXTRA_DESCRIPTION);
      title = extras.getString(EXTRA_TITLE);
    }

    if (message != null || title != null) {
      Log.d(NotifierConstants.LOG_TAG, "Notifying of user message");
      Notification notification =
          new Notification(context, NotificationType.USER, title, message);
      service.sendNotification(notification);
    } else {
      Log.d(NotifierConstants.LOG_TAG, "Got empty user message");
    }
  }
}
