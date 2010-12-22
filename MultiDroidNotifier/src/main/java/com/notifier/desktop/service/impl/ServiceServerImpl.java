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
package com.notifier.desktop.service.impl;

import java.io.*;
import java.net.*;
import java.nio.charset.*;
import java.util.concurrent.*;

import org.slf4j.*;

import com.google.common.base.*;
import com.google.common.io.*;
import com.google.inject.*;
import com.notifier.desktop.*;
import com.notifier.desktop.service.*;

@Singleton
public class ServiceServerImpl extends RestartableService implements ServiceServer {

	public static final int PORT = 10700;
	public static final Charset CHARSET = Charsets.UTF_8;

	private static final Logger logger = LoggerFactory.getLogger(ServiceServerImpl.class);

	private @Inject Application application;
	private @Inject ExecutorService executorService;
	private ServerSocket serverSocket;
	private boolean stopped;

	@Override
	public String getName() {
		return "service server";
	}

	@Override
	public void doStart() {
		logger.debug("Starting service server on port [{}]", PORT);
		try {
			serverSocket = new ServerSocket(PORT, 0, InetAddress.getByName(null));
			stopped = false;
			executorService.execute(new ServerRunnable());
		} catch (Exception e) {
			throw new RuntimeException("Error starting service server, you will not be able to stop it via command line", e);
		}
	}

	@Override
	public void doStop() {
		try {
			stopped = true;
			if (serverSocket != null) {
				serverSocket.close();
			}
		} catch (IOException e) {
			logger.warn("Error closing service socket", e);
		}
	}

	protected void commandReceived(final Command command) {
		executorService.execute(new Runnable() {
			@Override
			public void run() {
				application.shutdown();
			}
		});
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
					if (stopped) {
						break;
					}
					logger.error("Error on server socket", e);
				} catch (IOException e) {
					logger.error("Error handling service request", e);
				}
			}
		}
	}
}
