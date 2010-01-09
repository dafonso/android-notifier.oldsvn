package org.damazio.notifier;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Application preferences, used both by the service and by the settings UI.
 *
 * @author rdamazio
 */
public class NotifierPreferences {

  private static final String PREFERENCES_NAME = "org.damazio.notifier.preferences";

  private final SharedPreferences preferences;
  private final Context context;

  public NotifierPreferences(Context context) {
    this.context = context;
    preferences = context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE);
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
