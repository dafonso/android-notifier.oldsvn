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
package org.damazio.notifier.notification.methods;

import java.io.IOException;
import java.util.UUID;

import org.damazio.notifier.NotifierConstants;
import org.damazio.notifier.NotifierPreferences;
import org.damazio.notifier.util.BluetoothDeviceUtils;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.os.SystemClock;
import android.util.Log;

/**
 * A notification method which sends notifications over a Bluetooth RFCOMM
 * channel. The channel is opened and closed for every notification.
 * 
 * This class should only be loaded if we're running on API level 5 or above.
 *
 * @author Rodrigo Damazio
 */
class BluetoothNotificationMethod implements NotificationMethod {

  private static final String NOTIFICATION_UUID_STR = "7674047E-6E47-4BF0-831F-209E3F9DD23F";
  private static final UUID NOTIFICATION_UUID = UUID.fromString(NOTIFICATION_UUID_STR);
  private static final int MAX_RETRIES = 10;
  private static final int RETRY_WAIT_MILLIS = 1000;

  /**
   * Class which waits for bluetooth to be enabled before sending a
   * notification.
   * It will only wait up to a certain time before giving up.
   */
  private class BluetoothDelayedNotifier extends MethodEnablingNotifier<BluetoothDevice> {
    private static final String BLUETOOTH_LOCK_TAG = "org.damazio.notifier.BluetoothEnable";
    private WakeLock wakeLock;

    public BluetoothDelayedNotifier(byte[] payload, BluetoothDevice target, boolean isForeground,
        NotificationCallback callback, boolean previousEnabledState) {
      super(payload, target, isForeground, callback, previousEnabledState,
          BluetoothNotificationMethod.this);
    }

    @Override
    protected synchronized void acquireLock() {
      if (wakeLock == null) {
        // No bluetooth locking available - use a standard power lock
        PowerManager powerManager = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, BLUETOOTH_LOCK_TAG);
      }

      wakeLock.acquire();
    }

    @Override
    protected boolean isMediumReady() {
      return isBluetoothReady();
    }

    @Override
    protected void releaseLock() {
      wakeLock.release();
    }

    @Override
    protected void setMediumEnabled(boolean enabled) {
      if (enabled) {
        bluetoothAdapter.enable();
      } else {
        bluetoothAdapter.disable();
      }
    }
  }

  private final NotifierPreferences preferences;
  private final Context context;
  private final BluetoothAdapter bluetoothAdapter;
  private final BluetoothDeviceUtils deviceUtils;

  public BluetoothNotificationMethod(Context context, NotifierPreferences preferences) {
    this.context = context;
    this.preferences = preferences;
    this.bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    this.deviceUtils = BluetoothDeviceUtils.getInstance();
  }

  public void sendNotification(byte[] payload, Object targetObj, NotificationCallback callback,
      boolean isForeground) {
    BluetoothDevice target = (BluetoothDevice) targetObj;

    if (isBluetoothReady()) {
      doSendNotification(payload, target, callback);
      return;
    }

    // Delay the notification if either bluetooth is disabled, or if it's not ready
    // because it's in discovery mode.
    if (preferences.getAutoEnableBluetooth()) {
      Log.d(NotifierConstants.LOG_TAG, "Enabling bluetooth and delaying notification");
      sendDelayedNotification(payload, target, isForeground, callback);
      return;
    } else if (bluetoothAdapter.isDiscovering()) {
      Log.d(NotifierConstants.LOG_TAG, "Delaying bluetooth notification until discovery is done");
      bluetoothAdapter.cancelDiscovery();
      sendDelayedNotification(payload, target, isForeground, callback);
      return;
    } else {
      Log.e(NotifierConstants.LOG_TAG, "Not sending bluetooth notification - not enabled");
      callback.notificationDone(target, null);
    }
  }

  /**
   * Sends the given notification later, after bluetooth is enabled and ready.
   *
   * @param notification the notification to send
   * @param callback 
   */
  private void sendDelayedNotification(byte[] payload, BluetoothDevice target, boolean isForeground,
      NotificationCallback callback) {
    new BluetoothDelayedNotifier(payload, target, isForeground, callback,
        bluetoothAdapter.isEnabled()).start();
  }

  /**
   * Actually sends the notification to the device.
   * 
   * TODO(rdamazio): Sometimes, this will still give an error if too many
   *                 notifications happen in parallel.
   *
   * @param notification the notification to send
   * @param callback 
   */
  private synchronized void doSendNotification(byte[] payload,
      BluetoothDevice targetDevice, NotificationCallback callback) {
    bluetoothAdapter.cancelDiscovery();

    int retries = 0;
    while (true) {
      BluetoothSocket socket = null;
      try {
        socket = targetDevice.createRfcommSocketToServiceRecord(NOTIFICATION_UUID);

        Log.d(NotifierConstants.LOG_TAG,
            "Connecting to Bluetooth device " + targetDevice.getName());
        socket.connect();
        socket.getOutputStream().write(payload);
        socket.close();

        // Done, exit the while loop
        break;
      } catch (IOException e) {
        // Try to close the socket
        // (its finalize would do this too, but we expedite it)
        if (socket != null) {
          try {
            socket.close();
          } catch (IOException e1) {
            // Do nothing, we'll retry
            Log.e(NotifierConstants.LOG_TAG, "Error closing bluetooth socket", e1);
          }
        }

        retries++;
        if (retries > MAX_RETRIES) {
          Log.e(NotifierConstants.LOG_TAG,
              "Giving up sending bluetooth notification after " + retries + " retries", e);
          callback.notificationDone(targetDevice, e);
          return;
        }

        // Wait a bit, then let it retry
        Log.d(NotifierConstants.LOG_TAG, "Waiting to retry", e);
        SystemClock.sleep(RETRY_WAIT_MILLIS);
      }
    }

    callback.notificationDone(targetDevice, null);
    Log.d(NotifierConstants.LOG_TAG, "Sent notification over Bluetooth (" + retries + " retries).");
  }

  @Override
  public Iterable<BluetoothDevice> getTargets() {
    String targetStr = preferences.getTargetBluetoothDevice();
    return deviceUtils.findDevicesMatching(targetStr);
  }

  /**
   * @return whether the bluetooth system is enabled and not discovering
   */
  private boolean isBluetoothReady() {
    return bluetoothAdapter.isEnabled() && !bluetoothAdapter.isDiscovering();
  }

  public String getName() {
    return "bluetooth";
  }

  public boolean isEnabled() {
    return preferences.isBluetoothMethodEnabled() && bluetoothAdapter != null;
  }

  @Override
  public void shutdown() {
    // Nothing to do
  }
}
