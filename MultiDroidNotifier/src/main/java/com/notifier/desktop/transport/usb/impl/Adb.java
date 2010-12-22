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
package com.notifier.desktop.transport.usb.impl;

import java.io.*;
import java.util.*;

import org.slf4j.*;

import com.google.common.base.*;
import com.google.common.collect.*;
import com.google.common.io.*;

public class Adb {

	private static final Logger logger = LoggerFactory.getLogger(Adb.class);

	private static final String TOOLS_PATH = "/tools";
	private static final String ADB_PATH = TOOLS_PATH + "/adb";

	private File sdkHome;

	public List<Device> devices() throws IOException, InterruptedException {
		Preconditions.checkNotNull(sdkHome, "Android SDK home has not been set");
		String output = runAdb("devices");
		Iterator<String> lines = Splitter.on('\n').trimResults().split(output).iterator();
		for (;lines.next().startsWith("*");) {
			; // Ignore daemon messages
		}
		List<Device> devices = Lists.newArrayList();
		Splitter lineSplitter = Splitter.on('\t').trimResults();
		while (lines.hasNext()) {
			String line = lines.next();
			if (!line.isEmpty()) {
				Iterator<String> parts = lineSplitter.split(line).iterator();
				String serialNumber = parts.next();
				Device.Type type = Device.Type.parse(parts.next());
				devices.add(new Device(serialNumber, type));
			}
		}
		return devices;
	}

	public void forward(Device device, int hostPort, String unixSocketName) throws IOException, InterruptedException {
		Preconditions.checkNotNull(sdkHome, "Android SDK home has not been set");
		runAdb("-s", device.getSerialNumber(), "forward", "tcp:" + hostPort, "localabstract:" + unixSocketName);
	}

	public File getSdkHome() {
		return sdkHome;
	}

	public void setSdkHome(File sdkHome) {
		Preconditions.checkArgument(sdkHome.isDirectory(), "Android SDK home does not exist or is not a directory.");
		Preconditions.checkArgument(new File(sdkHome, TOOLS_PATH).isDirectory(), "The directory is not Android SDK home, it must contain a \"tools\" sub-directory.");
		this.sdkHome = sdkHome;
	}

	public static class Device {
		private final String serialNumber;
		private final Type type;

		public Device(String serialNumber, Type type) {
			this.serialNumber = serialNumber;
			this.type = type;
		}

		public static enum Type {
			DEVICE("device"), EMULATOR("emulator"), UNKNOWN("");
			
			private String name;

			private Type(String name) {
				this.name = name;
			}

			public static Type parse(String s) {
				for (Type t : values()) {
					if (t.name.equals(s)) {
						return t;
					}
				}
				return UNKNOWN;
			}
		}

		public static class TypePredicate implements Predicate<Device> {
			private final Type type;

			public TypePredicate(Type type) {
				this.type = type;
			}

			@Override
			public boolean apply(Device input) {
				return type.equals(input.getType());
			}
		}

		public String getSerialNumber() {
			return serialNumber;
		}

		public Type getType() {
			return type;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((serialNumber == null) ? 0 : serialNumber.hashCode());
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
			if (!(obj instanceof Device)) {
				return false;
			}
			Device other = (Device) obj;
			if (serialNumber == null) {
				if (other.serialNumber != null) {
					return false;
				}
			} else if (!serialNumber.equals(other.serialNumber)) {
				return false;
			}
			return true;
		}

		@Override
		public String toString() {
			return serialNumber.substring(0, serialNumber.length() / 2) + Strings.repeat("X", 6);
		}
	}

	protected String runAdb(String... args) throws IOException, InterruptedException {
		ProcessBuilder processBuilder = new ProcessBuilder();
		processBuilder.command(Lists.asList(getAdbPath(), args));
		processBuilder.redirectErrorStream(true);
		final Process process = processBuilder.start();
		String output = CharStreams.toString(new InputSupplier<BufferedReader>() {
			@Override
			public BufferedReader getInput() throws IOException {
				return new BufferedReader(new InputStreamReader(process.getInputStream()));
			}
		});
		logger.trace("adb output:\n{}", output);
		int exitCode = process.waitFor();
		if (exitCode != 0) {
			throw new IOException("adb returned error code: " + exitCode);
		}
		return output;
	}

	protected String getAdbPath() {
		try {
			return sdkHome.getCanonicalPath() + ADB_PATH;
		} catch (IOException e) {
			return sdkHome.getAbsolutePath() + ADB_PATH;
		}
	}
}
