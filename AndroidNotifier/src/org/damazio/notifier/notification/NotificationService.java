/*
 * Copyright 2010 Rodrigo Damazio <rodrigo@damazio.org>
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * 
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 * 3. The name of the author may not be used to endorse or promote products
 *    derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR
 * IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 * IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
 * NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
 * THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.damazio.notifier.notification;

import java.util.List;

import org.damazio.notifier.NotifierConstants;
import org.damazio.notifier.NotifierMain;
import org.damazio.notifier.NotifierPreferences;
import org.damazio.notifier.R;
import org.damazio.notifier.command.CommandService;
import org.damazio.notifier.notification.Notification;
import org.damazio.notifier.notification.Notifier;
import org.damazio.notifier.notification.events.BatteryReceiver;
import org.damazio.notifier.notification.events.VoicemailListener;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
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
 * @author Rodrigo Damazio
 */
public class NotificationService extends Service {
 
  /**
   * If a {@link Notification} object is passed as this extra when starting
   * the service, that notification will be sent.
   */
  private static final String EXTRA_NOTIFICATION = "org.damazio.notifier.service.EXTRA_NOTIFICATION";

  private NotifierPreferences preferences;
  private ServicePreferencesListener preferenceListener;
  private Notifier notifier;
  private CommandService commandService;
  private Handler instanceHandler;

  private final VoicemailListener voicemailListener = new VoicemailListener(this);
  private final BatteryReceiver batteryReceiver = new BatteryReceiver();
  private boolean started;

  /**
   * Listener for changes to the preferences.
   */
  private class ServicePreferencesListener
      implements SharedPreferences.OnSharedPreferenceChangeListener {
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
      if (key.equals(getString(R.string.notifications_enabled_key))) {
        if (!preferences.areNotificationsEnabled()) {
          stopSelf();
        }
      } else if (key.equals(getString(R.string.show_notification_icon_key))) {
        showOrHideLocalNotification();
      }
    }
  }

  /**
   * Sends the given notification.
   */
  private void sendNotification(final Notification notification) {
    instanceHandler.post(new Runnable() {
      public void run() {
        notifier.sendNotification(notification);
      }
    });
  }

  /**
   * If the given intent carries a bundled notification in its extras, sends it.
   */
  private void sendIntentNotification(Intent intent) {
    Notification notification = intent.getParcelableExtra(EXTRA_NOTIFICATION);
    if (notification != null) {
      sendNotification(notification);
    }
  }

  @Override
  public void onStart(Intent intent, int startId) {
    super.onStart(intent, startId);

    synchronized (this) {
      startNotificationService(intent);
      startCommandService();
    }
  }

  private void startNotificationService(Intent intent) {
    synchronized (this) {
      preferences = new NotifierPreferences(this);
      if (!preferences.areNotificationsEnabled()) {
        Log.w(NotifierConstants.LOG_TAG, "Not starting service - notifications disabled");
        // TODO: Don't stop if commands are enabled
        stopSelf();
        return;
      }

      if (started) {
        Log.d(NotifierConstants.LOG_TAG, "Not starting service again");
      } else {
        Log.i(NotifierConstants.LOG_TAG, "Starting notification service");
        started = true;
        instanceHandler = new Handler();
  
        notifier = new Notifier(this, preferences);
  
        // Register the voicemail receiver
        final TelephonyManager tm = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);
        tm.listen(voicemailListener, PhoneStateListener.LISTEN_MESSAGE_WAITING_INDICATOR);
  
        // Register the battery receiver
        // (can't be registered in the manifest for some reason)
        registerReceiver(batteryReceiver, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));

        showOrHideLocalNotification();
  
        preferenceListener = new ServicePreferencesListener();
        preferences.registerOnSharedPreferenceChangeListener(preferenceListener);
      }
    }

    sendIntentNotification(intent);
  }

  private void startCommandService() {
    synchronized (this) {
      commandService = new CommandService(this, preferences);
      commandService.start();
    }
  }

  @Override
  public void onDestroy() {
    Log.i(NotifierConstants.LOG_TAG, "Notification service going down.");

    synchronized (this) {
      notifier.shutdown();
      destroyNotificationService();
      destroyCommandService();
    }

    super.onDestroy();
  }

  private void destroyCommandService() {
    synchronized (this) {
      if (commandService != null) {
        commandService.shutdown();
      }
    }
  }

  private void destroyNotificationService() {
    synchronized (this) {
      if (preferenceListener != null) {
        preferences.unregisterOnSharedPreferenceChangeListener(preferenceListener);
      }
      hideLocalNotification();

      try {
        unregisterReceiver(batteryReceiver);
      } catch (IllegalArgumentException e) {
        Log.w(NotifierConstants.LOG_TAG, "Unable to unregister battery listener", e);
      }

      final TelephonyManager tm = (TelephonyManager)getSystemService(TELEPHONY_SERVICE);
      tm.listen(voicemailListener, PhoneStateListener.LISTEN_NONE);

      started = false;
    }
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
    android.app.Notification notification = createLocalNotification(this);

    NotificationManager notificationManager =
        (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
    notificationManager.notify(R.string.notification_icon_text, notification);
  }

  private static android.app.Notification createLocalNotification(Context context) {
    String notificationText = context.getString(R.string.notification_icon_text);
    android.app.Notification notification = new android.app.Notification(
        R.drawable.icon, notificationText, System.currentTimeMillis());
    PendingIntent intent = PendingIntent.getActivity(
        context, 0,
        new Intent(context, NotifierMain.class),
        Intent.FLAG_ACTIVITY_NEW_TASK);
    notification.setLatestEventInfo(context,
        context.getString(R.string.app_name),
        notificationText,
        intent);

    notification.flags = android.app.Notification.FLAG_NO_CLEAR
                       | android.app.Notification.FLAG_ONGOING_EVENT;
    return notification;
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

  public static void startAndSend(Context context, Notification notification) {
    Intent intent = new Intent(context, NotificationService.class);
    intent.putExtra(EXTRA_NOTIFICATION, notification);
    context.startService(intent);
  }

  /**
   * Uses the given context to determine whether the service is already running.
   */
  public static boolean isRunning(Context context) {
    ActivityManager activityManager = (ActivityManager) context.getSystemService(ACTIVITY_SERVICE);
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
