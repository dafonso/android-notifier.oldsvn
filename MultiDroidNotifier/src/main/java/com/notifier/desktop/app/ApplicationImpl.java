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
package com.notifier.desktop.app;

import java.util.*;

import javax.swing.*;

import org.slf4j.*;

import com.google.common.collect.*;
import com.google.inject.*;
import com.notifier.desktop.*;
import com.notifier.desktop.annotation.*;
import com.notifier.desktop.util.*;
import com.notifier.desktop.view.*;

public class ApplicationImpl implements Application {

	public static final int OK_EXIT_CODE = 0;
	public static final int TRAY_ERROR_EXIT_CODE = 1;
	public static final int BROADCASTER_ERROR_EXIT_CODE = 2;
	public static final int RECEIVER_ERROR_EXIT_CODE = 4;
	public static final int SWT_ERROR_EXIT_CODE = 8;
	
	private static final String BROADCASTER = "broadcaster";
	private static final String RECEIVER = "receiver";

	private static final Logger logger = LoggerFactory.getLogger(ApplicationImpl.class);

	private @Inject SwtManager swtManager;
	private @Inject TrayManager trayManager;
	private @Inject Provider<ApplicationPreferences> preferencesProvider;

	private @Inject @Tray NotificationBroadcaster trayBroadcaster;
	private @Inject @Growl NotificationBroadcaster growlBroadcaster;
	private @Inject @Libnotify NotificationBroadcaster libnotifyBroadcaster;
	private @Inject @Msn NotificationBroadcaster msnBroadcaster;

	private @Inject @Tcp NotificationReceiver tcpReceiver;
	private @Inject @Udp NotificationReceiver udpReceiver;
	private @Inject @Upnp NotificationReceiver upnpReceiver;
	private @Inject @Bluetooth NotificationReceiver bluetoothReceiver;

	private @Inject NotificationManager notificationManager;
	private @Inject NotificationParser<byte[]> notificationParser; 
	private @Inject UpdateManager updateManager;
	private @Inject ServiceServer serviceServer;

	private boolean showingTrayIcon;

	public void start(boolean showTrayIcon, boolean showPreferencesWindow) {
		try {
			logger.info("Starting SWT");
			swtManager.start();
		} catch (Throwable t) {
			logger.error("Error starting SWT", t);
			notifyUiNotLoaded();
			System.exit(SWT_ERROR_EXIT_CODE);
		}

		if (showTrayIcon) {
			try {
				logger.info("Showing tray icon");
				if (!trayManager.start()) {
					notifyTrayIconNotSupported();
					System.exit(TRAY_ERROR_EXIT_CODE);
				}
				showingTrayIcon = showTrayIcon;
			} catch (Throwable t) {
				logger.error("Error showing tray icon", t);
				notifyUiNotLoaded();
				System.exit(TRAY_ERROR_EXIT_CODE);
			}
		}
		startServiceServer();
		InetAddresses.startFindLocalAddress();

		ApplicationPreferences preferences = preferencesProvider.get();
		boolean startedAtLeastOne = startLifecycles(getBroadcasters(preferences), BROADCASTER);
		if (!startedAtLeastOne) {
			notifyBroadcasterNotLoaded();
		}

		startedAtLeastOne = startLifecycles(getReceivers(preferences), RECEIVER);
		if (!startedAtLeastOne) {
			notifyReceiverNotLoaded();
		}

		adjustStartAtLogin(preferences.isStartAtLogin(), true);
		if (showPreferencesWindow) {
			showPreferencesWindow();
		}

		swtManager.runEventLoop();
	}

	protected void showPreferencesWindow() {
		swtManager.update(new Runnable() {
			@Override
			public void run() {
				PreferencesDialog preferencesDialog = new PreferencesDialog(ApplicationImpl.this, notificationManager, notificationParser, swtManager);
				preferencesDialog.open();
			}
		});
	}

	@Override
	public boolean adjustStartAtLogin(boolean enabled, boolean silent) {
		return adjustStartup(enabled, silent);
	}

	@Override
	public boolean adjustWifiReceiver(boolean enabled) {
		boolean ok;
		ok = adjustLifecycle(tcpReceiver, enabled, RECEIVER);
		ok |= adjustLifecycle(udpReceiver, enabled, RECEIVER);

		return ok;
	}

	@Override
	public boolean adjustUpnpReceiver(boolean enabled) {
		return adjustLifecycle(upnpReceiver, enabled, RECEIVER);
	}

	@Override
	public boolean adjustBluetoothReceiver(boolean enabled) {
		return adjustLifecycle(bluetoothReceiver, enabled, RECEIVER);
	}

	@Override
	public boolean adjustSystemDefaultBroadcaster(boolean enabled) {
		return adjustLifecycle(trayBroadcaster, enabled, BROADCASTER);
	}

	@Override
	public boolean adjustGrowlBroadcaster(boolean enabled) {
		return adjustLifecycle(growlBroadcaster, enabled, BROADCASTER);
	}

	@Override
	public boolean adjustLibnotifyBroadcaster(boolean enabled) {
		return adjustLifecycle(libnotifyBroadcaster, enabled, BROADCASTER);
	}

	@Override
	public void showError(String title, String message) {
		Dialogs.showError(swtManager, title, message, true);
	}

	public void shutdown() {
		logger.info("Shutting down...");
		int exitCode = OK_EXIT_CODE;

		if (showingTrayIcon) {
			try {
				logger.info("Hiding tray icon");
				trayManager.stop();
			} catch (Throwable t) {
				logger.error("Error hiding tray icon", t);
				exitCode += TRAY_ERROR_EXIT_CODE;
			}
		}
		serviceServer.stop();

		ApplicationPreferences preferences = preferencesProvider.get();
		if (!stopLifecycles(getReceivers(preferences), RECEIVER)) {
			exitCode += RECEIVER_ERROR_EXIT_CODE;
		}
		if (!stopLifecycles(getBroadcasters(preferences), BROADCASTER)) {
			exitCode += BROADCASTER_ERROR_EXIT_CODE;
		}

		try {
			logger.info("Stopping SWT");
			swtManager.stop();
		} catch (Throwable t) {
			logger.error("Error stopping SWT", t);
			exitCode += SWT_ERROR_EXIT_CODE;
		}

		System.exit(exitCode);
	}

	@Override
	public Version getVersion() {
		try {
			return updateManager.getCurrentVersion();
		} catch (Exception e) {
			logger.error("Error loading current version", e);
			showError(Application.NAME + " Error Loading Current Version", "An error ocurred while loading current version.");
			return null;
		}
	}

	@Override
	public void checkForUpdates() {
		new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					boolean isLatest = updateManager.isLatestVersion();
					if (isLatest) {
						Dialogs.showInfo(swtManager, "Check for Updates", "Android Notifier Desktop is up-to-date.", true);
					} else {
						Version current = updateManager.getCurrentVersion();
						Version latest = updateManager.getCachedLatestVersion();
						Dialogs.showInfo(swtManager, "Check for Updates", "There is a newer version available, current is " + current + " and latest is " + latest + ".", true);
					}
				} catch (Exception e) {
					logger.error("Error checking for latest version", e);
					showError(Application.NAME + " Error Checking for Updates", "An error ocurred while checking for updates. Please, try again.");
				}
			}
		}, "check-for-updates").start();
	}

	protected void startServiceServer() {
		try {
			serviceServer.start();
		} catch (Exception e) {
			logger.warn("Error starting service server, you will not be able to stop it via command line", e);
		}
	}

	protected <T extends Lifecycle & Named> boolean startLifecycles(Map<T, Boolean> lifecycles, String category) {
		boolean startedAtLeastOne = false;
		for (Map.Entry<T, Boolean> entry : lifecycles.entrySet()) {
			if (Boolean.TRUE.equals(entry.getValue())) {
				if (startLifecycle(entry.getKey(), category)) {
					startedAtLeastOne = true;
				}
			}
		}
		return startedAtLeastOne;
	}

	protected <T extends Lifecycle & Named> boolean startLifecycle(T lifecycle, String category) {
		logger.info("Starting [{}] {}", lifecycle.getName(), category);
		try {
			lifecycle.start();
			return true;
		} catch (Throwable t) {
			logger.error("Error starting [" + lifecycle.getName() + "] " + category, t);
			notifyStartupError(t);
			return false;
		}
	}

	protected <T extends Lifecycle & Named> boolean stopLifecycles(Map<T, Boolean> lifecycles, String category) {
		boolean success = true;
		for (Map.Entry<T, Boolean> entry : lifecycles.entrySet()) {
			if (Boolean.TRUE.equals(entry.getValue())) {
				if (!stopLifecycle(entry.getKey(), category)) {
					success = false;
				}
			}
		}
		return success;
	}

	protected <T extends Lifecycle & Named> boolean stopLifecycle(T lifecycle, String category) {
		logger.info("Stopping [{}] {}", lifecycle.getName(), category);
		try {
			lifecycle.stop();
			return true;
		} catch (Throwable t) {
			logger.error("Error stopping [" + lifecycle.getName() + "] " + category, t);
			return false;
		}
	}
	
	protected boolean adjustStartup(boolean startAtLogin, boolean silent) {
		try {
			if (startAtLogin) {
				logger.info("Adding to startup");
				OperatingSystems.addToStartup();
			} else {
				logger.info("Removing from startup");
				OperatingSystems.removeFromStartup();
			}
			return true;
		} catch (Exception e) {
			logger.error("Error adjusting startup", e);
			if (!silent) {
				Dialogs.showError(swtManager, "Error", "An error ocurred while updating startup status.", true);
			}
			return false;
		}
	}
	
	protected <T extends Lifecycle & Named> boolean adjustLifecycle(T lifecycle, boolean enabled, String category) {
		if (enabled) {
			if (!lifecycle.isRunning()) {
				return startLifecycle(lifecycle, category);
			}
		} else {
			if (lifecycle.isRunning()) {
				return stopLifecycle(lifecycle, category);
			}
		}
		return true;
	}

	protected Map<NotificationBroadcaster, Boolean> getBroadcasters(ApplicationPreferences preferences) {
		return ImmutableMap.of(trayBroadcaster, preferences.isDisplayWithSystemDefault(),
							   growlBroadcaster, preferences.isDisplayWithGrowl(),
							   libnotifyBroadcaster, preferences.isDisplayWithLibnotify(),
							   msnBroadcaster, true);
	}
	
	protected Map<NotificationReceiver, Boolean> getReceivers(ApplicationPreferences preferences) {
		return ImmutableMap.of(tcpReceiver, preferences.isReceptionWithWifi(),
							   udpReceiver, preferences.isReceptionWithWifi(),
							   upnpReceiver, preferences.isReceptionWithUpnp(),
							   bluetoothReceiver, preferences.isReceptionWithBluetooth());
	}

	protected void notifyUiNotLoaded() {
		JOptionPane.showMessageDialog(null, "Could not load UI. Make sure you are using the correct version (32 or 64 bit) for your system.\nBoth Operating System and Java have to be 64 bit if you want to run the 64 bit version of this program.", "UI Not Loaded", JOptionPane.ERROR_MESSAGE);
	}

	protected void notifyTrayIconNotSupported() {
		Dialogs.showError(swtManager, "Tray Icon Not Supported", "System tray icon is not supported or the tray is gone.", true);
	}

	protected void notifyBroadcasterNotLoaded() {
		Dialogs.showError(swtManager, "Broadcaster Error", "Could not start at least one notification broadcaster(s). You will not be able to get notifications.", true);
	}

	protected void notifyReceiverNotLoaded() {
		Dialogs.showError(swtManager, "Receiver Error", "Could not start at least one notification receiver. You will not be able to get notifications.", true);
	}
	
	protected void notifyStartupError(Throwable t) {
		Dialogs.showError(swtManager, "Error", t.getMessage(), true);
	}

	protected void notifyErrorSavingPreferences() {
		Dialogs.showError(swtManager, "Error saving preferences", "Error saving preferences, please try again", true);
	}
}
