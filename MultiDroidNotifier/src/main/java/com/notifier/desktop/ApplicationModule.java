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

import java.util.concurrent.*;

import org.slf4j.*;

import com.google.common.util.concurrent.*;
import com.google.inject.*;
import com.notifier.desktop.annotation.*;
import com.notifier.desktop.device.*;
import com.notifier.desktop.device.impl.*;
import com.notifier.desktop.network.*;
import com.notifier.desktop.network.impl.*;
import com.notifier.desktop.notification.*;
import com.notifier.desktop.notification.broadcast.*;
import com.notifier.desktop.notification.broadcast.growl.*;
import com.notifier.desktop.notification.broadcast.libnotify.*;
import com.notifier.desktop.notification.broadcast.msn.*;
import com.notifier.desktop.notification.broadcast.tray.*;
import com.notifier.desktop.notification.impl.*;
import com.notifier.desktop.notification.parsing.*;
import com.notifier.desktop.notification.parsing.impl.*;
import com.notifier.desktop.os.*;
import com.notifier.desktop.os.impl.*;
import com.notifier.desktop.service.*;
import com.notifier.desktop.service.impl.*;
import com.notifier.desktop.transport.bluetooth.*;
import com.notifier.desktop.transport.bluetooth.impl.*;
import com.notifier.desktop.transport.usb.*;
import com.notifier.desktop.transport.usb.impl.*;
import com.notifier.desktop.transport.wifi.*;
import com.notifier.desktop.transport.wifi.impl.*;
import com.notifier.desktop.tray.*;
import com.notifier.desktop.tray.impl.*;
import com.notifier.desktop.update.*;
import com.notifier.desktop.update.impl.*;
import com.notifier.desktop.upnp.*;
import com.notifier.desktop.upnp.impl.*;
import com.notifier.desktop.view.*;
import com.notifier.desktop.view.impl.*;

public class ApplicationModule extends AbstractModule {

	private static final Logger logger = LoggerFactory.getLogger(ApplicationModule.class);

	@Override
	protected void configure() {
		bind(Application.class);

		bind(SwtManager.class).to(SwtManagerImpl.class);
		bind(TrayManager.class).to(SwtTrayManager.class);
		bind(PreferencesDialog.class);

		bind(NotificationManager.class).to(NotificationManagerImpl.class);
		bind(new TypeLiteral<NotificationParser<byte[]>>() {}).to(MultiNotificationParser.class);
		bind(DeviceManager.class).to(DeviceManagerImpl.class);

		bind(NotificationBroadcaster.class).annotatedWith(Tray.class).to(TrayNotificationBroadcaster.class);
		bind(NotificationBroadcaster.class).annotatedWith(Growl.class).to(GrowlNotificationBroadcaster.class);
		bind(NotificationBroadcaster.class).annotatedWith(Libnotify.class).to(LibnotifyNotificationBroadcaster.class);
		bind(InstantMessagingNotificationBroadcaster.class).annotatedWith(Msn.class).to(MsnNotificationBroadcaster.class);

		bind(WifiTransport.class).to(NioWifiTransport.class);
		bind(BluetoothTransport.class).to(BluetoothTransportImpl.class);
		bind(UsbTransport.class).to(UsbTransportImpl.class);
		bind(UsbPortClient.class);

		bind(NetworkManager.class).to(NetworkManagerImpl.class);
		bind(UpnpManager.class).to(UpnpManagerImpl.class);
		bind(UpdateManager.class).to(UpdateManagerImpl.class);
		bind(ServiceServer.class).to(ServiceServerImpl.class);
		bind(OperatingSystemProcessManager.class).to(OperatingSystemProcessManagerImpl.class);

		ThreadFactoryBuilder threadFactoryBuilder = new ThreadFactoryBuilder();
		threadFactoryBuilder.setNameFormat("task-%s");
		threadFactoryBuilder.setUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
			@Override
			public void uncaughtException(Thread t, Throwable e) {
				logger.error("Uncaught exception", e);
			}
		});
		ScheduledExecutorService executorService = Executors.newScheduledThreadPool(4, threadFactoryBuilder.build());
		bind(ExecutorService.class).toInstance(executorService);
		bind(ScheduledExecutorService.class).toInstance(executorService);
	}

	@Provides
	ApplicationPreferences providePreferences() {
		ApplicationPreferences preferences = new ApplicationPreferences();
		preferences.read();
		return preferences;
	}

}
