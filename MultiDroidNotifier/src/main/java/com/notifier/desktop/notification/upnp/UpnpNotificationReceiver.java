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
package com.notifier.desktop.notification.upnp;

import static java.util.concurrent.TimeUnit.*;

import java.io.*;
import java.util.concurrent.*;

import org.slf4j.*;

import net.sbbi.upnp.impls.*;
import net.sbbi.upnp.messages.*;

import com.google.inject.*;
import com.notifier.desktop.*;
import com.notifier.desktop.notification.*;
import com.notifier.desktop.util.*;

public class UpnpNotificationReceiver extends AbstractNotificationReceiver {

	private static final Logger logger = LoggerFactory.getLogger(UpnpNotificationReceiver.class);

	private static final String NAME = "UPNP";
	private static final int DISCOVERY_TIMEOUT = 3 * 1000;
	private static final int MAX_WAITS_FOR_LOCAL_ADDRESS = 5;

	private @Inject ExecutorService executorService;

	private InternetGatewayDevice internetDevice;

	public UpnpNotificationReceiver() {
		super(false);
	}

	@Override
	public String getName() {
		return NAME;
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

						String localAddress = null;
						for (int i = 0; i < MAX_WAITS_FOR_LOCAL_ADDRESS && localAddress == null; i++) {
							localAddress = InetAddresses.getLocalHostAddress();
							if (localAddress == null) {
								try {
									MILLISECONDS.sleep(500);
								} catch (InterruptedException e) {
									Thread.currentThread().interrupt();
									return;
								}
							}
						}

						boolean added = internetDevice.addPortMapping(Application.NAME, null, PORT, PORT, localAddress, 0, "TCP");
						added &= internetDevice.addPortMapping(Application.NAME, null, PORT, PORT, localAddress, 0, "UDP");
						if (added) {
							logger.info("Added UPNP mapping to port [{}] successfully", PORT);
						} else {
							logger.info("Another device mapped port [{}] before me, cannot map port", PORT);
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
					deletePortMapping("TCP");
					deletePortMapping("UDP");
					logger.info("Removed UPNP mappings to port [{}]", PORT);
					notifyStopped();
				}
			});
		}
	}

	protected void deletePortMapping(String protocol) {
		try {
			internetDevice.deletePortMapping(null, PORT, protocol);
		} catch (Exception e) {
			logger.warn("Error deleting upnp port mapping for protocol [" + protocol + "]", e);
		}
	}
}
