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
package com.notifier.desktop.discovery;

import java.util.*;

import com.google.protobuf.*;
import com.notifier.protocol.*;

public class DiscoveryInfo {

	private String desktopId;
	private byte[] ipAddress;
	private int ipPort;
	private String bluetoothAddress;
	private boolean onlyPaired;
	private Collection<String> pairedDeviceIds;

	public Protocol.Discovery toProtobuf() {
		Protocol.Discovery.Builder builder = Protocol.Discovery.newBuilder();
		builder.setDesktopId(desktopId);
		builder.setIpAddress(ByteString.copyFrom(ipAddress));
		builder.setIpPort(ipPort);
		builder.setBluetoothAddress(bluetoothAddress);
		builder.setOnlyPaired(onlyPaired);
		builder.addAllPairedDeviceIds(pairedDeviceIds);
		return builder.build();
	}

	public String getDesktopId() {
		return desktopId;
	}

	public void setDesktopId(String desktopId) {
		this.desktopId = desktopId;
	}

	public byte[] getIpAddress() {
		return ipAddress;
	}

	public void setIpAddress(byte[] ipAddress) {
		this.ipAddress = ipAddress;
	}

	public int getIpPort() {
		return ipPort;
	}

	public void setIpPort(int ipPort) {
		this.ipPort = ipPort;
	}

	public String getBluetoothAddress() {
		return bluetoothAddress;
	}

	public void setBluetoothAddress(String bluetoothAddress) {
		this.bluetoothAddress = bluetoothAddress;
	}

	public boolean isOnlyPaired() {
		return onlyPaired;
	}

	public void setOnlyPaired(boolean onlyPaired) {
		this.onlyPaired = onlyPaired;
	}

	public Collection<String> getPairedDeviceIds() {
		return pairedDeviceIds;
	}

	public void setPairedDeviceIds(Collection<String> pairedDeviceIds) {
		this.pairedDeviceIds = pairedDeviceIds;
	}

}
