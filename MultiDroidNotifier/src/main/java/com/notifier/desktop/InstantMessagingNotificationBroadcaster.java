package com.notifier.desktop;

public interface InstantMessagingNotificationBroadcaster extends NotificationBroadcaster {

	String getUsername();
	void setUsername(String username);

	String getPassword();
	void setPassword(String password);

	String getTargetUsername();
	void setTargetUsername(String targetUsername);

}
