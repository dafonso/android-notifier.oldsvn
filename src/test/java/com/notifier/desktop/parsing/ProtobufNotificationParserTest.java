package com.notifier.desktop.parsing;

import java.io.*;

import org.junit.*;

import com.google.inject.util.*;
import com.google.protobuf.*;
import com.notifier.desktop.*;
import com.notifier.protocol.*;

import static org.junit.Assert.*;

public class ProtobufNotificationParserTest {

	@Test
	public void parse() throws Exception {
		boolean encrypted = false;
		int deviceId = 123;
		int id = 12345;
		String data = "1234567890";
		String description = "Test description";

		Protocol.Notification.Builder builder = Protocol.Notification.newBuilder();
		builder.setDeviceId(deviceId).setId(id);
		builder.setType(Protocol.Notification.Type.RING).setPhoneNumber(data);
		builder.setDescription(description);

		Protocol.Notification protoNotification = builder.build();
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

		ApplicationPreferences preferences = new ApplicationPreferences();
		preferences.setEncryptCommunication(false);
		preferences.setCommunicationPassword(new byte[0]);

		ProtobufNotificationParser parser = new ProtobufNotificationParser(Providers.of(preferences));
		Notification notification = parser.parse(msg);

		assertEquals(Integer.toString(deviceId), notification.getDeviceId());
		assertEquals(Integer.toString(id), notification.getNotificationId());
		assertEquals(Notification.Type.RING, notification.getType());
		assertEquals(data, notification.getData());
		assertEquals(description, notification.getDescription());
	}
}
