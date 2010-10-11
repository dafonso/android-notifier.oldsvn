package com.notifier.desktop;

public interface InstantMessagingNotificationBroadcaster extends NotificationBroadcaster {

	void setUsername(String username);
	void setPassword(String password);
	void setTargetUsername(String targetUsername);

}
