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
