package org.damazio.notifier;

import android.content.Context;
import android.content.SharedPreferences;

public class NotifierPreferences {

  private static final String PREFERENCES_NAME = "org.damazio.notifier.preferences";
  private static final String STARTUP_AT_BOOT_KEY = "startAtBoot";
  private static final String WIFI_METHOD_ENABLED = "wifiMethod";
  private static final String BLUETOOTH_METHOD_ENABLED = "bluetoothMethod";

  private final SharedPreferences preferences;

  public NotifierPreferences(Context context) {
    preferences = context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE);
  }

  public boolean isStartAtBootEnabled() {
    return preferences.getBoolean(STARTUP_AT_BOOT_KEY, false);
  }

  public void setStartAtBootEnabled(boolean enabled) {
    preferences.edit().putBoolean(STARTUP_AT_BOOT_KEY, enabled).commit();
  }

  public boolean isWifiMethodEnabled() {
    return preferences.getBoolean(WIFI_METHOD_ENABLED, true);
  }

  public void setWifiMethodEnabled(boolean enabled) {
    preferences.edit().putBoolean(WIFI_METHOD_ENABLED, enabled).commit();
  }

  public boolean isBluetoothMethodEnabled() {
    return preferences.getBoolean(BLUETOOTH_METHOD_ENABLED, true);
  }

  public void setBluetoothMethodEnabled(boolean enabled) {
    preferences.edit().putBoolean(BLUETOOTH_METHOD_ENABLED, enabled).commit();
  }
}
