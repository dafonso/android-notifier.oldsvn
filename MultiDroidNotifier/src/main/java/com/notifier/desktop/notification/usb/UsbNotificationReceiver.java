package com.notifier.desktop.notification.usb;

import java.io.*;
import java.util.concurrent.*;

import com.google.inject.*;
import com.notifier.desktop.*;

public class UsbNotificationReceiver extends RestartableService implements NotificationReceiver {

	private ExecutorService executorService;
	private UsbPortForwarder portForwarder;
	private UsbPortListener portListener;

	@Inject
	public UsbNotificationReceiver(Provider<ApplicationPreferences> preferencesProvider, ExecutorService executorService) {
		this.executorService = executorService;
		this.portForwarder = new UsbPortForwarder();
		this.portListener = new UsbPortListener();
	}

	@Override
	public String getName() {
		return "USB";
	}

	@Override
	protected void doStart() throws Exception {
		if (portForwarder.getSdkHome() == null) {
			throw new IllegalStateException("Android SDK home has not been set");
		}
		portForwarder.prepare();
		portListener.prepare();

		executorService.execute(portForwarder);
		executorService.execute(portListener);
	}

	@Override
	protected void doStop() throws Exception {
		portForwarder.stop();
		portListener.stop();
	}

	public void setSdkHome(String sdkHome) {
		portForwarder.setSdkHome(new File(sdkHome));
	}
}
