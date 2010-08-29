package org.damazio.notifier.command;

import java.io.IOException;
import java.util.UUID;

import org.damazio.notifier.NotifierConstants;
import org.damazio.notifier.NotifierPreferences;
import org.damazio.notifier.notification.BluetoothDeviceUtils;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.util.Log;

/**
 * Listener for incoming RFCOMM bluetooth connections.
 * This only accepts connections and passes them to be handled by a
 * {@link CommandStreamHandler}.
 *
 * @author rdamazio
 */
public class BluetoothCommandListener extends Thread {
  private static final String COMMAND_UUID_STR = "E8D515B4-47C1-4813-B6D6-3EAB32F8953E";
  private static final UUID COMMAND_UUID = UUID.fromString(COMMAND_UUID_STR);
  private BluetoothServerSocket socket;
  private boolean shutdown = false;

  private final NotifierPreferences preferences;

  public BluetoothCommandListener(NotifierPreferences preferences) {
    this.preferences = preferences;
  }

  @Override
  public void run() {
    BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    try {
      socket = bluetoothAdapter.listenUsingRfcommWithServiceRecord("RemoteNotifier", COMMAND_UUID);
    } catch (IOException e) {
      Log.e(NotifierConstants.LOG_TAG, "Unable to listen on RFCOMM", e);
      return;
    }

    while (!shutdown) {
      try {
        // Accept the connection
        BluetoothSocket connectedSocket = socket.accept();
        Log.d(NotifierConstants.LOG_TAG, "Accepted bluetooth connection");

        // Check that the source address matches the expected
        String expectedAddress = preferences.getSourceBluetoothDevice();
        if (!expectedAddress.equals(BluetoothDeviceUtils.ANY_DEVICE)) {
          BluetoothDevice remoteDevice = connectedSocket.getRemoteDevice();
          String remoteAddress = remoteDevice.getAddress();
          if (!expectedAddress.equalsIgnoreCase(remoteAddress)) {
            Log.e(NotifierConstants.LOG_TAG, "Bluetooth connection from unexpected device, closed.");
            connectedSocket.close();
            continue;
          }
        }

        // Let it be handled in a separate thread
        CommandStreamHandler handler = new CommandStreamHandler(
            connectedSocket.getInputStream(),
            connectedSocket.getOutputStream(),
            connectedSocket);
        handler.start();

        // TODO: Manage references to active handlers for shutdown
      } catch (IOException e) {
        // This can happen either if there was an error, or if shutdown() was called
        Log.w(NotifierConstants.LOG_TAG, "Failed to accept an RFCOMM connection", e);
      }
    }
  }

  public void shutdown() {
    shutdown = true;
    interrupt();
  }
}
