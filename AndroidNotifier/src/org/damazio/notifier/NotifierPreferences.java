package org.damazio.notifier;

import java.util.Map;

import org.damazio.notifier.notification.BluetoothDeviceUtils;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.util.Log;

/**
 * Application preferences, used both by the service and by the settings UI.
 *
 * @author rdamazio
 */
public class NotifierPreferences {
  private static final String OLD_PREFERENCES_NAME = "org.damazio.notifier.preferences";

  private final SharedPreferences preferences;
  private final Context context;

  public NotifierPreferences(Context context) {
    this.context = context;

    this.preferences = PreferenceManager.getDefaultSharedPreferences(context);

    if (isFirstTime()) {
      maybeMigrateOldPreferences();
    }
  }

  /**
   * Copies all previous preferences from the private custom-named file to the
   * new, shared instance.
   */
  private void maybeMigrateOldPreferences() {
    SharedPreferences oldPreferences =
        context.getSharedPreferences(OLD_PREFERENCES_NAME, Context.MODE_PRIVATE);
    // Don't migrate if there are no old preferences.
    if (!oldPreferences.contains(context.getString(R.string.is_first_time_key)))
      return;

    Log.i(NotifierConstants.LOG_TAG, "Migrating old preferences");
    Map<String, ?> allPrefs = oldPreferences.getAll();
    Editor editor = preferences.edit();
    for (Map.Entry<String, ?> entry : allPrefs.entrySet()) {
      String key = entry.getKey();
      if (preferences.contains(key)) {
        Log.d(NotifierConstants.LOG_TAG, "Not migrating " + key + " - already exists");
        continue;
      }

      Object value = entry.getValue();
      if (value instanceof String) {
        editor.putString(key, (String) value);
      } else if (value instanceof Boolean) {
        editor.putBoolean(key, (Boolean) value);
      } else if (value instanceof Integer) {
        editor.putInt(key, (Integer) value);
      } else if (value instanceof Float) {
        editor.putFloat(key, (Float) value);
      } else if (value instanceof Long) {
        editor.putLong(key, (Long) value);
      } else {
        Log.e(NotifierConstants.LOG_TAG, "Unknown value " + value +
            " of type " + value.getClass().getName() + " for key " + key);
        continue;
      }

      Log.i(NotifierConstants.LOG_TAG, "Migrated key=" + key + "; value=" + value);
    }
    editor.commit();

    // Goodbye old preferences
    oldPreferences.edit().clear().commit();
  }

  /**
   * @return whether this is the first time starting the app
   */
  public boolean isFirstTime() {
    return preferences.getBoolean(context.getString(R.string.is_first_time_key), true);
  }

  /**
   * @param firstTime whether this is the first time starting the app
   */
  public void setFirstTime(boolean firstTime) {
    preferences.edit()
        .putBoolean(context.getString(R.string.is_first_time_key), firstTime)
        .commit();
  }

  /**
   * @return whether the service should start at boot
   */
  public boolean isStartAtBootEnabled() {
    return preferences.getBoolean(context.getString(R.string.start_at_boot_key), true);
  }

  /**
   * @return whether notifications should be sent over TCP/IP
   */
  public boolean isIpMethodEnabled() {
    return preferences.getBoolean(context.getString(R.string.method_ip_key), true);
  }

  /**
   * @return returns the current target IP address setting, which is one of
   *         "global", "dhcp" or "custom"
   */
  public String getTargetIpAddress() {
    return preferences.getString(context.getString(R.string.target_ip_address_key), "global");
  }

  /**
   * @return the custom IP address to be used if "custom" was returned by
   *         {@link #getTargetIpAddress}
   */
  public String getCustomTargetIpAddress() {
    return preferences.getString(context.getString(R.string.target_custom_ip_address_key),
        "255.255.255.255");
  }

  /**
   * Sets the custom TCP/IP address to use if "custom" is returned by
   * {@link #getTargetIpAddress}.
   *
   * @param address the IP address's textual representation
   */
  public void setCustomTargetIpAddress(String address) {
    preferences.edit()
        .putString(context.getString(R.string.target_custom_ip_address_key), address)
        .commit();
  }

  /**
   * @return one of "screen", "plugged" or "never", describing the system's
   *         current wifi sleep policy
   */
  public String getWifiSleepPolicy() {
    int value = Settings.System.getInt(context.getContentResolver(),
        Settings.System.WIFI_SLEEP_POLICY, Settings.System.WIFI_SLEEP_POLICY_DEFAULT);
    switch (value) {
      case Settings.System.WIFI_SLEEP_POLICY_DEFAULT:
        return "screen";
      case Settings.System.WIFI_SLEEP_POLICY_NEVER_WHILE_PLUGGED:
        return "plugged";
      case Settings.System.WIFI_SLEEP_POLICY_NEVER:
        return "never";
      default:
        Log.e(NotifierConstants.LOG_TAG, "Unknown sleep policy value " + value);
        return null;
    }
  }

  /**
   * Sets the value of the system's wifi sleep policy
   *
   * @param value one of "screen", "plugged", or "never"
   */
  public void setWifiSleepPolicy(String value) {
    int intValue = -1;
    if (value.equals("screen")) {
      intValue = Settings.System.WIFI_SLEEP_POLICY_DEFAULT;
    } else if (value.equals("plugged")) {
      intValue = Settings.System.WIFI_SLEEP_POLICY_NEVER_WHILE_PLUGGED;
    } else if (value.equals("never")) {
      intValue = Settings.System.WIFI_SLEEP_POLICY_NEVER;
    } else {
      Log.e(NotifierConstants.LOG_TAG, "Unknown sleep policy value " + value);
      return;
    }

    Settings.System.putInt(context.getContentResolver(),
        Settings.System.WIFI_SLEEP_POLICY, intValue);
  }

  /**
   * @return whether to enable wifi to send a notification
   */
  public boolean getEnableWifi() {
    return preferences.getBoolean(context.getString(R.string.enable_wifi_key), false);
  }

  /**
   * @return the address of the target bluetooth device, or "any"
   */
  public String getTargetBluetoothDevice() {
    return preferences.getString(context.getString(R.string.bluetooth_device_key),
                                 BluetoothDeviceUtils.ANY_DEVICE);
  }

  /**
   * @return whether notifications should be sent over bluetooth
   */
  public boolean isBluetoothMethodEnabled() {
    return preferences.getBoolean(context.getString(R.string.method_bluetooth_key), true);
  }

  /**
   * @return whether to enable bluetooth to send a notification
   */
  public boolean getEnableBluetooth() {
    return preferences.getBoolean(context.getString(R.string.enable_bluetooth_key), false);
  }
  
  /**
   * @return whether notifications should be sent over USB
   */
  public boolean isUsbMethodEnabled() {
    return preferences.getBoolean(context.getString(R.string.method_usb_key), false);
  }

  /**
   * @return whether to send notifications when the phone rings
   */
  public boolean isRingEventEnabled() {
    return preferences.getBoolean(context.getString(R.string.event_ring_key), true);
  }

  /**
   * @return whether to send notifications when an SMS is received
   */
  public boolean isSmsEventEnabled() {
    return preferences.getBoolean(context.getString(R.string.event_sms_key), true);
  }

  /**
   * @return whether to send notifications when an MMS is received
   */
  public boolean isMmsEventEnabled() {
    return preferences.getBoolean(context.getString(R.string.event_mms_key), true);
  }

  /**
   * @return whether to send notifications when the battery state changes
   */
  public boolean isBatteryEventEnabled() {
    return preferences.getBoolean(context.getString(R.string.event_battery_key), true);
  }

  /**
   * @return whether to send notifications when there's new voicemail
   */
  public boolean isVoicemailEventEnabled() {
    return preferences.getBoolean(context.getString(R.string.event_voicemail_key), true);
  }

  /**
   * @return whether to send notifications over UDP
   */
  public boolean isSendUdpEnabled() {
    return preferences.getBoolean(context.getString(R.string.send_udp_key), true);
  }

  /**
   * @return whether to send notifications over TCP
   */
  public boolean isSendTcpEnabled() {
    return preferences.getBoolean(context.getString(R.string.send_tcp_key), true);
  }

  /**
   * @return whether to send notifications over the cell phone network
   */
  public boolean getSendOverCellNetwork() {
    return preferences.getBoolean(context.getString(R.string.allow_cell_send_key), false);
  }

  public boolean isCommandEnabled() {
    return preferences.getBoolean(context.getString(R.string.command_enable_key), true);
  }

  public boolean isCallCommandEnabled() {
    return preferences.getBoolean(context.getString(R.string.command_call_key), true);
  }

  public boolean isSmsCommandEnabled() {
    return preferences.getBoolean(context.getString(R.string.command_sms_key), true);
  }

  public boolean isBluetoothCommandEnabled() {
    return preferences.getBoolean(context.getString(R.string.command_bluetooth_key), true);
  }

  public boolean isWifiCommandEnabled() {
    return preferences.getBoolean(context.getString(R.string.command_wifi_key), true);
  }

  /**
   * @return the address of the source bluetooth device, or "any"
   */
  public String getSourceBluetoothDevice() {
    return preferences.getString(context.getString(R.string.bluetooth_source_key),
                                 BluetoothDeviceUtils.ANY_DEVICE);
  }
}
