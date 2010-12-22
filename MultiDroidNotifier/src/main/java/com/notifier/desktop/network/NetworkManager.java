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
package com.notifier.desktop.network;

import java.net.*;
import java.util.concurrent.*;

import com.google.common.base.*;
import com.notifier.desktop.*;

public interface NetworkManager extends Service, Named {
	String getLocalHostName();
	InetAddress getLocalHostAddress();
	byte[] getLocalMacAddress();

	/**
	 * Waits for the first update to local address only.
	 */
	boolean waitForLocalNetworkInfo(long timeout, TimeUnit unit) throws InterruptedException;
}
