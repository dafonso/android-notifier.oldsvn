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
package com.notifier.desktop.transport.wifi.impl;

import java.io.*;
import java.net.*;
import java.util.concurrent.*;

import org.jboss.netty.bootstrap.*;
import org.jboss.netty.channel.*;
import org.jboss.netty.channel.group.*;
import org.jboss.netty.channel.socket.nio.*;
import org.jboss.netty.channel.socket.oio.*;
import org.slf4j.*;

import com.google.inject.*;
import com.notifier.desktop.*;
import com.notifier.desktop.discovery.*;
import com.notifier.desktop.notification.*;
import com.notifier.desktop.notification.parsing.*;
import com.notifier.desktop.notification.wifi.NotificationPipelineFactory;
import com.notifier.desktop.transport.wifi.*;

/**
 * Handles all network I/O to avoid creating too many classes.
 */
@Singleton
public class NioWifiTransport extends RestartableService implements WifiTransport {

	private static final Logger logger = LoggerFactory.getLogger(NioWifiTransport.class);

	private static final int PREFERRED_PORT = 10600;
	private static final int MAX_PORTS = 10;
	private static final int SHUTDOWN_TIMEOUT = 10000;

	private @Inject Application application;
	private @Inject NotificationManager notificationManager;
	private @Inject NotificationParser<byte[]> notificationParser;

	// TCP
	private ChannelGroup tcpChannels;
	private ChannelFactory tcpFactory;
	private ServerBootstrap tcpBootstrap;
	private int tcpPort;

	// UDP
	private ChannelFactory udpFactory;
	private ConnectionlessBootstrap udpBootstrap;

	public NioWifiTransport() {
		tcpChannels = new DefaultChannelGroup("TCP");
	}

	@Override
	public String getName() {
		return "wifi";
	}

	@Override
	public int getIpPort() {
		return tcpPort;
	}

	@Override
	public void broadcastDiscoveryInfo(final DiscoveryInfo discoveryInfo) {
		udpBootstrap.connect().addListener(new ChannelFutureListener() {
			@Override
			public void operationComplete(ChannelFuture future) throws Exception {
				if (future.isSuccess()) {
					future.getChannel().write(discoveryInfo.toProtobuf()).addListener(ChannelFutureListener.CLOSE);
				}
			}
		});
	}

	@Override
	protected void doStart() throws Exception {
		startTcp();
		startUdp();
	}

	@Override
	protected void doStop() throws Exception {
		stopTcp();
		stopUdp();
	}

	protected void startTcp() throws IOException {
		tcpFactory = new NioServerSocketChannelFactory(Executors.newCachedThreadPool(), Executors.newCachedThreadPool());
		tcpBootstrap = new ServerBootstrap(tcpFactory);
		tcpBootstrap.setPipelineFactory(new NotificationPipelineFactory(tcpChannels, application, notificationManager, notificationParser, true, true));

		tcpBootstrap.setOption("child.tcpNoDelay", true);
		tcpBootstrap.setOption("child.keepAlive", true);

		boolean bound = false;
		for (tcpPort = PREFERRED_PORT; tcpPort < PREFERRED_PORT + MAX_PORTS; tcpPort++) {
			try {
				Channel channel = tcpBootstrap.bind(new InetSocketAddress(tcpPort));
				tcpChannels.add(channel);
				bound = true;
				break;
			} catch (ChannelException e) {
				if (e.getCause() instanceof BindException) {
					logger.warn("Failed to bind to TCP port [{}]", tcpPort);
				} else {
					throw e;
				}
			}
		}

		if (!bound) {
			throw new IOException("Could not bind to any TCP port, notifications and commands will not work over wifi");
		}
	}

	protected void stopTcp() {
		if (tcpFactory != null) {
			tcpChannels.close().awaitUninterruptibly(SHUTDOWN_TIMEOUT);
			tcpFactory.releaseExternalResources();
		}
	}

	protected void startUdp() {
		udpFactory = new OioDatagramChannelFactory(Executors.newCachedThreadPool());
		udpBootstrap = new ConnectionlessBootstrap(udpFactory);
		udpBootstrap.setPipelineFactory(new DiscoveryPipelineFactory());
		InetSocketAddress address;
		try {
			address = new InetSocketAddress(InetAddress.getByAddress(new byte[] { -1, -1, -1, -1 }), PREFERRED_PORT);
		} catch (UnknownHostException e) {
			throw new RuntimeException(e);
		}
		udpBootstrap.setOption("remoteAddress", address);
	}

	protected void stopUdp() {
		if (udpFactory != null) {
			udpFactory.releaseExternalResources();
		}
	}
}
