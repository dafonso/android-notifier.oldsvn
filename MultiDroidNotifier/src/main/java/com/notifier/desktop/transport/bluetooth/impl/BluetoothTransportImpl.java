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
package com.notifier.desktop.transport.bluetooth.impl;

import java.io.*;

import javax.bluetooth.*;
import javax.microedition.io.*;

import org.slf4j.*;

import com.google.common.base.*;
import com.google.common.io.*;
import com.google.inject.*;
import com.notifier.desktop.*;
import com.notifier.desktop.notification.*;
import com.notifier.desktop.notification.parsing.*;
import com.notifier.desktop.os.*;
import com.notifier.desktop.transport.bluetooth.*;

@Singleton
public class BluetoothTransportImpl extends RestartableService implements BluetoothTransport {

	private static final String NAME = "bluetooth";
	private static final String BASE_URL = "btspp://localhost:7674047e6e474bf0831f209e3f9dd23f;name=AndroidNotifierService;authenticate=true";
	private static final String URL_MAC = BASE_URL;
	private static final String URL_WINDOWS_LINUX = BASE_URL + ";encrypt=true";

	private static final Logger logger = LoggerFactory.getLogger(BluetoothTransportImpl.class);

	private @Inject Application application;
	private @Inject NotificationManager notificationManager;
	private @Inject NotificationParser<byte[]> notificationParser;

	private String address;
	private volatile boolean enabled;
	private Thread acceptorThread;

	@Override
	public String getName() {
		return NAME;
	}

	@Override
	public String getAddress() {
		return address;
	}

	@Override
	public void doStart() {
		LocalDevice localDevice = null;
		try {
			localDevice = LocalDevice.getLocalDevice();
			address = localDevice.getBluetoothAddress();
		} catch (BluetoothStateException e) {
			String message = Strings.nullToEmpty(e.getMessage());
			if ("BluetoothStack not detected".equals(message)) {
				throw new IllegalStateException("Bluetooth not detected, disabling it.", e);
			} else if (message.contains("libbluetooth.so")) {
				throw new IllegalStateException("You have to install the package libbluetooth-dev on Ubuntu or bluez-libs-devel on Fedora or bluez-devel on openSUSE to be able to receive bluetooth notifications.");
			} else {
				throw new RuntimeException(e);
			}
		}
		enabled = true;
		if (localDevice != null && acceptorThread == null) {
			acceptorThread = new Thread(new Runnable() {
				@Override
				public void run() {
					StreamConnectionNotifier notifier = null;
					try {
						if (OperatingSystems.CURRENT_FAMILY == OperatingSystems.Family.MAC) {
							notifier = (StreamConnectionNotifier) Connector.open(URL_MAC);
						} else {
							notifier = (StreamConnectionNotifier) Connector.open(URL_WINDOWS_LINUX);
						}
						while (!Thread.currentThread().isInterrupted()) {
							StreamConnection connection = null;
							InputStream inputStream = null;
							try {
								// acceptAndOpen() will never return without connection and it
								// cannot be interrupted
								connection = notifier.acceptAndOpen();
								inputStream = connection.openInputStream();
								byte[] data = ByteStreams.toByteArray(inputStream);
								if (enabled) {
									Notification notification = notificationParser.parse(data);
									if (notification != null) {
										notificationManager.notificationReceived(notification);
									}
								}
							} catch (InterruptedIOException e) {
								break;
							} catch (Exception e) {
								if (e instanceof InterruptedException) {
									break;
								} else {
									logger.error("Error handling bluetooth notification", e);
									application.showError(Application.NAME + " Bluetooth Error", "An error ocurred while receiving bluetooth notification.");
								}
							} finally {
								Closeables.closeQuietly(inputStream);
								if (connection != null) {
									try {
										connection.close();
									} catch (Exception e) {
										logger.warn("Error closing bluetooth connection", e);
									}
								}
							}
						}
					} catch (Exception e) {
						logger.error("Error setting up bluetooth", e);
						application.showError("Error setting up Bluetooth", "An error occurred while setting up bluetooth to receive connections.");
					} finally {
						if (notifier != null) {
							try {
								notifier.close();
							} catch (Exception e) {
								logger.warn("Error closing bluetooth", e);
							}
						}
						acceptorThread = null; // Allows a new thread to be started if this one dies
					}
				}
			}, NAME);
			acceptorThread.setDaemon(true);
			acceptorThread.start();
		}
	}

	@Override
	public void doStop() {
		enabled = false;
	}

}
