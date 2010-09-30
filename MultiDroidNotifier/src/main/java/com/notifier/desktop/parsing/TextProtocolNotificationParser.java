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

import java.nio.charset.*;
import java.util.*;

import org.slf4j.*;

import com.google.common.base.*;
import com.google.common.collect.*;
import com.google.inject.*;
import com.notifier.desktop.*;

public class TextProtocolNotificationParser extends EncryptedNotificationParser {

	public static final Charset CHARSET = Charsets.UTF_8;
	public static final char FIELD_SEPARATOR = '/';
	public static final int FIELD_COUNT = 6;
	public static final String SUPPORTED_VERSION = "v2";

	private static final Logger logger = LoggerFactory.getLogger(TextProtocolNotificationParser.class);

	@Inject
	public TextProtocolNotificationParser(Provider<ApplicationPreferences> preferencesProvider) {
		super(preferencesProvider.get());
	}

	@Override
	public Notification parse(byte[] msg) {
		byte[] msgToUse = decryptIfNecessary(msg);
		if (msgToUse == null) {
			return null;
		}

		String s = new String(msgToUse, CHARSET);
		Iterable<String> splitted = Splitter.on(FIELD_SEPARATOR).split(s);
		if (Iterables.size(splitted) < FIELD_COUNT) {
			logger.debug("Got notification but it has less fields than expected, maybe it's encrypted, ignoring");
			return null;
		}

		Iterator<String> iterator = splitted.iterator();
		String version = iterator.next();
		if (!SUPPORTED_VERSION.equals(version)) {
			throw new IllegalStateException("Protocol version [" + version + "] is not supported");
		}

		String deviceId = iterator.next();
		String notificationId = iterator.next();
		Notification.Type type = Notification.Type.valueOf(iterator.next());
		String data = iterator.next();
		StringBuilder contents = new StringBuilder();
		while (iterator.hasNext()) {
			contents.append(iterator.next());
			if (iterator.hasNext()) {
				contents.append(FIELD_SEPARATOR);
			}
		}

		return new Notification(deviceId, notificationId, type, data, contents.toString());
	}

}
