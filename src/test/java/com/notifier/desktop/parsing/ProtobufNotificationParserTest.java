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

import org.junit.*;

import com.google.protobuf.*;
import com.notifier.desktop.*;
import com.notifier.protocol.*;

import static org.junit.Assert.*;

public class ProtobufNotificationParserTest extends AbstractNotificationParserTest {

	@Test
	public void parse() throws Exception {
		boolean encrypted = false;

		Protocol.Notification protoNotification = createProtobufNotification();
		byte[] protoData = protoNotification.toByteArray();

		int length = protoData.length;
		length += CodedOutputStream.computeRawVarint32Size(length);
		length += CodedOutputStream.computeBoolSizeNoTag(encrypted);

		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		CodedOutputStream codedOutputStream = CodedOutputStream.newInstance(baos);
		codedOutputStream.writeRawVarint32(length);
		codedOutputStream.writeBoolNoTag(encrypted);
		codedOutputStream.writeRawBytes(protoNotification.toByteArray());
		codedOutputStream.flush();

		byte[] msg = baos.toByteArray();
		assertEquals(length, msg.length);

		ProtobufNotificationParser parser = new ProtobufNotificationParser(getPreferencesProvider());
		Notification notification = parser.parse(msg);

		Notification expectedNotification = createNotification();
		assertEquals(expectedNotification, notification);
	}
}
