package org.damazio.notifier;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

public class NotifierPreferences {

  private static final String PREFERENCES_NAME = "org.damazio.notifier.preferences";
  private static final String STARTUP_AT_BOOT_KEY = "startAtBoot";
  private static final String WIFI_METHOD_ENABLED = "wifiMethod";
  private static final String BLUETOOTH_METHOD_ENABLED = "bluetoothMethod";
  private static final String USB_METHOD_ENABLED = "usbMethod";
  private static final String RING_EVENT_ENABLED = "ringEvent";
  private static final String SMS_EVENT_ENABLED = "smsEvent";
  private static final String MMS_EVENT_ENABLED = "mmsEvent";
  private static final String BATTERY_EVENT_ENABLED = "batteryEvent";

  private final SharedPreferences preferences;
  private Editor editor;

  public NotifierPreferences(Context context) {
    preferences = context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE);
  }

  public void saveChanges() {
    if (editor != null) {
      editor.commit();
    }
  }

  public void discardChanges() {
    editor = null;
  }
  
  public boolean isStartAtBootEnabled() {
    return preferences.getBoolean(STARTUP_AT_BOOT_KEY, false);
  }

  public void setStartAtBootEnabled(boolean enabled) {
    ensureEditing();
    editor.putBoolean(STARTUP_AT_BOOT_KEY, enabled);
  }

  public boolean isWifiMethodEnabled() {
    return preferences.getBoolean(WIFI_METHOD_ENABLED, true);
  }

  public void setWifiMethodEnabled(boolean enabled) {
    ensureEditing();
    editor.putBoolean(WIFI_METHOD_ENABLED, enabled);
  }

  public boolean isBluetoothMethodEnabled() {
    return preferences.getBoolean(BLUETOOTH_METHOD_ENABLED, true);
  }

  public void setBluetoothMethodEnabled(boolean enabled) {
    ensureEditing();
    editor.putBoolean(BLUETOOTH_METHOD_ENABLED, enabled);
  }

  public boolean isUsbMethodEnabled() {
    return preferences.getBoolean(USB_METHOD_ENABLED, false);
  }

  public void setUsbMethodEnabled(boolean enabled) {
    ensureEditing();
    editor.putBoolean(USB_METHOD_ENABLED, enabled);
  }

  public boolean isRingEventEnabled() {
    return preferences.getBoolean(RING_EVENT_ENABLED, true);
  }

  public void setRingEventEnabled(boolean enabled) {
    ensureEditing();
    editor.putBoolean(RING_EVENT_ENABLED, enabled);
  }

  public boolean isSmsEventEnabled() {
    return preferences.getBoolean(SMS_EVENT_ENABLED, true);
  }

  public void setSmsEventEnabled(boolean enabled) {
    ensureEditing();
    editor.putBoolean(SMS_EVENT_ENABLED, enabled);
  }

  public boolean isMmsEventEnabled() {
    return preferences.getBoolean(MMS_EVENT_ENABLED, true);
  }

  public void setMmsEventEnabled(boolean enabled) {
    ensureEditing();
    editor.putBoolean(MMS_EVENT_ENABLED, enabled);
  }

  public boolean isBatteryEventEnabled() {
    return preferences.getBoolean(BATTERY_EVENT_ENABLED, true);
  }

  public void setBatteryEventEnabled(boolean enabled) {
    ensureEditing();
    editor.putBoolean(BATTERY_EVENT_ENABLED, enabled);
  }

  private void ensureEditing() {
    if (editor == null) {
      editor = preferences.edit();
    }
  }
}
