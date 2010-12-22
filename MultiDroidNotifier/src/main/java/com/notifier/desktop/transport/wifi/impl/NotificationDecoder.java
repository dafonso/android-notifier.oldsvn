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

import org.jboss.netty.buffer.*;
import org.jboss.netty.channel.*;
import org.jboss.netty.handler.codec.oneone.*;

import com.notifier.desktop.notification.parsing.*;

public class NotificationDecoder extends OneToOneDecoder {

	public static final char FIELD_SEPARATOR = '/';
	public static final String SUPPORTED_VERSION = "v2";

	private final NotificationParser<byte[]> notificationParser;

	public NotificationDecoder(NotificationParser<byte[]> notificationParser) {
		this.notificationParser = notificationParser;
	}

	@Override
	protected Object decode(ChannelHandlerContext ctx, Channel channel, Object msg) throws Exception {
		ChannelBuffer buffer = (ChannelBuffer) msg;
		byte[] data = new byte[buffer.readableBytes()];
		buffer.readBytes(data);
		return notificationParser.parse(data);
	}

}
