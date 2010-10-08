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
import net.sf.jml.message.*;

public class MsnHandler extends MsnAdapter {

	private static final Logger logger = LoggerFactory.getLogger(MsnNotificationBroadcaster.class);

	private String username;
	private String targetUsername;
	private Email targetEmail;
	private MsnMessenger msnMessenger;
	private MsnSwitchboard currentSwitchboard;
	private SwitchboardState switchboardState;
	private Queue<NotificationItem> notificationsToSend = new ConcurrentLinkedQueue<NotificationItem>();

	public MsnHandler(MsnMessenger msnMessenger, String username, String targetUsername) {
		this.msnMessenger = msnMessenger;
		this.username = username;
		this.targetUsername = targetUsername;
		this.targetEmail = Email.parseStr(targetUsername);
		this.switchboardState = SwitchboardState.CREATING;
	}

	public void send(Notification notification, String deviceName, boolean privateMode) {
		notificationsToSend.add(new NotificationItem(notification, deviceName, privateMode));
		sendToSwitchboard();
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
			sendToSwitchboard();
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

	protected void sendToSwitchboard() {
		switch (switchboardState) {
			case CREATING:
			case INVITING:
				// After invite, sendToSwitchboard will be called again
				break;
			case INVITED:
				sendToSwitchboardInternal();
				break;
			default:
				throw new IllegalStateException("Unknown switchboard state: " + switchboardState);
		}
	}

	protected void createSwitchboard() {
		msnMessenger.newSwitchboard(Application.ARTIFACT_ID);
		switchboardState = SwitchboardState.CREATING;
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
		while ((item = notificationsToSend.poll()) != null) {
			boolean sentIcon = false;
			try {
				String iconName = item.notification.getType() == Notification.Type.BATTERY ? item.notification.getBatteryIconName() : item.notification.getType().getIconName();
				MsnObject emoticon = loadMsnIcon(username, iconName, MsnObject.TYPE_CUSTOM_EMOTICON);
				MsnEmoticonMessage emoticonMsg = new MsnEmoticonMessage();
				emoticonMsg.putEmoticon(getEmoticonName(item.notification.getType()), emoticon, msnMessenger.getDisplayPictureDuelManager());
				currentSwitchboard.sendMessage(emoticonMsg, true);
				sentIcon = true;
			} catch (Exception e) {
				logger.warn("Error sending emoticon to switchboard", e);
				sentIcon = false;
			}
	
			StringBuilder sb = new StringBuilder();
			if (sentIcon) {
				sb.append(getEmoticonName(item.notification.getType()));
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
