package com.notifier.desktop.parsing;

import com.google.inject.*;
import com.google.inject.util.*;
import com.notifier.desktop.*;
import com.notifier.protocol.*;

public abstract class AbstractNotificationParserTest {

	private int deviceId = 123;
	private int notificationId = 12345;
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
		return TextNotificationParser.SUPPORTED_VERSION + "/" + deviceId + "/" + notificationId + "/" + type.name() + "/" + data + "/" + description;
	}

	protected Protocol.Notification createProtobufNotification() {
		Protocol.Notification.Builder builder = Protocol.Notification.newBuilder();
		builder.setDeviceId(deviceId).setId(notificationId);
		builder.setType(Protocol.Notification.Type.RING).setPhoneNumber(data);
		builder.setDescription(description);

		return builder.build();
	}

	protected Notification createNotification() {
		return new Notification(Integer.toString(deviceId), Integer.toString(notificationId), type, data, description);
	}
}
