package org.damazio.notifier.service;

import org.damazio.notifier.NotifierConstants;
import org.damazio.notifier.R;
import org.damazio.notifier.mms.EncodedStringValue;
import org.damazio.notifier.mms.PduHeaders;
import org.damazio.notifier.mms.PduParser;
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

    // Parse the WAP push contents
    PduParser parser = new PduParser();
    PduHeaders headers = parser.parseHeaders(intent.getByteArrayExtra("data"));
    if (headers == null) {
      Log.e(NotifierConstants.LOG_TAG, "Couldn't parse headers for WAP PUSH.");
      return;
    }
    int messageType = headers.getMessageType();
    Log.d(NotifierConstants.LOG_TAG, "WAP PUSH message type: 0x" + Integer.toHexString(messageType));

    // Check if it's a MMS notification
    if (messageType == PduHeaders.MESSAGE_TYPE_NOTIFICATION_IND) {
      String contents = "";
      EncodedStringValue from = headers.getFrom();
      if (from != null) {
        // TODO(rdamazio): Use CallerId for sender
        contents = service.getString(R.string.mms_contents, from.getString());
      }
      Notification notification = new Notification(context, NotificationType.MMS, contents);
      service.sendNotification(notification);
    }
  }
}
