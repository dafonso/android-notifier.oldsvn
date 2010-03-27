package org.damazio.notifier.service;

import org.damazio.notifier.NotifierConstants;
import org.damazio.notifier.notification.Notification;
import org.damazio.notifier.notification.NotificationType;

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
class MmsReceiver extends BroadcastReceiver {
  static final String ACTION = "android.provider.Telephony.WAP_PUSH_RECEIVED";
  static final String DATA_TYPE = "application/vnd.wap.mms-message";

  private final NotificationService service;

  public MmsReceiver(NotificationService service) {
    this.service = service;
  }

  @Override
  public void onReceive(Context context, Intent intent) {
    if (!ACTION.equals(intent.getAction()) ||
        !DATA_TYPE.equals(intent.getType())) {
      Log.e(NotifierConstants.LOG_TAG, "Wrong intent received by MMS receiver - " + intent.getAction());
      return;
    }
    
    // TODO(rdamazio): Eliminate duplicate events
    // TODO(rdamazio): Parse the pdu from the "data" extra to get source phone #

    Notification notification = new Notification(context, NotificationType.MMS, "");
    service.sendNotification(notification);
  }
}
