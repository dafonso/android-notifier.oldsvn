package org.damazio.notifier.service;

import org.damazio.notifier.R;
import org.damazio.notifier.notification.Notification;
import org.damazio.notifier.notification.NotificationType;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.BatteryManager;
import android.os.Bundle;
import android.util.Log;

public class BatteryReceiver extends BroadcastReceiver {

  /**
   * Minimum percentual battery level change for us to send a notification.
   */
  private static final int MIN_LEVEL_CHANGE = 5;
  
  // Keep the last notified state - only send a notification if it has changed
  // We need this because the granularity of the battery level changes is much
  // finer than we want to notify for.
  private int lastBatteryStatus = -1;
  private int lastBatteryLevelPercentage = -1;

  @Override
  public void onReceive(Context context, Intent intent) {
    if (!intent.getAction().equals(Intent.ACTION_BATTERY_CHANGED)) {
      Log.e("RemoteNotifier", "Wrong intent received by battery receiver - " + intent.getAction());
      return;
    }

    NotificationService service = NotificationService.getRunningInstance();
    if (service == null) {
      Log.w("RemoteNotifier", "Got battery status but service not found");
      return;
    }

    int level = -1;
    int maxLevel = -1;
    int status = -1;
    int batteryLevelPercentage = -1;

    // Try to read extras from intent
    Bundle extras = intent.getExtras();
    if (extras != null) {
      level = extras.getInt(BatteryManager.EXTRA_LEVEL, -1);
      maxLevel = extras.getInt(BatteryManager.EXTRA_SCALE, -1);
      status = extras.getInt(BatteryManager.EXTRA_STATUS, -1);
    }

    // Add message about the battery level
    StringBuilder contentsBuilder = new StringBuilder();
    if (level != -1 && maxLevel != -1) {
      batteryLevelPercentage = 100 * level / maxLevel;
      Log.d("RemoteNotifier", "Got battery level: " + batteryLevelPercentage);
      String levelString = context.getString(R.string.battery_level, batteryLevelPercentage);
      contentsBuilder.append(levelString);
    } else {
      Log.w("RemoteNotifier", "Unknown battery level");
      contentsBuilder.append(context.getString(R.string.battery_level_unknown));
    }

    // Add message about battery status
    if (status != -1) {
      int statusStringId = -1;
      switch (status) {
        case BatteryManager.BATTERY_STATUS_CHARGING:
          statusStringId = R.string.battery_charging;
          break;
        case BatteryManager.BATTERY_STATUS_DISCHARGING:
          statusStringId = R.string.battery_discharging;
          break;
        case BatteryManager.BATTERY_STATUS_NOT_CHARGING:
          statusStringId = R.string.battery_discharging;
          break;
        case BatteryManager.BATTERY_STATUS_FULL:
          statusStringId = R.string.battery_full;
          break;
      }

      if (statusStringId != -1) {
        contentsBuilder.append(", ");
        String statusString = context.getString(statusStringId);
        Log.d("RemoteNotifier", "Battery status: " + statusString);
        contentsBuilder.append(statusString);
      } else {
        Log.w("RemoteNotifier", "Unknown battery status");
      }
    } else {
      Log.w("RemoteNotifier", "Unknown battery status");
    }

    // Only notify if there were relevant changes
    if (status != lastBatteryStatus ||
        lastBatteryLevelPercentage - batteryLevelPercentage >= MIN_LEVEL_CHANGE) {
      Notification notification = new Notification(context, NotificationType.BATTERY, contentsBuilder.toString());
      service.sendNotification(notification);

      lastBatteryStatus = status;
      lastBatteryLevelPercentage = batteryLevelPercentage;
    }
  }
}
