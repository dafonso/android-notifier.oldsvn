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
package org.damazio.notifier;

import java.util.ArrayList;
import java.util.List;

import org.damazio.notifier.backup.BackupPreferencesListener;
import org.damazio.notifier.notification.Notification;
import org.damazio.notifier.notification.NotificationType;
import org.damazio.notifier.service.NotifierService;
import org.damazio.notifier.util.BluetoothDeviceUtils;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceActivity;
import android.provider.Settings;
import android.util.Log;
import android.widget.Toast;

/**
 * Main activity for the notifier.
 * This activity displays the application's settings,
 * and allows the user to manipulate the service.
 *
 * @author Rodrigo Damazio
 */
public class NotifierMain extends PreferenceActivity {
  private NotifierPreferences preferences;
  private BackupPreferencesListener backupPreferencesListener;

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    // Register to listen for preference changes
    backupPreferencesListener = BackupPreferencesListener.create(this);
    getPreferenceManager().getSharedPreferences()
        .registerOnSharedPreferenceChangeListener(backupPreferencesListener);

    // Load preferences
    preferences = new NotifierPreferences(this);

    // Initialize the preferences UI
    addPreferencesFromResource(R.xml.preferences);

    // Show welcome screen if it's the first time
    maybeShowWelcomeScreen();

    // Start the service
    maybeStartService();
  }

  @Override
  protected void onResume() {
    super.onResume();

    // Configure all special preferences
    // This has to be done on resume so special values (such as the bluetooth device list and the
    // wifi sleep policy) are reloaded when we return from another activity.
    configureBluetoothPreferences();
    configureIpPreferences();
    configureServicePreferences();
    configureMiscPreferences();
  }

  @Override
  protected void onDestroy() {
    super.onDestroy();

    // Stop listening to preference changes
    getPreferenceManager().getSharedPreferences()
        .unregisterOnSharedPreferenceChangeListener(backupPreferencesListener);
  }

  /**
   * Shows a welcome screen if it's the first time the user runs the app.
   */
  private void maybeShowWelcomeScreen() {
    if (preferences.isFirstTime()) {
      preferences.setFirstTime(false);

      showAlertDialog(R.string.about_message, R.string.welcome_title);
    }
  }

  /**
   * Starts the service if it was configured to start at boot.
   * This is useful in case the service was either stopped, or the app
   * is newly installed.
   */
  private void maybeStartService() {
    if (preferences.isStartAtBootEnabled()) {
      // Ensure the service is started if it should have been auto-started
      NotifierService.start(this);
    }
  }

  /**
   * Configures preference actions related to the service.
   */
  private void configureServicePreferences() {
    // Make the enable notifications preference start or stop the service
    CheckBoxPreference enabledPreference =
        (CheckBoxPreference) findPreference(getString(R.string.notifications_enabled_key));
    enabledPreference.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
      @Override
      public boolean onPreferenceChange(Preference preference, Object newValue) {
        boolean enabled = (Boolean) newValue;
        setServiceStarted(enabled);
        return true;
      }
    });
  }

  /**
   * Configures preference actions related to TCP/IP.
   */
  private void configureIpPreferences() {
    final CheckBoxPreference cellSendPreference =
        (CheckBoxPreference) findPreference(getString(R.string.allow_cell_send_key));
    final CheckBoxPreference enableWifiPreference =
        (CheckBoxPreference) findPreference(getString(R.string.enable_wifi_key));

    // Make these two be mutually exclusive (can only take one action if wifi is off)
    enableWifiPreference.setEnabled(!cellSendPreference.isChecked());
    enableWifiPreference.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
      public boolean onPreferenceChange(Preference preference, Object newValue) {
    	// Allow cell send if not auto-enabling wifi and using a custom IP
    	boolean enableWifiValue = (Boolean) newValue;
    	boolean customIp = preferences.getTargetIpAddress().equals("custom");
        cellSendPreference.setEnabled(!enableWifiValue && customIp);
        return true;
      }
    });
    cellSendPreference.setEnabled(!enableWifiPreference.isChecked());
    cellSendPreference.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
      public boolean onPreferenceChange(Preference preference, Object newValue) {
        enableWifiPreference.setEnabled(!((Boolean) newValue));
        return true;
      }
    });

    // Set initial state and values
    updateIpPreferences(preferences.getTargetIpAddress().equals("custom"), false);

    // Attach custom IP address selector
    Preference ipAddressPreference = findPreference(getString(R.string.target_ip_address_key));
    ipAddressPreference.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
      public boolean onPreferenceChange(Preference preference, Object newValue) {
        ListPreference listPreference = (ListPreference) preference;
        String value = (String) newValue;
        String oldValue = listPreference.getValue();

        boolean isCustomIp = value.equals("custom");
        boolean isChanging = !newValue.equals(oldValue);
        updateIpPreferences(isCustomIp, isChanging);
        return true;
      }
    });
    EditableListPreference customIpsPreference =
        (EditableListPreference) findPreference(getString(R.string.target_custom_ips_key));
    customIpsPreference.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
      @Override
      public boolean onPreferenceChange(Preference preference, Object newValue) {
        String[] contents = (String[]) newValue;

        for (String address : contents) {
          if (!isValidCustomAddress(address)) {
            Toast.makeText(NotifierMain.this, R.string.invalid_custom_ip, Toast.LENGTH_LONG).show();
            return false;
          }
        }

        return true;
      }
    });

    // Load wifi sleep policy from system settings, and save back only there
    ListPreference sleepPolicyPreference =
        (ListPreference) findPreference(getString(R.string.wifi_sleep_policy_key));
    sleepPolicyPreference.setValue(preferences.getWifiSleepPolicy());
    sleepPolicyPreference.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
      public boolean onPreferenceChange(Preference preference, Object newValue) {
        preferences.setWifiSleepPolicy((String) newValue);
        return true;
      }
    });
  }

  /**
   * Updates the state of the "send over TCP" preference.
   *
   * @param isCustomIp whether a custom IP is selected as the target
   * @param isChanging whether the IP type has changed
   */
  private void updateIpPreferences(boolean isCustomIp, boolean isChanging) {
    // TODO: Give some type of warning if both UDP and TCP are disabled
    final CheckBoxPreference sendTcpPreference =
        (CheckBoxPreference) findPreference(getString(R.string.send_tcp_key));
    final CheckBoxPreference cellSendPreference =
        (CheckBoxPreference) findPreference(getString(R.string.allow_cell_send_key));
    final CheckBoxPreference enableWifiPreference =
        (CheckBoxPreference) findPreference(getString(R.string.enable_wifi_key));
    final EditableListPreference customIpsPreference =
        (EditableListPreference) findPreference(getString(R.string.target_custom_ips_key));

    if (isCustomIp) {
      if (!enableWifiPreference.isChecked()) {
        cellSendPreference.setEnabled(true);
        cellSendPreference.setSummaryOff(R.string.allow_cell_send_summary_off);
      }

      sendTcpPreference.setEnabled(true);
      if (isChanging) sendTcpPreference.setChecked(true);
      sendTcpPreference.setSummaryOff(R.string.send_tcp_summary_off);
      customIpsPreference.setEnabled(true);
    } else {
      sendTcpPreference.setEnabled(false);
      sendTcpPreference.setChecked(false);
      sendTcpPreference.setSummaryOff(R.string.custom_ip_needed);
      cellSendPreference.setEnabled(false);
      cellSendPreference.setChecked(false);
      cellSendPreference.setSummaryOff(R.string.custom_ip_needed);
      customIpsPreference.setEnabled(false);

      // Just in case having "send over cell network" enabled made this one be disabled
      enableWifiPreference.setEnabled(true);
    }
  }

  // From http://stackoverflow.com/questions/106179
  private static final String IP_ADDRESS_PATTERN =
      "^(([0-9]|[1-9][0-9]|1[0-9]{2}|2[0-4][0-9]|25[0-5])\\.){3}" +
      "([0-9]|[1-9][0-9]|1[0-9]{2}|2[0-4][0-9]|25[0-5])$";
  private static final String HOSTNAME_PATTERN =
      "^(([a-zA-Z0-9]|[a-zA-Z0-9][a-zA-Z0-9\\-]*[a-zA-Z0-9])\\.)*" +
      "([A-Za-z]|[A-Za-z][A-Za-z0-9\\-]*[A-Za-z0-9])$";

  /**
   * Checks and returns whether the given address is a valid hostname or
   * IP address. This does not ensure that the hostname will be successfully
   * resolved.
   */
  private static boolean isValidCustomAddress(String address) {
    return address.matches(IP_ADDRESS_PATTERN)
        || address.matches(HOSTNAME_PATTERN);
  }

  /**
   * Configures preference actions related to bluetooth.
   */
  private void configureBluetoothPreferences() {
    CheckBoxPreference bluetoothPreference =
        (CheckBoxPreference) findPreference(getString(R.string.method_bluetooth_key));
//    TODO: Re-enable after bugfix release
//    CheckBoxPreference bluetoothCommandPreference =
//        (CheckBoxPreference) findPreference(getString(R.string.command_bluetooth_key));
    if (!BluetoothDeviceUtils.isBluetoothMethodSupported()) {
      // Disallow enabling bluetooth, if it's not supported
      bluetoothPreference.setChecked(false);
      bluetoothPreference.setEnabled(false);
      bluetoothPreference.setSummaryOff(R.string.eclair_required);

//      bluetoothCommandPreference.setChecked(false);
//      bluetoothCommandPreference.setEnabled(false);
//      bluetoothCommandPreference.setSummaryOff(R.string.eclair_required);
    } else {
      // Populate the list of bluetooth devices
      populateBluetoothDeviceList();

      // Make the pair devices preference go to the system preferences
      findPreference(getString(R.string.bluetooth_pairing_key))
          .setOnPreferenceClickListener(new OnPreferenceClickListener() {
            public boolean onPreferenceClick(Preference preference) {
              Intent settingsIntent = new Intent(Settings.ACTION_BLUETOOTH_SETTINGS);
              startActivity(settingsIntent);
              return false;
            }
          });
    }
  }

  /**
   * Populates the list preference with all available bluetooth devices.
   */
  private void populateBluetoothDeviceList() {
    // Build the list of entries and their values
    List<String> sourceEntries = new ArrayList<String>();
    List<String> sourceEntryValues = new ArrayList<String>();

    // First value is "any"
    sourceEntries.add(getString(R.string.bluetooth_device_any));
    sourceEntryValues.add(BluetoothDeviceUtils.ANY_DEVICE);

    // Other values are actual devices
    BluetoothDeviceUtils.getInstance().populateDeviceLists(sourceEntries, sourceEntryValues);

    // Create a separate list for the target devices
    List<String> targetEntries = new ArrayList<String>(sourceEntries);
    List<String> targetEntryValues = new ArrayList<String>(sourceEntryValues);

    // Add the "all" option only to the targets
    targetEntries.add(0, getString(R.string.bluetooth_device_all));
    targetEntryValues.add(0, BluetoothDeviceUtils.ALL_DEVICES);

    CharSequence[] sourceEntriesArray = sourceEntries.toArray(new CharSequence[sourceEntries.size()]);
    CharSequence[] sourceEntryValuesArray = sourceEntryValues.toArray(new CharSequence[sourceEntryValues.size()]);
    CharSequence[] targetEntriesArray = targetEntries.toArray(new CharSequence[targetEntries.size()]);
    CharSequence[] targetEntryValuesArray = targetEntryValues.toArray(new CharSequence[targetEntryValues.size()]);

    ListPreference targetDevicePreference =
        (ListPreference) findPreference(getString(R.string.bluetooth_device_key));
    targetDevicePreference.setEntryValues(targetEntryValuesArray);
    targetDevicePreference.setEntries(targetEntriesArray);

    ListPreference sourceDevicePreference =
      (ListPreference) findPreference(getString(R.string.bluetooth_source_key));
    sourceDevicePreference.setEntryValues(sourceEntryValuesArray);
    sourceDevicePreference.setEntries(sourceEntriesArray);
  }

  /**
   * Configures miscellaneous preference actions.
   */
  private void configureMiscPreferences() {
    // Attach an action to send the test notification
    Preference testNotificationPreference = findPreference(getString(R.string.test_notification_key));
    testNotificationPreference.setOnPreferenceClickListener(new OnPreferenceClickListener() {
      public boolean onPreferenceClick(Preference preference) {
        sendTestNotification();
        return true;
      }
    });

    // Attach an action to open the about screen
    Preference aboutPreference = findPreference(getString(R.string.about_key));
    aboutPreference.setOnPreferenceClickListener(new OnPreferenceClickListener() {
      public boolean onPreferenceClick(Preference preference) {
        showAlertDialog(R.string.about_message, R.string.about_title);
        return true;
      }
    });

    // Show the version number in the about preference
    try {
      PackageInfo info = getPackageManager().getPackageInfo(getPackageName(), 0);
      String versionString = getString(R.string.version, info.versionName);
      aboutPreference.setSummary(versionString);
    } catch (NameNotFoundException e) {
      Log.e(NotifierConstants.LOG_TAG, "Can't find my own version", e);
    }

    // Attach an action to report a bug
    Preference bugReportPreference = findPreference(getString(R.string.report_bug_key));
    bugReportPreference.setOnPreferenceClickListener(new OnPreferenceClickListener() {
      public boolean onPreferenceClick(Preference preference) {
        BugReporter.reportBug(NotifierMain.this);
        return true;
      }
    });
  }

  /**
   * Toggles the service status between running and stopped.
   */
  private void setServiceStarted(boolean start) {
    boolean isServiceRunning = NotifierService.isRunning(this);
    int textId = -1;
    if (start && !isServiceRunning) {
      NotifierService.start(this);
      textId = R.string.service_started;
    } else if (!start && isServiceRunning) {
      NotifierService.stop(this);
      textId = R.string.service_stopped;
    }

    if (textId != -1) {
      Toast.makeText(this, textId, Toast.LENGTH_SHORT).show();
    }
  }

  /**
   * Sends a test notification.
   */
  private void sendTestNotification() {
    // TODO: Warn if none of the selected methods are available
    //       (e.g. bluetooth and/or IP turned off)

    String contents = getString(R.string.ping_contents);
    Notification notification =
        new Notification(this, NotificationType.PING, null, contents);
    NotifierService.startAndSend(this, notification);

    Toast.makeText(this, R.string.ping_sent, Toast.LENGTH_LONG).show();
  }

  /**
   * Shows an alert dialog with the given message and title, as well as an OK
   * button to dismiss it.
   *
   * @param messageId the ID of the message resource
   * @param titleId the ID of the title resource
   */
  private void showAlertDialog(int messageId, int titleId) {
    AlertDialog.Builder builder = new AlertDialog.Builder(NotifierMain.this);
    builder.setMessage(messageId);
    builder.setTitle(titleId);
    builder.setNeutralButton(android.R.string.ok, null);
    builder.create().show();
  }
}