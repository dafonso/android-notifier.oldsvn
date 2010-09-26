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
    String deviceId =
        Settings.System.getString(context.getContentResolver(), Settings.System.ANDROID_ID);
    if (deviceId == null) {
      deviceId = Long.toHexString(Double.doubleToRawLongBits(Math.random() * Long.MAX_VALUE));
      Log.d(NotifierConstants.LOG_TAG, "No device ID found - created random ID " + deviceId);
    }
    return deviceId;
  }
}
