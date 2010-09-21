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
package com.google.code.notifier.desktop.parsing;

import java.util.*;

import com.google.code.notifier.desktop.*;
import com.google.common.base.*;

public class StringNotificationParser implements NotificationParser<String> {

	public static final char FIELD_SEPARATOR = '/';
	public static final String SUPPORTED_VERSION = "v2";

	@Override
	public Notification parse(String s) {
		Iterator<String> splitted = Splitter.on(FIELD_SEPARATOR).split(s).iterator();

		String version = splitted.next();
		if (!SUPPORTED_VERSION.equals(version)) {
			throw new IllegalStateException("Protocol version [" + version + "] is not supported");
		}

		String deviceId = splitted.next();
		String notificationId = splitted.next();
		Notification.Type type = Notification.Type.valueOf(splitted.next());
		String data = splitted.next();
		StringBuilder contents = new StringBuilder();
		while (splitted.hasNext()) {
			contents.append(splitted.next());
			if (splitted.hasNext()) {
				contents.append(FIELD_SEPARATOR);
			}
		}

		return new Notification(deviceId, notificationId, type, data, contents.toString());
	}
}
