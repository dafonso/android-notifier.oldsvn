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
package org.damazio.notifier.notification.events;

import org.damazio.notifier.NotifierConstants;
import org.damazio.notifier.NotifierPreferences;
import org.damazio.notifier.R;
import org.damazio.notifier.notification.Notification;
import org.damazio.notifier.notification.NotificationType;
import org.damazio.notifier.service.NotifierService;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.BatteryManager;
import android.os.Bundle;
import android.util.Log;

/**
 * Receiver for battery state change events.
 *
 * @author rdamazio
 */
public class BatteryReceiver extends BroadcastReceiver {
  
  // Keep the last notified state - only send a notification if it has changed
  // We need this because the granularity of the battery level changes is much
  // finer than we want to notify for.
  private int lastBatteryStatus = -1;
  private int lastBatteryLevelPercentage = -1;

  @Override
  public void onReceive(Context context, Intent intent) {
    if (!intent.getAction().equals(Intent.ACTION_BATTERY_CHANGED)) {
      Log.e(NotifierConstants.LOG_TAG,
          "Wrong intent received by battery receiver - " + intent.getAction());
      return;
    }
    NotifierPreferences preferences = new NotifierPreferences(context);
    if (!preferences.isBatteryEventEnabled()) {
      Log.d(NotifierConstants.LOG_TAG, "Ignoring battery event, disabled.");
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

    // Add message about battery status
    String contents;
    if (status != -1) {
      int statusStringId;
      switch (status) {
        case BatteryManager.BATTERY_STATUS_CHARGING:
          statusStringId = R.string.battery_charging;
          break;
        case BatteryManager.BATTERY_STATUS_DISCHARGING:
          statusStringId = R.string.battery_discharging;
          break;
        case BatteryManager.BATTERY_STATUS_NOT_CHARGING:
          statusStringId = R.string.battery_not_charging;
          break;
        case BatteryManager.BATTERY_STATUS_FULL:
          statusStringId = R.string.battery_full;
          break;
        default:
          statusStringId = R.string.battery_unknown;
          break;
      }
      String statusString = context.getString(statusStringId);
      Log.d(NotifierConstants.LOG_TAG, "Battery status: " + statusString);

      // Add message about the battery level
      if (level != -1 && maxLevel != -1) {
        batteryLevelPercentage = 100 * level / maxLevel;
        Log.d(NotifierConstants.LOG_TAG, "Got battery level: " + batteryLevelPercentage);
        contents = context.getString(R.string.battery_level, statusString, batteryLevelPercentage);
      } else {
        Log.w(NotifierConstants.LOG_TAG, "Unknown battery level");
        contents = context.getString(R.string.battery_level_unknown, statusString);
      }
    } else {
      Log.w(NotifierConstants.LOG_TAG, "Unknown battery status");
      return;
    }

    synchronized (this) {
      int batteryLevelChange = Math.abs(lastBatteryLevelPercentage - batteryLevelPercentage);
      Log.d(NotifierConstants.LOG_TAG, "Battery level change: " + batteryLevelChange);

      // Only notify if there were relevant changes - either:
      // 1. The status changed (charging/discharging/etc) and we're in range
      // 2. The percentage changed enough and we're in range
      boolean inRange =
          batteryLevelPercentage >= preferences.getMinBatteryLevel() &&
          batteryLevelPercentage <= preferences.getMaxBatteryLevel();
      if (inRange &&
          (status != lastBatteryStatus ||
           batteryLevelChange >= preferences.getMinBatteryLevelChange())) {
        Log.d(NotifierConstants.LOG_TAG, "Notifying of battery state change");
        String data = Integer.toString(batteryLevelPercentage);
        Notification notification =
            new Notification(context, NotificationType.BATTERY, data, contents);
        NotifierService.startAndSend(context, notification);
  
        lastBatteryStatus = status;
        lastBatteryLevelPercentage = batteryLevelPercentage;
      } else {
        Log.d(NotifierConstants.LOG_TAG, "Got battery update, but state change was not relevant");
      }
    }
  }
}
