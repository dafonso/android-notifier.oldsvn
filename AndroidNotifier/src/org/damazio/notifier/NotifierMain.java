package org.damazio.notifier;

import org.damazio.notifier.notification.Notification;
import org.damazio.notifier.notification.NotificationMethods;
import org.damazio.notifier.notification.NotificationType;
import org.damazio.notifier.notification.Notifier;
import org.damazio.notifier.service.NotificationService;

import android.app.Activity;
import android.app.AlertDialog;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.CompoundButton.OnCheckedChangeListener;

/**
 * Main activity for the notifier.
 * This activity displays the application's settings,
 * and allows the user to manipulate the service.
 *
 * @author rdamazio
 */
public class NotifierMain extends Activity {
  private Notifier notifier;
  private NotifierPreferences preferences;

  // Widgets that hold preferences
  private CheckBox startAtBootView;
  private CheckBox wifiMethodView;
  private CheckBox bluetoothMethodView;
  private CheckBox usbMethodView;
  private CheckBox ringEventView;
  private CheckBox smsEventView;
  private CheckBox mmsEventView;
  private CheckBox batteryEventView;
  private Button toggleServiceButton;
  private TextView serviceStatusText;

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    // Initialize the UI
    setContentView(R.layout.main);
	setTitle(R.string.settings_title);
	
	// TODO(rdamazio): Show a first time welcome dialog telling the user to go
	//                 to the website to get the desktop client and instructions

	// Grab preference views
    startAtBootView = (CheckBox) findViewById(R.id.start_at_boot);
    wifiMethodView = (CheckBox) findViewById(R.id.method_wifi);
    bluetoothMethodView = (CheckBox) findViewById(R.id.method_bluetooth);
    usbMethodView = (CheckBox) findViewById(R.id.method_usb);
    ringEventView = (CheckBox) findViewById(R.id.event_ring);
    smsEventView = (CheckBox) findViewById(R.id.event_sms);
    mmsEventView = (CheckBox) findViewById(R.id.event_mms);
    batteryEventView = (CheckBox) findViewById(R.id.event_battery);

    // Load preferences
    preferences = new NotifierPreferences(this);
    loadSettings();

    // Start the service
    if (preferences.isStartAtBootEnabled()) {
      // Ensure the service is started if it should have been auto-started
      NotificationService.start(this);
    }

    // Attach UI handlers
	Button testButton = (Button) findViewById(R.id.send_test_notification);
	testButton.setOnClickListener(new OnClickListener() {
      public void onClick(View v) {
        sendTestNotification();
      }
    });

	toggleServiceButton = (Button) findViewById(R.id.toggle_service);
	toggleServiceButton.setOnClickListener(new OnClickListener() {
      public void onClick(View v) {
        toggleServiceStatus();
      }
    });
	serviceStatusText = (TextView) findViewById(R.id.service_status);

	Button saveSettingsButton = (Button) findViewById(R.id.save_settings);
	saveSettingsButton.setOnClickListener(new OnClickListener() {
      public void onClick(View v) {
        saveSettings();
      }
    });
    Button revertSettingsButton = (Button) findViewById(R.id.revert_settings);
    revertSettingsButton.setOnClickListener(new OnClickListener() {
      public void onClick(View v) {
        revertSettings();
      }
    });

    if (!NotificationMethods.isBluetoothMethodSupported()) {
      bluetoothMethodView.setChecked(false);
      bluetoothMethodView.setOnCheckedChangeListener(new OnCheckedChangeListener() {
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
          AlertDialog.Builder builder = new AlertDialog.Builder(NotifierMain.this);
          builder.setMessage(R.string.bluetooth_eclair);
          builder.setTitle(R.string.eclair_required);
          builder.setNeutralButton(android.R.string.ok, null);
          builder.create().show();

          bluetoothMethodView.setChecked(false);
        }
      });
    }

    // Update the status that shows whether the service is running
    updateServiceStatus();
  }

  /**
   * Loads all settings from the preferences into the UI.
   */
  private void loadSettings() {
    startAtBootView.setChecked(preferences.isStartAtBootEnabled());

    wifiMethodView.setChecked(preferences.isWifiMethodEnabled());
    bluetoothMethodView.setChecked(preferences.isBluetoothMethodEnabled());
    usbMethodView.setChecked(preferences.isUsbMethodEnabled());

    ringEventView.setChecked(preferences.isRingEventEnabled());
    smsEventView.setChecked(preferences.isSmsEventEnabled());
    mmsEventView.setChecked(preferences.isMmsEventEnabled());
    batteryEventView.setChecked(preferences.isBatteryEventEnabled());
  }

  /**
   * Saves all settings from the UI into the preferences.
   */
  private void saveSettings() {
    preferences.setStartAtBootEnabled(startAtBootView.isChecked());

    // TODO(rdamazio): Give a warning about bluetooth not working in android < 2.0
    preferences.setWifiMethodEnabled(wifiMethodView.isChecked());
    preferences.setBluetoothMethodEnabled(bluetoothMethodView.isChecked());
    preferences.setUsbMethodEnabled(usbMethodView.isChecked());

    preferences.setRingEventEnabled(ringEventView.isChecked());
    preferences.setSmsEventEnabled(smsEventView.isChecked());
    preferences.setMmsEventEnabled(mmsEventView.isChecked());
    preferences.setBatteryEventEnabled(batteryEventView.isChecked());

    preferences.saveChanges();

    Toast.makeText(this, R.string.settings_saved, Toast.LENGTH_LONG).show();
  }

  /**
   * Reverts any unsaved changes to settings, both in the preferences and in the
   * UI.
   */
  protected void revertSettings() {
    preferences.discardChanges();
    loadSettings();
  }

  /**
   * Toggles the service status between running and stopped.
   */
  private void toggleServiceStatus() {
    boolean isServiceRunning = NotificationService.isRunning(this);
    if (isServiceRunning) {
      NotificationService.stop(this);
    } else {
      NotificationService.start(this);
    }
    updateServiceStatus();
  }

  /**
   * Updates the service status on the UI.
   */
  private void updateServiceStatus() {
    boolean isServiceRunning = NotificationService.isRunning(this);
      serviceStatusText.setText(isServiceRunning
        ? R.string.service_status_running
        : R.string.service_status_stopped);
      toggleServiceButton.setText(isServiceRunning
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
  }
}