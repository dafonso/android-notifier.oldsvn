package com.notifier.desktop.parsing;

import org.junit.*;

import com.notifier.desktop.*;

import static org.junit.Assert.*;

public class MultiNotificationParserTest extends AbstractNotificationParserTest {

	@Test
	public void selectText() throws Exception {
		TextNotificationParser textParser = new TextNotificationParser(getPreferencesProvider());
		ProtobufNotificationParser protobufParser = new ProtobufNotificationParser(getPreferencesProvider());
		MultiNotificationParser parser = new MultiNotificationParser(textParser, protobufParser);
		
		Notification notification = parser.parse(createTextNotification().getBytes(TextNotificationParser.CHARSET));
		Notification expectedNotification = createNotification();

		assertEquals(expectedNotification, notification);
	}

}
