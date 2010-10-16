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
package org.damazio.notifier.command;

import org.damazio.notifier.NotifierConstants;
import org.damazio.notifier.NotifierPreferences;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.util.Log;

public class CommandService implements OnSharedPreferenceChangeListener {
  private final Context context;
  private final NotifierPreferences preferences;
  private BluetoothCommandListener bluetoothListener;
  private IpCommandListener ipListener;

  public CommandService(Context context, NotifierPreferences preferences) {
    this.context = context;
    this.preferences = preferences;

    preferences.registerOnSharedPreferenceChangeListener(this);
  }

  public void start() {
    synchronized (this) {
      if (!preferences.isCommandEnabled()) {
        Log.w(NotifierConstants.LOG_TAG, "Commands disabled, not starting");
        return;
      }

      startBluetoothListener();
      startIpListener();

      // TODO: Listen for discovery
    }
  }

  private void startIpListener() {
    if (ipListener == null && preferences.isIpCommandEnabled()) {
      ipListener = new IpCommandListener(context);
      ipListener.start();
    }
  }

  private void startBluetoothListener() {
    if (bluetoothListener == null && preferences.isBluetoothCommandEnabled()) {
      bluetoothListener = new BluetoothCommandListener(context, preferences);
      bluetoothListener.start();
    }
  }

  public void shutdown() {
    synchronized (this) {
      shutdownIpListener();
      shutdownBluetoothListener();
    }
  }

  private void shutdownBluetoothListener() {
    if (bluetoothListener != null) {
      bluetoothListener.shutdown();
      bluetoothListener = null;
    }
  }

  private void shutdownIpListener() {
    if (ipListener != null) {
      ipListener.shutdown();
      ipListener = null;
    }
  }

  @Override
  public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
    // TODO Auto-generated method stub
    
  }
}
