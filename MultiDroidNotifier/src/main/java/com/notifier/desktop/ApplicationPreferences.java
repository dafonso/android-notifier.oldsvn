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
package com.notifier.desktop;

import java.io.*;
import java.util.*;
import java.util.prefs.*;

import org.slf4j.*;

import com.google.common.base.*;
import com.google.common.collect.*;
import com.notifier.desktop.notification.*;

public class ApplicationPreferences {

	private static final Logger logger = LoggerFactory.getLogger(ApplicationPreferences.class);

	private static final String START_AT_LOGIN = "startAtLogin";
	private static final String PRIVATE_MODE = "privateMode";

	private static final String RECEPTION_WITH_WIFI = "receptionWithWifi";
	private static final String RECEPTION_WITH_UPNP = "receptionWithUpnp";
	private static final String RECEPTION_WITH_BLUETOOTH = "receptionWithBluetooth";
	private static final String RECEPTION_WITH_USB = "receptionWithUsb";

	private static final String ANDROID_SDK_HOME = "androidSdkHome";

	private static final String ENCRYPT_COMMUNICATION = "encryptCommunication";
	private static final String COMMUNICATION_PASSWORD = "communicationPassword";

	private static final String DISPLAY_WITH_SYSTEM_DEFAULT = "displayWithSystemDefault";
	private static final String DISPLAY_WITH_GROWL = "displayWithGrowl";
	private static final String DISPLAY_WITH_LIBNOTIFY = "displayWithLibnotify";
	private static final String DISPLAY_WITH_MSN = "displayWithMsn";

	private static final String MSN_USERNAME = "msnUsername";
	private static final String MSN_PASSWORD = "msnPassword";
	private static final String MSN_TARGET = "msnTarget";

	private static final String RECEPTION_FROM_ANY_DEVICE = "receptionFromAnyDevice";
	private static final String ALLOWED_DEVICES_IDS = "allowedDevicesIds";
	private static final String ALLOWED_DEVICES_NAMES = "allowedDevicesNames";

	private static final String NOTIFICATION_ENABLED = "_enabled";
	private static final String NOTIFICATION_CLIPBOARD = "_clipboard";
	private static final String NOTIFICATION_EXECUTE_COMMAND = "_executeCommand";
	private static final String NOTIFICATION_COMMAND = "_command";

	private static final char ALLOWED_DEVICES_SEPARATOR = '|';
	private static final Splitter ALLOWED_DEVICES_SPLITTER = Splitter.on(ALLOWED_DEVICES_SEPARATOR).omitEmptyStrings();
	private static final Joiner ALLOWED_DEVICES_JOINER = Joiner.on(ALLOWED_DEVICES_SEPARATOR);

	private static final String EXPAND_PREFERENCE_GROUP = "_expand";

	private boolean startAtLogin;
	private boolean privateMode;

	private boolean receptionWithWifi;
	private boolean receptionWithUpnp;
	private boolean receptionWithBluetooth;
	private boolean receptionWithUsb;

	private String androidSdkHome;

	private boolean encryptCommunication;
	private byte[] communicationPassword;

	private boolean displayWithSystemDefault;
	private boolean displayWithGrowl;
	private boolean displayWithLibnotify;
	private boolean displayWithMsn;

	private boolean receptionFromAnyDevice;
	private Set<String> allowedDevicesIds;
	private List<String> allowedDevicesNames;

	private Map<String, Object> notificationsSettings;
	private Map<Group, Boolean> groupsExpansion;

	private String msnUsername;
	private String msnPassword;
	private String msnTarget;

	public ApplicationPreferences() {
		allowedDevicesIds = Sets.newTreeSet();
		allowedDevicesNames = Lists.newArrayList();
		notificationsSettings = Maps.newHashMap();
		groupsExpansion = Maps.newEnumMap(Group.class);
	}

	public void read() {
		migratePreferences();
		doRead(getPreferences());
	}

	protected void doRead(Preferences prefs) {
		startAtLogin = prefs.getBoolean(START_AT_LOGIN, false);
		privateMode = prefs.getBoolean(PRIVATE_MODE, false);

		receptionWithWifi = prefs.getBoolean(RECEPTION_WITH_WIFI, true);
		receptionWithUpnp = prefs.getBoolean(RECEPTION_WITH_UPNP, false);
		receptionWithBluetooth = prefs.getBoolean(RECEPTION_WITH_BLUETOOTH, false);
		receptionWithUsb = prefs.getBoolean(RECEPTION_WITH_USB, false);

		androidSdkHome = prefs.get(ANDROID_SDK_HOME, "");

		encryptCommunication = prefs.getBoolean(ENCRYPT_COMMUNICATION, false);
		communicationPassword = prefs.getByteArray(COMMUNICATION_PASSWORD, new byte[0]);

		displayWithSystemDefault = prefs.getBoolean(DISPLAY_WITH_SYSTEM_DEFAULT, true);
		displayWithGrowl = prefs.getBoolean(DISPLAY_WITH_GROWL, false);
		displayWithLibnotify = prefs.getBoolean(DISPLAY_WITH_LIBNOTIFY, false);
		displayWithMsn = prefs.getBoolean(DISPLAY_WITH_MSN, false);

		receptionFromAnyDevice = prefs.getBoolean(RECEPTION_FROM_ANY_DEVICE, true);
		allowedDevicesIds = Sets.newHashSet(ALLOWED_DEVICES_SPLITTER.split(prefs.get(ALLOWED_DEVICES_IDS, "")));

		allowedDevicesNames = Lists.newArrayList(ALLOWED_DEVICES_SPLITTER.split(prefs.get(ALLOWED_DEVICES_NAMES, "")));
		if (allowedDevicesNames.size() != allowedDevicesIds.size()) { // Can happen if updating from older versions
			allowedDevicesNames.clear();
			for (String deviceId : allowedDevicesIds) {
				allowedDevicesNames.add(deviceId);
			}
		}

		for (Notification.Type type : Notification.Type.values()) {
			String enabledName = type.name() + NOTIFICATION_ENABLED;
			boolean enabled = prefs.getBoolean(enabledName, true);
			notificationsSettings.put(enabledName, enabled);

			String clipboardName = type.name() + NOTIFICATION_CLIPBOARD;
			boolean clipboard = prefs.getBoolean(clipboardName, false);
			notificationsSettings.put(clipboardName, clipboard);

			String executeCommandName = type.name() + NOTIFICATION_EXECUTE_COMMAND;
			boolean executeCommand = prefs.getBoolean(executeCommandName, false);
			notificationsSettings.put(executeCommandName, executeCommand);

			String commandName = type.name() + NOTIFICATION_COMMAND;
			String command = prefs.get(commandName, "");
			notificationsSettings.put(commandName, command);
		}

		for (Group group : Group.values()) {
			groupsExpansion.put(group, prefs.getBoolean(group.name() + EXPAND_PREFERENCE_GROUP, true));
		}

		msnUsername = prefs.get(MSN_USERNAME, "");
		msnPassword = prefs.get(MSN_PASSWORD, "");
		msnTarget = prefs.get(MSN_TARGET, "");
	}

	public void write() throws IOException {
		Preferences prefs = getPreferences();
		prefs.putBoolean(START_AT_LOGIN, startAtLogin);
		prefs.putBoolean(PRIVATE_MODE, privateMode);

		prefs.putBoolean(RECEPTION_WITH_WIFI, receptionWithWifi);
		prefs.putBoolean(RECEPTION_WITH_UPNP, receptionWithUpnp);
		prefs.putBoolean(RECEPTION_WITH_BLUETOOTH, receptionWithBluetooth);
		prefs.putBoolean(RECEPTION_WITH_USB, receptionWithUsb);

		prefs.put(ANDROID_SDK_HOME, androidSdkHome);

		prefs.putBoolean(ENCRYPT_COMMUNICATION, encryptCommunication);
		prefs.putByteArray(COMMUNICATION_PASSWORD, communicationPassword);

		prefs.putBoolean(DISPLAY_WITH_SYSTEM_DEFAULT, displayWithSystemDefault);
		prefs.putBoolean(DISPLAY_WITH_GROWL, displayWithGrowl);
		prefs.putBoolean(DISPLAY_WITH_LIBNOTIFY, displayWithLibnotify);
		prefs.putBoolean(DISPLAY_WITH_MSN, displayWithMsn);

		prefs.putBoolean(RECEPTION_FROM_ANY_DEVICE, receptionFromAnyDevice);
		prefs.put(ALLOWED_DEVICES_IDS, ALLOWED_DEVICES_JOINER.join(allowedDevicesIds));
		prefs.put(ALLOWED_DEVICES_NAMES, ALLOWED_DEVICES_JOINER.join(allowedDevicesNames));

		for (Notification.Type type : Notification.Type.values()) {
			String enabledName = type.name() + NOTIFICATION_ENABLED;
			prefs.putBoolean(enabledName, (Boolean)notificationsSettings.get(enabledName));

			String clipboardName = type.name() + NOTIFICATION_CLIPBOARD;
			prefs.putBoolean(clipboardName, (Boolean)notificationsSettings.get(clipboardName));

			String executeCommandName = type.name() + NOTIFICATION_EXECUTE_COMMAND;
			prefs.putBoolean(executeCommandName, (Boolean)notificationsSettings.get(executeCommandName));

			String commandName = type.name() + NOTIFICATION_COMMAND;
			prefs.put(commandName, (String)notificationsSettings.get(commandName));
		}

		for (Group group : Group.values()) {
			boolean expand = groupsExpansion.get(group);
			prefs.putBoolean(group.name() + EXPAND_PREFERENCE_GROUP, expand);
		}

		prefs.put(MSN_USERNAME, msnUsername);
		prefs.put(MSN_PASSWORD, msnPassword);
		prefs.put(MSN_TARGET, msnTarget);

		try {
			prefs.flush();
		} catch (BackingStoreException e) {
			throw new IOException("Error flushing preferences", e);
		}
	}

	public boolean addAllowedDeviceId(String deviceId, String name) {
		boolean added = allowedDevicesIds.add(deviceId);
		if (added) {
			allowedDevicesNames.add(name);
		}
		return added;
	}

	public void removeAllowedDeviceId(String deviceId, String name) {
		allowedDevicesIds.remove(deviceId);
		allowedDevicesNames.remove(name);
	}

	public Map<String, String> getAllowedDevices() {
		Map<String, String> devices = Maps.newHashMap();
		Iterator<String> ids = allowedDevicesIds.iterator();
		Iterator<String> names = allowedDevicesNames.iterator();
		while (ids.hasNext()) {
			devices.put(ids.next(), names.next());
		}
		return devices;
	}

	public void setNotificationEnabled(Notification.Type type, boolean enabled) {
		String name = type.name() + NOTIFICATION_ENABLED;
		notificationsSettings.put(name, enabled);
	}

	public boolean isNotificationEnabled(Notification.Type type) {
		String name = type.name() + NOTIFICATION_ENABLED;
		return (Boolean)notificationsSettings.get(name);
	}

	public void setNotificationClipboard(Notification.Type type, boolean enabled) {
		String name = type.name() + NOTIFICATION_CLIPBOARD;
		notificationsSettings.put(name, enabled);
	}

	public boolean isNotificationClipboard(Notification.Type type) {
		String name = type.name() + NOTIFICATION_CLIPBOARD;
		return (Boolean)notificationsSettings.get(name);
	}
	
	public void setNotificationExecuteCommand(Notification.Type type, boolean enabled) {
		String name = type.name() + NOTIFICATION_EXECUTE_COMMAND;
		notificationsSettings.put(name, enabled);
	}

	public boolean isNotificationExecuteCommand(Notification.Type type) {
		String name = type.name() + NOTIFICATION_EXECUTE_COMMAND;
		return (Boolean)notificationsSettings.get(name);
	}

	public void setNotificationCommand(Notification.Type type, String command) {
		String name = type.name() + NOTIFICATION_COMMAND;
		notificationsSettings.put(name, command);
	}

	public String getNotificationCommand(Notification.Type type) {
		String name = type.name() + NOTIFICATION_COMMAND;
		return (String)notificationsSettings.get(name);
	}

	public boolean isGroupExpanded(Group group) {
		return groupsExpansion.get(group);
	}

	public void setGroupExpanded(Group group, boolean expanded) {
		groupsExpansion.put(group, expanded);
	}

	protected Preferences getPreferences() {
		return Preferences.userNodeForPackage(ApplicationPreferences.class);
	}

	protected void migratePreferences() {
		try {
			String oldNode = "com/google/code/notifier/desktop";
			if (Preferences.userRoot().nodeExists(oldNode)) {
				Preferences oldPrefs = Preferences.userRoot().node(oldNode);
				doRead(oldPrefs);
				write();
				oldPrefs.removeNode();
			}
		} catch (Exception e) {
			logger.warn("Could no migrate old preferences, will try reset");
			doRead(getPreferences());
		}
	}

	// Getters/Setters

	public boolean isStartAtLogin() {
		return startAtLogin;
	}

	public void setStartAtLogin(boolean startAtLogin) {
		this.startAtLogin = startAtLogin;
	}

	public boolean isPrivateMode() {
		return privateMode;
	}

	public void setPrivateMode(boolean privateMode) {
		this.privateMode = privateMode;
	}

	public boolean isReceptionWithWifi() {
		return receptionWithWifi;
	}

	public void setReceptionWithWifi(boolean receptionWithWifi) {
		this.receptionWithWifi = receptionWithWifi;
	}

	public boolean isReceptionWithUpnp() {
		return receptionWithUpnp;
	}

	public void setReceptionWithUpnp(boolean receptionWithUpnp) {
		this.receptionWithUpnp = receptionWithUpnp;
	}

	public boolean isReceptionWithBluetooth() {
		return receptionWithBluetooth;
	}

	public void setReceptionWithBluetooth(boolean receptionWithBluetooth) {
		this.receptionWithBluetooth = receptionWithBluetooth;
	}

	public boolean isReceptionWithUsb() {
		return receptionWithUsb;
	}

	public void setReceptionWithUsb(boolean receptionWithUsb) {
		this.receptionWithUsb = receptionWithUsb;
	}

	public String getAndroidSdkHome() {
		return androidSdkHome;
	}

	public void setAndroidSdkHome(String androidSdkHome) {
		this.androidSdkHome = androidSdkHome;
	}

	public boolean isEncryptCommunication() {
		return encryptCommunication;
	}

	public void setEncryptCommunication(boolean encryptCommunication) {
		this.encryptCommunication = encryptCommunication;
	}

	public byte[] getCommunicationPassword() {
		return communicationPassword;
	}

	public void setCommunicationPassword(byte[] communicationPassword) {
		this.communicationPassword = communicationPassword;
	}

	public boolean isDisplayWithSystemDefault() {
		return displayWithSystemDefault;
	}

	public void setDisplayWithSystemDefault(boolean displayWithSystemDefault) {
		this.displayWithSystemDefault = displayWithSystemDefault;
	}

	public boolean isDisplayWithGrowl() {
		return displayWithGrowl;
	}

	public void setDisplayWithGrowl(boolean displayWithGrowl) {
		this.displayWithGrowl = displayWithGrowl;
	}

	public boolean isDisplayWithLibnotify() {
		return displayWithLibnotify;
	}

	public void setDisplayWithLibnotify(boolean displayWithLibnotify) {
		this.displayWithLibnotify = displayWithLibnotify;
	}

	public boolean isDisplayWithMsn() {
		return displayWithMsn;
	}

	public void setDisplayWithMsn(boolean displayWithMsn) {
		this.displayWithMsn = displayWithMsn;
	}

	public boolean isReceptionFromAnyDevice() {
		return receptionFromAnyDevice;
	}

	public void setReceptionFromAnyDevice(boolean receptionFromAnyDevice) {
		this.receptionFromAnyDevice = receptionFromAnyDevice;
	}

	public Set<String> getAllowedDevicesIds() {
		return ImmutableSet.copyOf(allowedDevicesIds);
	}

	public List<String> getAllowedDevicesNames() {
		return ImmutableList.copyOf(allowedDevicesNames);
	}

	public String getMsnUsername() {
		return msnUsername;
	}

	public void setMsnUsername(String msnUsername) {
		this.msnUsername = msnUsername;
	}

	public String getMsnPassword() {
		return msnPassword;
	}

	public void setMsnPassword(String msnPassword) {
		this.msnPassword = msnPassword;
	}

	public String getMsnTarget() {
		return msnTarget;
	}

	public void setMsnTarget(String msnTarget) {
		this.msnTarget = msnTarget;
	}

	public static enum Group {
		GENERAL, RECEPTION, DISPLAY, ACTION, PAIRING
	}
}
