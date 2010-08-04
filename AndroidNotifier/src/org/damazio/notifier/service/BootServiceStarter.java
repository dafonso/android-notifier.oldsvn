package org.damazio.notifier.service;

import org.damazio.notifier.NotifierConstants;
import org.damazio.notifier.NotifierPreferences;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.util.Log;

/**
 * Receiver for boot events, which starts the service if the user chose to have
 * it started at boot.
 *
 * @author rdamazio
 */
public class BootServiceStarter extends BroadcastReceiver {
  private static final double MAX_STARTUP_DELAY_MS = 10000.0;

  @Override
  public void onReceive(final Context context, Intent intent) {
    NotifierPreferences preferences = new NotifierPreferences(context);
    if (!preferences.isStartAtBootEnabled()) {
      Log.d(NotifierConstants.LOG_TAG, "Not starting at boot.");
      return;
    }

    assert(intent.getAction().equals("android.intent.action.BOOT_COMPLETED"));

    // Wait some random time before starting the service - we don't really need
    // to make the system boot slower by having it start right away.
    long waitMs = (long) (Math.random() * MAX_STARTUP_DELAY_MS);
    Log.d(NotifierConstants.LOG_TAG, "Starting at boot after " + waitMs + "ms");
    new Handler().postDelayed(new Runnable() {
      public void run() {
        NotificationService.start(context);
      }
    }, waitMs);
  }
}
