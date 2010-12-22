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
package com.notifier.desktop.notification.parsing.impl;

import java.io.*;

import org.slf4j.*;

import com.google.inject.*;
import com.google.protobuf.*;
import com.notifier.desktop.notification.*;
import com.notifier.desktop.notification.parsing.*;
import com.notifier.desktop.view.*;

@Singleton
public class MultiNotificationParser implements NotificationParser<byte[]> {

	private static final Logger logger = LoggerFactory.getLogger(MultiNotificationParser.class);

	private final SwtManager swtManager;
	private final TextNotificationParser textParser;
	private final ProtobufNotificationParser protobufParser;

	@Inject
	public MultiNotificationParser(SwtManager swtManager, TextNotificationParser textParser, ProtobufNotificationParser protobufParser) {
		this.swtManager = swtManager;
		this.textParser = textParser;
		this.protobufParser = protobufParser;
	}

	@Override
	public Notification parse(byte[] data) throws ParseException {
		if (data.length == 0) {
			logger.warn("Got an empty notification, it may be a bluetooth issue, discarding");
			Dialogs.showError(swtManager, "Empty notification received", "Android Notifier sent an empty notification, this may be caused by a known issue in bluetooth communication.\nMore info: http://code.google.com/p/android-notifier/issues/detail?id=3", true);
			return null;
		}
		try {
			CodedInputStream codedInputStream = CodedInputStream.newInstance(data);
			int length = codedInputStream.readRawVarint32();
			if (length == data.length) {
				try {
					return protobufParser.parse(data);
				} catch (Exception e) {
					// Could not read protobuf message, must be text protocol
					return textParser.parse(data);
				}
			}
			return textParser.parse(data);
		} catch (IOException e) {
			return textParser.parse(data);
		}
	}

	@Override
	public void setEncryption(boolean decrypt, byte[] key) {
		textParser.setEncryption(decrypt, key);
		protobufParser.setEncryption(decrypt, key);
	}
}
