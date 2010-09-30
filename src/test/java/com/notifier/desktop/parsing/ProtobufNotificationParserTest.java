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
