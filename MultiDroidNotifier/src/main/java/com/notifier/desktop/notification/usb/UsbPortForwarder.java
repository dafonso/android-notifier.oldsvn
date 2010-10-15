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
package com.notifier.desktop.notification.usb;

import java.io.*;
import java.util.*;
import java.util.concurrent.*;

import org.slf4j.*;

import com.google.common.base.*;
import com.google.common.collect.*;
import com.google.inject.*;
import com.notifier.desktop.*;

import static java.util.concurrent.TimeUnit.*;

public class UsbPortForwarder implements Runnable {

	private static final Logger logger = LoggerFactory.getLogger(UsbPortForwarder.class);

	private static final int LOCAL_PORT = 10602;
	private static final String ANDROID_SOCKET_NAME = "androidnotifier";
	private static final int SLEEP_TIME = 3;

	private ExecutorService executorService;
	private Provider<UsbPortClient> portClientProvider;

	private boolean stopRequested;
	private Adb adb;

	private int localPortCounter;
	private Map<Adb.Device, UsbPortClient> devicesAndListeners;

	@Inject
	public UsbPortForwarder(Provider<ApplicationPreferences> preferencesProvider, Provider<UsbPortClient> portClientProvider, ExecutorService executorService) {
		this.adb = new Adb();
		ApplicationPreferences preferences = preferencesProvider.get();
		if (!preferences.getAndroidSdkHome().isEmpty()) {
			this.adb.setSdkHome(new File(preferences.getAndroidSdkHome()));
		}
		this.executorService = executorService;
		this.portClientProvider = portClientProvider;
	}

	public void prepare() {
		stopRequested = false;
		localPortCounter = LOCAL_PORT;
		devicesAndListeners = Maps.newHashMap();
	}

	public void stop() {
		stopRequested = true;
	}

	@Override
	public void run() {
		Preconditions.checkState(localPortCounter == LOCAL_PORT, "prepare() has not been called");
		while (!stopRequested) {
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
					}
				}
			}

			if (!stopRequested) {
				try {
					SECONDS.sleep(SLEEP_TIME);
				} catch (InterruptedException e) {
					break;
				}
			}
		}
		for (UsbPortClient client : devicesAndListeners.values()) {
			client.stop();
		}
	}

	protected void forwardAndListen(Adb.Device device) throws InterruptedException {
		try {
			if (!devicesAndListeners.containsKey(device)) {
				int port = localPortCounter++;
				logger.debug("Forwarding port [{}] for device [{}]", port, device);
				adb.forward(device, port, ANDROID_SOCKET_NAME);
				logger.debug("Forwarded successfully, starting client");
				UsbPortClient client = portClientProvider.get();
				client.setDevice(device);
				client.setPort(port);
				devicesAndListeners.put(device, client);
				executorService.execute(client);
			}
		} catch (Exception e) {
			logger.error("Error forwarding port for device [" + device.getSerialNumber() + "]", e);
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

	public File getSdkHome() {
		return adb.getSdkHome();
	}

	public void setSdkHome(File sdkHome) {
		adb.setSdkHome(sdkHome);
	}

}
