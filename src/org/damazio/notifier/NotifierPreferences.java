package org.damazio.notifier;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.util.Log;

/**
 * Application preferences, used both by the service and by the settings UI.
 *
 * @author rdamazio
 */
public class NotifierPreferences {
  // TODO(rdamazio): Migrate from old preferences file when detected
  private static final String OLD_PREFERENCES_NAME = "org.damazio.notifier.preferences";

  private final SharedPreferences preferences;
  private final Context context;

  public NotifierPreferences(Context context) {
    this.context = context;

    this.preferences = PreferenceManager.getDefaultSharedPreferences(context);
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
    preferences.edit().putBoolean(context.getString(R.string.is_first_time_key), firstTime).commit();
  }

  /**
   * @return whether the service should start at boot
   */
  public boolean isStartAtBootEnabled() {
    return preferences.getBoolean(context.getString(R.string.start_at_boot_key), true);
  }

  /**
   * @return whether notifications should be sent over wifi
   */
  public boolean isWifiMethodEnabled() {
    return preferences.getBoolean(context.getString(R.string.method_wifi_key), true);
  }

  public String getWifiTargetIpAddress() {
    return preferences.getString(context.getString(R.string.target_ip_address_key), "global");
  }
  
  public String getCustomWifiTargetIpAddress() {
    return preferences.getString(context.getString(R.string.target_custom_ip_address_key), "255.255.255.255");
  }

  public void setCustomWifiTargetIpAddress(String address) {
    preferences.edit().putString(context.getString(R.string.target_custom_ip_address_key), address).commit();
  }

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

  public boolean getEnableWifi() {
    return preferences.getBoolean(context.getString(R.string.enable_wifi_key), false);
  }
 
  /**
   * @return whether notifications should be sent over bluetooth
   */
  public boolean isBluetoothMethodEnabled() {
    return preferences.getBoolean(context.getString(R.string.method_bluetooth_key), true);
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
}
