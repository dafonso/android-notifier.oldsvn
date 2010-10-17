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
package org.damazio.notifier.notification.events;

import org.damazio.notifier.NotifierConstants;
import org.damazio.notifier.NotifierPreferences;
import org.damazio.notifier.notification.Notification;
import org.damazio.notifier.notification.NotificationType;
import org.damazio.notifier.service.NotifierService;
import org.damazio.notifier.util.SmsDecoder;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

/**
 * Receiver which catches SMS messages and notifies about them.
 *
 * @author Rodrigo Damazio
 */
public class SmsReceiver extends BroadcastReceiver {
  private static final String ACTION = "android.provider.Telephony.SMS_RECEIVED";

  @Override
  public void onReceive(Context context, Intent intent) {
    if (!intent.getAction().equals(ACTION)) {
      Log.e(NotifierConstants.LOG_TAG,
          "Wrong intent received by SMS receiver - " + intent.getAction());
      return;
    }
    NotifierPreferences preferences = new NotifierPreferences(context);
    if (!preferences.isSmsEventEnabled()) {
      Log.d(NotifierConstants.LOG_TAG, "Ignoring SMS event, disabled.");
      return;
    }

    // Create the notification contents using the SMS contents
    boolean notificationSent = false;
    Bundle bundle = intent.getExtras();
    if (bundle != null) {
      Object[] pdus = (Object[]) bundle.get("pdus");
      for (int i = 0; i < pdus.length; i++) {
        SmsDecoder decoder = SmsDecoder.create(context, pdus[i]);
        if (!decoder.isValidMessage()) {
          continue;
        }

        String contents = decoder.getSmsContents();
        String data = decoder.getSenderAddress();
        Log.d(NotifierConstants.LOG_TAG, "Received Sms: " + contents);
        Notification notification = new Notification(context, NotificationType.SMS, data, contents);
        NotifierService.startAndSend(context, notification);
        notificationSent = true;
      }
    }

    if (!notificationSent) {
      // If no notification sent (extra info was not there), send one without info
      Notification notification = new Notification(context, NotificationType.SMS, null, null);
      NotifierService.startAndSend(context, notification);
    }
  }
}
