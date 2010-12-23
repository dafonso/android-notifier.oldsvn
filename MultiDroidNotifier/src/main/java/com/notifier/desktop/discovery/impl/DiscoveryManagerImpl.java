/*
 * Android Notifier Desktop is a multiplatform remote notification client for Android devices.
 *
 * Copyright (C) 2010  Leandro Aparecido
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation version 3 of the License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.notifier.desktop.discovery.impl;

import java.net.*;
import java.security.*;
import java.util.*;
import java.util.concurrent.*;

import com.google.code.jgntp.internal.util.*;
import com.google.inject.*;
import com.notifier.desktop.*;
import com.notifier.desktop.device.*;
import com.notifier.desktop.discovery.*;
import com.notifier.desktop.network.*;
import com.notifier.desktop.transport.bluetooth.*;
import com.notifier.desktop.transport.usb.*;
import com.notifier.desktop.transport.wifi.*;

import static java.util.concurrent.TimeUnit.*;

public class DiscoveryManagerImpl extends RestartableService implements DiscoveryManager, Runnable {

	private static final long BROADCAST_INTERVAL = 3;

	private @Inject ScheduledExecutorService executorService;
	private @Inject NetworkManager networkManager;
	private @Inject DeviceManager deviceManager;
	private @Inject WifiTransport wifiTransport;
	private @Inject UsbTransport usbTransport;
	private @Inject BluetoothTransport bluetoothTransport;

	@Override
	protected void doStart() throws Exception {
		executorService.scheduleWithFixedDelay(this, 0, BROADCAST_INTERVAL, SECONDS);
	}

	@Override
	public void run() {
		DiscoveryInfo info = getDiscoveryInfo();
		try {
			wifiTransport.broadcastDiscoveryInfo(info);
		} finally {
			usbTransport.broadcastDiscoveryInfo(info);
		}
	}

	protected DiscoveryInfo getDiscoveryInfo() {
		InetAddress ipAddress = networkManager.getLocalHostAddress();
		byte[] macAddress = networkManager.getLocalMacAddress();

		int ipPort = wifiTransport.getIpPort();
		String bluetoothAddress = bluetoothTransport.getAddress();

		boolean onlyPaired = !deviceManager.isReceptionFromAnyDevice();
		Collection<String> pairedDeviceIds = deviceManager.getPairedDeviceIds();

		if (ipAddress == null || macAddress == null) {
			return null;
		}

		DiscoveryInfo info = new DiscoveryInfo();
		info.setDesktopId(calculateDesktopId(macAddress));
		info.setIpAddress(ipAddress.getAddress());
		info.setIpPort(ipPort);
		info.setBluetoothAddress(bluetoothAddress);
		info.setOnlyPaired(onlyPaired);
		info.setPairedDeviceIds(pairedDeviceIds);

		return info;
	}

	protected String calculateDesktopId(byte[] data) {
		try {
			MessageDigest digest = MessageDigest.getInstance("MD5");
			byte[] digested = digest.digest(data);
			return Hex.toHexadecimal(digested);
		} catch (NoSuchAlgorithmException e) {
			throw new RuntimeException(e);
		}
	}
}
