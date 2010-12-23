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
package com.notifier.desktop.device.impl;

import java.util.*;
import java.util.concurrent.atomic.*;

import org.slf4j.*;

import com.google.common.base.*;
import com.google.inject.*;
import com.notifier.desktop.*;
import com.notifier.desktop.device.*;

@Singleton
public class DeviceManagerImpl extends RestartableService implements DeviceManager {

	private static final Logger logger = LoggerFactory.getLogger(DeviceManagerImpl.class);

	private boolean receptionFromAnyDevice;
	private Map<String, String> pairedDevices;

	private AtomicBoolean waitingForPairing;
	private PairingListener pairingListener;

	@Inject
	public DeviceManagerImpl(Provider<ApplicationPreferences> preferencesProvider) {
		ApplicationPreferences prefs = preferencesProvider.get();
		this.receptionFromAnyDevice = prefs.isReceptionFromAnyDevice();
		this.pairedDevices = prefs.getAllowedDevices();
		this.waitingForPairing = new AtomicBoolean();
	}

	@Override
	public String getName() {
		return "device manager";
	}

	@Override
	public void waitForPairing(PairingListener listener) {
		Preconditions.checkNotNull(listener);
		logger.info("Waiting for test notification to pair device");
		pairingListener = listener;
		waitingForPairing.set(true);
	}

	@Override
	public void cancelWaitForPairing() {
		logger.info("Pairing stopped");
		waitingForPairing.set(false);
		pairingListener = null;
	}

	@Override
	public boolean isWaitingForPairing() {
		return waitingForPairing.get();
	}

	@Override
	public void onPairingSuccessful(String deviceId) {
		if (pairingListener != null) {
			pairingListener.onPairingSuccessful(deviceId);
		}
	}

	@Override
	public void pairDevice(String deviceId, String deviceName) {
		pairedDevices.put(deviceId, deviceName);
	}

	@Override
	public void unpairDevice(String deviceId) {
		pairedDevices.remove(deviceId);
	}

	@Override
	public boolean isReceptionFromAnyDevice() {
		return receptionFromAnyDevice;
	}

	@Override
	public void setReceptionFromAnyDevice(boolean enabled) {
		this.receptionFromAnyDevice = enabled;
	}

	@Override
	public Collection<String> getPairedDeviceIds() {
		return pairedDevices.keySet();
	}

	@Override
	public boolean isAllowedDeviceId(String deviceId) {
		return isReceptionFromAnyDevice() || pairedDevices.containsKey(deviceId);
	}

	@Override
	public String getDeviceName(String deviceId) {
		return isReceptionFromAnyDevice() ? null : pairedDevices.get(deviceId);
	}

}
