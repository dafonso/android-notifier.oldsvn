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
