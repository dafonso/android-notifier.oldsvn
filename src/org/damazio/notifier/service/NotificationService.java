package org.damazio.notifier.service;

import org.damazio.notifier.notification.Notification;
import org.damazio.notifier.notification.NotificationType;
import org.damazio.notifier.notification.Notifier;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;

public class NotificationService extends Service {

  private Notifier notifier;
  private final PhoneStateListener phoneListener = new PhoneStateListener() {
    @Override
    public void onCallStateChanged(int state, String incomingNumber) {
      if (state == TelephonyManager.CALL_STATE_RINGING && notifier != null) {
        Notification notification = new Notification(
            NotificationService.this, NotificationType.RING, incomingNumber);
        notifier.sendNotification(notification);
      }
    }
  };

  @Override
  public void onStart(Intent intent, int startId) {
    super.onStart(intent, startId);

    Log.i("RemoteNotifier", "Starting notification service");

    notifier = new Notifier(this);

    final TelephonyManager tm = (TelephonyManager)getSystemService(TELEPHONY_SERVICE);
    tm.listen(phoneListener, PhoneStateListener.LISTEN_CALL_STATE);
  }

  @Override
  public void onDestroy() {
    final TelephonyManager tm = (TelephonyManager)getSystemService(TELEPHONY_SERVICE);
    tm.listen(phoneListener, PhoneStateListener.LISTEN_NONE);

    super.onDestroy();
  }

  @Override
  public IBinder onBind(Intent arg0) {
	return null;
  }

  public static void start(Context context) {
    context.startService(new Intent(context, NotificationService.class));
  }
}
