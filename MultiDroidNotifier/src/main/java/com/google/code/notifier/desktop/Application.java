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

public interface Application {

	String NAME = "Android Notifier";
	String ARTIFACT_ID = "android-notifier-desktop";
	String ICON_NAME = "icon.png";
	String ICON_NAME_MAC = "icon-mac.png";
	String LICENSE = "license.txt";

	void start(boolean showTrayIcon, boolean showPreferencesWindow);

	void shutdown();

	boolean adjustStartAtLogin(boolean enabled, boolean silent);

	boolean adjustWifiReceiver(boolean enabled);

	boolean adjustUpnpReceiver(boolean enabled);

	boolean adjustBluetoothReceiver(boolean enabled);

	boolean adjustSystemDefaultBroadcaster(boolean enabled);

	boolean adjustLibnotifyBroadcaster(boolean enabled);

	boolean adjustGrowlBroadcaster(boolean enabled);

	void showError(String title, String message);

	Version getVersion();

	void checkForUpdates();

}
