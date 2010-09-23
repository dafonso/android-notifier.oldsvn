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
package com.google.code.notifier.desktop;

import java.io.*;
import java.util.*;
import java.util.prefs.*;

import com.google.common.base.*;
import com.google.common.collect.*;

public class ApplicationPreferences {

	private static final String START_AT_LOGIN = "startAtLogin";
	private static final String RECEPTION_WITH_WIFI = "receptionWithWifi";
	private static final String RECEPTION_WITH_UPNP = "receptionWithUpnp";
	private static final String RECEPTION_WITH_BLUETOOTH = "receptionWithBluetooth";
	private static final String RECEPTION_WITH_USB = "receptionWithUsb";
	private static final String DISPLAY_WITH_SYSTEM_DEFAULT = "displayWithSystemDefault";
	private static final String DISPLAY_WITH_GROWL = "displayWithGrowl";
	private static final String DISPLAY_WITH_LIBNOTIFY = "displayWithLibnotify";
	private static final String RECEPTION_FROM_ANY_DEVICE = "receptionFromAnyDevice";
	private static final String ALLOWED_DEVICES_IDS = "allowedDevicesIds";

	private static final String NOTIFICATION_ENABLED = "_enabled";
	private static final String NOTIFICATION_CLIPBOARD = "_clipboard";
	private static final String NOTIFICATION_EXECUTE_COMMAND = "_executeCommand";
	private static final String NOTIFICATION_COMMAND = "_command";

	private static final char ALLOWED_DEVICES_IDS_SEPARATOR = '|';

	private boolean startAtLogin;

	private boolean receptionWithWifi;
	private boolean receptionWithUpnp;
	private boolean receptionWithBluetooth;
	private boolean receptionWithUsb;

	private boolean displayWithSystemDefault;
	private boolean displayWithGrowl;
	private boolean displayWithLibnotify;

	private boolean receptionFromAnyDevice;
	private Set<String> allowedDevicesIds;

	private Map<String, Object> notificationsSettings;

	public ApplicationPreferences() {
		allowedDevicesIds = Sets.newTreeSet();
		notificationsSettings = Maps.newHashMap();
	}

	public void read() {
		Preferences prefs = Preferences.userNodeForPackage(ApplicationPreferences.class);
		startAtLogin = prefs.getBoolean(START_AT_LOGIN, false);
		receptionWithWifi = prefs.getBoolean(RECEPTION_WITH_WIFI, true);
		receptionWithUpnp = prefs.getBoolean(RECEPTION_WITH_UPNP, false);
		receptionWithBluetooth = prefs.getBoolean(RECEPTION_WITH_BLUETOOTH, false);
		receptionWithUsb = prefs.getBoolean(RECEPTION_WITH_USB, false);
		displayWithSystemDefault = prefs.getBoolean(DISPLAY_WITH_SYSTEM_DEFAULT, true);
		displayWithGrowl = prefs.getBoolean(DISPLAY_WITH_GROWL, false);
		displayWithLibnotify = prefs.getBoolean(DISPLAY_WITH_LIBNOTIFY, false);
		receptionFromAnyDevice = prefs.getBoolean(RECEPTION_FROM_ANY_DEVICE, true);
		allowedDevicesIds = Sets.newTreeSet(Splitter.on(ALLOWED_DEVICES_IDS_SEPARATOR).omitEmptyStrings().split(prefs.get(ALLOWED_DEVICES_IDS, "")));

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
	}

	public void write() throws IOException {
		Preferences prefs = Preferences.userNodeForPackage(ApplicationPreferences.class);
		prefs.putBoolean(START_AT_LOGIN, startAtLogin);
		prefs.putBoolean(RECEPTION_WITH_WIFI, receptionWithWifi);
		prefs.putBoolean(RECEPTION_WITH_UPNP, receptionWithUpnp);
		prefs.putBoolean(RECEPTION_WITH_BLUETOOTH, receptionWithBluetooth);
		prefs.putBoolean(RECEPTION_WITH_USB, receptionWithUsb);
		prefs.putBoolean(DISPLAY_WITH_SYSTEM_DEFAULT, displayWithSystemDefault);
		prefs.putBoolean(DISPLAY_WITH_GROWL, displayWithGrowl);
		prefs.putBoolean(DISPLAY_WITH_LIBNOTIFY, displayWithLibnotify);
		prefs.putBoolean(RECEPTION_FROM_ANY_DEVICE, receptionFromAnyDevice);
		prefs.put(ALLOWED_DEVICES_IDS, Joiner.on(ALLOWED_DEVICES_IDS_SEPARATOR).join(allowedDevicesIds));

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

		try {
			prefs.flush();
		} catch (BackingStoreException e) {
			throw new IOException("Error flushing preferences", e);
		}
	}

	public boolean addAllowedDeviceId(String deviceId) {
		return allowedDevicesIds.add(deviceId);
	}

	public void removeAllowedDeviceId(String deviceId) {
		allowedDevicesIds.remove(deviceId);
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

	// Getters/Setters

	public boolean isStartAtLogin() {
		return startAtLogin;
	}

	public void setStartAtLogin(boolean startAtLogin) {
		this.startAtLogin = startAtLogin;
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

	public boolean isReceptionFromAnyDevice() {
		return receptionFromAnyDevice;
	}

	public void setReceptionFromAnyDevice(boolean receptionFromAnyDevice) {
		this.receptionFromAnyDevice = receptionFromAnyDevice;
	}

	public Set<String> getAllowedDevicesIds() {
		return allowedDevicesIds;
	}

	public void setAllowedDevicesIds(Set<String> allowedDevicesIds) {
		this.allowedDevicesIds = allowedDevicesIds;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("startAtLogin=");
		builder.append(startAtLogin);
		builder.append(", receptionWithWifi=");
		builder.append(receptionWithWifi);
		builder.append(", receptionWithUpnp=");
		builder.append(receptionWithUpnp);
		builder.append(", receptionWithBluetooth=");
		builder.append(receptionWithBluetooth);
		builder.append(", receptionWithUsb=");
		builder.append(receptionWithUsb);
		builder.append(", displayWithSystemDefault=");
		builder.append(displayWithSystemDefault);
		builder.append(", displayWithGrowl=");
		builder.append(displayWithGrowl);
		builder.append(", displayWithLibnotify=");
		builder.append(displayWithLibnotify);
		builder.append(", receptionFromAnyDevice=");
		builder.append(receptionFromAnyDevice);
		builder.append(", allowedDevicesIds=");
		builder.append(allowedDevicesIds);

		for (Notification.Type type : Notification.Type.values()) {
			builder.append(", ");
			builder.append(type.name() + NOTIFICATION_ENABLED);
			builder.append('=');
			builder.append(isNotificationEnabled(type));

			builder.append(", ");
			builder.append(type.name() + NOTIFICATION_CLIPBOARD);
			builder.append('=');
			builder.append(isNotificationClipboard(type));

			builder.append(", ");
			builder.append(type.name() + NOTIFICATION_EXECUTE_COMMAND);
			builder.append('=');
			builder.append(isNotificationExecuteCommand(type));

			builder.append(", ");
			builder.append(type.name() + NOTIFICATION_COMMAND);
			builder.append('=');
			builder.append(getNotificationCommand(type));
		}

		return builder.toString();
	}

}
