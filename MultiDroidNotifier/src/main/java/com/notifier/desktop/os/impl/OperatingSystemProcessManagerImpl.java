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
package com.notifier.desktop.os.impl;

import java.io.*;
import java.util.concurrent.*;

import org.slf4j.*;

import com.google.common.base.*;
import com.google.common.io.*;
import com.google.inject.*;
import com.notifier.desktop.*;
import com.notifier.desktop.notification.*;
import com.notifier.desktop.os.*;

@Singleton
public class OperatingSystemProcessManagerImpl implements OperatingSystemProcessManager {

	public static final String PLACEHOLDER_START = "{";
	public static final String PLACEHOLDER_END = "}";
	public static final String DEVICE_ID_PLACEHOLDER = PLACEHOLDER_START + "deviceId" + PLACEHOLDER_END;
	public static final String NOTIFICATION_ID_PLACEHOLDER = PLACEHOLDER_START + "id" + PLACEHOLDER_END;
	public static final String NOTIFICATION_TYPE_PLACEHOLDER = PLACEHOLDER_START + "type" + PLACEHOLDER_END;
	public static final String NOTIFICATION_DATA_PLACEHOLDER = PLACEHOLDER_START + "data" + PLACEHOLDER_END;
	public static final String NOTIFICATION_DESCRIPTION_PLACEHOLDER = PLACEHOLDER_START + "description" + PLACEHOLDER_END;
	public static final String NOTIFICATION_TITLE_PLACEHOLDER = PLACEHOLDER_START + "title" + PLACEHOLDER_END;

	private static final Splitter COMMAND_SPLITTER = Splitter.on(';').trimResults().omitEmptyStrings();

	private static final Logger logger = LoggerFactory.getLogger(OperatingSystemProcessManagerImpl.class);

	private final Application application;
	private final ExecutorService executorService;

	@Inject
	public OperatingSystemProcessManagerImpl(Application application, ExecutorService executorService) {
		this.application = application;
		this.executorService = executorService;
	}

	@Override
	public void executeCommand(Notification notification, String deviceName, String command, boolean privateMode) {
		ProcessBuilder processBuilder = new ProcessBuilder();
		processBuilder.redirectErrorStream(true);
		processBuilder.directory(new File(System.getProperty("user.dir")));

		boolean notifiedError = false;
		Iterable<String> commands = COMMAND_SPLITTER.split(command);
		for (String c : commands) {
			c = c.replace(DEVICE_ID_PLACEHOLDER, notification.getDeviceId());
			c = c.replace(NOTIFICATION_ID_PLACEHOLDER, Long.toString(notification.getNotificationId()));
			c = c.replace(NOTIFICATION_TYPE_PLACEHOLDER, notification.getType().name());
			c = c.replace(NOTIFICATION_DATA_PLACEHOLDER, "\"" + notification.getData() + "\"");
			c = c.replace(NOTIFICATION_DESCRIPTION_PLACEHOLDER, "\"" + notification.getDescription(privateMode) + "\"");
			c = c.replace(NOTIFICATION_TITLE_PLACEHOLDER, "\"" + notification.getTitle(deviceName) + "\"");

			final String commandToExecute = c;
			if (OperatingSystems.CURRENT_FAMILY == OperatingSystems.Family.WINDOWS) {
				processBuilder.command("cmd.exe", "/X", "/C", "\"" + commandToExecute.replaceAll("\"", "\\\"") + "\"");
			} else {
				processBuilder.command("sh", "-c", commandToExecute);
			}
			try {
				final Process process = processBuilder.start();
				executorService.execute(new Runnable() {
					@Override
					public void run() {
						try {
							String output = CharStreams.toString(new InputSupplier<BufferedReader>() {
								@Override
								public BufferedReader getInput() throws IOException {
									return new BufferedReader(new InputStreamReader(process.getInputStream()));
								}
							});
							logger.info("Output of command execution: {}\n{}", commandToExecute, output);
						} catch (Exception e) {
							logger.error("Could not get command output: " + commandToExecute, e);
						}

						try {
							int exitCode = process.waitFor();
							logger.info("Command [{}] exited with code [{}]", commandToExecute, exitCode);
						} catch (InterruptedException e) {
							// Will exit
						}
					}
				});
			} catch (Exception e) {
				logger.error("Error executing notification command: " + c, e);
				if (!notifiedError) {
					notifiedError = true;
					application.showError(Application.NAME + " Error Executing Command", "An error occurred while executing command.\n" + c);
				}
			}
		}
	}

}
