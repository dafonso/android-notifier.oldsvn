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
package com.notifier.desktop.app;

import java.io.*;
import java.util.regex.*;

import com.google.common.base.*;
import com.google.common.io.*;
import com.notifier.desktop.*;
import com.notifier.desktop.app.windows.*;

public class OperatingSystems {

	public static Family CURRENT_FAMILY = getFamily();

	private static final String WINDOWS_EXE = System.getProperty("launch4j.exefile", Application.ARTIFACT_ID + ".exe");
	private static final String WINDOWS_SHORTCUT = Application.ARTIFACT_ID + ".lnk";

	private static final String LINUX_APPLICATIONS_DIR = "/usr/share/applications";
	private static final String LINUX_DESKTOP_FILE = Application.ARTIFACT_ID + ".desktop";

	private static final String WINDOWS_LINE_DELIMITER = "\r\n";
	private static final String UNIX_LINE_DELIMITER = "\n";

	private static final Pattern UNIX_LINE_DELIMITER_PATTERN = Pattern.compile(UNIX_LINE_DELIMITER);

	public static enum Family {
		WINDOWS, LINUX, MAC
	}

	private OperatingSystems() {
	}

	public static void addToStartup() throws IOException {
		switch (CURRENT_FAMILY) {
			case WINDOWS:
				addToWindowsStartup();
				break;
			case MAC:
				// Not supported
				break;
			case LINUX:
				addToLinuxStartup();
				break;
			default:
				throw new IllegalStateException("Unknown family: " + CURRENT_FAMILY);
		}
	}

	public static void removeFromStartup() throws IOException {
		switch (CURRENT_FAMILY) {
			case WINDOWS:
				removeFromWindowsStartup();
				break;
			case MAC:
				// Not supported
				break;
			case LINUX:
				removeFromLinuxStartup();
				break;
			default:
				throw new IllegalStateException("Unknown family: " + CURRENT_FAMILY);
		}
	}

	public static String convertLineDelimiters(String s) {
		if (Strings.isNullOrEmpty(s)) {
			return s;
		}

		if (CURRENT_FAMILY == Family.WINDOWS) {
			return UNIX_LINE_DELIMITER_PATTERN.matcher(s).replaceAll(WINDOWS_LINE_DELIMITER);
		}

		return s;
	}

	public static String[] getExecutableFileExtensions() {
		switch (CURRENT_FAMILY) {
			case WINDOWS:
				return new String[] { "*.exe", "*" };
			case MAC:
				return null;
			case LINUX:
				return new String[] { "*.sh;*.py;*.pl", "*" };
			default:
				throw new IllegalStateException("Unknown OS: " + CURRENT_FAMILY);
		}
	}

	public static String[] getExecutableFileExtensionsNames() {
		switch (CURRENT_FAMILY) {
			case WINDOWS:
				return new String[] { "Executable Files", "All Files (*)" };
			case MAC:
				return null;
			case LINUX:
				return new String[] { "Script Files", "All Files (*)" };
			default:
				throw new IllegalStateException("Unknown OS: " + CURRENT_FAMILY);
		}
	}

	public static String getApplicationsRoot() {
		switch (CURRENT_FAMILY) {
			case WINDOWS:
				return "c:\\";
			case MAC:
				return "/Applications";
			default:
				return "/";
		}
	}

	public static String getExecutable(String path) {
		String executable = path.contains(" ") ? "\"" + path + "\"" : path;
		if (CURRENT_FAMILY == Family.MAC && path.endsWith(".app")) {
			executable = "open " + executable;
		}
		return executable;
	}

	public static String getWorkDirectory() {
		if (CURRENT_FAMILY == Family.LINUX) {
			return System.getProperty("user.home", "~") + "/.local/share/" + Application.ARTIFACT_ID;
		}
		return System.getProperty("user.home", "") + "/.android/" + Application.ARTIFACT_ID;
	}

// Private stuff

	private static Family getFamily() {
		String osName = System.getProperty("os.name");
		if (osName == null) {
			return Family.WINDOWS;
		}
		osName = osName.toLowerCase();
		if (osName.contains("windows")) {
			return Family.WINDOWS;
		} else if (osName.contains("linux")) {
			return Family.LINUX;
		}
		return Family.MAC;
	}

	private static void addToWindowsStartup() throws IOException {
		File startupDir = getWindowsStartupDir();
		Shortcut shortcut = new Shortcut(new File(WINDOWS_EXE));
		File shortcutFile = new File(startupDir, WINDOWS_SHORTCUT);
		FileOutputStream fos = new FileOutputStream(shortcutFile);
		try {
			fos.write(shortcut.getBytes());
		} finally {
			Closeables.closeQuietly(fos);
		}
	}

	private static void removeFromWindowsStartup() throws IOException {
		File startupDir = getWindowsStartupDir();
		File shortcutFile = new File(startupDir, WINDOWS_SHORTCUT);
		if (shortcutFile.exists()) {
			Files.deleteRecursively(shortcutFile);
		}
	}

	private static File getWindowsStartupDir() throws IOException {
		File startupDir = getWindowsStartupDirFromRegistry();
		if (startupDir == null || !startupDir.isDirectory()) {
			startupDir = getWindowsStartupDirFromHardcodedDocumentsAndSettings();
			if (!startupDir.isDirectory()) {
				startupDir = getWindowsStartupDirFromHardcodedNewerPath();
				if (!startupDir.isDirectory()) {
					throw new IOException("Could not find windows startup directory.");
				}
			}
		}
		return startupDir;
	}

	private static File getWindowsStartupDirFromRegistry() {
		try {
			ProcessBuilder builder = new ProcessBuilder("reg", "query", "\"HKCU\\Software\\Microsoft\\Windows\\CurrentVersion\\Explorer\\Shell Folders\"", "/v", "Startup");
			final Process process = builder.start();
			String result = CharStreams.toString(new InputSupplier<InputStreamReader>() {
				@Override
				public InputStreamReader getInput() throws IOException {
					return new InputStreamReader(process.getInputStream());
				}
			});
			String startupDirName = result.substring(result.indexOf("REG_SZ") + 6).trim();
			return new File(startupDirName);
		} catch (Exception e) {
			return null;
		}
	}

	private static File getWindowsStartupDirFromHardcodedDocumentsAndSettings() {
		String username = System.getProperty("user.name");
		File startupDir = new File("c:\\Documents and Settings\\" + username + "\\Start Menu\\Programs\\Startup");
		if (!startupDir.exists()) {
			startupDir = new File("c:\\Documents and Settings\\All Users\\Start Menu\\Programs\\Startup");
		}
		return startupDir;
	}

	private static File getWindowsStartupDirFromHardcodedNewerPath() {
		String username = System.getProperty("user.name");
		return new File("C:\\Users\\" + username + "\\AppData\\Roaming\\Microsoft\\Windows\\Start Menu\\Programs\\Startup");
	}

	private static void addToLinuxStartup() throws IOException {
		File startupDir = getLinuxStartupDir();
		File desktopFile = new File(LINUX_APPLICATIONS_DIR, LINUX_DESKTOP_FILE);
		if (!desktopFile.isFile()) {
			throw new IOException("Could not find .desktop file");
		}
		File targetFile = new File(startupDir, LINUX_DESKTOP_FILE);
		Files.copy(desktopFile, targetFile);
	}

	private static void removeFromLinuxStartup() throws IOException {
		File startupDir = getLinuxStartupDir();
		File targetFile = new File(startupDir, LINUX_DESKTOP_FILE);
		if (targetFile.exists() && !targetFile.delete()) {
			throw new IOException("Could not delete .desktop file from autostart directory");
		}
	}

	private static File getLinuxStartupDir() throws IOException {
		String configDir = System.getProperty("configDir");
		File startupDir = new File(configDir, "autostart");
		if (!startupDir.isDirectory()) {
			throw new IOException("Could not find autostart directory");
		}
		return startupDir;
	}

	static File getMacStartupDir() throws IOException {
		String userHome = System.getProperty("user.home");
		File startupDir = new File(userHome + "/Library/LaunchAgents");
		if (!startupDir.isDirectory()) {
			throw new IOException("Could not find mac startup directory");
		}
		return startupDir;
	}
}
