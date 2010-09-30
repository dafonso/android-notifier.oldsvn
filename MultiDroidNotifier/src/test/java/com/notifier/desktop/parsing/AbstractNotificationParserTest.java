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
package com.notifier.desktop.parsing;

import com.google.inject.*;
import com.google.inject.util.*;
import com.notifier.desktop.*;
import com.notifier.protocol.*;

public abstract class AbstractNotificationParserTest {

	private long deviceId = 123;
	private long notificationId = 12345;
	private Notification.Type type = Notification.Type.RING;
	private String data = "1234567890";
	private String description = "Test description";

	protected Provider<ApplicationPreferences> getPreferencesProvider() {
		ApplicationPreferences preferences = new ApplicationPreferences();
		preferences.setEncryptCommunication(false);
		preferences.setCommunicationPassword(new byte[0]);
		return Providers.of(preferences);
	}

	protected String createTextNotification() {
		return TextNotificationParser.SUPPORTED_VERSION + "/" + Long.toHexString(deviceId) + "/" + Long.toHexString(notificationId) + "/" + type.name() + "/" + data + "/" + description;
	}

	protected Protocol.Notification createProtobufNotification() {
		Protocol.Notification.Builder builder = Protocol.Notification.newBuilder();
		builder.setDeviceId(deviceId).setId(notificationId);
		builder.setType(Protocol.Notification.Type.RING).setPhoneNumber(data);
		builder.setDescription(description);

		return builder.build();
	}

	protected Notification createNotification() {
		return new Notification(deviceId, notificationId, type, data, description);
	}
}
