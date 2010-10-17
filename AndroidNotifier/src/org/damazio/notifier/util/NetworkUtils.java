package org.damazio.notifier.util;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import org.damazio.notifier.NotifierConstants;

import android.content.Context;
import android.net.DhcpInfo;
import android.net.wifi.WifiManager;
import android.util.Log;

public class NetworkUtils {
  private final Context context;

  public NetworkUtils(Context context) {
    this.context = context;
  }

  public List<InetAddress> getAllLocalIps() {
    List<InetAddress> addresses = new ArrayList<InetAddress>();
    try {
      for (Enumeration<NetworkInterface> allInterfaces = NetworkInterface.getNetworkInterfaces();
           allInterfaces.hasMoreElements();) {
        NetworkInterface oneInterface = allInterfaces.nextElement();
        for (Enumeration<InetAddress> allIps = oneInterface.getInetAddresses(); allIps.hasMoreElements();) {
          InetAddress oneIp = allIps.nextElement();
          if (!oneIp.isLoopbackAddress()) {
            addresses.add(oneIp);
          }
        }
      }
    } catch (SocketException e) {
      Log.e(NotifierConstants.LOG_TAG, "Unable to get network interfaces", e);
    }

    return addresses;
  }

  public InetAddress getWifiDhcpBroadcastAddress() {
    WifiManager wifi = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);

    // Get the DHCP info from Wi-fi
    if (wifi == null) {
      Log.e(NotifierConstants.LOG_TAG, "Could not obtain Wifi info");
      return null;
    }

    DhcpInfo dhcp = wifi.getDhcpInfo();
    if (dhcp == null) {
      Log.e(NotifierConstants.LOG_TAG, "Could not obtain DHCP info");
      return null;
    }

    // Calculate the broadcast address
    int broadcast = (dhcp.ipAddress & dhcp.netmask) | ~dhcp.netmask;
    byte[] quads = new byte[4];
    for (int k = 0; k < 4; k++) {
      quads[k] = (byte) ((broadcast >> k * 8) & 0xFF);
    }

    try {
      return InetAddress.getByAddress(quads);
    } catch (UnknownHostException e) {
      Log.e(NotifierConstants.LOG_TAG, "Could not obtain broadcast address", e);
      return null;
    }
  }
}
