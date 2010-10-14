package com.notifier.desktop.notification.usb;

import java.io.*;
import java.util.*;

import com.google.common.base.*;
import com.google.common.collect.*;
import com.google.common.io.*;

public class Adb {

	private static final String ADB_PATH = "/tools/adb";

	private File sdkHome;

	public List<Device> devices() throws IOException, InterruptedException {
		String output = runAdb("devices");
		Iterator<String> lines = Splitter.on('\n').trimResults().split(output).iterator();
		for (;lines.next().startsWith("*");) {
			; // Ignore daemon messages
		}
		if (!lines.next().equals("List of devices attached")) {
			throw new IllegalStateException("Unknown response from adb");
		}

		List<Device> devices = Lists.newArrayList();
		Splitter lineSplitter = Splitter.on(' ').trimResults();
		while (lines.hasNext()) {
			String line = lines.next();
			Iterator<String> parts = lineSplitter.split(line).iterator();
			String serialNumber = parts.next();
			Device.Type type = Device.Type.parse(parts.next());
			devices.add(new Device(serialNumber, type));
		}
		return devices;
	}

	public void forward(Device device, int hostPort, int devicePort) throws IOException, InterruptedException {
		runAdb("-s", device.getSerialNumber(), "forward", "tcp:" + hostPort, "tcp:" + devicePort);
	}

	public File getSdkHome() {
		return sdkHome;
	}

	public void setSdkHome(File sdkHome) {
		Preconditions.checkArgument(sdkHome.isDirectory(), "Android SDK home does not exist or is not a directory.");
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
			DEVICE("device"), EMULATOR("emulator");
			
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
				return null;
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
