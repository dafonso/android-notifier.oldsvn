package org.damazio.notifier.service;

import java.util.List;

import org.damazio.notifier.NotifierConstants;
import org.damazio.notifier.NotifierMain;
import org.damazio.notifier.NotifierPreferences;
import org.damazio.notifier.R;
import org.damazio.notifier.command.BluetoothCommandListener;
import org.damazio.notifier.notification.BluetoothDeviceUtils;
import org.damazio.notifier.notification.Notification;
import org.damazio.notifier.notification.Notifier;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentFilter.MalformedMimeTypeException;
import android.content.SharedPreferences;
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
  private ServicePreferencesListener preferenceListener;
  private Notifier notifier;
  private Handler instanceHandler;

  private final PhoneStateListener ringListener = new PhoneRingListener(this);
  private final VoicemailListener voicemailListener = new VoicemailListener(this);
  private BatteryReceiver batteryReceiver;
  private final SmsReceiver smsReceiver = new SmsReceiver(this);
  private final MmsReceiver mmsReceiver = new MmsReceiver(this);
  private BluetoothCommandListener bluetoothCommandListener;
  private boolean started;

  /**
   * Listener for changes to the preferences.
   */
  private class ServicePreferencesListener
      implements SharedPreferences.OnSharedPreferenceChangeListener {
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
      if (key.equals(getString(R.string.show_notification_icon_key))) {
        showOrHideLocalNotification();
      }

      // TODO: Start/stop listening for actions and commands
    }
  }

  /**
   * Sends the given notification.
   */
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

    synchronized (this) {
      if (started) {
        Log.d(NotifierConstants.LOG_TAG, "Not starting service again");
        return;
      }
      started = true;

      Log.i(NotifierConstants.LOG_TAG, "Starting notification service");
      instanceHandler = new Handler();
  
      preferences = new NotifierPreferences(this);
      notifier = new Notifier(this, preferences);
  
      final TelephonyManager tm = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);
  
      // Register the ring listener
      tm.listen(ringListener, PhoneStateListener.LISTEN_CALL_STATE);
  
      // Register the viocemail receiver
      tm.listen(voicemailListener, PhoneStateListener.LISTEN_MESSAGE_WAITING_INDICATOR);
  
      // Register the battery receiver
      batteryReceiver = new BatteryReceiver(this, preferences);
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
  
      // If enabled, start command listeners
      // TODO: Re-enable after bugfix release
      // TODO: Handle preference changes
      /*
      if (preferences.isCommandEnabled()) {
        if (preferences.isBluetoothCommandEnabled() && BluetoothDeviceUtils.isBluetoothMethodSupported()) {
          bluetoothCommandListener = new BluetoothCommandListener(preferences);
          bluetoothCommandListener.start();
        }
  
        if (preferences.isWifiCommandEnabled()) {
          // TODO
        }
      }
      */
  
      showOrHideLocalNotification();
  
      preferenceListener = new ServicePreferencesListener();
      preferences.registerOnSharedPreferenceChangeListener(preferenceListener);
    }
  }

  @Override
  public void onDestroy() {
    Log.i(NotifierConstants.LOG_TAG, "Notification service going down.");

    synchronized (this) {
      preferences.unregisterOnSharedPreferenceChangeListener(preferenceListener);
      hideLocalNotification();
  
      if (bluetoothCommandListener != null) {
        bluetoothCommandListener.shutdown();
        try {
          bluetoothCommandListener.join(1000);
        } catch (InterruptedException e) {
          Log.e(NotifierConstants.LOG_TAG, "Unable to join bluetooth listner", e);
        }
      }
  
      unregisterReceiver(mmsReceiver);
      unregisterReceiver(smsReceiver);
      unregisterReceiver(batteryReceiver);
  
      final TelephonyManager tm = (TelephonyManager)getSystemService(TELEPHONY_SERVICE);
      tm.listen(ringListener, PhoneStateListener.LISTEN_NONE);
      tm.listen(voicemailListener, PhoneStateListener.LISTEN_NONE);

      started = false;
    }

    super.onDestroy();
  }

  @Override
  public IBinder onBind(Intent arg0) {
	return null;
  }

  /**
   * Shows or hides the local notification, according to the user's preference.
   */
  private void showOrHideLocalNotification() {
    // If enabled, show a notification
    if (preferences.isServiceNotificationEnabled()) {
      showLocalNotification();
    } else {
      hideLocalNotification();
    }
  }

  /**
   * Shows the local status bar notification.
   */
  private void showLocalNotification() {
    android.app.Notification notification = new android.app.Notification();
    PendingIntent intent = PendingIntent.getActivity(
        this, 0,
        new Intent(this, NotifierMain.class),
        Intent.FLAG_ACTIVITY_NEW_TASK);
    notification.setLatestEventInfo(this,
        getString(R.string.app_name),
        getString(R.string.notification_icon_text),
        intent);

    notification.icon = R.drawable.icon;
    notification.flags = android.app.Notification.FLAG_NO_CLEAR
                       | android.app.Notification.FLAG_ONGOING_EVENT;

    NotificationManager notificationManager =
        (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
    notificationManager.notify(R.string.notification_icon_text, notification);
  }

  /**
   * Hides the local status bar notification.
   */
  private void hideLocalNotification() {
    NotificationManager notificationManager =
        (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
    notificationManager.cancel(R.string.notification_icon_text);
  }

  // Service control utilities.

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
