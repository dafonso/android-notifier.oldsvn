package org.damazio.notifier.service;

import org.damazio.notifier.NotifierPreferences;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.SystemClock;

/**
 * Receiver for boot events, which starts the service if the user chose to have
 * it started at boot.
 *
 * @author rdamazio
 */
public class BootServiceStarter extends BroadcastReceiver {
  private static final double MAX_STARTUP_DELAY_MS = 30000.0;

  @Override
  public void onReceive(Context context, Intent intent) {
    NotifierPreferences preferences = new NotifierPreferences(context);
    if (!preferences.isStartAtBootEnabled()) {
      return;
    }

    assert(intent.getAction().equals("android.intent.action.BOOT_COMPLETED"));

    // Wait some random time before starting the service - we don't really need
    // to make the system boot slower by having it start right away.
    SystemClock.sleep((long) (Math.random() * MAX_STARTUP_DELAY_MS));

    NotificationService.start(context);
  }
}
