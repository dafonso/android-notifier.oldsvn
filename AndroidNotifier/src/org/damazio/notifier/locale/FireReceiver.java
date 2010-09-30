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
package org.damazio.notifier.locale;

import org.damazio.notifier.NotifierConstants;
import org.damazio.notifier.NotifierPreferences;
import org.damazio.notifier.locale.LocaleSettings.OnOffKeep;
import org.damazio.notifier.service.NotificationService;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

/**
 * Receiver which executes settings changes when requested by Locale.
 *
 * @author rdamazio
 */
public class FireReceiver extends BroadcastReceiver {
  @Override
  public void onReceive(Context context, Intent intent) {
    if (!com.twofortyfouram.Intent.ACTION_FIRE_SETTING.equals(intent.getAction())) {
      return;
    }
    NotifierPreferences preferences = new NotifierPreferences(context);

    Bundle extras = intent.getExtras();
    LocaleSettings settings = new LocaleSettings(context, extras);
    Log.d(NotifierConstants.LOG_TAG, "Applying locale settings: " + settings);

    setEnabledState(settings, preferences, context);
    setIpEnabledState(settings, preferences);
    setBluetoothEnabledState(settings, preferences);
    setTargetIp(settings, preferences);
    setCustomIps(settings, preferences);
  }

  private void setEnabledState(LocaleSettings settings, NotifierPreferences preferences, Context context) {
    OnOffKeep enabledState = settings.getEnabledState();
    boolean running = NotificationService.isRunning(context);
    switch (enabledState) {
      case ON:
        preferences.setNotificationsEnabled(true);
        if (!running) {
          NotificationService.start(context);
        }
        break;
      case OFF:
        preferences.setNotificationsEnabled(false);

        // Changing the preference should be enough for the service to suicide,
        // but we kill it just in case.
        NotificationService.stop(context);
        break;
      default:
        return;
    }
  }

  private void setIpEnabledState(LocaleSettings settings, NotifierPreferences preferences) {
    OnOffKeep state = settings.getIpEnabledState();
    if (state != OnOffKeep.KEEP) {
      preferences.setIpMethodEnabled(state == OnOffKeep.ON);
    }
  }

  private void setBluetoothEnabledState(LocaleSettings settings, NotifierPreferences preferences) {
    OnOffKeep state = settings.getBluetoothEnabledState();
    if (state != OnOffKeep.KEEP) {
      preferences.setBluetoothMethodEnabled(state == OnOffKeep.ON);
    }
  }

  private void setTargetIp(LocaleSettings settings, NotifierPreferences preferences) {
    String targetIp = settings.getTargetIp();
    if (!OnOffKeep.KEEP.name().equals(targetIp)) {
      preferences.setTargetIpAddress(targetIp);
    }
  }

  private void setCustomIps(LocaleSettings settings, NotifierPreferences preferences) {
    String[] customIps = settings.getCustomIps();
    if (customIps.length > 0) {
      preferences.setCustomTargetIpAddresses(customIps);
    }
  }
}
