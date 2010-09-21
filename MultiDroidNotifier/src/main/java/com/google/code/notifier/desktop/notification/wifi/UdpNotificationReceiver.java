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
package com.google.code.notifier.desktop.notification.wifi;

import java.net.*;
import java.util.concurrent.*;

import org.jboss.netty.bootstrap.*;
import org.jboss.netty.channel.*;
import org.jboss.netty.channel.group.*;
import org.jboss.netty.channel.socket.oio.*;

import com.google.code.notifier.desktop.notification.*;

public class UdpNotificationReceiver extends AbstractNotificationReceiver {

	public static final String NAME = "UDP";

	private ChannelGroup allChannels;
	private ConnectionlessBootstrap bootstrap;
	private ChannelFactory factory;

	public UdpNotificationReceiver() {
		allChannels = new DefaultChannelGroup(NAME);
	}

	@Override
	public String getName() {
		return NAME;
	}

	@Override
	public void doStart() {
		factory = new OioDatagramChannelFactory(Executors.newCachedThreadPool());
		bootstrap = new ConnectionlessBootstrap(factory);
		bootstrap.setPipelineFactory(new NotificationPipelineFactory(allChannels, getApplication(), getNotificationManager(), getNotificationParser(), false));
		bootstrap.bind(new InetSocketAddress(PORT));
	}

	@Override
	public void doStop() {
		if (factory != null) {
			allChannels.close().awaitUninterruptibly(SHUTDOWN_TIMEOUT);
			factory.releaseExternalResources();
		}
	}

}
