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
package com.google.code.notifier.desktop.util;

import java.net.*;
import java.util.Enumeration;

import org.slf4j.*;

import com.google.common.base.*;

public class InetAddresses {

	private static final String VMWARE_ADAPTER_NAME = "vmware";
	private static final String VIRTUALBOX_ADAPTER_NAME = "virtualbox";

	private static final Logger logger = LoggerFactory.getLogger(InetAddresses.class);

	private static InetAddress localHostAddress;
	private static InetAddress localAddress;

	private InetAddresses() {
	}

	public static String getLocalHostName() {
		if (localHostAddress == null) {
			return null;
		}
		return localHostAddress.getHostName();
	}

	public static String getLocalHostAddress() {
		if (localAddress == null) {
			return null;
		}
		return localAddress.getHostAddress();
	}

	public static void startFindLocalAddress() {
		new Thread(new Runnable() {
			@Override
			public void run() {
				logger.debug("Looking for local address");
				try {
					localHostAddress = InetAddress.getLocalHost();
				} catch (Exception e) {
					logger.error("Error looking for local host address", e);
				}
				try {
					// We need any adapter that's not loopback and not virtualized
					Enumeration<NetworkInterface> networkInterfaces = NetworkInterface.getNetworkInterfaces();
					NetworkInterface networkInterface = null;
					while (networkInterfaces.hasMoreElements()) {
						NetworkInterface ni = networkInterfaces.nextElement();
						String name = Strings.nullToEmpty(ni.getDisplayName()).toLowerCase();
						if (!ni.isLoopback() &&
							ni.isUp() &&
							!name.contains(VMWARE_ADAPTER_NAME) &&
							!name.contains(VIRTUALBOX_ADAPTER_NAME)) {
							networkInterface = ni;
							break;
						}
					}
					if (networkInterface == null) {
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
					localAddress = inetAddress;
					logger.debug("Local address found");
				} catch (Exception e) {
					logger.warn("Error looking for local address", e);
				}
			}
		}, "local-address").start();
	}
}
