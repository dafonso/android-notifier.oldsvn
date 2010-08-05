package org.damazio.notifier.notification;

import java.io.IOException;
import java.io.OutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
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
class WifiNotificationMethod implements NotificationMethod {

  /**
   * Class which waits for wifi to be enabled before sending a notification.
   * It will only wait up to a certain time before giving up.
   */
  private class WifiDelayedNotifier extends MethodEnablingNotifier {
    private static final String WIFI_LOCK_TAG = "org.damazio.notifier.WifiEnable";

    private WifiLock wifiLock;

    private WifiDelayedNotifier(Notification notification, NotificationCallback callback,
        boolean previousWifiEnabledState) {
      super(notification, callback, previousWifiEnabledState, WifiNotificationMethod.this);
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
  private static final int TCP_PORT = 10600;
  private static final int TCP_CONNECT_TIMEOUT_MS = 5000;
  private static final byte DELIMITER_BYTE = 0;
  private final NotifierPreferences preferences;
  private final WifiManager wifi;
  private final ConnectivityManager connectivity;

  public WifiNotificationMethod(Context context, NotifierPreferences preferences) {
    this.preferences = preferences;
    this.wifi = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
    this.connectivity =
        (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
  }

  public void sendNotification(Notification notification, NotificationCallback callback) {
    // If background data is disabled, don't send any of the background notifications
    // PING is the only notification that's sent from the app, so it's not considered background
    if (!connectivity.getBackgroundDataSetting() && notification.getType() != NotificationType.PING) {
      Log.w(NotifierConstants.LOG_TAG, "Background data is turned off, not notifying.");
      return;
    }

    // Check if wifi is disabled
    if (!isWifiConnected()) {
      if (preferences.getEnableWifi() || isWifiConnecting()) {
        // We should enable it, or if it's already being enabled
        // (in which case we just send it after a little while)
        Log.d(NotifierConstants.LOG_TAG, "Enabling wifi and delaying notification");

        // Check periodically if wifi connected, then try to send it
        new WifiDelayedNotifier(notification, callback, wifi.isWifiEnabled()).start();
        return;
      } else if (canSendOverCellNetwork()) {
        // Wifi is not enabled, but we can try the cell network
      } else {
        // It won't be enabled, and we cannot send it over the cell phone network
        Log.d(NotifierConstants.LOG_TAG, "Not notifying over IP/wifi - not connected.");
        callback.notificationFailed(notification, null);
        return;
      }
    }

    // Connected, so try to send notification now
    try {
      byte[] messageBytes = notification.toString().getBytes();
      messageBytes = addDelimiter(messageBytes);

      // Get the address to send it to
      InetAddress address = getTargetAddress();
      Log.d(NotifierConstants.LOG_TAG,
          "Sending wifi notification to IP " + address.getHostAddress());

      if (preferences.isSendUdpEnabled()) {
        sendUdpNotification(messageBytes, address);
      }
      if (preferences.isSendTcpEnabled()) {
        if (preferences.getWifiTargetIpAddress().equals("custom")) {
          sendTcpNotification(messageBytes, address);
        } else {
          Log.e(NotifierConstants.LOG_TAG, "TCP enabled but trying to use a broadcast address");
        }
      }

      callback.notificationSent(notification);
      Log.i(NotifierConstants.LOG_TAG, "Sent notification over WiFi.");
    } catch (SocketException e) {
      callback.notificationFailed(notification, e);
      Log.e(NotifierConstants.LOG_TAG, "Unable to open socket", e);
    } catch (IOException e) {
      callback.notificationFailed(notification, e);
      Log.e(NotifierConstants.LOG_TAG, "Unable to send TCP or UDP packet", e);
    }
  }

  /**
   * Adds a final delimiter to the message so that its end can be easily
   * detected.
   *
   * @param messageBytes the original message bytes
   * @return message bytes with the delimiter
   */
  private byte[] addDelimiter(byte[] messageBytes) {
    byte[] result = new byte[messageBytes.length  + 1];
    System.arraycopy(messageBytes, 0, result, 0, messageBytes.length);
    result[messageBytes.length] = DELIMITER_BYTE;
    return result;
  }

  /**
   * Sends a notification over TCP.
   *
   * @param messageBytes the bytes of the notification to send
   * @param address the address to send it to
   */
  private void sendTcpNotification(byte[] messageBytes, InetAddress address) throws IOException {
    Log.d(NotifierConstants.LOG_TAG, "Sending over TCP");
    Socket socket = new Socket();
    SocketAddress remoteAddr = new InetSocketAddress(address, TCP_PORT);
    socket.connect(remoteAddr, TCP_CONNECT_TIMEOUT_MS);
    socket.setSendBufferSize(messageBytes.length * 2);
    OutputStream stream = socket.getOutputStream();
    stream.write(messageBytes);
    stream.flush();
    socket.close();
    Log.d(NotifierConstants.LOG_TAG, "Sent over TCP");
  }

  /**
   * Sends a notification over UDP.
   *
   * @param messageBytes the bytes of the notification to send
   * @param address the address to send it to
   */
  private void sendUdpNotification(byte[] messageBytes, InetAddress targetAddress)
      throws IOException, SocketException {
    Log.d(NotifierConstants.LOG_TAG, "Sending over UDP");

    // Create the packet to send
    DatagramPacket packet =
        new DatagramPacket(messageBytes, messageBytes.length, targetAddress, UDP_PORT);

    // Send it
    sendDatagramPacket(packet);

    Log.d(NotifierConstants.LOG_TAG, "Sent over UDP");
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
  private synchronized void sendDatagramPacket(DatagramPacket packet)
      throws IOException, SocketException {
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

  /**
   * @return whether to try to send data over the cell phone data network
   */
  private boolean canSendOverCellNetwork() {
    // User must have enabled the option
    if (!preferences.getSendOverCellNetwork()) return false;

    // User must have configured a custom IP or host
    if (!preferences.getWifiTargetIpAddress().equals("custom")) return false;

    // We must know about the network connection
    if (connectivity == null) return false;
    NetworkInfo networkInfo = connectivity.getActiveNetworkInfo();
    if (networkInfo == null) return false;

    // It must be connected
    return networkInfo.isConnected(); 
  }

  public String getName() {
    return "wifi";
  }

  public boolean isEnabled() {
    return preferences.isWifiMethodEnabled();
  }
}
