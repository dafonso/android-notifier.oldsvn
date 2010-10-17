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

import org.damazio.notifier.NotifierConstants;
import org.damazio.notifier.NotifierPreferences;
import org.damazio.notifier.notification.events.BatteryReceiver;
import org.damazio.notifier.notification.events.VoicemailListener;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;

/**
 * Service which listens for relevant events and sends notifications about
 * them.
 *
 * @author Rodrigo Damazio
 */
public class NotificationService {

  private final Context context;
  private final NotifierPreferences preferences;
  private Notifier notifier;
  private Handler instanceHandler;

  private final VoicemailListener voicemailListener;
  private final BatteryReceiver batteryReceiver;

  public NotificationService(Context context, NotifierPreferences preferences) {
    this.context = context;
    this.preferences = preferences;

    this.voicemailListener = new VoicemailListener(context);
    this.batteryReceiver = new BatteryReceiver();
  }

  /**
   * Sends the given notification.
   */
  public void sendNotification(final Notification notification) {
    synchronized (this) {
      instanceHandler.post(new Runnable() {
        public void run() {
          notifier.sendNotification(notification);
        }
      });
    }
  }

  public void start() {
    synchronized (this) {
      if (notifier != null) {
        Log.d(NotifierConstants.LOG_TAG, "Not starting service again");
        return;
      }

      Log.i(NotifierConstants.LOG_TAG, "Starting notification service");
      instanceHandler = new Handler();

      notifier = new Notifier(context, preferences);

      // Register the voicemail receiver
      final TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
      tm.listen(voicemailListener, PhoneStateListener.LISTEN_MESSAGE_WAITING_INDICATOR);

      // Register the battery receiver
      // (can't be registered in the manifest for some reason)
      context.registerReceiver(batteryReceiver, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
    }
  }

  public void shutdown() {
    Log.i(NotifierConstants.LOG_TAG, "Notification service going down.");

    synchronized (this) {
      notifier.shutdown();

      try {
        context.unregisterReceiver(batteryReceiver);
      } catch (IllegalArgumentException e) {
        Log.w(NotifierConstants.LOG_TAG, "Unable to unregister battery listener", e);
      }

      final TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
      tm.listen(voicemailListener, PhoneStateListener.LISTEN_NONE);
    }
  }
}
