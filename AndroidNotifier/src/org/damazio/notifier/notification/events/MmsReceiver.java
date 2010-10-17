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
import org.damazio.notifier.R;
import org.damazio.notifier.notification.Notification;
import org.damazio.notifier.notification.NotificationType;
import org.damazio.notifier.service.NotifierService;
import org.damazio.notifier.util.CallerId;
import org.damazio.notifier.util.mms.EncodedStringValue;
import org.damazio.notifier.util.mms.PduHeaders;
import org.damazio.notifier.util.mms.PduParser;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

/**
 * Receiver which catches MMS messages and notifies about them.
 * This does NOT fetch the MMS contents from the WAP server, so no real
 * MMS contents are sent in the notification.
 *
 * @author rdamazio
 */
public class MmsReceiver extends BroadcastReceiver {
  private static final String ACTION = "android.provider.Telephony.WAP_PUSH_RECEIVED";
  private static final String DATA_TYPE = "application/vnd.wap.mms-message";

  private final NotifierService service;

  public MmsReceiver(NotifierService service) {
    this.service = service;
  }

  @Override
  public void onReceive(Context context, Intent intent) {
    if (!ACTION.equals(intent.getAction()) ||
        !DATA_TYPE.equals(intent.getType())) {
      Log.e(NotifierConstants.LOG_TAG,
          "Wrong intent received by MMS receiver - " + intent.getAction());
      return;
    }
    NotifierPreferences preferences = new NotifierPreferences(context);
    if (!preferences.isMmsEventEnabled()) {
      Log.d(NotifierConstants.LOG_TAG, "Ignoring MMS event, disabled.");
      return;
    }

    // Parse the WAP push contents
    PduParser parser = new PduParser();
    PduHeaders headers = parser.parseHeaders(intent.getByteArrayExtra("data"));
    if (headers == null) {
      Log.e(NotifierConstants.LOG_TAG, "Couldn't parse headers for WAP PUSH.");
      return;
    }
    int messageType = headers.getMessageType();
    Log.d(NotifierConstants.LOG_TAG,
        "WAP PUSH message type: 0x" + Integer.toHexString(messageType));

    // Check if it's a MMS notification
    if (messageType == PduHeaders.MESSAGE_TYPE_NOTIFICATION_IND) {
      String contents = null;
      String data = null;
      EncodedStringValue from = headers.getFrom();
      if (from != null) {
        String fromStr = from.getString();
        String identifiedFrom = CallerId.create(service).buildCallerIdString(fromStr);
        contents = service.getString(R.string.mms_contents, identifiedFrom);
        data = fromStr;
      }
      Notification notification = new Notification(context, NotificationType.MMS, data, contents);
      NotifierService.startAndSend(context, notification);
    }
  }
}
