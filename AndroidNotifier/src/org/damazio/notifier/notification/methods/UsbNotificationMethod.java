/*
 * Copyright 2010 Rodrigo Damazio <rodrigo@damazio.org>
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * 
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR
 * IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 * IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
 * NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
 * THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.damazio.notifier.notification.methods;

import java.io.IOException;
import java.io.OutputStream;
import java.util.*;

import org.damazio.notifier.NotifierConstants;
import org.damazio.notifier.NotifierPreferences;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.*;
import android.os.*;
import android.os.PowerManager.WakeLock;
import android.util.Log;

/**
 * Notification method for sending notifications over USB.
 *
 * @author Leandro de Oliveira
 * @author Rodrigo Damazio
 */
class UsbNotificationMethod implements NotificationMethod {

  private static final String SOCKET_NAME = "androidnotifier";
  private static final int SHUTDOWN_TIMEOUT = 5 * 1000;
  private static final String USB_LOCK_TAG = UsbNotificationMethod.class.getName();
  private static final byte POISON_PILL = Byte.MAX_VALUE;

  private final Context context;
  private final NotifierPreferences preferences;

  private Thread serverThread;
  private LocalServerSocket serverSocket;
  private List<LocalSocket> openSockets = new ArrayList<LocalSocket>();
  private WakeLock wakeLock;

  private final BroadcastReceiver powerReceiver = new BroadcastReceiver() {
    @Override
    public void onReceive(Context context, Intent intent) {
      String action = intent.getAction();

      if (Intent.ACTION_BATTERY_CHANGED.equals(action)) {
        int plugged = intent.getIntExtra(BatteryManager.EXTRA_PLUGGED, 0);
        if (plugged == BatteryManager.BATTERY_PLUGGED_USB) {
          startServer();
        } else {
          stopServer();
        }
      } else {
        Log.e(NotifierConstants.LOG_TAG, "Got unexpected action: " + action);
      }
    }
  };

  public UsbNotificationMethod(Context context, NotifierPreferences preferences) {
    this.context = context;
    this.preferences = preferences;

    IntentFilter batteryIntentFilter = new IntentFilter();
    batteryIntentFilter.addAction(Intent.ACTION_BATTERY_CHANGED);
    context.registerReceiver(powerReceiver, batteryIntentFilter);
  }

  @Override
  public void sendNotification(byte[] payload, Object target, NotificationCallback callback,
      boolean isForeground) {
    LocalSocket socket = (LocalSocket) target;
    synchronized (socket) {
      try {
        socket.setSendBufferSize(payload.length * 2);
        OutputStream stream = socket.getOutputStream();
        stream.write(payload);
        stream.flush();
        callback.notificationDone(target, null);
      } catch (IOException e) {
        Throwable throwableToReturn;
        if ("Broken pipe".equals(e.getMessage())) {
          Log.d(NotifierConstants.LOG_TAG, "A usb socket has been closed");
          throwableToReturn = null;
        } else {
          Log.e(NotifierConstants.LOG_TAG, "Could not send notification over usb socket", e);
          throwableToReturn = e;
        }
        closeSocket(socket);
        openSockets.remove(socket);
        callback.notificationDone(target, throwableToReturn);
      }
    }
  }

  @Override
  public String getName() {
    return "usb";
  }

  @Override
  public boolean isEnabled() {
    return preferences.isUsbMethodEnabled();
  }

  @Override
  public Iterable<LocalSocket> getTargets() {
    synchronized (openSockets) {
      return new ArrayList<LocalSocket>(openSockets);
    }
  }

  private void startServer() {
    synchronized (this) {
      if (serverSocket != null) {
        return;
      }

      // Ensure the CPU keeps running while we listen for connections
      PowerManager powerManager = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
      wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, USB_LOCK_TAG);
      wakeLock.acquire();

      Log.d(NotifierConstants.LOG_TAG, "Starting usb SocketServer");
      try {
        serverSocket = new LocalServerSocket(SOCKET_NAME);
        serverThread = new Thread("usb-server-thread") {
          @Override
          public void run() {
            for (;;) {
              try {
                LocalSocket socket = serverSocket.accept();
                synchronized (openSockets) {
                  openSockets.add(socket);
                }

                byte pill = (byte) socket.getInputStream().read();
                if (pill == POISON_PILL) {
                  Log.d(NotifierConstants.LOG_TAG, "Received poison pill on usb SocketServer");
                  break;
                }
              } catch (IOException e) {
                Log.e(NotifierConstants.LOG_TAG, "Error handling usb socket connection", e);
              }
            }

            try {
              synchronized (openSockets) {
                for (Iterator<LocalSocket> iterator = openSockets.iterator(); iterator.hasNext(); ) {
                  closeSocket(iterator.next());
                  iterator.remove();
                }
              }
            } catch (Throwable t) {
              Log.e(NotifierConstants.LOG_TAG, "Error shutting down usb socket", t);
            }
          }
        };

        serverThread.start();
      } catch (IOException e) {
        serverSocket = null;
        Log.e(NotifierConstants.LOG_TAG, "Could not start usb ServerSocket, usb notifications will not work", e);
        stopServer();
      }
    }
  }

  private void stopServer() {
    synchronized (this) {
      if (serverSocket == null) {
        return;
      }

      Log.d(NotifierConstants.LOG_TAG, "Stopping usb SocketServer");
      LocalSocket poisonPillSocket = new LocalSocket();
      try {
        poisonPillSocket.connect(new LocalSocketAddress(SOCKET_NAME));
        poisonPillSocket.getOutputStream().write(POISON_PILL);
        poisonPillSocket.getOutputStream().flush();
      } catch (IOException e) {
        Log.e(NotifierConstants.LOG_TAG, "Could not stop usb SocketServer", e);
        return;
      } finally {
        try {
          poisonPillSocket.close();
        } catch (IOException e) {
          Log.w(NotifierConstants.LOG_TAG, "Could not close socket used to stop usb SocketServer");
        }
      }

      try {
        serverThread.join(SHUTDOWN_TIMEOUT);
        Log.d(NotifierConstants.LOG_TAG, "Stopped usb SocketServer successfully");
      } catch (InterruptedException e1) {
        Thread.currentThread().interrupt();
      } finally {
        synchronized (openSockets) {
          openSockets.clear();
        }
        try {
          serverSocket.close();
        } catch (IOException e) {
          Log.w(NotifierConstants.LOG_TAG, "Error closing usb ServerSocket", e);
        } finally {
          serverSocket = null;
          wakeLock.release();
          wakeLock = null;
        }
      }
    }
  }

  private void closeSocket(LocalSocket socket) {
    try {
      socket.close();
    } catch (IOException ce) {
      Log.e(NotifierConstants.LOG_TAG, "Error closing usb socket", ce);
    }
  }

  @Override
  public void shutdown() {
    context.unregisterReceiver(powerReceiver);
    stopServer();
  }
}
