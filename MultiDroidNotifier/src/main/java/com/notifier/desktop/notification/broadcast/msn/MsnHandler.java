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
package com.notifier.desktop.notification.broadcast.msn;

import java.io.*;
import java.util.*;
import java.util.concurrent.*;

import org.slf4j.*;

import com.google.common.io.*;
import com.notifier.desktop.*;

import net.sf.jml.*;
import net.sf.jml.event.*;
import net.sf.jml.exception.*;
import net.sf.jml.message.*;

public class MsnHandler extends MsnAdapter {

	private static final Logger logger = LoggerFactory.getLogger(MsnHandler.class);

	private Application application;
	private MsnNotificationBroadcaster broadcaster;
	private MsnMessenger msnMessenger;
	private String username;
	private String targetUsername;
	private Email targetEmail;

	private MsnSwitchboard currentSwitchboard;
	private SwitchboardState switchboardState;
	private Queue<NotificationItem> notificationsToSend = new ConcurrentLinkedQueue<NotificationItem>();

	public MsnHandler(Application application, MsnNotificationBroadcaster broadcaster, MsnMessenger msnMessenger, String username, String targetUsername) {
		this.application = application;
		this.broadcaster = broadcaster;
		this.msnMessenger = msnMessenger;
		this.username = username;
		this.targetUsername = targetUsername;
		this.targetEmail = Email.parseStr(targetUsername);
		this.switchboardState = SwitchboardState.CREATING;
	}

	public void send(Notification notification, String deviceName, boolean privateMode) {
		notificationsToSend.add(new NotificationItem(notification, deviceName, privateMode));
		switch (switchboardState) {
			case CREATING:
			case INVITING:
				// After invite, queued notifications will be sent
				break;
			case INVITED:
				sendToSwitchboardInternal();
				break;
			default:
				throw new IllegalStateException("Unknown switchboard state: " + switchboardState);
		}
	}

	@Override
	public void exceptionCaught(MsnMessenger messenger, Throwable throwable) {
		if (throwable instanceof LoginException) {
			logger.warn("Could not log into msn");
			broadcaster.notifyFailed(new Exception("Could not log into Windows Live Messaging, invalid username or password.", throwable));
		} else {
			logger.error("Exception on msn client", throwable);
		}
	}

	@Override
	public void datacastMessageReceived(MsnSwitchboard switchboard, MsnDatacastMessage message, MsnContact contact) {
		logger.info("Datacast received: " + message);
	}

	@Override
	public void contactListSyncCompleted(MsnMessenger messenger) {
		if (!containsTargetContact()) {
			String msg = "Target contact is not among my friends, unable to send messages to him.";
			logger.info(msg);
			application.showError(Application.NAME + " Windows Live Messaging Error", msg);
		}
	}

	@Override
	public void contactListInitCompleted(MsnMessenger messenger) {
		logger.debug("Logged into msn successfully");
		broadcaster.notifyStarted();
		createSwitchboard();
	}

	@Override
	public void contactAddedMe(MsnMessenger messenger, MsnContact contact) {
		logger.debug("Contact added me [{}]", contact.getEmail());
		msnMessenger.addFriend(contact.getEmail(), contact.getFriendlyName());
		if (targetEmail.equals(contact.getEmail())) {
			inviteToSwitchboard();
		}
	}

	@Override
	public void contactStatusChanged(MsnMessenger messenger, MsnContact contact) {
		if (targetEmail.equals(contact.getEmail()) && currentSwitchboard != null) {
			if (contact.getStatus() == MsnUserStatus.ONLINE) {
				logger.debug("Target contact is online, inviting");
				inviteToSwitchboard();
			}
		}
	}

	@Override
	public void switchboardStarted(MsnSwitchboard switchboard) {
		if (Application.ARTIFACT_ID.equals(switchboard.getAttachment())) {
			logger.debug("Switchboard started");
			currentSwitchboard = switchboard;
			inviteToSwitchboard();
		}
	}

	@Override
	public void contactJoinSwitchboard(MsnSwitchboard switchboard, MsnContact contact) {
		if (Application.ARTIFACT_ID.equals(switchboard.getAttachment())) {
			logger.debug("Target contact joined switchboard");
			switchboardState = SwitchboardState.INVITED;
			sendToSwitchboardInternal();
		}
	}

	@Override
	public void contactLeaveSwitchboard(MsnSwitchboard switchboard, MsnContact contact) {
		if (Application.ARTIFACT_ID.equals(switchboard.getAttachment())) {
			inviteToSwitchboard();
		}
	}

	@Override
	public void switchboardClosed(MsnSwitchboard switchboard) {
		if (Application.ARTIFACT_ID.equals(switchboard.getAttachment())) {
			logger.debug("Current switchboard has been closed");
			currentSwitchboard = null;
			createSwitchboard();
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

	protected void createSwitchboard() {
		if (currentSwitchboard == null) {
			msnMessenger.newSwitchboard(Application.ARTIFACT_ID);
			switchboardState = SwitchboardState.CREATING;
		}
	}

	protected void inviteToSwitchboard() {
		if (switchboardState == null) {
			createSwitchboard();
		} else if (!currentSwitchboard.containContact(targetEmail) &&
					containsTargetContact() && isTargetOnline()) {
			switchboardState = SwitchboardState.INVITING;
			currentSwitchboard.inviteContact(targetEmail);
		}
	}

	protected void sendToSwitchboardInternal() {
		NotificationItem item;
		while ((item = notificationsToSend.peek()) != null) {
			boolean sentEmoticon = false;
			try {
				String iconName = item.notification.getType() == Notification.Type.BATTERY ? item.notification.getBatteryIconName() : item.notification.getType().getIconName();
				MsnObject emoticon = loadMsnIcon(username, iconName, MsnObject.TYPE_CUSTOM_EMOTICON);
				MsnEmoticonMessage emoticonMsg = new MsnEmoticonMessage();
				emoticonMsg.putEmoticon(getEmoticonName(item.notification.getType()), emoticon, msnMessenger.getDisplayPictureDuelManager());
				if (currentSwitchboard == null) {
					return;
				}
				currentSwitchboard.sendMessage(emoticonMsg, true);
				sentEmoticon = true;
			} catch (Exception e) {
				logger.warn("Error sending emoticon to switchboard", e);
				sentEmoticon = false;
			}

			MsnInstantMessage msg = new MsnInstantMessage();
			msg.setContent(getNotificationMessage(item.notification, item.deviceName, item.privateMode, sentEmoticon));
			if (currentSwitchboard != null) {
				currentSwitchboard.sendMessage(msg);
				notificationsToSend.remove();
			}
		}
	}

	protected String getNotificationMessage(Notification notification, String deviceName, boolean privateMode, boolean emoticon) {
		StringBuilder sb = new StringBuilder();
		if (emoticon) {
			sb.append(getEmoticonName(notification.getType()));
		}
		sb.append(notification.getTitle(deviceName));
		if (!privateMode) {
			sb.append(":\n");
			sb.append(notification.getDescription(privateMode));
		}
		return sb.toString();
	}

	protected boolean containsTargetContact() {
		return msnMessenger.getContactList().getContactByEmail(targetEmail) != null;
	}

	protected boolean isTargetOnline() {
		MsnContact contact = msnMessenger.getContactList().getContactByEmail(targetEmail);
		return contact != null && contact.getStatus() != MsnUserStatus.OFFLINE;
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

	protected String getEmoticonName(Notification.Type type) {
		return ":" + "emoticon_" + type.name() + ":";
	}

	private static enum SwitchboardState {
		CREATING, INVITING, INVITED
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
