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
package org.damazio.notifier.command.listeners;

import java.io.IOException;
import java.util.UUID;

import org.damazio.notifier.NotifierConstants;
import org.damazio.notifier.NotifierPreferences;
import org.damazio.notifier.command.CommandHistory;
import org.damazio.notifier.util.BluetoothDeviceUtils;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.util.Log;

/**
 * Listener for incoming RFCOMM bluetooth connections.
 * This only accepts connections and passes them to be handled by a
 * {@link CommandStreamHandler}.
 *
 * @author Rodrigo Damazio
 */
public class BluetoothCommandListener extends CommandListener {
  private static final String COMMAND_UUID_STR = "E8D515B4-47C1-4813-B6D6-3EAB32F8953E";
  private static final UUID COMMAND_UUID = UUID.fromString(COMMAND_UUID_STR);

  private final NotifierPreferences preferences;

  private BluetoothServerSocket socket;

  public BluetoothCommandListener(Context context, CommandHistory history, NotifierPreferences preferences) {
    super(context, history);

    this.preferences = preferences;
  }

  @Override
  protected void initialize() throws IOException {
    BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    socket = bluetoothAdapter.listenUsingRfcommWithServiceRecord("RemoteNotifier", COMMAND_UUID);
    Log.i(NotifierConstants.LOG_TAG, "Listening for commands over Bluetooth");
  }

  @Override
  protected void runOnce() throws IOException {
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
        return;
      }
    }

    handleConnection(
        connectedSocket.getInputStream(),
        connectedSocket.getOutputStream(),
        connectedSocket);
  }

  @Override
  public void shutdown() {
    Log.d(NotifierConstants.LOG_TAG, "No longer listening for bluetooth commands");
    if (socket != null) {
      try {
        socket.close();
      } catch (IOException e) {
        Log.w(NotifierConstants.LOG_TAG, "Error closing socket", e);
      }
    }

    super.shutdown();
  }
}
