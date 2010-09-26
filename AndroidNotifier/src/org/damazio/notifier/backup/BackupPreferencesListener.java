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
package org.damazio.notifier.backup;

import org.damazio.notifier.NotifierConstants;

import android.app.backup.BackupManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;

/**
 * Shared preferences listener which notifies the backup system about new data
 * being available for backup.
 * This class is API-version-safe and will provide a dummy implementation if
 * the device doesn't support backup services.
 *
 * @author rdamazio
 */
public abstract class BackupPreferencesListener implements OnSharedPreferenceChangeListener {
  
  /**
   * Real implementation of the listener, which calls the {@link BackupManager}.
   */
  private static class BackupPreferencesListenerImpl extends BackupPreferencesListener {
    private final BackupManager backupManager;

    public BackupPreferencesListenerImpl(Context context) {
      this.backupManager = new BackupManager(context);
    }

    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
      backupManager.dataChanged();
    }
  }

  /**
   * Dummy implementation of the listener which does nothing.
   */
  private static class DummyBackupPreferencesListener extends BackupPreferencesListener {
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
      // Do nothing
    }
  }

  /**
   * Creates and returns a proper instance of the listener for this device.
   */
  public static BackupPreferencesListener create(Context context) {
    if (NotifierConstants.ANDROID_SDK_INT >= 8) {
      return new BackupPreferencesListenerImpl(context);
    } else {
      return new DummyBackupPreferencesListener();
    }
  }
}
