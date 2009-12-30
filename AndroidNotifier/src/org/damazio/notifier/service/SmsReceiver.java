package org.damazio.notifier.service;

import org.damazio.notifier.NotifierConstants;
import org.damazio.notifier.R;
import org.damazio.notifier.notification.Notification;
import org.damazio.notifier.notification.NotificationType;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.SmsMessage;
import android.util.Log;

/**
 * Receiver which catches receives SMS messages and notifies about them.
 *
 * @author rdamazio
 */
public class SmsReceiver extends BroadcastReceiver {
  private static final String ACTION = "android.provider.Telephony.SMS_RECEIVED";

  @Override
  public void onReceive(Context context, Intent intent) {
    if (!intent.getAction().equals(ACTION)) {
      Log.e(NotifierConstants.LOG_TAG, "Wrong intent received by SMS receiver - " + intent.getAction());
      return;
    }

    NotificationService service = NotificationService.getRunningInstance();
    if (service == null) {
      Log.i(NotifierConstants.LOG_TAG, "Got SMS but service not found");
      return;
    }

    // Create the notification contents using the SMS contents
    boolean notificationSent = false;
    Bundle bundle = intent.getExtras();
    if (bundle != null) {
      Object[] pdus = (Object[]) bundle.get("pdus");
      for (int i = 0; i < pdus.length; i++) {
        SmsMessage message = SmsMessage.createFromPdu((byte[])pdus[i]);

        String contents = context.getString(R.string.sms_contents,
            message.getOriginatingAddress(),
            message.getMessageBody());

        Log.d(NotifierConstants.LOG_TAG, "Received Sms: " + contents);
        Notification notification = new Notification(context, NotificationType.SMS, contents);
        service.sendNotification(notification);
        notificationSent = true;
      }
    }

    if (!notificationSent) {
      // If no notification sent (extra info was not there), send one without info
      Notification notification = new Notification(context, NotificationType.SMS, "");
      service.sendNotification(notification);
    }
  }
}
