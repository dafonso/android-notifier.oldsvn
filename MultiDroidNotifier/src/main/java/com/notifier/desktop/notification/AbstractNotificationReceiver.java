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

import com.google.inject.*;
import com.notifier.desktop.*;

public abstract class AbstractNotificationReceiver extends RestartableService implements NotificationReceiver {

	public static final int PORT = 10600;
	public static final int SHUTDOWN_TIMEOUT = 10000;

	private @Inject NotificationManager notificationManager;
	private @Inject NotificationParser<byte[]> notificationParser;

	public AbstractNotificationReceiver() {
	}

	public AbstractNotificationReceiver(boolean autoAcknowledgeStateTransitions) {
		super(autoAcknowledgeStateTransitions);
	}
	
	public NotificationManager getNotificationManager() {
		return notificationManager;
	}

	public NotificationParser<byte[]> getNotificationParser() {
		return notificationParser;
	}
}
