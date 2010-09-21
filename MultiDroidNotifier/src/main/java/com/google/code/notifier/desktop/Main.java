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
package com.google.code.notifier.desktop;

import static java.util.concurrent.TimeUnit.*;

import java.io.*;
import java.nio.channels.*;
import java.util.*;

import org.apache.commons.cli.*;
import org.slf4j.*;

import com.google.code.notifier.desktop.annotation.*;
import com.google.code.notifier.desktop.app.*;
import com.google.code.notifier.desktop.notification.*;
import com.google.code.notifier.desktop.notification.bluetooth.*;
import com.google.code.notifier.desktop.notification.broadcast.*;
import com.google.code.notifier.desktop.notification.wifi.*;
import com.google.code.notifier.desktop.parsing.*;
import com.google.code.notifier.desktop.service.*;
import com.google.code.notifier.desktop.tray.*;
import com.google.code.notifier.desktop.view.*;
import com.google.common.io.*;
import com.google.inject.*;

public class Main {

	private static final Logger logger = LoggerFactory.getLogger(Main.class);

	private static final String NO_TRAY_SHORT = "t";
	private static final String NO_TRAY_LONG = "no-tray";

	private static final String SHOW_PREFERENCES_SHORT = "p";
	private static final String SHOW_PREFERENCES_LONG = "show-preferences";

	private static final String IS_RUNNING_SHORT = "i";
	private static final String IS_RUNNING_LONG = "is-running";

	private static final String STOP_SHORT = "s";
	private static final String STOP_LONG = "stop";

	private static final String HELP_SHORT = "h";
	private static final String HELP_LONG = "help";

	public static void main(String[] args) {
		Options options = createCommandLineOptions();
		try {
			CommandLineParser commandLineParser = new GnuParser();
			CommandLine line = commandLineParser.parse(options, args);

			if (line.getOptions().length > 1) {
				showMessage("Only one parameter may be specified");
			}
			if (line.getArgs().length > 0) {
				showMessage("Non-recognized parameters: " + Arrays.toString(line.getArgs()));
			}
			if (line.hasOption(HELP_SHORT)) {
				printHelp(options);
				return;
			}
			if (line.hasOption(IS_RUNNING_SHORT)) {
				ServiceClient client = new ServiceClientImpl();
				if (client.isRunning()) {
					showMessage(Application.FULL_NAME + " is running");
				} else {
					showMessage(Application.FULL_NAME + " is not running");
				}
				return;
			}
			if (line.hasOption(STOP_SHORT)) {
				ServiceClient client = new ServiceClientImpl();
				if (client.stop()) {
					showMessage("Sent stop signal to " + Application.FULL_NAME + " successfully");
				} else {
					showMessage(Application.FULL_NAME + " is not running or an error occurred, see log for details");
				}
				return;
			}

			boolean trayIcon = !line.hasOption(NO_TRAY_SHORT);
			boolean showPreferences = line.hasOption(SHOW_PREFERENCES_SHORT);

			if (!getExclusiveExecutionLock()) {
				showMessage("There can be only one instance of " + Application.FULL_NAME + " running at a time");
				return;
			}
			Injector injector = Guice.createInjector(Stage.PRODUCTION, new Module());
			Application application = injector.getInstance(Application.class);
			application.start(trayIcon, showPreferences);
		} catch (Throwable t) {
			System.out.println(t.getMessage());
			logger.error("Error starting", t);
		}
	}

	public static class Module extends AbstractModule {
		@Override
		protected void configure() {
			bind(Application.class).to(ApplicationImpl.class).in(Singleton.class);
			bind(NotificationManager.class).to(NotificationManagerImpl.class).in(Singleton.class);
			bind(new TypeLiteral<NotificationParser<String>>() {}).to(StringNotificationParser.class).in(Singleton.class);

			bind(NotificationBroadcaster.class).annotatedWith(Tray.class).to(TrayNotificationBroadcaster.class).in(Singleton.class);
			bind(NotificationBroadcaster.class).annotatedWith(Growl.class).to(GrowlNotificationBroadcaster.class).in(Singleton.class);
			bind(NotificationBroadcaster.class).annotatedWith(Libnotify.class).to(LibnotifyNotificationBroadcaster.class).in(Singleton.class);

			bind(NotificationReceiver.class).annotatedWith(Tcp.class).to(TcpNotificationReceiver.class).in(Singleton.class);
			bind(NotificationReceiver.class).annotatedWith(Udp.class).to(UdpNotificationReceiver.class).in(Singleton.class);
			bind(NotificationReceiver.class).annotatedWith(Bluetooth.class).to(BluetoothNotificationReceiver.class).in(Singleton.class);

			bind(TrayManager.class).to(SwtTrayManager.class).in(Singleton.class);
			bind(SwtManager.class).to(SwtManagerImpl.class).in(Singleton.class);

			bind(OperatingSystemProcessManager.class).to(OperatingSystemProcessManagerImpl.class).in(Singleton.class);
			bind(UpdateManager.class).to(UpdateManagerImpl.class).in(Singleton.class);
			bind(ServiceServer.class).to(ServiceServerImpl.class).in(Singleton.class);
		}

		@Provides
		ApplicationPreferences providePreferences() {
			ApplicationPreferences preferences = new ApplicationPreferences();
			preferences.read();
			return preferences;
		}
	}

	private static Options createCommandLineOptions() {
		Options options = new Options();
		options.addOption(NO_TRAY_SHORT, NO_TRAY_LONG, false, "don't show tray icon (System default notification display will not be shown)");
		options.addOption(SHOW_PREFERENCES_SHORT, SHOW_PREFERENCES_LONG, false, "show preferences window immediately");
		options.addOption(IS_RUNNING_SHORT, IS_RUNNING_LONG, false, "show running status");
		options.addOption(STOP_SHORT, STOP_LONG, false, "stop " + Application.FULL_NAME + " if it's running");
		options.addOption(HELP_SHORT, HELP_LONG, false, "show help information");
		return options;
	}

	private static void printHelp(Options options) {
		HelpFormatter formatter = new HelpFormatter();
		String cmdSyntax = "android-notifier-desktop";
		if (OperatingSystems.CURRENT_FAMILY == OperatingSystems.Family.WINDOWS) {
			StringWriter s = new StringWriter();
			formatter.printHelp(new PrintWriter(s), 150, cmdSyntax, null, options, formatter.getLeftPadding(), formatter.getDescPadding(), null, true);
			showMessage(s.toString());
		} else {
			formatter.printHelp(cmdSyntax, options, true);
		}
	}

	private static boolean getExclusiveExecutionLock() throws IOException {
		File lockFile = new File(OperatingSystems.getWorkDirectory(), Application.ARTIFACT_ID + ".lock");
		lockFile.createNewFile();
		final RandomAccessFile randomAccessFile = new RandomAccessFile(lockFile, "rw");
		final FileChannel fileChannel = randomAccessFile.getChannel();
		final FileLock fileLock = fileChannel.tryLock();
		if (fileLock == null) {
			Closeables.closeQuietly(fileChannel);
			Closeables.closeQuietly(randomAccessFile);
			return false;
		}

		Runtime.getRuntime().addShutdownHook(new Thread() {
			@Override
			public void run() {
				try {
					fileLock.release();
				} catch (IOException e) {
					System.err.println("Error releasing file lock");
					e.printStackTrace(System.err);
				} finally {
					Closeables.closeQuietly(fileChannel);
					Closeables.closeQuietly(randomAccessFile);
				}
			}
		});
		return true;
	}

	private static void showMessage(final String msg) {
		if (OperatingSystems.CURRENT_FAMILY == OperatingSystems.Family.WINDOWS) {
			// Launch4j does not send output to stdout
			final SwtManager swtManager = new SwtManagerImpl();
			try {
				swtManager.start();
				new Thread(new Runnable() {
					@Override
					public void run() {
						Dialogs.showInfo(swtManager, Application.FULL_NAME, msg, true);
						try {
							SECONDS.sleep(5);
						} catch (InterruptedException e) {
							// Do nothing
						}
						swtManager.stop();
					}
				}).start();
				swtManager.runEventLoop();
			} catch (Throwable t) {
				// No need to handle this
			} finally {
				logger.info(msg);
			}
		} else {
			System.out.println(msg);
		}
	}
}
