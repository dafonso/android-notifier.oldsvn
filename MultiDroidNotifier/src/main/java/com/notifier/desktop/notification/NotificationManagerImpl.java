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
package com.notifier.desktop.notification;

import static java.util.concurrent.TimeUnit.*;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.*;

import org.slf4j.*;

import com.google.common.base.*;
import com.google.common.collect.*;
import com.google.inject.*;
import com.notifier.desktop.*;
import com.notifier.desktop.Notification.*;
import com.notifier.desktop.annotation.*;

public class NotificationManagerImpl implements NotificationManager {

	private static final Logger logger = LoggerFactory.getLogger(NotificationManagerImpl.class);

	private final SwtManager swtManager;
	private final OperatingSystemProcessManager processManager;
	private final ImmutableList<NotificationBroadcaster> broadcasters;

	private final ConcurrentMap<Long, Notification> lastNotifications;
	private boolean receptionFromAnyDevice;
	private Set<Long> allowedDevicesIds;
	private final Map<Notification.Type, NotificationConfiguration> notificationConfigurations;

	private AtomicBoolean waitingForPairing;
	private PairingListener pairingListener;

	@Inject
	public NotificationManagerImpl(Provider<ApplicationPreferences> preferencesProvider,
	                               SwtManager swtManager,
	                               OperatingSystemProcessManager processManager,
								   @Tray NotificationBroadcaster trayBroadcaster,
	                               @Growl NotificationBroadcaster growlBroadcaster,
	                               @Libnotify NotificationBroadcaster libnotifyBroadcaster) {
		this.broadcasters = ImmutableList.of(trayBroadcaster, growlBroadcaster, libnotifyBroadcaster);
		this.swtManager = swtManager;
		this.processManager = processManager;
		this.lastNotifications = new MapMaker().initialCapacity(50).expiration(60, SECONDS).makeMap();

		ApplicationPreferences prefs = preferencesProvider.get();
		this.receptionFromAnyDevice = prefs.isReceptionFromAnyDevice();
		this.allowedDevicesIds = Sets.newTreeSet(prefs.getAllowedDevicesIds());
		this.waitingForPairing = new AtomicBoolean();
		this.notificationConfigurations = Maps.newEnumMap(Notification.Type.class);
		for (Notification.Type type : Notification.Type.values()) {
			NotificationConfiguration config = new NotificationConfiguration(type);
			config.setEnabled(prefs.isNotificationEnabled(type));
			config.setSendToClipboard(prefs.isNotificationClipboard(type));
			config.setExecuteCommand(prefs.isNotificationExecuteCommand(type));
			config.setCommand(prefs.getNotificationCommand(type));
			notificationConfigurations.put(type, config);
		}
	}

	@Override
	public void notificationReceived(Notification notification) {
		if (lastNotifications.putIfAbsent(notification.getNotificationId(), notification) == null) {
			logger.info("Notification received: " + notification);
			if (notification.getType() == Notification.Type.PING &&
				waitingForPairing.get()) {
				if (pairingListener.onPairingSuccessful(notification.getDeviceId())) {
					waitingForPairing.set(false);
					pairingListener = null;
					logger.info("Paired with device [{}]", notification.getDeviceId());
				}
			} else {
				handleNotification(notification);
			}
		}
	}

	@Override
	public void waitForPairing(PairingListener listener) {
		Preconditions.checkNotNull(listener);
		logger.info("Waiting for test notification to pair device");
		pairingListener = listener;
		waitingForPairing.set(true);
	}

	@Override
	public void cancelWaitForPairing() {
		logger.info("Pairing canceled");
		waitingForPairing.set(false);
		pairingListener = null;
	}

	@Override
	public void setReceptionFromAnyDevice(boolean enabled) {
		this.receptionFromAnyDevice = enabled;
	}

	@Override
	public void setPairedDevices(String[] deviceIds) {
		this.allowedDevicesIds = Sets.newTreeSet();
		for (String deviceId : deviceIds) {
			this.allowedDevicesIds.add(Long.parseLong(deviceId));
		}
	}

	@Override
	public void setNotificationEnabled(Type type, boolean enabled) {
		getConfiguration(type).setEnabled(enabled);
	}

	@Override
	public void setNotificationClipboard(Type type, boolean enabled) {
		getConfiguration(type).setSendToClipboard(enabled);
	}

	@Override
	public void setNotificationExecuteCommand(Type type, boolean enabled) {
		getConfiguration(type).setExecuteCommand(enabled);
	}

	@Override
	public void setNotificationCommand(Type type, String command) {
		getConfiguration(type).setCommand(command);
	}

	protected void handleNotification(Notification notification) {
		NotificationConfiguration config = notificationConfigurations.get(notification.getType());
		if (config == null) {
			throw new IllegalStateException("No configuration found for notification type: " + notification.getType());
		}

		if (config.isEnabled()) {
			doBroadcast(notification);
			if (config.isSendToClipboard()) {
				sendNotificationToClipboard(notification);
			}
			if (config.isExecuteCommand()) {
				executeNotificationCommand(notification, config.getCommand());
			}
		} else {
			logger.debug("Notification type [{}] is not enabled, ignoring", notification.getType());
		}
	}

	protected void doBroadcast(Notification notification) {
		if (receptionFromAnyDevice || allowedDevicesIds.contains(notification.getDeviceId())) {
			for (NotificationBroadcaster broadcaster : broadcasters) {
				try {
					broadcaster.broadcast(notification);
				} catch (Throwable t) {
					logger.error("Error broadcasting using [" + broadcaster.getName() + "]", t);
				}
			}
		}
	}

	protected void sendNotificationToClipboard(Notification notification) {
		swtManager.sendTextToClipboard(notification.getDescription());
	}

	protected void executeNotificationCommand(Notification notification, String command) {
		processManager.executeCommand(notification, command);
	}

	protected NotificationConfiguration getConfiguration(Notification.Type type) {
		NotificationConfiguration config = notificationConfigurations.get(type);
		Preconditions.checkNotNull(config, "No configuration found for notification type: %s", type);
		return config;
	}

}
