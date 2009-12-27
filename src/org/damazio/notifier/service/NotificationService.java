package org.damazio.notifier.service;

import java.util.List;

import org.damazio.notifier.NotifierPreferences;
import org.damazio.notifier.notification.Notification;
import org.damazio.notifier.notification.NotificationType;
import org.damazio.notifier.notification.Notifier;

import android.app.ActivityManager;
import android.app.Service;
import android.app.ActivityManager.RunningServiceInfo;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;

public class NotificationService extends Service {

  // TODO(rdamazio): Replace this method with an AIDL binding?
  private static NotificationService runningInstance = null;
 
  private NotifierPreferences preferences;
  private Notifier notifier;
  private Handler instanceHandler;

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

  public static NotificationService getRunningInstance() {
    return runningInstance;
  }

  public void sendNotification(final Notification notification) {
    instanceHandler.post(new Runnable() {
      public void run() {
        notifier.sendNotification(notification);
      }
    });
  }

  @Override
  public void onStart(Intent intent, int startId) {
    super.onStart(intent, startId);

    Log.i("RemoteNotifier", "Starting notification service");
    runningInstance = this;
    instanceHandler = new Handler();

    preferences = new NotifierPreferences(this);
    notifier = new Notifier(this, preferences);

    final TelephonyManager tm = (TelephonyManager)getSystemService(TELEPHONY_SERVICE);
    tm.listen(phoneListener, PhoneStateListener.LISTEN_CALL_STATE);
  }

  @Override
  public void onDestroy() {
    final TelephonyManager tm = (TelephonyManager)getSystemService(TELEPHONY_SERVICE);
    tm.listen(phoneListener, PhoneStateListener.LISTEN_NONE);

    runningInstance = null;
    super.onDestroy();
  }

  @Override
  public IBinder onBind(Intent arg0) {
	return null;
  }

  public static void start(Context context) {
    context.startService(new Intent(context, NotificationService.class));
  }
  
  public static boolean isRunning(Context context) {
    ActivityManager activityManager = (ActivityManager)context.getSystemService(ACTIVITY_SERVICE);
    List<RunningServiceInfo> services = activityManager.getRunningServices(Integer.MAX_VALUE);
    
    for (RunningServiceInfo serviceInfo : services) {
      ComponentName componentName = serviceInfo.service;
      String serviceName = componentName.getClassName();
      if (serviceName.equals(NotificationService.class.getName())) {
        return true;
      }
    }

    return false;
  }

  public static void stop(Context context) {
    context.stopService(new Intent(context, NotificationService.class));
  }
}
