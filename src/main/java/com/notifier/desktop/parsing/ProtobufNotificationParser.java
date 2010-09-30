package com.notifier.desktop.parsing;

import java.io.*;

import com.google.inject.*;
import com.google.protobuf.*;
import com.notifier.desktop.*;
import com.notifier.desktop.exception.*;
import com.notifier.protocol.*;

public class ProtobufNotificationParser extends EncryptedNotificationParser {

	private static final int BOOL_SIZE = CodedOutputStream.computeBoolSizeNoTag(true);

	@Inject
	public ProtobufNotificationParser(Provider<ApplicationPreferences> preferencesProvider) {
		super(preferencesProvider.get());
	}

	@Override
	public Notification parse(byte[] data) throws ParseException {
		try {
			CodedInputStream codedInputStream = CodedInputStream.newInstance(data);
			int length = codedInputStream.readRawVarint32();
			boolean encrypted = codedInputStream.readBool();
			int messageLength = length - CodedOutputStream.computeRawVarint32Size(length) - BOOL_SIZE;
			byte[] msg = codedInputStream.readRawBytes(messageLength);

			if (encrypted) {
				msg = decryptIfNecessary(msg);
				if (msg == null) {
					return null;
				}
			}

			Protocol.Notification protoNotification = Protocol.Notification.parseFrom(msg);
			return parseNotificationFromProto(protoNotification);
		} catch (IOException e) {
			throw new ParseException(e);
		}
	}

	protected Notification parseNotificationFromProto(Protocol.Notification protoNotification) {
		String deviceId = Integer.toString(protoNotification.getDeviceId());
		String id = Integer.toString(protoNotification.getId());
		Notification.Type type = parseTypeFromProto(protoNotification.getType());
		String data;
		switch (type) {
			case BATTERY:
				data = Integer.toString(protoNotification.getBatteryLevel());
				break;
			case RING:
			case SMS:
			case MMS:
				data = protoNotification.getPhoneNumber();
				break;
			case USER:
				data = protoNotification.getTitle();
				break;
			default:
				data = null;
				break;
		}
		String description = protoNotification.getDescription();

		return new Notification(deviceId, id, type, data, description);
	}

	protected Notification.Type parseTypeFromProto(Protocol.Notification.Type protoType) {
		switch (protoType) {
			case BATTERY:
				return Notification.Type.BATTERY;
			case MMS:
				return Notification.Type.MMS;
			case PING:
				return Notification.Type.PING;
			case RING:
				return Notification.Type.RING;
			case SMS:
				return Notification.Type.SMS;
			case USER:
				return Notification.Type.USER;
			case VOICEMAIL:
				return Notification.Type.VOICEMAIL;
			default:
				throw new IllegalStateException("Unknown proto notification type: " + protoType);
		}
	}
}
