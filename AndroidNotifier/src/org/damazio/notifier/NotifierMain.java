package org.damazio.notifier;

import org.damazio.notifier.notification.Notification;
import org.damazio.notifier.notification.NotificationType;
import org.damazio.notifier.notification.Notifier;
import org.damazio.notifier.service.NotificationService;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.Toast;

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

    // Attach UI handlers
	Button testButton = (Button) findViewById(R.id.send_test_notification);
	testButton.setOnClickListener(new OnClickListener() {
      public void onClick(View v) {
        sendTestNotification();
      }
    });

	Button toggleServiceButton = (Button) findViewById(R.id.toggle_service);
	toggleServiceButton.setOnClickListener(new OnClickListener() {
      public void onClick(View v) {
        toggleServiceStatus();
      }
    });
    // TODO: Show service status

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

    // Start the service
    NotificationService.start(this);
  }

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

  protected void revertSettings() {
    preferences.discardChanges();
    loadSettings();
  }

  private void toggleServiceStatus() {
    // TODO: Implement
  }

  private Notifier getNotifier() {
    if (notifier == null) {
      notifier = new Notifier(this, preferences);
    }
    return notifier;
  }

  private void sendTestNotification() {
    Notification notification =
        new Notification(NotifierMain.this, NotificationType.PING, "Test notification");
    getNotifier().sendNotification(notification);
  }
}