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
package com.notifier.desktop.notification.usb;

import java.io.*;
import java.util.concurrent.*;

import com.google.inject.*;
import com.notifier.desktop.*;

public class UsbNotificationReceiver extends RestartableService implements NotificationReceiver {

	private ExecutorService executorService;
	private UsbPortForwarder portForwarder;

	@Inject
	public UsbNotificationReceiver(ExecutorService executorService, UsbPortForwarder portForwarder) {
		this.executorService = executorService;
		this.portForwarder = portForwarder;
	}

	@Override
	public String getName() {
		return "USB";
	}

	@Override
	protected void doStart() throws Exception {
		if (portForwarder.getSdkHome() == null) {
			throw new IllegalStateException("Android SDK home has not been set");
		}
		portForwarder.prepare();
		executorService.execute(portForwarder);
	}

	@Override
	protected void doStop() throws Exception {
		portForwarder.stop();
	}

	public void setSdkHome(String sdkHome) {
		portForwarder.setSdkHome(new File(sdkHome));
	}
}
