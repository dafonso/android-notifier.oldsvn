package org.damazio.notifier;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

/**
 * Application preferences, used both by the service and by the settings UI.
 *
 * @author rdamazio
 */
public class NotifierPreferences {

  private static final String PREFERENCES_NAME = "org.damazio.notifier.preferences";
  private static final String IS_FIRST_TIME_KEY = "isFirstTime";
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

  /**
   * Saves any changes that have been made to these settings.
   */
  public void saveChanges() {
    if (editor != null) {
      editor.commit();
    }
  }

  /**
   * Discards any changes that have been made to these settings.
   */
  public void discardChanges() {
    editor = null;
  }

  /**
   * @return whether this is the first time starting the app
   */
  public boolean isFirstTime() {
    return preferences.getBoolean(IS_FIRST_TIME_KEY, true);
  }

  /**
   * @param firstTime whether this is the first time starting the app
   */
  public void setFirstTime(boolean firstTime) {
    ensureEditing();
    editor.putBoolean(IS_FIRST_TIME_KEY, firstTime);
  }

  /**
   * @return whether the service should start at boot
   */
  public boolean isStartAtBootEnabled() {
    return preferences.getBoolean(STARTUP_AT_BOOT_KEY, false);
  }

  /**
   * @param enabled whether the service should start at boot
   */
  public void setStartAtBootEnabled(boolean enabled) {
    ensureEditing();
    editor.putBoolean(STARTUP_AT_BOOT_KEY, enabled);
  }

  /**
   * @return whether notifications should be sent over wifi
   */
  public boolean isWifiMethodEnabled() {
    return preferences.getBoolean(WIFI_METHOD_ENABLED, true);
  }

  /**
   * @param enabled whether notifications should be sent over wifi
   */
  public void setWifiMethodEnabled(boolean enabled) {
    ensureEditing();
    editor.putBoolean(WIFI_METHOD_ENABLED, enabled);
  }

  /**
   * @return whether notifications should be sent over bluetooth
   */
  public boolean isBluetoothMethodEnabled() {
    return preferences.getBoolean(BLUETOOTH_METHOD_ENABLED, true);
  }

  /**
   * @param enabled whether notifications should be sent over bluetooth
   */
  public void setBluetoothMethodEnabled(boolean enabled) {
    ensureEditing();
    editor.putBoolean(BLUETOOTH_METHOD_ENABLED, enabled);
  }

  /**
   * @return whether notifications should be sent over USB
   */
  public boolean isUsbMethodEnabled() {
    return preferences.getBoolean(USB_METHOD_ENABLED, false);
  }

  /**
   * @param enabled whether notifications should be sent over USB
   */
  public void setUsbMethodEnabled(boolean enabled) {
    ensureEditing();
    editor.putBoolean(USB_METHOD_ENABLED, enabled);
  }

  /**
   * @return whether to send notifications when the phone rings
   */
  public boolean isRingEventEnabled() {
    return preferences.getBoolean(RING_EVENT_ENABLED, true);
  }

  /**
   * @param enabled whether to send notifications when the phone rings
   */
  public void setRingEventEnabled(boolean enabled) {
    ensureEditing();
    editor.putBoolean(RING_EVENT_ENABLED, enabled);
  }

  /**
   * @return whether to send notifications when an SMS is received
   */
  public boolean isSmsEventEnabled() {
    return preferences.getBoolean(SMS_EVENT_ENABLED, true);
  }

  /**
   * @param enabled whether to send notifications when an SMS is received
   */
  public void setSmsEventEnabled(boolean enabled) {
    ensureEditing();
    editor.putBoolean(SMS_EVENT_ENABLED, enabled);
  }

  /**
   * @return whether to send notifications when an MMS is received
   */
  public boolean isMmsEventEnabled() {
    return preferences.getBoolean(MMS_EVENT_ENABLED, true);
  }

  /**
   * @param enabled whether to send notifications when an MMS is received
   */
  public void setMmsEventEnabled(boolean enabled) {
    ensureEditing();
    editor.putBoolean(MMS_EVENT_ENABLED, enabled);
  }

  /**
   * @return whether to send notifications when the battery state changes
   */
  public boolean isBatteryEventEnabled() {
    return preferences.getBoolean(BATTERY_EVENT_ENABLED, true);
  }

  /**
   * @param enabled whether to send notifications when the battery state changes
   */
  public void setBatteryEventEnabled(boolean enabled) {
    ensureEditing();
    editor.putBoolean(BATTERY_EVENT_ENABLED, enabled);
  }

  /**
   * Ensures we're ready to change preferences.
   */
  private void ensureEditing() {
    if (editor == null) {
      editor = preferences.edit();
    }
  }
}
