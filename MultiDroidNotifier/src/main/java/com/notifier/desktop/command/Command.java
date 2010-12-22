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
package com.notifier.desktop.command;

import java.util.concurrent.atomic.*;

public abstract class Command {

	private static final AtomicLong ID_COUNTER = new AtomicLong();

	private long deviceId;
	private long commandId;

	public Command() {
		commandId = ID_COUNTER.getAndIncrement();
	}

	public static class Call extends Command {
		private String phoneNumber;

		@Override
		public Type getType() {
			return Type.CALL;
		}
	}

	public static class Sms extends Command {
		private String phoneNumber;
		private String text;

		@Override
		public Type getType() {
			return Type.SEND_SMS;
		}
	}

	public static enum Type {
	    CALL,
	    ANSWER,
	    HANG_UP,
	    SEND_SMS,
	    SEND_MMS,
	    QUERY,
	    DISCOVER,
	}

	public abstract Type getType();

	public long getDeviceId() {
		return deviceId;
	}

	public long getCommandId() {
		return commandId;
	}

}
