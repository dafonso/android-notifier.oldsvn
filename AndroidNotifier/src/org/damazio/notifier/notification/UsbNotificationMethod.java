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
package org.damazio.notifier.notification;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import org.damazio.notifier.NotifierConstants;
import org.damazio.notifier.NotifierPreferences;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.LocalServerSocket;
import android.net.LocalSocket;
import android.os.PowerManager;
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

  private final Context context;
  private final NotifierPreferences preferences;

  private Thread serverThread;
  private boolean stopRequested;
  private LocalServerSocket serverSocket;
  private List<LocalSocket> openSockets = new ArrayList<LocalSocket>();
  private WakeLock wakeLock;

  private final BroadcastReceiver powerReceiver = new BroadcastReceiver() {
    @Override
    public void onReceive(Context context, Intent intent) {
      String action = intent.getAction();
      // We abuse the power connected/disconnected as meaning USB connected/disconnected
      if (Intent.ACTION_POWER_CONNECTED.equals(action)) {
        startServer();
      } else if (Intent.ACTION_POWER_DISCONNECTED.equals(action)) {
        stopServer();
      } else {
        Log.e(NotifierConstants.LOG_TAG, "Got unexpected action: " + action);
      }
    }
  };

  public UsbNotificationMethod(Context context, NotifierPreferences preferences) {
    this.context = context;
    this.preferences = preferences;

    // TODO: These constants are not available in cupcake
    IntentFilter powerIntentFilter = new IntentFilter();
    powerIntentFilter.addAction(Intent.ACTION_POWER_CONNECTED);
    powerIntentFilter.addAction(Intent.ACTION_POWER_DISCONNECTED);
    context.registerReceiver(powerReceiver, powerIntentFilter);
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
        Log.e(NotifierConstants.LOG_TAG, "Could not send notification over usb socket", e);
        closeSocket(socket);
        callback.notificationDone(target, e);
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
      stopRequested = false;
      try {
        serverSocket = new LocalServerSocket(SOCKET_NAME);
        serverThread = new Thread("usb-server-thread") {
          @Override
          public void run() {
            while (!stopRequested) {
              try {
                LocalSocket socket = serverSocket.accept();
                synchronized (openSockets) {
                  openSockets.add(socket);
                }
              } catch (IOException e) {
                Log.e(NotifierConstants.LOG_TAG, "Error handling usb socket connection", e);
              }
            }

            synchronized (openSockets) {
              for (LocalSocket socket : openSockets) {
                try {
                  socket.shutdownOutput();
                } catch (IOException e) {
                  Log.w(NotifierConstants.LOG_TAG, "Error shutting down usb socket", e);
                } finally {
                  closeSocket(socket);
                }
              }
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
      stopRequested = true;
      try {
        serverThread.interrupt();
        serverThread.join(SHUTDOWN_TIMEOUT);
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
      synchronized (socket) {
        socket.close();
        synchronized(openSockets) {
          openSockets.remove(socket);
        }
      }
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
