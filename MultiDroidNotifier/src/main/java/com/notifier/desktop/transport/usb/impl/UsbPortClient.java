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
package com.notifier.desktop.transport.usb.impl;

import java.io.*;
import java.net.*;
import java.util.concurrent.*;

import org.jboss.netty.bootstrap.*;
import org.jboss.netty.buffer.*;
import org.jboss.netty.channel.*;
import org.jboss.netty.channel.socket.nio.*;
import org.slf4j.*;

import com.google.inject.*;
import com.notifier.desktop.*;
import com.notifier.desktop.discovery.*;
import com.notifier.desktop.notification.*;
import com.notifier.desktop.notification.parsing.*;
import com.notifier.desktop.notification.wifi.*;

import static java.util.concurrent.TimeUnit.*;

public class UsbPortClient {

	private static final Logger logger = LoggerFactory.getLogger(UsbPortClient.class);

	private static final int RECONNECT_INTERVAL = 3;
	private static final int SHUTDOWN_TIMEOUT = 10000;

	private @Inject Application application;
	private @Inject NotificationManager notificationManager;
	private @Inject NotificationParser<byte[]> notificationParser;

	private Adb.Device device;
	private int port;
	private Channel channel;
	private NioClientSocketChannelFactory factory;
	private ClientBootstrap bootstrap;

	private boolean stopRequested;

	public void start() {
		factory = new NioClientSocketChannelFactory(Executors.newCachedThreadPool(), Executors.newCachedThreadPool());
		bootstrap = new ClientBootstrap(factory);
		bootstrap.setPipelineFactory(new NotificationPipelineFactory(notificationParser, false, true, new UsbPortChannelHandler()));
		bootstrap.setOption("tcpNoDelay", true);
		bootstrap.setOption("keepAlive", true);
		tryToConnect(false);
	}

	public void stop() {
		stopRequested = true;
		if (channel != null) {
			channel.close().awaitUninterruptibly(SHUTDOWN_TIMEOUT);
		}
		factory.releaseExternalResources();
	}

	public void sendDiscoveryInfo(DiscoveryInfo discoveryInfo) {
		if (channel != null) {
			channel.write(discoveryInfo.toProtobuf());
		}
	}

	protected void tryToConnect(boolean sleep) {
		if (stopRequested) {
			return;
		}
		if (sleep) {
			logger.debug("Could not connect to device [{}] over usb, will try again in [{}] seconds", device, RECONNECT_INTERVAL);
			try {
				SECONDS.sleep(RECONNECT_INTERVAL);
			} catch (InterruptedException ie) {
				return;
			}
		}
		if (!stopRequested) {
			try {
				bootstrap.connect(new InetSocketAddress(InetAddress.getByName(null), port)).addListener(new ChannelFutureListener() {
					@Override
					public void operationComplete(ChannelFuture future) throws Exception {
						if (future.isSuccess()) {
							channel = future.getChannel();
							channel.write(ChannelBuffers.wrappedBuffer(new byte[] { 1 }));
						}
					}
				});
			} catch (UnknownHostException e) {
				e.printStackTrace();
			}
		}
	}

	class UsbPortChannelHandler extends SimpleChannelHandler {
		@Override
		public void channelDisconnected(ChannelHandlerContext ctx, ChannelStateEvent e) throws Exception {
			tryToConnect(true);
		}

		@Override
		public void messageReceived(ChannelHandlerContext ctx, MessageEvent e) throws Exception {
			Notification notification = (Notification) e.getMessage();
			if (notification != null) {
				notificationManager.notificationReceived(notification);
			}
		}

		@Override
		public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e) throws Exception {
			if (e.getCause() instanceof ConnectException) {
				tryToConnect(true);
			} else if (e.getCause() instanceof java.nio.channels.ClosedByInterruptException) {
				// We've been interrupted, nothing to do
			} else {
				logger.error("Error handling usb notification", e.getCause());
				e.getChannel().close();
				channel = null;
				if (e.getCause() instanceof IOException) {
					tryToConnect(true);
				} else {
					application.showError(Application.NAME + " USB Error", "An error occurred while receiving usb notification:\n" + e.getCause().getMessage());
				}
			}
		}
	}

	public void setDevice(Adb.Device device) {
		this.device = device;
	}

	public void setPort(int port) {
		this.port = port;
	}

}
