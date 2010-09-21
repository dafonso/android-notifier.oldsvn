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
package com.google.code.notifier.desktop.app;

import java.io.*;
import java.net.*;
import java.util.*;

import org.htmlparser.*;
import org.htmlparser.filters.*;
import org.htmlparser.util.*;

import com.google.code.notifier.desktop.*;
import com.google.common.base.*;
import com.google.common.io.*;

public class UpdateManagerImpl implements UpdateManager {

	private static final String POM_PROPERTIES_NAME = "/META-INF/maven/com.google.code.android-notifier-desktop/android-notifier-desktop/pom.properties";
	private static final String POM_VERSION_PROPERTY = "version";
	private static final URI TAGS_URI = URI.create("http://android-notifier-desktop.googlecode.com/svn/tags/");

	private Version latestVersion;

	@Override
	public boolean isLatestVersion() throws IOException {
		Version current = getCurrentVersion();
		latestVersion = getLatestVersion();

		return latestVersion == null || current.compareTo(latestVersion) >= 0;
	}

	public Version getCurrentVersion() throws IOException {
		InputStream is = getClass().getResourceAsStream(POM_PROPERTIES_NAME);
		if (is == null) {
			return Version.DEV;
		}
		Properties pom = new Properties();
		try {
			pom.load(is);
		} finally {
			Closeables.closeQuietly(is);
		}
		return Version.parse(pom.getProperty(POM_VERSION_PROPERTY));
	}

	public Version getLatestVersion() throws IOException {
		try {
			System.setProperty("java.net.useSystemProxies", Boolean.TRUE.toString());
			Parser parser = new Parser(TAGS_URI.toString());
			NodeList list = parser.parse(new TagNameFilter("li"));
			Version currentLatestVersion = null;
			for (int i = 0; i < list.size(); i++) {
				String value = Strings.nullToEmpty(list.elementAt(i).getFirstChild().getFirstChild().getText());
				if (value.startsWith(Application.ARTIFACT_ID)) {
					int endIndex = value.endsWith("/") ? value.length() - 1 : value.length();
					String versionString = value.substring(Application.ARTIFACT_ID.length() + 1, endIndex);
					Version version = Version.parse(versionString);
					if (currentLatestVersion == null || version.compareTo(currentLatestVersion) > 0) {
						currentLatestVersion = version;
					}
				}
			}
			latestVersion = currentLatestVersion;
			return currentLatestVersion;
		} catch (ParserException e) {
			throw new IOException("Could not parse tags page", e);
		}
	}

	@Override
	public Version getCachedLatestVersion() {
		return latestVersion;
	}
}
