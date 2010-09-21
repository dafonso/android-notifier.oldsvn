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

import java.util.*;

import com.google.common.base.*;
import com.google.common.collect.*;

public class Version implements Comparable<Version> {

	public static final Version DEV = new Version(Integer.MAX_VALUE, Integer.MAX_VALUE, Integer.MAX_VALUE);

	private static final Splitter SNAPSHOT_SPLITTER = Splitter.on('-');
	private static final Splitter NUMBER_SPLITTER = Splitter.on('.');

	private final int major;
	private final int minor;
	private final int incremental;

	public Version(int major, int minor, int incremental) {
		this.major = major;
		this.minor = minor;
		this.incremental = incremental;
	}

	public static Version parse(String s) {
		String number = Iterables.get(SNAPSHOT_SPLITTER.split(s), 0);
		Iterator<String> parts = NUMBER_SPLITTER.split(number).iterator();

		int major, minor, incremental;
		major = minor = incremental = 0;

		if (parts.hasNext()) {
			major = Integer.parseInt(parts.next());
		}

		if (parts.hasNext()) {
			minor = Integer.parseInt(parts.next());
		}

		if (parts.hasNext()) {
			incremental = Integer.parseInt(parts.next());
		}

		return new Version(major, minor, incremental);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + incremental;
		result = prime * result + major;
		result = prime * result + minor;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (!(obj instanceof Version)) {
			return false;
		}
		Version other = (Version) obj;
		if (major != other.major) {
			return false;
		}
		if (minor != other.minor) {
			return false;
		}
		if (incremental != other.incremental) {
			return false;
		}
		return true;
	}

	@Override
	public int compareTo(Version o) {
		int diff = major - o.major;
		if (diff == 0) {
			diff = minor - o.minor;
			if (diff == 0) {
				return incremental - o.incremental;
			}
			return diff;
		}
		return diff;
	}

	@Override
	public String toString() {
		if (this.equals(DEV)) {
			return "DEV";
		}
		return major + "." + minor + "." + incremental;
	}
}
