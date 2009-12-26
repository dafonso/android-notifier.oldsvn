package org.damazio.notifier.notification;

import android.content.Context;
import android.provider.Settings;
import android.util.Log;

public class DeviceIdProvider {

  private DeviceIdProvider() {
    
  }

  public static String getDeviceId(Context context) {
    String deviceId = Settings.System.getString(context.getContentResolver(), Settings.System.ANDROID_ID);
    if (deviceId == null) {
      deviceId = Long.toHexString(Double.doubleToRawLongBits(Math.random() * Long.MAX_VALUE));
      Log.d("RemoteNotifier", "No device ID found - created random ID " + deviceId);
    }
    return deviceId;
  }
}
