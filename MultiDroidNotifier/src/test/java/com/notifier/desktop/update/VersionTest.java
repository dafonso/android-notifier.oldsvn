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
package com.notifier.desktop.update;

import static org.junit.Assert.*;

import org.junit.*;

public class VersionTest {

	@Test
	public void parseSnapshot() throws Exception {
		Version version = Version.parse("0.1.1-SNAPSHOT");
		assertEquals(new Version(0, 1, 1), version);
	}

	@Test
	public void testCompare() throws Exception {
		Version version1 = new Version(0, 2, 0);
		Version version2 = new Version(0, 2, 2);

		assertTrue(version1.compareTo(version2) < 0);
		assertTrue(version2.compareTo(version1) > 0);

		version1 = new Version(3, 2, 2);
		assertTrue(version1.compareTo(version2) > 0);
	}
}
