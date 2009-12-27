package org.damazio.notifier.service;

import org.damazio.notifier.R;
import org.damazio.notifier.notification.Notification;
import org.damazio.notifier.notification.NotificationType;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.SmsMessage;
import android.util.Log;

public class SmsReceiver extends BroadcastReceiver {
  private static final String ACTION = "android.provider.Telephony.SMS_RECEIVED";

  
  
  @Override
  public void onReceive(Context context, Intent intent) {
    if (!intent.getAction().equals(ACTION)) {
      Log.e("RemoteNotifier", "Wrong intent received by SMS receiver - " + intent.getAction());
      return;
    }

    NotificationService service = NotificationService.getRunningInstance();
    if (service == null) {
      Log.i("RemoteNotifier", "Got SMS but service not found");
      return;
    }

    boolean notificationSent = false;
    Bundle bundle = intent.getExtras();
    if (bundle != null) {
      Object[] pdus = (Object[]) bundle.get("pdus");
      for (int i = 0; i < pdus.length; i++) {
        SmsMessage message = SmsMessage.createFromPdu((byte[])pdus[i]);

        String contents = context.getString(R.string.sms_contents,
            message.getOriginatingAddress(),
            message.getMessageBody());

        Log.d("RemoteNotifier", "Received Sms: " + contents);
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
