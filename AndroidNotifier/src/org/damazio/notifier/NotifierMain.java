package org.damazio.notifier;

import org.damazio.notifier.notification.Notification;
import org.damazio.notifier.notification.NotificationMethods;
import org.damazio.notifier.notification.NotificationType;
import org.damazio.notifier.notification.Notifier;
import org.damazio.notifier.service.NotificationService;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.Preference.OnPreferenceClickListener;
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

    // Initialize the preferences UI
    addPreferencesFromResource(R.xml.preferences);

    // Load preferences
    preferences = new NotifierPreferences(this);

    // Show welcome screen if it's the first time
    if (preferences.isFirstTime()) {
      preferences.setFirstTime(false);

      showAlertDialog(R.string.about_message, R.string.welcome_title);
    }

    // Start the service
    if (preferences.isStartAtBootEnabled()) {
      // Ensure the service is started if it should have been auto-started
      NotificationService.start(this);
    }

    CheckBoxPreference bluetoothPreference = (CheckBoxPreference) findPreference(getString(R.string.method_bluetooth_key));
    Preference bluetoothOptionsPreference = findPreference(getString(R.string.method_bluetooth_options_key));
    if (!NotificationMethods.isBluetoothMethodSupported()) {
      // Disallow enabling bluetooth, if it's not supported
      bluetoothPreference.setChecked(false);
      bluetoothPreference.setEnabled(false);
      bluetoothPreference.setSummaryOff(R.string.eclair_required);
      bluetoothOptionsPreference.setEnabled(false);
    } else {
      // Disable bluetooth options if bluetooth is disabled
      attachCheckboxToEnable(bluetoothPreference, bluetoothOptionsPreference);
    }

    // Disable wifi options if wifi is disabled
    attachCheckboxToEnable((CheckBoxPreference) findPreference(getString(R.string.method_wifi_key)),
                        findPreference(getString(R.string.method_wifi_options_key)));

    // Attach custom IP address selector
    Preference ipAddressPreference = findPreference(getString(R.string.target_ip_address_key));
    ipAddressPreference.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
      public boolean onPreferenceChange(Preference preference, Object newValue) {
        String value = (String) newValue;
        if (value.equals("custom")) {
          selectCustomIpAddress();
        }

        return true;
      }

    });

    // Load wifi sleep policy from system settings, and save back only there
    ListPreference sleepPolicyPreference = (ListPreference) findPreference(getString(R.string.wifi_sleep_policy_key));
    sleepPolicyPreference.setValue(preferences.getWifiSleepPolicy());
    sleepPolicyPreference.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
      public boolean onPreferenceChange(Preference preference, Object newValue) {
        preferences.setWifiSleepPolicy((String) newValue);
        return false;
      }
    });

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
   * Makes changing of the values on the given checkbox enable and disable
   * the given destination preference.
   * 
   * @param checkbox the checkbox to listen for value changes
   * @param dest the destination which will be enabled/disabled with it
   */
  private void attachCheckboxToEnable(CheckBoxPreference checkbox, final Preference dest) {
    checkbox.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
      public boolean onPreferenceChange(Preference preference, Object newValue) {
        boolean value = ((Boolean) newValue).booleanValue();
        dest.setEnabled(value);
        return true;
      }
    });
  }

  /**
   * Toggles the service status between running and stopped.
   */
  private void toggleServiceStatus() {
    boolean isServiceRunning = NotificationService.isRunning(this);
    if (isServiceRunning) {
      NotificationService.stop(this);

      Toast.makeText(this, R.string.service_stopped, Toast.LENGTH_SHORT).show();
    } else {
      NotificationService.start(this);

      Toast.makeText(this, R.string.service_started, Toast.LENGTH_SHORT).show();
    }
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

  private void selectCustomIpAddress() {
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

    alert.setNegativeButton(android.R.string.cancel,
        new DialogInterface.OnClickListener() {
          public void onClick(DialogInterface dialog, int whichButton) {
            // TODO
          }
        });

    alert.show();
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