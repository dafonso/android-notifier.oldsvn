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
import org.jboss.netty.channel.socket.nio.*;

import com.google.code.notifier.desktop.notification.*;

public class TcpNotificationReceiver extends AbstractNotificationReceiver {

	public static final String NAME = "TCP";

	private ChannelGroup allChannels;
	private ChannelFactory factory;
	private ServerBootstrap bootstrap;

	public TcpNotificationReceiver() {
		allChannels = new DefaultChannelGroup(NAME);
	}

	@Override
	public String getName() {
		return NAME;
	}

	@Override
	public void doStart() {
		factory = new NioServerSocketChannelFactory(Executors.newCachedThreadPool(), Executors.newCachedThreadPool());
		bootstrap = new ServerBootstrap(factory);
		bootstrap.setPipelineFactory(new NotificationPipelineFactory(allChannels, getApplication(), getNotificationManager(), getNotificationParser(), true));

		bootstrap.setOption("child.tcpNoDelay", true);
		bootstrap.setOption("child.keepAlive", true);

		allChannels.add(bootstrap.bind(new InetSocketAddress(PORT)));
	}

	@Override
	public void doStop() {
		if (factory != null) {
			allChannels.close().awaitUninterruptibly(SHUTDOWN_TIMEOUT);
			factory.releaseExternalResources();
		}
	}
}
