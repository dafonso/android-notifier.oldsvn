package org.damazio.notifier.notification;

import org.damazio.notifier.NotifierConstants;

import android.content.Context;
import android.provider.Settings;
import android.util.Log;

/**
 * Provider of unique IDs for the device.
 *
 * @author rdamazio
 */
public class DeviceIdProvider {

  private DeviceIdProvider() {
    
  }

  /**
   * Get a unique ID to identify this device.
   * The ID shouldn't ever change for the same device, but should usually be
   * different for different devices.
   * For emulators, this returns a random ID.
   *
   * @param context a context to get content from
   * @return the unique ID
   */
  public static String getDeviceId(Context context) {
    String deviceId = Settings.System.getString(context.getContentResolver(), Settings.System.ANDROID_ID);
    if (deviceId == null) {
      deviceId = Long.toHexString(Double.doubleToRawLongBits(Math.random() * Long.MAX_VALUE));
      Log.d(NotifierConstants.LOG_TAG, "No device ID found - created random ID " + deviceId);
    }
    return deviceId;
  }
}
