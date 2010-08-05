package org.damazio.notifier.service;

import org.damazio.notifier.NotifierConstants;
import org.damazio.notifier.NotifierPreferences;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

/**
 * Receiver for boot events, which starts the service if the user chose to have
 * it started at boot.
 *
 * @author rdamazio
 */
public class BootServiceStarter extends BroadcastReceiver {
  @Override
  public void onReceive(final Context context, Intent intent) {
    NotifierPreferences preferences = new NotifierPreferences(context);
    if (!preferences.isStartAtBootEnabled()) {
      Log.d(NotifierConstants.LOG_TAG, "Not starting at boot.");
      return;
    }

    assert(intent.getAction().equals("android.intent.action.BOOT_COMPLETED"));

    NotificationService.start(context);
  }
}
