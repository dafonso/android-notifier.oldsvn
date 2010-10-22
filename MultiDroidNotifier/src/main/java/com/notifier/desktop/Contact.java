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
package com.notifier.desktop;

import java.util.*;

import com.google.common.collect.*;

public class Contact {

	private String name;
	private List<PhoneNumber> phoneNumbers;

	public Contact() {
		phoneNumbers = Lists.newArrayList();
	}

	public String getName() {
		return name;
	}

	public List<PhoneNumber> getPhoneNumbers() {
		return phoneNumbers;
	}

	public static class PhoneNumber {
		private String number;
		private Type type;

		public String getNumber() {
			return number;
		}

		public Type getType() {
			return type;
		}

		public static enum Type {
			HOME, MOBILE, WORK, OTHER
		}
	}
}
