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

import org.jboss.netty.channel.*;
import org.jboss.netty.channel.group.*;
import org.jboss.netty.channel.socket.*;
import org.slf4j.*;

import com.notifier.desktop.*;

public class NotificationChannelHandler extends SimpleChannelHandler {

	private static final Logger logger = LoggerFactory.getLogger(NotificationChannelHandler.class);

	private final ChannelGroup channelGroup;
	private final Application application;
	private final NotificationManager notificationManager;

	public NotificationChannelHandler(ChannelGroup channelGroup, Application application, NotificationManager notificationManager) {
		this.channelGroup = channelGroup;
		this.application = application;
		this.notificationManager = notificationManager;
	}

	@Override
	public void channelOpen(ChannelHandlerContext ctx, ChannelStateEvent e) throws Exception {
		channelGroup.add(e.getChannel());
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
		logger.error("Error handling network notification", e.getCause());
		if (!(e.getChannel() instanceof DatagramChannel)) { // Cannot close datagram channels
			e.getChannel().close();
		}
		application.showError(Application.NAME + " Wifi Error", "An error occurred while receiving wifi notification:\n" + e.getCause().getMessage());
	}
}
