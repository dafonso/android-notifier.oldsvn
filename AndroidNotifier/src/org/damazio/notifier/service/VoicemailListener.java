package org.damazio.notifier.service;

import org.damazio.notifier.notification.Notification;
import org.damazio.notifier.notification.NotificationType;

import android.telephony.PhoneStateListener;

/**
 * Receiver for "new voicemail" events.
 *
 * @author rdamazio
 */
public class VoicemailListener extends PhoneStateListener {
  private final NotificationService service;

  public VoicemailListener(NotificationService service) {
    this.service = service;
  }

  @Override
  public void onMessageWaitingIndicatorChanged(boolean mwi) {
    if (mwi) {
      service.sendNotification(new Notification(service, NotificationType.VOICEMAIL, ""));
    }
  }
}
