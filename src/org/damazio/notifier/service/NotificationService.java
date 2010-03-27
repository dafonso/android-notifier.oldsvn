package org.damazio.notifier.service;

import java.util.List;

import org.damazio.notifier.NotifierConstants;
import org.damazio.notifier.NotifierPreferences;
import org.damazio.notifier.notification.Notification;
import org.damazio.notifier.notification.Notifier;

import android.app.ActivityManager;
import android.app.Service;
import android.app.ActivityManager.RunningServiceInfo;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentFilter.MalformedMimeTypeException;
import android.os.Handler;
import android.os.IBinder;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;

/**
 * Service which listens for relevant events and sends notifications about
 * them.
 *
 * @author rdamazio
 */
public class NotificationService extends Service {
 
  private NotifierPreferences preferences;
  private Notifier notifier;
  private Handler instanceHandler;

  private final PhoneStateListener phoneListener = new PhoneRingListener(this);
  private final BatteryReceiver batteryReceiver = new BatteryReceiver(this);
  private final SmsReceiver smsReceiver = new SmsReceiver(this);
  private final MmsReceiver mmsReceiver = new MmsReceiver(this);

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

    Log.i(NotifierConstants.LOG_TAG, "Starting notification service");
    instanceHandler = new Handler();

    preferences = new NotifierPreferences(this);
    notifier = new Notifier(this, preferences);

    final TelephonyManager tm = (TelephonyManager)getSystemService(TELEPHONY_SERVICE);
    tm.listen(phoneListener, PhoneStateListener.LISTEN_CALL_STATE);

    // Register the battery receiver
    registerReceiver(batteryReceiver, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));

    // Register the SMS receiver
    registerReceiver(smsReceiver, new IntentFilter(SmsReceiver.ACTION));

    // Register the MMS receiver
    try {
      registerReceiver(mmsReceiver,
          new IntentFilter(MmsReceiver.ACTION, MmsReceiver.DATA_TYPE));
    } catch (MalformedMimeTypeException e) {
      Log.e(NotifierConstants.LOG_TAG, "Unable to register MMS receiver", e);
    }
  }

  @Override
  public void onDestroy() {
    unregisterReceiver(smsReceiver);
    unregisterReceiver(batteryReceiver);

    final TelephonyManager tm = (TelephonyManager)getSystemService(TELEPHONY_SERVICE);
    tm.listen(phoneListener, PhoneStateListener.LISTEN_NONE);

    super.onDestroy();
  }

  @Override
  public IBinder onBind(Intent arg0) {
	return null;
  }

  /**
   * Starts the service in the given context.
   */
  public static void start(Context context) {
    context.startService(new Intent(context, NotificationService.class));
  }

  /**
   * Uses the given context to determine whether the service is already running.
   */
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

  /**
   * Stops the service in the given context.
   */
  public static void stop(Context context) {
    context.stopService(new Intent(context, NotificationService.class));
  }
}
