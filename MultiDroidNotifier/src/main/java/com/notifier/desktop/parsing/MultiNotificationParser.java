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

import java.io.*;

import com.google.inject.*;
import com.google.protobuf.*;
import com.notifier.desktop.*;
import com.notifier.desktop.exception.*;

public class MultiNotificationParser implements NotificationParser<byte[]> {

	private final TextNotificationParser textParser;
	private final ProtobufNotificationParser protobufParser;

	@Inject
	public MultiNotificationParser(TextNotificationParser textParser, ProtobufNotificationParser protobufParser) {
		this.textParser = textParser;
		this.protobufParser = protobufParser;
	}

	@Override
	public Notification parse(byte[] data) throws ParseException {
		try {
			CodedInputStream codedInputStream = CodedInputStream.newInstance(data);
			int length = codedInputStream.readRawVarint32();
			if (length == data.length) {
				try {
					return protobufParser.parse(data);
				} catch (ParseException e) {
					// Could not read protobuf message, must be text protocol
					return textParser.parse(data);
				}
			}
			return textParser.parse(data);
		} catch (IOException e) {
			throw new ParseException(e);
		}
	}

	@Override
	public void setEncryption(boolean decrypt, byte[] key) {
		textParser.setEncryption(decrypt, key);
		protobufParser.setEncryption(decrypt, key);
	}
}
