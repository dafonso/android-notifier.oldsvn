package org.damazio.notifier.notification;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;

import org.damazio.notifier.NotifierConstants;
import org.damazio.notifier.NotifierPreferences;

import android.content.Context;
import android.net.DhcpInfo;
import android.net.wifi.SupplicantState;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.util.Log;

/**
 * Notification method which sends notifications as UDP broadcast
 * packets.
 * These packets are only sent if Wi-Fi is enabled and connected.
 *
 * @author rdamazio
 */
public class WifiNotificationMethod implements NotificationMethod {

  private static final int UDP_PORT = 10600;
  private final NotifierPreferences preferences;
  private WifiManager wifi;

  public WifiNotificationMethod(Context context, NotifierPreferences preferences) {
    this.preferences = preferences;
    wifi = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
  }

  public void sendNotification(Notification notification) {
    if (!preferences.isWifiMethodEnabled()) {
      return;
    }

    if (!isWifiEnabled()) {
      Log.d(NotifierConstants.LOG_TAG, "Not notifying over wifi - not connected.");
      return;
    }

    try {
      // TODO(rdamazio): Add an end-of-message marker in case the packets get split
      byte[] messageBytes = notification.toString().getBytes();

      // TODO(rdamazio): Do we want/need to support IPv6 as well?
      InetAddress broadcastAddress = getTargetAddress();
      DatagramPacket packet = new DatagramPacket(messageBytes, messageBytes.length, broadcastAddress, UDP_PORT);
      sendDatagramPacket(packet);

      Log.d(NotifierConstants.LOG_TAG, "Sent notification over WiFi.");
    } catch (SocketException e) {
      Log.e(NotifierConstants.LOG_TAG, "Unable to open socket", e);
    } catch (IOException e) {
      Log.e(NotifierConstants.LOG_TAG, "Unable to send UDP packet", e);
    }
  }

  /**
   * Returns the proper address to send the notification to according to the
   * user's preferences, or null if it cannot be determined.
   */
  private InetAddress getTargetAddress() {
    String addressStr = preferences.getWifiTargetIpAddress();
    try {
      if (addressStr.equals("global")) {
        return InetAddress.getByAddress(new byte[] { -1, -1, -1, -1 });
      } else if (addressStr.equals("dhcp")) {
        // Get the DHCP info from Wi-fi
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
        return InetAddress.getByAddress(quads);
      } else if (addressStr.equals("custom")) {
        addressStr = preferences.getCustomWifiTargetIpAddress();
        return InetAddress.getByName(addressStr);
      } else {
        Log.e(NotifierConstants.LOG_TAG, "Invalid value for IP target: " + addressStr);
        return null;
      }
    } catch (UnknownHostException e) {
      Log.e(NotifierConstants.LOG_TAG, "Could not resolve address " + addressStr, e);
      return null;
    }
  }

  /**
   * Sends an UDP packet.
   *
   * @param packet the packet to send
   */
  protected void sendDatagramPacket(DatagramPacket packet) throws IOException, SocketException {
    DatagramSocket socket = new DatagramSocket();
    socket.setBroadcast(true);
    socket.send(packet);
  }

  /**
   * @return whether wifi is enabled and connected
   */
  private boolean isWifiEnabled() {
    WifiInfo connectionInfo = wifi.getConnectionInfo();
    if (connectionInfo == null) return false;

    SupplicantState state = connectionInfo.getSupplicantState();
    return (state == SupplicantState.COMPLETED);
  }
}
