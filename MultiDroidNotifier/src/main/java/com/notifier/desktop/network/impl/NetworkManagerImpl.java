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
package com.notifier.desktop.network.impl;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;

import org.slf4j.*;

import com.google.common.base.*;
import com.google.inject.*;
import com.notifier.desktop.*;
import com.notifier.desktop.network.*;

import static java.util.concurrent.TimeUnit.*;

@Singleton
public class NetworkManagerImpl extends RestartableService implements NetworkManager, Runnable {

	private static final Logger logger = LoggerFactory.getLogger(NetworkManagerImpl.class);

	private static final long INTERVAL = 5;
	private static final String[] VIRTUAL_ADAPTER_NAMES = { "vmware", "vmnet", "virtualbox" };

	private @Inject ScheduledExecutorService executorService;

	private String localHostName;
	private InetAddress localHostAddress;
	private byte[] localMacAddress;

	private final CountDownLatch firstUpdateLatch;

	public NetworkManagerImpl() {
		firstUpdateLatch = new CountDownLatch(1);
	}

	@Override
	public String getName() {
		return "network manager";
	}

	@Override
	public String getLocalHostName() {
		return localHostName;
	}

	@Override
	public InetAddress getLocalHostAddress() {
		return localHostAddress;
	}

	@Override
	public byte[] getLocalMacAddress() {
		return localMacAddress;
	}

	@Override
	public boolean waitForLocalNetworkInfo(long timeout, TimeUnit unit) throws InterruptedException {
		return firstUpdateLatch.await(timeout, unit);
	}

	@Override
	protected void doStart() throws Exception {
		executorService.scheduleWithFixedDelay(this, 0, INTERVAL, SECONDS);
	}

	@Override
	public void run() {
		try {
			findNetworkInfo();
		} catch (Exception e) {
			logger.error("Error looking for network info", e);
		}
	}

	protected void findNetworkInfo() throws UnknownHostException, IOException {
		logger.trace("Looking for network info");
		InetAddress localHost = InetAddress.getLocalHost();
		if (localHost != null) {
			localHostName = localHost.getHostName();
		}
		// We need any adapter that's not loopback and not virtualized
		Enumeration<NetworkInterface> networkInterfaces = NetworkInterface.getNetworkInterfaces();
		NetworkInterface networkInterface = null;
		while (networkInterfaces.hasMoreElements()) {
			NetworkInterface ni = networkInterfaces.nextElement();
			if (!ni.isLoopback() && ni.isUp() && !isVirtual(ni)) {
				networkInterface = ni;
				break;
			}
		}
		if (networkInterface == null) {
			logger.warn("Could not find suitable network interface to load network info");
			return;
		}
		Enumeration<InetAddress> inetAddresses = networkInterface.getInetAddresses();
		InetAddress inetAddress = null;
		while (inetAddresses.hasMoreElements()) {
			InetAddress ia = inetAddresses.nextElement();
			if (ia instanceof Inet4Address) {
				inetAddress = ia;
				break;
			}
		}
		localHostAddress = inetAddress;
		localMacAddress = networkInterface.getHardwareAddress();
		logger.trace("Network info found");
	}

	protected boolean isVirtual(NetworkInterface networkInterface) {
		String name = Strings.nullToEmpty(networkInterface.getDisplayName()).toLowerCase();
		for (String virtualName : VIRTUAL_ADAPTER_NAMES) {
			if (name.contains(virtualName)) {
				return true;
			}
		}
		return false;
	}
}
