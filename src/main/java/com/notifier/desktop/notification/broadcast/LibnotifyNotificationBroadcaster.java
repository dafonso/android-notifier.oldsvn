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

import java.io.*;

import org.slf4j.*;

import com.notifier.desktop.*;
import com.notifier.desktop.app.*;

public class LibnotifyNotificationBroadcaster extends AbstractLifecycle implements NotificationBroadcaster {

	public static final String LIB_NOTIFY_COMMAND = "notify-send";
	private static final String ICON_DIRECTORY = "/usr/share/icons/" + Application.ARTIFACT_ID + "/";

	private static final Logger logger = LoggerFactory.getLogger(LibnotifyNotificationBroadcaster.class);

	@Override
	public String getName() {
		return "Libnotify";
	}

	@Override
	protected void doStart() {
		ProcessBuilder builder = new ProcessBuilder(LIB_NOTIFY_COMMAND);
		try {
			builder.start();
		} catch (IOException e) {
			throw new IllegalStateException("You have to install the package libnotify-bin to be able to see libnotify notifications.", e);
		}
	}

	@Override
	public void broadcast(Notification notification) {
		if (!isRunning()) {
			return;
		}

		String iconName = notification.getType() == Notification.Type.BATTERY ? notification.getBatteryIconName() : notification.getType().getIconName();
		String title = notification.getTitle();
		String description = notification.getDescription();

		ProcessBuilder builder = new ProcessBuilder(LIB_NOTIFY_COMMAND, "-i", ICON_DIRECTORY + iconName, "-c", notification.getType().name(), title, description);
		try {
			builder.start();
		} catch (IOException e) {
			logger.error("Error sending notification [" + notification + "] to libnotify", e);
			getApplication().showError(Application.NAME + " Libnotify Error", "An error while sending notification to libnotify.");
		}
	}

}
