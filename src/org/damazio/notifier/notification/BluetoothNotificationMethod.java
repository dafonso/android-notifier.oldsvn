package org.damazio.notifier.notification;

import java.io.IOException;
import java.util.Set;
import java.util.UUID;

import org.damazio.notifier.NotifierConstants;
import org.damazio.notifier.NotifierPreferences;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothClass;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.util.Log;

/**
 * A notification method which sends notifications over a Bluetooth RFCOMM
 * channel. The channel is opened and closed for every notification.
 * 
 * This class should only be loaded if we're running on API level 5 or above.
 *
 * @author rdamazio
 */
public class BluetoothNotificationMethod implements NotificationMethod {

  private static final String NOTIFICATION_UUID_STR = "7674047E-6E47-4BF0-831F-209E3F9DD23F";
  private static final UUID NOTIFICATION_UUID = UUID.fromString(NOTIFICATION_UUID_STR);
  private final NotifierPreferences preferences;

  public BluetoothNotificationMethod(NotifierPreferences preferences) {
    this.preferences = preferences;
  }

  public void sendNotification(Notification notification) {
    if (!preferences.isBluetoothMethodEnabled()) {
      return;
    }

    BluetoothAdapter bluetoothAdapter = getBluetoothAdapter();
    if (bluetoothAdapter == null) {
      // No bluetooth support
      Log.e(NotifierConstants.LOG_TAG, "No bluetooth support");
      return;
    }
    if (!bluetoothAdapter.isEnabled()) {
      // Bluetooth disabled
      Log.e(NotifierConstants.LOG_TAG, "Not sending bluetooth notification - not enabled");
      return;
    }

    Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();
    if (pairedDevices.isEmpty()) {
      // No devices
      Log.e(NotifierConstants.LOG_TAG, "Not sending bluetooth notification - no paired devices.");
      return;
    }

    BluetoothSocket socket = null;
    for (BluetoothDevice device : pairedDevices) {
      if (device.getBluetoothClass().getMajorDeviceClass() == BluetoothClass.Device.Major.COMPUTER) {
        try {
          socket = device.createRfcommSocketToServiceRecord(NOTIFICATION_UUID);
        } catch (IOException e) {
          // Couldn't create socket with this UUID on this device
          // (but other devices may accept it)
        }
      }

      if (socket != null) {
        break;
      }
    }

    if (socket == null) {
      Log.e(NotifierConstants.LOG_TAG, "Unable to find a proper bluetooth device to send notifications to");
      return;
    }

    // TODO(rdamazio): Add an end-of-message marker in case the packets get split
    byte[] messageBytes = notification.toString().getBytes();
    try {
      Log.d(NotifierConstants.LOG_TAG, "Connecting to Bluetooth device " + socket.getRemoteDevice().getName());
      socket.connect();
      socket.getOutputStream().write(messageBytes);
      Log.d(NotifierConstants.LOG_TAG, "Sent notification over Bluetooth.");
    } catch (IOException e) {
      Log.e(NotifierConstants.LOG_TAG, "Error sending bluetooth notification", e);
    }

    try {
      socket.close();
    } catch (IOException e) {
      Log.e(NotifierConstants.LOG_TAG, "Error closing bluetooth socket", e);
    }
  }

  /**
   * @return the default bluetooth adapter
   */
  protected BluetoothAdapter getBluetoothAdapter() {
    return BluetoothAdapter.getDefaultAdapter();
  }
}
