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
package com.notifier.desktop.notification;

import static org.junit.Assert.*;

import org.junit.*;

public class NotificationTest {

	@Test
	public void testGetBatteryIconName() throws Exception {
		testBatteryIcon(null, "100");
		testBatteryIcon("", "100");
		testBatteryIcon("100", "100");
		testBatteryIcon("73", "75");
		testBatteryIcon("67", "65");
		testBatteryIcon("59", "60");
		testBatteryIcon("0", "0");
		testBatteryIcon("3", "5");
		testBatteryIcon("2", "0");
	}

	protected void testBatteryIcon(String data, String expected) {
		Notification notification = new Notification("0", 0, Notification.Type.BATTERY, data, null);
		assertEquals(Notification.BATTERY_ICON_PREFIX + expected + Notification.BATTERY_ICON_SUFFIX, notification.getBatteryIconName());
	}
}
