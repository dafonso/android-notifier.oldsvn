package org.damazio.notifier.service;

import org.damazio.notifier.notification.Notification;
import org.damazio.notifier.notification.NotificationType;

import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;

/**
 * Listener which can detect and notify when the phone rings.
 *
 * @author rdamazio
 */
class PhoneRingListener extends PhoneStateListener {
  private final NotificationService service;
  private CallerId callerId;

  public PhoneRingListener(NotificationService context) {
    this.service = context;
    this.callerId = CallerId.create(context);
  }

  @Override
  public void onCallStateChanged(int state, String incomingNumber) {
    if (state == TelephonyManager.CALL_STATE_RINGING) {
      String notificationContents = callerId.buildCallerIdString(incomingNumber);
      Notification notification = new Notification(service, NotificationType.RING, notificationContents);
      service.sendNotification(notification);
    }
  }
}