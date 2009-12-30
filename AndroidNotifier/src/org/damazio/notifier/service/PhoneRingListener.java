/**
 * 
 */
package org.damazio.notifier.service;

import org.damazio.notifier.NotifierConstants;
import org.damazio.notifier.notification.Notification;
import org.damazio.notifier.notification.NotificationType;

import android.content.Context;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;

/**
 * Listener which can detect and notify when the phone rings.
 *
 * @author rdamazio
 */
public class PhoneRingListener extends PhoneStateListener {
  private final Context context;
  
  public PhoneRingListener(Context context) {
    super();
    this.context = context;
  }

  @Override
  public void onCallStateChanged(int state, String incomingNumber) {
    NotificationService service = NotificationService.getRunningInstance();
    if (service == null) {
      Log.w(NotifierConstants.LOG_TAG, "Phone is ringing but service was not found");
      return;
    }

    if (state == TelephonyManager.CALL_STATE_RINGING) {
      Notification notification = new Notification(context, NotificationType.RING, incomingNumber);
      service.sendNotification(notification);
    }
  }
}