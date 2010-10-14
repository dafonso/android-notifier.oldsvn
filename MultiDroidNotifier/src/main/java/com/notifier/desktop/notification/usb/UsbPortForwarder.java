package com.notifier.desktop.notification.usb;

import java.io.*;
import java.util.*;

import org.slf4j.*;

import com.google.common.collect.*;

import static java.util.concurrent.TimeUnit.*;

public class UsbPortForwarder implements Runnable {

	private static final Logger logger = LoggerFactory.getLogger(UsbPortForwarder.class);

	private static final int LOCAL_PORT = 10602;
	private static final int ANDROID_PORT = 10601;
	private static final int SLEEP_TIME = 5;

	private boolean stopRequested;
	private Adb adb;

	@Override
	public void run() {
		while (!Thread.currentThread().isInterrupted() && !stopRequested) {
			Collection<Adb.Device> realDevices = Collections.emptyList();
			try {
				List<Adb.Device> devices = adb.devices();
				realDevices = Collections2.filter(devices, new Adb.Device.TypePredicate(Adb.Device.Type.DEVICE)); 
			} catch (IOException e) {
				logger.error("Error while running adb", e);
			} catch (InterruptedException e) {
				break;
			}

			for (Adb.Device device : realDevices) {
				try {
					adb.forward(device, LOCAL_PORT, ANDROID_PORT);
				} catch (IOException e) {
					logger.error("Error forwarding port for device [" + device.getSerialNumber() + "]", e);
				} catch (InterruptedException e) {
					break;
				}
			}

			try {
				SECONDS.sleep(SLEEP_TIME);
			} catch (InterruptedException e) {
				break;
			}
		}
	}

	public File getSdkHome() {
		return adb.getSdkHome();
	}

	public void setSdkHome(File sdkHome) {
		adb.setSdkHome(sdkHome);
	}

	public void prepare() {
		stopRequested = false;
	}

	public void stop() {
		stopRequested = true;
	}
}
