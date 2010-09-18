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
 * Receiver which catches SMS messages and notifies about them.
 *
 * @author rdamazio
 */
class SmsReceiver extends BroadcastReceiver {
  static final String ACTION = "android.provider.Telephony.SMS_RECEIVED";

  private final NotificationService service;

  public SmsReceiver(NotificationService service) {
    this.service = service;
  }

  @Override
  public void onReceive(Context context, Intent intent) {
    if (!intent.getAction().equals(ACTION)) {
      Log.e(NotifierConstants.LOG_TAG,
          "Wrong intent received by SMS receiver - " + intent.getAction());
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
        service.sendNotification(notification);
        notificationSent = true;
      }
    }

    if (!notificationSent) {
      // If no notification sent (extra info was not there), send one without info
      Notification notification = new Notification(context, NotificationType.SMS, null, null);
      service.sendNotification(notification);
    }
  }
}
