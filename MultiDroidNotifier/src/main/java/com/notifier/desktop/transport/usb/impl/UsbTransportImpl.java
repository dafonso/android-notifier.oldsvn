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
import java.util.concurrent.*;

import com.google.inject.*;
import com.notifier.desktop.*;
import com.notifier.desktop.discovery.*;
import com.notifier.desktop.transport.usb.*;

import static java.util.concurrent.TimeUnit.*;

@Singleton
public class UsbTransportImpl extends RestartableService implements UsbTransport {

	private static final int PORT_FORWARD_INTERVAL = 3;

	private @Inject ScheduledExecutorService executorService;
	private @Inject Provider<ApplicationPreferences> preferencesProvider;
	private @Inject Provider<UsbPortClient> portClientProvider;

	private UsbPortForwarder portForwarder;
	private ScheduledFuture<?> portForwarderFuture;

	@Override
	public String getName() {
		return "usb";
	}

	@Override
	public void broadcastDiscoveryInfo(DiscoveryInfo discoveryInfo) {
		if (portForwarder != null) {
			for (Map.Entry<Adb.Device, UsbPortClient> entry : portForwarder.getDevicesAndListeners().entrySet()) {
				entry.getValue().sendDiscoveryInfo(discoveryInfo);
			}
		}
	}

	@Override
	protected void doStart() throws Exception {
		String androidSdkHome = preferencesProvider.get().getAndroidSdkHome();
		if (androidSdkHome == null) {
			throw new IllegalStateException("Android SDK home has not been set");
		}
		portForwarder = new UsbPortForwarder(new File(androidSdkHome), portClientProvider);
		portForwarderFuture = executorService.scheduleWithFixedDelay(portForwarder, 0, PORT_FORWARD_INTERVAL, SECONDS);
	}

	@Override
	protected void doStop() throws Exception {
		portForwarderFuture.cancel(true);
		portForwarder.stop();
		portForwarder = null;
	}

	public void setSdkHome(String sdkHome) {
		if (portForwarder != null) {
			portForwarder.setSdkHome(new File(sdkHome));
		}
	}
}
