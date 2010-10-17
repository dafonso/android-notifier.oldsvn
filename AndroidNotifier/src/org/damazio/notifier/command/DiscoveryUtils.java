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

import java.net.InetAddress;
import java.util.List;

import org.damazio.notifier.command.CommandProtocol.DeviceAddresses;
import org.damazio.notifier.util.BluetoothDeviceUtils;
import org.damazio.notifier.util.NetworkUtils;

import android.content.Context;

import com.google.protobuf.ByteString;

/**
 * Utilities for device discovery.
 *
 * @author Rodrigo Damazio
 */
public class DiscoveryUtils {
  private final BluetoothDeviceUtils bluetoothUtils = BluetoothDeviceUtils.getInstance();
  private final NetworkUtils networkUtils;

  public DiscoveryUtils(Context context) {
    this.networkUtils = new NetworkUtils(context);
  }

  /**
   * Returns a protocol buffer with all the device's addresses.
   */
  public DeviceAddresses getDeviceAddresses() {
    DeviceAddresses.Builder addresses = DeviceAddresses.newBuilder();

    // Add IP addresses
    List<InetAddress> allIps = networkUtils.getAllLocalIps();
    for (InetAddress oneIp : allIps) {
      byte[] address = oneIp.getAddress();
      addresses.addIpAddress(ByteString.copyFrom(address));
    }

    // Add bluetooth addresses
    byte[] bluetoothMac = bluetoothUtils.getLocalMacAddress();
    if (bluetoothMac != null) {
      addresses.setBluetoothMac(ByteString.copyFrom(bluetoothMac));
    }

    return addresses.build();
  }
}
