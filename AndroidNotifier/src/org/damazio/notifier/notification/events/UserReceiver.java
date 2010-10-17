/*
 * Copyright 2010 Maarten Krijn
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
package org.damazio.notifier.notification.events;

import org.damazio.notifier.NotifierConstants;
import org.damazio.notifier.NotifierPreferences;
import org.damazio.notifier.notification.Notification;
import org.damazio.notifier.notification.NotificationType;
import org.damazio.notifier.service.NotifierService;

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
public class UserReceiver extends BroadcastReceiver {
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

  @Override
  public void onReceive(Context context, Intent intent) {
    if (!intent.getAction().equals(ACTION)) {
      Log.e(NotifierConstants.LOG_TAG,
          "Wrong intent received by user receiver - " + intent.getAction());
      return;
    }
    NotifierPreferences preferences = new NotifierPreferences(context);
    if (!preferences.isUserEventEnabled()) {
      Log.d(NotifierConstants.LOG_TAG, "Ignoring user event, disabled.");
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
      NotifierService.startAndSend(context, notification);
    } else {
      Log.d(NotifierConstants.LOG_TAG, "Got empty user message");
    }
  }
}
