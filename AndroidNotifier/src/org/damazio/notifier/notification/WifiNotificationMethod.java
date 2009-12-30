package org.damazio.notifier.notification;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;

import org.damazio.notifier.NotifierConstants;
import org.damazio.notifier.NotifierPreferences;

import android.content.Context;
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
      // TODO(rdamazio): Allow the user to pick whether to use:
      //                 255.255.255.255, the DHCP-provided broadcast address,
      //                 or a fixed broadcast address
      InetAddress broadcastAddress = InetAddress.getByAddress(new byte[] { -1, -1, -1, -1 });
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
