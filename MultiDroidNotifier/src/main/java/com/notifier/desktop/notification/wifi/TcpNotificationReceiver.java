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
package com.notifier.desktop.notification.wifi;

import java.net.*;
import java.util.concurrent.*;

import org.jboss.netty.bootstrap.*;
import org.jboss.netty.channel.*;
import org.jboss.netty.channel.group.*;
import org.jboss.netty.channel.socket.nio.*;
import org.slf4j.*;

import com.notifier.desktop.notification.*;

public class TcpNotificationReceiver extends AbstractNotificationReceiver {

	public static final String NAME = "TCP";

	private static final Logger logger = LoggerFactory.getLogger(TcpNotificationReceiver.class);

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
		bootstrap.setPipelineFactory(new NotificationPipelineFactory(allChannels, getApplication(), getNotificationManager(), getNotificationParser(), true, true));

		bootstrap.setOption("child.tcpNoDelay", true);
		bootstrap.setOption("child.keepAlive", true);

		try {
			allChannels.add(bootstrap.bind(new InetSocketAddress(PORT)));
		} catch (ChannelException e) {
			// Don't propagate bind exceptions because the user may start
			// AND from two user sessions in his OS
			if (e.getCause() instanceof BindException) {
				logger.warn("Failed to bind to TCP port, there can be only one program bound to a given port, dont worry, you will still be able to get UDP notifications", e);
			} else {
				throw e;
			}
		}
	}

	@Override
	public void doStop() {
		if (factory != null) {
			allChannels.close().awaitUninterruptibly(SHUTDOWN_TIMEOUT);
			factory.releaseExternalResources();
		}
	}
}
