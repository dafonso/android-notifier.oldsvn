package org.damazio.notifier;

import java.util.ArrayList;
import java.util.List;

import org.damazio.notifier.notification.BluetoothDeviceUtils;
import org.damazio.notifier.notification.Notification;
import org.damazio.notifier.notification.NotificationType;
import org.damazio.notifier.notification.Notifier;
import org.damazio.notifier.service.NotificationService;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.Preference.OnPreferenceClickListener;
import android.provider.Settings;
import android.widget.EditText;
import android.widget.Toast;

/**
 * Main activity for the notifier.
 * This activity displays the application's settings,
 * and allows the user to manipulate the service.
 *
 * @author rdamazio
 */
public class NotifierMain extends PreferenceActivity {
  private Notifier notifier;
  private NotifierPreferences preferences;
  private Preference serviceStatePreference;

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

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
    configureWifiPreferences();
    configureServicePreferences();
    configureMiscPreferences();
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
      NotificationService.start(this);
    }
  }

  /**
   * Configures preference actions related to the service.
   */
  private void configureServicePreferences() {
    // Attach an action to start and stop the service
    serviceStatePreference = findPreference(getString(R.string.service_state_key));
    serviceStatePreference.setOnPreferenceClickListener(new OnPreferenceClickListener() {
      public boolean onPreferenceClick(Preference preference) {
        toggleServiceStatus();
        return true;
      }
    });

    // Update the status that shows whether the service is initially running
    updateServiceStatus();
  }

  /**
   * Configures preference actions related to Wifi.
   */
  private void configureWifiPreferences() {
    // Attach custom IP address selector
    Preference ipAddressPreference = findPreference(getString(R.string.target_ip_address_key));
    ipAddressPreference.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
      public boolean onPreferenceChange(Preference preference, Object newValue) {
        String value = (String) newValue;
        if (value.equals("custom")) {
          selectCustomIpAddress((ListPreference) preference);
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
   * Opens a dialog asking the user for the custom IP address to use.
   * If the user cancels the dialog, the preference will be returned to its
   * previous value.
   *
   * @param preference the IP address type preference being set
   */
  private void selectCustomIpAddress(final ListPreference preference) {
    AlertDialog.Builder alert = new AlertDialog.Builder(NotifierMain.this);
    alert.setTitle(R.string.custom_ip_title);
    alert.setMessage(R.string.custom_ip);

    // Set an EditText view to get user input 
    final EditText input = new EditText(NotifierMain.this);
    input.setText(preferences.getCustomWifiTargetIpAddress());
    alert.setView(input);

    alert.setPositiveButton(android.R.string.ok,
        new DialogInterface.OnClickListener() {
          public void onClick(DialogInterface dialog, int whichButton) {
            String value = input.getText().toString();
            preferences.setCustomWifiTargetIpAddress(value);
          }
        });

    final String previousAddress = preference.getValue();
    alert.setNegativeButton(android.R.string.cancel,
        new DialogInterface.OnClickListener() {
          public void onClick(DialogInterface dialog, int whichButton) {
            preference.setValue(previousAddress);
          }
        });

    alert.show();
  }

  /**
   * Configures preference actions related to bluetooth.
   */
  private void configureBluetoothPreferences() {
    CheckBoxPreference bluetoothPreference =
        (CheckBoxPreference) findPreference(getString(R.string.method_bluetooth_key));
    if (!BluetoothDeviceUtils.isBluetoothMethodSupported()) {
      // Disallow enabling bluetooth, if it's not supported
      bluetoothPreference.setChecked(false);
      bluetoothPreference.setEnabled(false);
      bluetoothPreference.setSummaryOff(R.string.eclair_required);
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
    List<String> entries = new ArrayList<String>();
    List<String> entryValues = new ArrayList<String>();

    // First value is "any"
    entries.add(getString(R.string.bluetooth_device_any));
    entryValues.add(BluetoothDeviceUtils.ANY_DEVICE);

    // Other values are actual devices
    BluetoothDeviceUtils.getInstance().populateDeviceLists(entries, entryValues);

    CharSequence[] entriesArray = entries.toArray(new CharSequence[entries.size()]);
    CharSequence[] entryValuesArray = entryValues.toArray(new CharSequence[entryValues.size()]);
    ListPreference devicesPreference =
        (ListPreference) findPreference(getString(R.string.bluetooth_device_key));
    devicesPreference.setEntryValues(entryValuesArray);
    devicesPreference.setEntries(entriesArray);
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
  }

  /**
   * Toggles the service status between running and stopped.
   */
  private void toggleServiceStatus() {
    boolean isServiceRunning = NotificationService.isRunning(this);
    int textId;
    if (isServiceRunning) {
      NotificationService.stop(this);
      textId = R.string.service_stopped;
    } else {
      NotificationService.start(this);
      textId = R.string.service_started;
    }
    Toast.makeText(this, textId, Toast.LENGTH_SHORT).show();
    updateServiceStatus();
  }

  /**
   * Updates the service status on the UI.
   */
  private void updateServiceStatus() {
    boolean isServiceRunning = NotificationService.isRunning(this);
      serviceStatePreference.setSummary(isServiceRunning
          ? R.string.service_status_running
          : R.string.service_status_stopped);
      serviceStatePreference.setTitle(isServiceRunning
          ? R.string.stop_service
          : R.string.start_service);
  }

  /**
   * Sends a test notification.
   */
  private void sendTestNotification() {
    if (notifier == null) {
      notifier = new Notifier(this, preferences);
    }

    String contents = getString(R.string.ping_contents);
    Notification notification =
        new Notification(NotifierMain.this, NotificationType.PING, contents);
    notifier.sendNotification(notification);

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