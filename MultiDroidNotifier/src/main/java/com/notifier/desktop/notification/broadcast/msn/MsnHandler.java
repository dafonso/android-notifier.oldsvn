package com.notifier.desktop.notification.broadcast.msn;

import java.io.*;
import java.util.*;
import java.util.concurrent.*;

import org.slf4j.*;

import com.google.common.io.*;
import com.notifier.desktop.*;

import net.sf.jml.*;
import net.sf.jml.event.*;
import net.sf.jml.message.*;

public class MsnHandler extends MsnAdapter {

	private static final Logger logger = LoggerFactory.getLogger(MsnNotificationBroadcaster.class);

	private String username;
	private String targetUsername;
	private Email targetEmail;
	private MsnMessenger msnMessenger;
	private CountDownLatch loginLatch;

	public MsnHandler(MsnMessenger msnMessenger, String username, String targetUsername) {
		this.msnMessenger = msnMessenger;
		this.username = username;
		this.targetUsername = targetUsername;
		this.targetEmail = Email.parseStr(targetUsername);
		this.loginLatch = new CountDownLatch(1);
	}

	private MsnSwitchboard currentSwitchboard;
	private Queue<NotificationItem> notificationsToSend = new ConcurrentLinkedQueue<NotificationItem>();

	public void send(Notification notification, String deviceName, boolean privateMode) {
		notificationsToSend.add(new NotificationItem(notification, deviceName, privateMode));
		if (currentSwitchboard == null) {
			msnMessenger.newSwitchboard(Application.ARTIFACT_ID);
		} else {
			sendToSwitchboard();
		}
	}

	@Override
	public void exceptionCaught(MsnMessenger messenger, Throwable throwable) {
		logger.error("Exception on msn client", throwable);
	}

	@Override
	public void contactListSyncCompleted(MsnMessenger messenger) {
		if (!containsTargetContact()) {
			logger.info("Target contact is not among my friends, unable to send messages to him");
		}
	}

	@Override
	public void contactListInitCompleted(MsnMessenger messenger) {
		logger.debug("Logged into msn successfully");
		loginLatch.countDown();
	}

	@Override
	public void switchboardStarted(MsnSwitchboard switchboard) {
		if (currentSwitchboard == null && Application.ARTIFACT_ID.equals(switchboard.getAttachment())) {
			logger.debug("Switchboard started");
			currentSwitchboard = switchboard;
			if (isContactOnline()) {
				if (currentSwitchboard.containContact(targetEmail)) {
					sendToSwitchboard();
				} else {
					currentSwitchboard.inviteContact(targetEmail);
				}
			}
		}
	}

	@Override
	public void contactJoinSwitchboard(MsnSwitchboard switchboard, MsnContact contact) {
		if (Application.ARTIFACT_ID.equals(switchboard.getAttachment())) {
			logger.debug("Target contact joined switchboard");
			sendToSwitchboard();
		}
	}

	@Override
	public void switchboardClosed(MsnSwitchboard switchboard) {
		if (Application.ARTIFACT_ID.equals(switchboard.getAttachment())) {
			logger.debug("Current switchboard has been closed");
			currentSwitchboard = null;
		}
	}

	@Override
	public void instantMessageReceived(MsnSwitchboard switchboard, MsnInstantMessage message, MsnContact contact) {
		switchboard.sendText("Don't talk to me, I don't understand what you are saying!");
	}

	@Override
	public void logout(MsnMessenger messenger) {
		logger.debug("Logged out of msn");
	}

	protected void sendToSwitchboard() {
		try {
			loginLatch.await();
		} catch (InterruptedException e) {
			return;
		}

		NotificationItem item;
		while ((item = notificationsToSend.poll()) != null) {
			boolean sentIcon = false;
			try {
				String iconName = item.notification.getType() == Notification.Type.BATTERY ? item.notification.getBatteryIconName() : item.notification.getType().getIconName();
				MsnObject emoticon = loadMsnIcon(username, iconName, MsnObject.TYPE_CUSTOM_EMOTICON);
				MsnEmoticonMessage emoticonMsg = new MsnEmoticonMessage();
				emoticonMsg.putEmoticon(":" + item.notification.getType().name() + ":", emoticon, msnMessenger.getDisplayPictureDuelManager());
				currentSwitchboard.sendMessage(emoticonMsg, true);
				sentIcon = true;
			} catch (Exception e) {
				logger.warn("Error sending emoticon to switchboard", e);
				sentIcon = false;
			}

			StringBuilder sb = new StringBuilder();
			if (sentIcon) {
				sb.append(":" + item.notification.getType().name() + ": ");
			}
			sb.append(item.notification.getTitle(item.deviceName));
			if (!item.privateMode) {
				sb.append(":\n");
				String description = item.notification.getDescription(item.privateMode);
				sb.append(description.substring(0, description.length() - 1));
			}
			MsnInstantMessage msg = new MsnInstantMessage();
			msg.setContent(sb.toString());
			currentSwitchboard.sendMessage(msg);
		}
	}

	protected boolean containsTargetContact() {
		return msnMessenger.getContactList().getContactByEmail(targetEmail) != null;
	}

	protected boolean isContactOnline() {
		return msnMessenger.getContactList().getContactByEmail(targetEmail).getStatus() != MsnUserStatus.OFFLINE;
	}

	protected MsnObject loadMsnIcon(String creator, String name, int type) throws IOException {
		InputStream is = getClass().getResourceAsStream(name);
		try {
			byte[] data = ByteStreams.toByteArray(is);
			MsnObject msnObject = MsnObject.getInstance(creator, data);
			msnObject.setType(type);
			return msnObject;
		} finally {
			Closeables.closeQuietly(is);
		}
	}

	private static class NotificationItem {
		final Notification notification;
		final String deviceName;
		final boolean privateMode;

		NotificationItem(Notification notification, String deviceName, boolean privateMode) {
			this.notification = notification;
			this.deviceName = deviceName;
			this.privateMode = privateMode;
		}
	}
}
