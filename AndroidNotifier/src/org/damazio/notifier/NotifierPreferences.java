/*
 * Copyright 2010 Rodrigo Damazio <rodrigo@damazio.org>
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * 
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR
 * IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 * IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
 * NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
 * THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.damazio.notifier;

import org.damazio.notifier.notification.BluetoothDeviceUtils;
import org.damazio.notifier.util.Base64;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.util.Log;

/**
 * Application preferences, used both by the service and by the settings UI.
 *
 * @author rdamazio
 */
public class NotifierPreferences {
  private final SharedPreferences preferences;
  private final Context context;

  public NotifierPreferences(Context context) {
    this.context = context;

    this.preferences = PreferenceManager.getDefaultSharedPreferences(context);
  }

  public void registerOnSharedPreferenceChangeListener(
      OnSharedPreferenceChangeListener listener) {
    preferences.registerOnSharedPreferenceChangeListener(listener);
  }

  public void unregisterOnSharedPreferenceChangeListener(
      OnSharedPreferenceChangeListener listener) {
    preferences.unregisterOnSharedPreferenceChangeListener(listener);
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
   * @return whether the service should send notifications
   */
  public boolean areNotificationsEnabled() {
    return preferences.getBoolean(context.getString(R.string.notifications_enabled_key), true);
  }

  public void setNotificationsEnabled(boolean enabled) {
    preferences.edit()
        .putBoolean(context.getString(R.string.notifications_enabled_key), enabled)
        .commit();
  }

  /**
   * @return whether notifications should be sent over TCP/IP
   */
  public boolean isIpMethodEnabled() {
    return preferences.getBoolean(context.getString(R.string.method_ip_key), true);
  }

  /**
   * Sets whether notifications will be sent with the IP method.
   */
  public void setIpMethodEnabled(boolean ipEnabled) {
    preferences.edit()
        .putBoolean(context.getString(R.string.method_ip_key), ipEnabled)
        .commit();
  }

  /**
   * @return returns the current target IP address setting, which is one of
   *         "global", "dhcp" or "custom"
   */
  public String getTargetIpAddress() {
    return preferences.getString(context.getString(R.string.target_ip_address_key), "global");
  }

  public void setTargetIpAddress(String targetIp) {
    preferences.edit()
        .putString(context.getString(R.string.target_ip_address_key), targetIp)
        .commit();
  }

  /**
   * @return the custom IP addresses to be used if "custom" was returned by
   *         {@link #getTargetIpAddress}
   */
  public String[] getCustomTargetIpAddresses() {
    return preferences.getString(context.getString(R.string.target_custom_ips_key), "").split(",");
  }

  /**
   * Sets the custom IP addresses to be used if "custom" is the target type
   */
  public void setCustomTargetIpAddresses(String[] targetIps) {
    StringBuilder builder = new StringBuilder();
    for (int i = 0; i < targetIps.length; i++) {
      String targetIp = targetIps[i];

      if (i > 0) builder.append(',');
      builder.append(targetIp);
    }

    preferences.edit()
        .putString(context.getString(R.string.target_custom_ips_key), builder.toString())
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
  public boolean getWifiAutoEnable() {
    return preferences.getBoolean(context.getString(R.string.enable_wifi_key), false);
  }

  public void setWifiAutoEnable(boolean autoEnable) {
    preferences.edit()
        .putBoolean(context.getString(R.string.enable_wifi_key), autoEnable)
        .commit();
  }

  /**
   * @return the address of the target bluetooth device, or "any"
   */
  public String getTargetBluetoothDevice() {
    return preferences.getString(context.getString(R.string.bluetooth_device_key),
                                 BluetoothDeviceUtils.ALL_DEVICES);
  }

  /**
   * Changes the bluetooth device to send notifications to.
   *
   * @param bluetoothTarget the MAC address of the device
   */
  public void setTargetBluetoothDevice(String bluetoothTarget) {
    preferences.edit()
        .putString(context.getString(R.string.bluetooth_device_key), bluetoothTarget)
        .commit();
  }

  /**
   * @return whether notifications should be sent over bluetooth
   */
  public boolean isBluetoothMethodEnabled() {
    return preferences.getBoolean(context.getString(R.string.method_bluetooth_key), true);
  }

  /**
   * Sets whether notifications will be sent with the bluetooth method.
   */
  public void setBluetoothMethodEnabled(boolean btEnabled) {
    preferences.edit()
        .putBoolean(context.getString(R.string.method_bluetooth_key), btEnabled)
        .commit();
  }

  /**
   * @return whether to enable bluetooth to send a notification
   */
  public boolean getAutoEnableBluetooth() {
    return preferences.getBoolean(context.getString(R.string.enable_bluetooth_key), false);
  }

  /**
   * Sets whether to auto-enable bluetooth when sending a notification.
   */
  public void setAutoEnableBluetooth(boolean autoEnable) {
    preferences.edit()
        .putBoolean(context.getString(R.string.enable_bluetooth_key), autoEnable)
        .commit();
  }

  /**
   * @return whether notifications should be sent over USB
   */
  public boolean isUsbMethodEnabled() {
    return preferences.getBoolean(context.getString(R.string.method_usb_key), false);
  }

  /**
   * Sets whether notifications will be sent with the usb method.
   */
  public void setUsbMethodEnabled(boolean enabled) {
    preferences.edit()
        .putBoolean(context.getString(R.string.method_usb_key), enabled)
        .commit();
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
   * @return whether to send notifications when there's a new user message
   */
  public boolean isUserEventEnabled() {
    return preferences.getBoolean(context.getString(R.string.event_user_key), true);
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

  public boolean isIpCommandEnabled() {
    return preferences.getBoolean(context.getString(R.string.command_wifi_key), true);
  }

  /**
   * @return the address of the source bluetooth device, or "any"
   */
  public String getSourceBluetoothDevice() {
    return preferences.getString(context.getString(R.string.bluetooth_source_key),
                                 BluetoothDeviceUtils.ANY_DEVICE);
  }

  public boolean isServiceNotificationEnabled() {
    return preferences.getBoolean(context.getString(R.string.show_notification_icon_key), false);
  }

  public int getMinBatteryLevel() {
    return preferences.getInt(context.getString(R.string.battery_min_level_key), 0);
  }

  public int getMaxBatteryLevel() {
    return preferences.getInt(context.getString(R.string.battery_max_level_key), 0);
  }

  public int getMinBatteryLevelChange() {
    return preferences.getInt(context.getString(R.string.battery_min_level_change_key), 0);
  }

  public boolean isEncryptionEnabled() {
    return preferences.getBoolean(context.getString(R.string.enable_encryption_key), false);
  }

  /**
   * @return the notification encryption key, or null if one is not defined
   */
  public byte[] getEncryptionKey() {
    String base64Key = preferences.getString(
        context.getString(R.string.encryption_pass_key), "");
    if (base64Key.length() == 0) {
      return null;
    }

    try {
      return Base64.decode(base64Key, Base64.DEFAULT);
    } catch (IllegalArgumentException e) {
      Log.e(NotifierConstants.LOG_TAG, "Failed to read encryption key from '" + base64Key + "'.");
      return null;
    }
  }
}
