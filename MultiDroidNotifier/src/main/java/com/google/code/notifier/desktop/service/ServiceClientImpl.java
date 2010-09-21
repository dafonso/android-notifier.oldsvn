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
package com.google.code.notifier.desktop.service;

import java.io.*;
import java.net.*;

import org.slf4j.*;

import com.google.code.notifier.desktop.*;
import com.google.common.io.*;

public class ServiceClientImpl implements ServiceClient {

	private static final int SOCKET_TIMEOUT = 3 * 1000;

	private static final Logger logger = LoggerFactory.getLogger(ServiceClientImpl.class);

	@Override
	public boolean isRunning() {
		return sendCommand(ServiceServer.Command.PING);
	}

	@Override
	public boolean stop() {
		return sendCommand(ServiceServer.Command.STOP);
	}

	protected boolean sendCommand(ServiceServer.Command command) {
		Socket socket = null;
		try {
			socket = getSocket();
			final Socket socketToUse = socket;
			CharStreams.write(command.name(), new OutputSupplier<BufferedWriter>() {
				@Override
				public BufferedWriter getOutput() throws IOException {
					return new BufferedWriter(new OutputStreamWriter(socketToUse.getOutputStream(), ServiceServerImpl.CHARSET));
				}
			});
			return true;
		} catch (ConnectException e) {
			return false;
		} catch (IOException e) {
			logger.warn("Error sending command to service server", e);
			return false;
		} finally {
			if (socket != null) {
				try {
					socket.close();
				} catch (IOException e) {
					logger.warn("Error closing socket to service server", e);
				}
			}
		}
	}

	protected Socket getSocket() throws IOException {
		Socket socket = new Socket(InetAddress.getByName(null), ServiceServerImpl.PORT);
		socket.setSoTimeout(SOCKET_TIMEOUT);
		return socket;
	}
}
