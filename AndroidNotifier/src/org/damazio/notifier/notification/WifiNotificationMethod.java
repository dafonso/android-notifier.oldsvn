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
import android.net.ConnectivityManager;
import android.net.DhcpInfo;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.net.wifi.WifiManager.WifiLock;
import android.util.Log;

/**
 * Notification method which sends notifications as UDP broadcast
 * packets.
 * These packets are only sent if Wi-Fi is enabled and connected.
 *
 * @author rdamazio
 */
public class WifiNotificationMethod implements NotificationMethod {

  /**
   * Class which waits for wifi to be enabled before sending a notification.
   * It will only wait up to a certain time before giving up.
   */
  private class WifiDelayedNotifier extends MethodEnablingNotifier {
    private static final String WIFI_LOCK_TAG = "org.damazio.notifier.WifiEnable";

    private WifiLock wifiLock;

    private WifiDelayedNotifier(Notification notification, boolean previousWifiEnabledState) {
      super(notification, previousWifiEnabledState, WifiNotificationMethod.this);
    }

    @Override
    protected synchronized void acquireLock() {
      if (wifiLock == null) {
        wifiLock = wifi.createWifiLock(WIFI_LOCK_TAG);
      }
      wifiLock.acquire();
    }

    @Override
    protected void releaseLock() {
      wifiLock.release();
    }

    @Override
    protected boolean isMediumReady() {
      return isWifiConnected();
    }

    @Override
    protected void setMediumEnabled(boolean enabled) {
      if (!wifi.setWifiEnabled(enabled)) {
        Log.e(NotifierConstants.LOG_TAG, "Unable to enable wifi");
      }
    }
  }

  private static final int UDP_PORT = 10600;
  private final NotifierPreferences preferences;
  private final WifiManager wifi;
  private final ConnectivityManager connectivity;

  public WifiNotificationMethod(Context context, NotifierPreferences preferences) {
    this.preferences = preferences;
    this.wifi = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
    this.connectivity = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
  }

  public void sendNotification(Notification notification) {
    if (!preferences.isWifiMethodEnabled()) {
      return;
    }

    // Check if wifi is disabled
    if (!isWifiConnected()) {
      // Check if we should enable it, or if it's already being enabled
      // (in which case we just send it after a little while)
      if (preferences.getEnableWifi() || isWifiConnecting()) {
        Log.d(NotifierConstants.LOG_TAG, "Enabling wifi and delaying notification");

        // Check periodically if wifi connected, then try to send it
        new WifiDelayedNotifier(notification, wifi.isWifiEnabled()).start();
      } else {
        Log.d(NotifierConstants.LOG_TAG, "Not notifying over wifi - not connected.");
      }
      return;
    }

    // Wifi is connected, so try to send notification now
    try {
      // TODO(rdamazio): Add an end-of-message marker in case the packets get split
      byte[] messageBytes = notification.toString().getBytes();

      // Get the address to send it to
      InetAddress broadcastAddress = getTargetAddress();
      Log.d(NotifierConstants.LOG_TAG, "Sending wifi notification to IP " + broadcastAddress.getHostAddress());

      // Create the packet to send
      DatagramPacket packet = new DatagramPacket(messageBytes, messageBytes.length, broadcastAddress, UDP_PORT);

      // Send it
      sendDatagramPacket(packet);

      Log.i(NotifierConstants.LOG_TAG, "Sent notification over WiFi.");
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
        // Send to 255.255.255.255
        return InetAddress.getByAddress(new byte[] { -1, -1, -1, -1 });
      } else if (addressStr.equals("dhcp")) {
        // Get the DHCP info from Wi-fi
        DhcpInfo dhcp = null;
        if (wifi != null) {
          dhcp = wifi.getDhcpInfo();
        }
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
        // Get the custom IP address from the other preference key
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
  private synchronized void sendDatagramPacket(DatagramPacket packet) throws IOException, SocketException {
    DatagramSocket socket = new DatagramSocket();
    socket.setBroadcast(true);
    socket.send(packet);
  }

  /**
   * @return whether wifi is enabled and connected
   */
  private boolean isWifiConnected() {
    // Check if wifi is supported and enabled
    if (wifi == null || connectivity == null) return false;
    if (!wifi.isWifiEnabled()) return false;
    if (wifi.getWifiState() != WifiManager.WIFI_STATE_ENABLED) return false;

    // Check if wifi is connected
    NetworkInfo wifiInfo = connectivity.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
    return (wifiInfo.getState() == NetworkInfo.State.CONNECTED);
  }

  /**
   * @return whether wifi is in the processing of being connected
   */
  private boolean isWifiConnecting() {
    if (wifi == null || connectivity == null) return false;
    if (wifi.getWifiState() == WifiManager.WIFI_STATE_ENABLING) return true;
    NetworkInfo wifiInfo = connectivity.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
    return (wifiInfo.getState() == NetworkInfo.State.CONNECTING);
  }

  public String getName() {
    return "wifi";
  }
}
