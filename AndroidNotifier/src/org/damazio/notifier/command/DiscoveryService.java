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
import java.math.BigInteger;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketException;

import org.damazio.notifier.NotifierConstants;
import org.damazio.notifier.command.CommandProtocol.CommandDiscoveryReply;
import org.damazio.notifier.command.CommandProtocol.CommandDiscoveryRequest;
import org.damazio.notifier.command.CommandProtocol.DeviceAddresses;
import org.damazio.notifier.notification.DeviceIdProvider;

import android.content.Context;
import android.util.Log;

import com.google.protobuf.ByteString;

/**
 * Service which allows discovery of a device's addresses for sending commands.
 *
 * TODO: Use Zeroconf/Bonjour instead.
 *
 * @author Rodrigo Damazio
 */
class DiscoveryService extends Thread {
  private static final int DISCOVERY_PORT = 10700;
  private static final int MAX_PACKET_SIZE = 1500;  // Common MTU
  private final Context context;
  private boolean shouldShutdown;

  DiscoveryService(Context context) {
    this.context = context;
  }

  @Override
  public void run() {
    long deviceId = new BigInteger(DeviceIdProvider.getDeviceId(context), 16).longValue();
    DiscoveryUtils discoveryUtils = new DiscoveryUtils(context);

    try {
      DatagramSocket datagramSocket = new DatagramSocket(null);
      datagramSocket.setReuseAddress(true);
      datagramSocket.bind(new InetSocketAddress((InetAddress) null, DISCOVERY_PORT));
      Log.i(NotifierConstants.LOG_TAG, "Listening for discovery");

      shouldShutdown = false;
      while (!shouldShutdown) {
        DatagramPacket packet = new DatagramPacket(new byte[MAX_PACKET_SIZE], MAX_PACKET_SIZE);
        try {
          // Receive and parse the request
          datagramSocket.receive(packet);
          ByteString payload = ByteString.copyFrom(packet.getData(), packet.getOffset(), packet.getLength());
          CommandDiscoveryRequest request = CommandDiscoveryRequest.parseFrom(payload);

          if (request.getDeviceId() != deviceId) {
            // Discovery is not for this device, ignore
            Log.d(NotifierConstants.LOG_TAG, "Ignoring discovery not for this device: " + request);
            continue;
          }

          // Create the reply
          // We don't want to cache this reply, as addresses may change
          DeviceAddresses addresses = discoveryUtils.getDeviceAddresses();
          CommandDiscoveryReply reply = CommandDiscoveryReply.newBuilder()
              .setDeviceId(deviceId)
              .setAddresses(addresses)
              .build();
          byte[] replyBytes = reply.toByteArray();

          // Send the reply
          DatagramPacket replyPacket = new DatagramPacket(replyBytes, replyBytes.length, packet.getAddress(), DISCOVERY_PORT);
          datagramSocket.send(replyPacket);
        } catch (IOException e) {
          Log.e(NotifierConstants.LOG_TAG, "Error during discovery", e);
        }
      }

      datagramSocket.close();
      Log.i(NotifierConstants.LOG_TAG, "No longer listening for discovery");
    } catch (SocketException e) {
      Log.e(NotifierConstants.LOG_TAG, "Unable to open discovery socket", e);
    }

  }

  void shutdown() {
    shouldShutdown = true;
    interrupt();
  }
}
