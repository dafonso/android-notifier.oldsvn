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

public class NotificationConfiguration {

	private final Notification.Type type;
	private boolean enabled;
	private boolean sendToClipboard;
	private boolean executeCommand;
	private String command;

	public NotificationConfiguration(Notification.Type type) {
		this.type = type;
	}

	public Notification.Type getType() {
		return type;
	}

	public boolean isEnabled() {
		return enabled;
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	public boolean isSendToClipboard() {
		return sendToClipboard;
	}

	public void setSendToClipboard(boolean sendToClipboard) {
		this.sendToClipboard = sendToClipboard;
	}

	public boolean isExecuteCommand() {
		return executeCommand;
	}

	public void setExecuteCommand(boolean executeCommand) {
		this.executeCommand = executeCommand;
	}

	public String getCommand() {
		return command;
	}

	public void setCommand(String command) {
		this.command = command;
	}

}
