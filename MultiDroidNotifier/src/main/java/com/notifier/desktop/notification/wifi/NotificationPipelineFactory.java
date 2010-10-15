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

import org.jboss.netty.buffer.*;
import org.jboss.netty.channel.*;
import org.jboss.netty.channel.group.*;
import org.jboss.netty.handler.codec.frame.*;
import org.jboss.netty.handler.timeout.*;
import org.jboss.netty.util.*;

import com.notifier.desktop.*;

public class NotificationPipelineFactory implements ChannelPipelineFactory {

	public static final int MAX_MESSAGE_LENGTH = 100 * 1024;
	public static final ChannelBuffer MESSAGE_DELIMITER = ChannelBuffers.wrappedBuffer(new byte[] { 0 });
	public static final int READ_TIMEOUT = 30;

	private final ChannelGroup channelGroup;
	private final Application application;
	private final NotificationManager notificationManager;
	private final NotificationParser<byte[]> notificationParser;
	private final boolean hasTimeout;
	private final boolean useDelimiter;
	private final ChannelHandler handler;

	public NotificationPipelineFactory(ChannelGroup channelGroup, Application application, NotificationManager notificationManager, NotificationParser<byte[]> notificationParser, boolean hasTimeout, boolean useDelimiter) {
		this.channelGroup = channelGroup;
		this.application = application;
		this.notificationManager = notificationManager;
		this.notificationParser = notificationParser;
		this.hasTimeout = hasTimeout;
		this.useDelimiter = useDelimiter;
		this.handler = null;
	}

	public NotificationPipelineFactory(NotificationParser<byte[]> notificationParser, boolean hasTimeout, boolean useDelimiter, ChannelHandler handler) {
		this.channelGroup = null;
		this.application = null;
		this.notificationManager = null;
		this.notificationParser = notificationParser;
		this.hasTimeout = hasTimeout;
		this.useDelimiter = useDelimiter;
		this.handler = handler;
	}

	@Override
	public ChannelPipeline getPipeline() throws Exception {
		ChannelPipeline pipeline = Channels.pipeline();
		if (useDelimiter) {
			pipeline.addLast("delimiter", new DelimiterBasedFrameDecoder(MAX_MESSAGE_LENGTH, false, MESSAGE_DELIMITER));
		}

		if (hasTimeout) {
			Timer timer = new HashedWheelTimer();
			pipeline.addLast("read-timeout", new ReadTimeoutHandler(timer, READ_TIMEOUT));
		}
		pipeline.addLast("decoder", new NotificationDecoder(notificationParser));
		if (handler == null) {
			pipeline.addLast("handler", new NotificationChannelHandler(channelGroup, application, notificationManager));
		} else {
			pipeline.addLast("handler", handler);
		}

		return pipeline;
	}

}
