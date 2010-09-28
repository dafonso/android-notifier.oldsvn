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
package com.notifier.desktop.service;

import java.io.*;
import java.net.*;
import java.nio.charset.*;

import org.slf4j.*;

import com.google.common.base.*;
import com.google.common.io.*;
import com.google.inject.*;
import com.notifier.desktop.*;

public class ServiceServerImpl implements ServiceServer {

	public static final int PORT = 10700;
	public static final Charset CHARSET = Charsets.UTF_8;

	private static final long SHUTDOWN_TIMEOUT = 5 * 1000;

	private static final Logger logger = LoggerFactory.getLogger(ServiceServerImpl.class);

	private @Inject Application application;
	private ServerSocket serverSocket;
	private Thread serverThread;

	@Override
	public void start() throws Exception {
		logger.debug("Starting service server on port [{}]", PORT);
		serverSocket = new ServerSocket(PORT, 0, InetAddress.getByName(null));
		serverThread = new Thread(new ServerRunnable(), "service-server");
		serverThread.start();
	}

	@Override
	public boolean isRunning() {
		return serverThread != null;
	}

	@Override
	public void stop() {
		logger.debug("Stopping service server");
		try {
			serverSocket.close();
		} catch (IOException e) {
			logger.warn("Error closing service socket", e);
		}

		serverThread.interrupt();
		try {
			serverThread.join(SHUTDOWN_TIMEOUT);
		} catch (InterruptedException e) {
			// We are closing already
		}
		serverThread = null;
	}

	protected void commandReceived(final Command command) {
		new Thread(new Runnable() {
			@Override
			public void run() {
				application.shutdown();
			}
		}).start();
	}

	private class ServerRunnable implements Runnable {
		@Override
		public void run() {
			while (!Thread.currentThread().isInterrupted()) {
				try {
					final Socket socket = serverSocket.accept();
					try {
						String data = CharStreams.toString(new InputSupplier<BufferedReader>() {
							@Override
							public BufferedReader getInput() throws IOException {
								return new BufferedReader(new InputStreamReader(socket.getInputStream(), CHARSET));
							}
						});
						Command command = Command.valueOf(data);
						switch (command) {
							case STOP:
								commandReceived(command);
								return; // Exit this thread
							default:
								// Just ignore
								break;
						}
					} finally {
						socket.close();
					}
				} catch (SocketException e) {
					// stop() has been called
				} catch (IOException e) {
					logger.error("Error handling service request", e);
				}
			}
		}
	}
}
