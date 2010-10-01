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
package com.notifier.desktop.notification.broadcast;

import static java.util.concurrent.TimeUnit.*;

import java.awt.image.*;
import java.io.*;
import java.net.*;
import java.util.*;

import javax.imageio.*;

import org.slf4j.*;

import com.binaryblizzard.growl.*;
import com.google.code.jgntp.*;
import com.google.common.base.*;
import com.google.common.collect.*;
import com.google.common.io.*;
import com.notifier.desktop.*;
import com.notifier.desktop.app.*;

public class GrowlNotificationBroadcaster extends AbstractLifecycle implements NotificationBroadcaster {

	public static final String APPLICATION_ICON = "app-icon.png";

	private static final long NOTIFY_TIMEOUT = 1;
	private static final long SHUTDOWN_TIMEOUT = 10;

	private String REGISTRATION_ERROR_TITLE = Application.NAME + " Growl Registration Error";
	private String NOTIFICATION_ERROR_TITLE = Application.NAME + " Growl Notification Error";

	private static final Logger logger = LoggerFactory.getLogger(GrowlNotificationBroadcaster.class);

	private GntpClient gntpClient;
	private EnumMap<Notification.Type, GntpNotificationInfo> notificationInfos;

	private GrowlPatched macGrowl;

	private int growlConnectionRetries;

	@Override
	public String getName() {
		return "Growl";
	}

	@Override
	public void doStart() throws Exception {
		if (OperatingSystems.CURRENT_FAMILY == OperatingSystems.Family.MAC) {
			macGrowl = new GrowlPatched();
			macGrowl.addGrowlHost("localhost", null);
			GrowlRegistrations registrations = macGrowl.getRegistrations(Application.NAME);
			registrations.registerNotification(Notification.Type.RING.toString(), true);
			registrations.registerNotification(Notification.Type.SMS.toString(), true);
			registrations.registerNotification(Notification.Type.MMS.toString(), true);
			registrations.registerNotification(Notification.Type.BATTERY.toString(), true);
			registrations.registerNotification(Notification.Type.VOICEMAIL.toString(), true);
			registrations.registerNotification(Notification.Type.PING.toString(), true);
			registrations.registerNotification(Notification.Type.USER.toString(), true);
			macGrowl.sendRegistrations();
		} else {
			GntpApplicationInfo appInfo = Gntp.appInfo(Application.NAME).icon(getIcon(APPLICATION_ICON)).build();

			notificationInfos = Maps.newEnumMap(Notification.Type.class);
			notificationInfos.put(Notification.Type.RING, infoForType(appInfo, Notification.Type.RING));
			notificationInfos.put(Notification.Type.SMS, infoForType(appInfo, Notification.Type.SMS));
			notificationInfos.put(Notification.Type.MMS, infoForType(appInfo, Notification.Type.MMS));
			notificationInfos.put(Notification.Type.BATTERY, infoForType(appInfo, Notification.Type.BATTERY));
			notificationInfos.put(Notification.Type.VOICEMAIL, infoForType(appInfo, Notification.Type.VOICEMAIL));
			notificationInfos.put(Notification.Type.PING, infoForType(appInfo, Notification.Type.PING));
			notificationInfos.put(Notification.Type.USER, infoForType(appInfo, Notification.Type.USER));

			gntpClient = Gntp.client(appInfo).listener(new Listener()).build();
			gntpClient.register();
		}
	}

	@Override
	public void broadcast(Notification notification, boolean privateMode) {
		if (!isRunning()) {
			return;
		}
		if (OperatingSystems.CURRENT_FAMILY == OperatingSystems.Family.MAC) {
			GrowlNotification n = new GrowlNotification(notification.getType().toString(), notification.getTitle(), notification.getDescription(privateMode), Application.NAME, false,
					GrowlNotification.NORMAL);
			try {
				macGrowl.sendNotification(n);
			} catch (GrowlException e) {
				logger.error("Error sending notification using jgrowl", e);
				getApplication().showError(NOTIFICATION_ERROR_TITLE, "Error sending notification to Growl.");
			}
		} else {
			try {
				if (notification.getType() == Notification.Type.BATTERY) {
					String iconName = notification.getBatteryIconName();
					try {
						RenderedImage icon = getIcon(iconName);
						gntpClient.notify(Gntp.notification(notificationInfos.get(Notification.Type.BATTERY), notification.getTitle()).text(notification.getDescription(privateMode)).icon(icon).build(), NOTIFY_TIMEOUT, SECONDS);
					} catch (IOException e) {
						doNotify(notification, privateMode);
					}
				} else {
					doNotify(notification, privateMode);
				}
			} catch (InterruptedException e) {
				logger.debug("Interrupted sending GNTP notification");
				Thread.currentThread().interrupt();
			}
		}
	}

	@Override
	public void doStop() {
		if (OperatingSystems.CURRENT_FAMILY != OperatingSystems.Family.MAC) {
			try {
				gntpClient.shutdown(SHUTDOWN_TIMEOUT, SECONDS);
			} catch (InterruptedException e) {
				logger.debug("Interrupted shutting down GNTP client");
				Thread.currentThread().interrupt();
			}
		}
	}

	protected GntpNotificationInfo infoForType(GntpApplicationInfo appInfo, Notification.Type type) throws IOException {
		return Gntp.notificationInfo(appInfo, type.name()).displayName(type.getTitle()).icon(getIcon(type.getIconName())).build();
	}

	protected void doNotify(Notification notification, boolean privateMode) throws InterruptedException {
		GntpNotificationInfo info = notificationInfos.get(notification.getType());
		Preconditions.checkState(info != null, "Unknown notification type: %s", notification.getType());
		gntpClient.notify(Gntp.notification(info, notification.getTitle()).text(notification.getDescription(privateMode)).build(), NOTIFY_TIMEOUT, SECONDS);
	}

	protected RenderedImage getIcon(String name) throws IOException {
		InputStream is = getClass().getResourceAsStream(name);
		Preconditions.checkState(is != null);
		try {
			return ImageIO.read(is);
		} finally {
			Closeables.closeQuietly(is);
		}
	}

	private class Listener implements GntpListener {

		private boolean notifiedGrowlNotRunning;
		private boolean notifiedGrowlAuthenticationError;
		private boolean notifiedInvalidRegistration;
		private boolean notifiedGrowlInternalError;
		private boolean notifiedNotRegistered;

		@Override
		public void onRegistrationSuccess() {
			logger.info("Registered with GNTP server sucessfully");
			notifiedGrowlAuthenticationError = false;
			notifiedGrowlInternalError = false;
			growlConnectionRetries = 0;
		}

		@Override
		public void onRegistrationError(GntpErrorStatus status, String description) {
			logger.error("GNTP server refused registration [{}-{}]", status, description);
			switch (status) {
				case NOT_AUTHORIZED:
					if (!notifiedGrowlAuthenticationError) {
						notifiedGrowlAuthenticationError = true;
						getApplication().showError(REGISTRATION_ERROR_TITLE, "Growl denied registration, did you set it to require password for LAN apps?");
					}
					break;
				case INVALID_REQUEST:
				case REQUIRED_HEADER_MISSING:
				case UNKNOWN_PROTOCOL:
				case UNKNOWN_PROTOCOL_VERSION:
					if (!notifiedInvalidRegistration) {
						notifiedInvalidRegistration = true;
						getApplication().showError(REGISTRATION_ERROR_TITLE, "Growl said I sent an invalid registration request, I may need updating.");
					}
					break;
				case INTERNAL_SERVER_ERROR:
				case NETWORK_FAILURE:
				case UNKNOWN_APPLICATION:
				case UNKNOWN_NOTIFICATION:
				case TIMED_OUT:
				case RESERVED:
					if (!notifiedGrowlInternalError) {
						notifiedGrowlInternalError = true;
						getApplication().showError(REGISTRATION_ERROR_TITLE, "Growl had trouble handling registration request. It may need restart or updating.");
					}
					break;
			}
		}

		@Override
		public void onNotificationSuccess(GntpNotification notification) {
			notifiedGrowlAuthenticationError = false;
			notifiedGrowlInternalError = false;
			notifiedNotRegistered = false;
		}

		@Override
		public void onClickCallback(GntpNotification notification) {
			// Do nothing
		}

		@Override
		public void onCloseCallback(GntpNotification notification) {
			// Do nothing
		}

		@Override
		public void onTimeoutCallback(GntpNotification notification) {
			// Do nothing
		}

		@Override
		public void onNotificationError(GntpNotification notification, GntpErrorStatus status, String description) {
			logger.error("GNTP server refused notification [{}-{}]", status, description);
			switch (status) {
				case NOT_AUTHORIZED:
					if (!notifiedGrowlAuthenticationError) {
						notifiedGrowlAuthenticationError = true;
						getApplication().showError(NOTIFICATION_ERROR_TITLE, "Growl denied notification, did you set it to require password for LAN apps?");
					}
					break;
				case UNKNOWN_APPLICATION:
				case UNKNOWN_NOTIFICATION:
					if (!notifiedNotRegistered) {
						notifiedNotRegistered = true;
						getApplication().showError(NOTIFICATION_ERROR_TITLE, "Growl said I am not registered, trying registration again (this notification has been lost).");
					}
					break;
				default:
					if (!notifiedGrowlInternalError) {
						notifiedGrowlInternalError = true;
						getApplication().showError(NOTIFICATION_ERROR_TITLE, "Growl had trouble handling notification. It may need restart or updating.");
					}
					break;
			}
		}

		@Override
		public void onCommunicationError(Throwable t) {
			logger.error("Error communicating with GNTP server", t);
			if (t instanceof ConnectException &&
				growlConnectionRetries > 3 &&
				!notifiedGrowlNotRunning) {
				notifiedGrowlNotRunning = true;
				getApplication().showError(NOTIFICATION_ERROR_TITLE, "Growl is not running.");
			}
			growlConnectionRetries++;
		}
	}
}
