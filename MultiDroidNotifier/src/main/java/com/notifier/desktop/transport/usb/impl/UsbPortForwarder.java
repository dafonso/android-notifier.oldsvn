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
package com.notifier.desktop.transport.usb.impl;

import java.io.*;
import java.util.*;

import org.slf4j.*;

import com.google.common.base.*;
import com.google.common.collect.*;
import com.google.inject.*;

public class UsbPortForwarder implements Runnable {

	private static final Logger logger = LoggerFactory.getLogger(UsbPortForwarder.class);

	private static final int LOCAL_PORT = 10602;
	private static final String ANDROID_SOCKET_NAME = "androidnotifier";

	private Provider<UsbPortClient> portClientProvider;

	private Adb adb;
	private int localPortCounter;
	private Map<Adb.Device, UsbPortClient> devicesAndListeners;

	public UsbPortForwarder(File androidSdkHome, Provider<UsbPortClient> portClientProvider) {
		this.adb = new Adb();
		this.adb.setSdkHome(androidSdkHome);
		this.portClientProvider = portClientProvider;
		this.localPortCounter = LOCAL_PORT;
		this.devicesAndListeners = Maps.newConcurrentMap();
	}

	@Override
	public void run() {
		logger.trace("Listing adb devices");
		Collection<Adb.Device> actualDevices = Collections.emptyList();
		try {
			List<Adb.Device> devices = adb.devices();
			purgeDisconnectedDevices(devices);
			actualDevices = Collections2.filter(devices, new Adb.Device.TypePredicate(Adb.Device.Type.DEVICE));
			actualDevices = Collections2.filter(actualDevices, new Predicate<Adb.Device>() {
				public boolean apply(Adb.Device input) {
					return !devicesAndListeners.keySet().contains(input);
				}
			});
		} catch (IOException e) {
			logger.error("Error running adb", e);
		} catch (InterruptedException e) {
			return;
		} catch (Exception e) {
			logger.error("Error running adb", e);
		}
		if (!actualDevices.isEmpty()) {
			logger.debug("Found [{}] new device(s)", actualDevices.size());

			for (Adb.Device device : actualDevices) {
				try {
					forwardAndListen(device);
				} catch (InterruptedException e) {
					return;
				} catch (Exception e) {
					logger.error("Error forwarding port for device [" + device.getSerialNumber() + "]", e);
				}
			}
		}
	}

	public void stop() {
		for (UsbPortClient client : devicesAndListeners.values()) {
			client.stop();
		}
	}

	protected void forwardAndListen(Adb.Device device) throws InterruptedException, IOException {
		if (!devicesAndListeners.containsKey(device)) {
			int port = localPortCounter++;
			logger.debug("Forwarding port [{}] for device [{}]", port, device);
			adb.forward(device, port, ANDROID_SOCKET_NAME);
			logger.debug("Forwarded successfully, starting client");
			UsbPortClient client = portClientProvider.get();
			client.setDevice(device);
			client.setPort(port);
			devicesAndListeners.put(device, client);
			client.start();
		}
	}

	protected void purgeDisconnectedDevices(List<Adb.Device> devices) {
		for (Iterator<Map.Entry<Adb.Device, UsbPortClient>> iterator = devicesAndListeners.entrySet().iterator(); iterator.hasNext();) {
			Map.Entry<Adb.Device, UsbPortClient> entry = iterator.next();
			if (!devices.contains(entry.getKey())) {
				logger.debug("Device [{}] has been disconnected from usb", entry.getKey());
				entry.getValue().stop();
				iterator.remove();
			}
		}
	}

	public void setSdkHome(File sdkHome) {
		adb.setSdkHome(sdkHome);
	}

	public ImmutableMap<Adb.Device, UsbPortClient> getDevicesAndListeners() {
		return ImmutableMap.copyOf(devicesAndListeners);
	}

}
