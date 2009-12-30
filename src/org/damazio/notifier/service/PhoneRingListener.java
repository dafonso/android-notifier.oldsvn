/**
 * 
 */
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
  
  public PhoneRingListener(NotificationService context) {
    this.service = context;
  }

  @Override
  public void onCallStateChanged(int state, String incomingNumber) {
    if (state == TelephonyManager.CALL_STATE_RINGING) {
      Notification notification = new Notification(service, NotificationType.RING, incomingNumber);
      service.sendNotification(notification);
    }
  }
}