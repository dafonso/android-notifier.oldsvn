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
package com.notifier.desktop.upnp.impl;

import java.io.*;
import java.util.concurrent.*;

import net.sbbi.upnp.impls.*;
import net.sbbi.upnp.messages.*;

import org.slf4j.*;

import com.google.inject.*;
import com.notifier.desktop.*;
import com.notifier.desktop.network.*;
import com.notifier.desktop.transport.wifi.*;
import com.notifier.desktop.upnp.*;

import static java.util.concurrent.TimeUnit.*;

@Singleton
public class UpnpManagerImpl extends RestartableService implements UpnpManager {

	private static final Logger logger = LoggerFactory.getLogger(UpnpManagerImpl.class);

	private static final int DISCOVERY_TIMEOUT = 3 * 1000;
	private static final int LOCAL_INFO_TIMEOUT = 5;

	private @Inject ScheduledExecutorService executorService;
	private @Inject NetworkManager networkManager;
	private @Inject WifiTransport wifiTransport;

	private InternetGatewayDevice internetDevice;

	@Override
	public String getName() {
		return "upnp";
	}

	@Override
	protected void doStart() {
		executorService.execute(new Runnable() {
			@Override
			public void run() {
				try {
					InternetGatewayDevice[] internetDevices = InternetGatewayDevice.getDevices(DISCOVERY_TIMEOUT);
					if (internetDevices != null && internetDevices.length > 0) {
						internetDevice = internetDevices[0];
						logger.debug("Found upnp internet device [{}]", internetDevice.getIGDRootDevice().getModelName());

						try {
							if (!networkManager.waitForLocalNetworkInfo(LOCAL_INFO_TIMEOUT, TimeUnit.SECONDS)) {
								logger.error("Timed out waiting for local address, cannot map port for internet access");
								return;
							}
							if (!wifiTransport.isRunning()) {
								SECONDS.sleep(LOCAL_INFO_TIMEOUT);
								if (!wifiTransport.isRunning()) {
									logger.error("Wifi is not running, cannot map port for internet access");
									return;
								}
							}
						} catch (InterruptedException e) {
							logger.warn("Interrupted while waiting for local network info to map port for internet access");
							return;
						}
						String localAddress = networkManager.getLocalHostAddress().getHostAddress();

						boolean added = internetDevice.addPortMapping(Application.NAME, null, wifiTransport.getIpPort(), wifiTransport.getIpPort(), localAddress, 0, "TCP");
						if (added) {
							logger.info("Added UPNP mapping to port [{}] successfully", wifiTransport.getIpPort());
						} else {
							logger.info("Another device mapped port [{}] before me, cannot map port", wifiTransport.getIpPort());
						}
					} else {
						logger.info("No upnp internet devices found");
					}
				} catch (IOException e) {
					logger.error("Error communicating with upnp internet devices", e);
				} catch (UPNPResponseException e) {
					logger.error("UPNP internet device refused registration", e);
				} finally {
					notifyStarted();
				}
			}
		});
	}

	@Override
	protected void doStop() {
		if (internetDevice == null) {
			notifyStopped();
		} else {
			executorService.execute(new Runnable() {
				@Override
				public void run() {
					deletePortMapping();
					logger.info("Removed UPNP mappings to port [{}]", wifiTransport.getIpPort());
					notifyStopped();
				}
			});
		}
	}

	protected void deletePortMapping() {
		try {
			internetDevice.deletePortMapping(null, wifiTransport.getIpPort(), "TCP");
		} catch (Exception e) {
			logger.warn("Error deleting upnp port mapping", e);
		}
	}

}
