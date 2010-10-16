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
import java.util.HashMap;
import java.util.Map;

import org.damazio.notifier.NotifierConstants;
import org.damazio.notifier.NotifierPreferences;

import android.net.LocalServerSocket;
import android.net.LocalSocket;
import android.util.Log;

/**
 * Notification method for sending notifications over USB.
 * 
 * TODO: Stop server when not connected to USB
 *
 * @author Leandro de Oliveira
 */
class UsbNotificationMethod implements NotificationMethod {

  private static final String SOCKET_NAME = "androidnotifier";
  private static final int SHUTDOWN_TIMEOUT = 5 * 1000;

  private final NotifierPreferences preferences;
  private LocalServerSocket serverSocket;
  private Thread serverThread;
  private boolean stopRequested;
  private Map<String, LocalSocket> openSockets;

  public UsbNotificationMethod(NotifierPreferences preferences) {
    this.preferences = preferences;
    startServer();
  }

  @Override
  public void sendNotification(byte[] payload, Object target, NotificationCallback callback,
      boolean isForeground) {
    Throwable failure = null;
    LocalSocket socket = openSockets.get(target);
    if (socket != null) { // Socket may not be in the map here because getTargets() is not synchronized
      synchronized (socket) {
        try {
          socket.setSendBufferSize(payload.length * 2);
          OutputStream stream = socket.getOutputStream();
          stream.write(payload);
          stream.flush();
        } catch (IOException e) {
          failure = e;
          Log.e(NotifierConstants.LOG_TAG, "Could not send notification over usb socket", e);
          close(socket);
          openSockets.remove(target);
        }
      }
    }
    callback.notificationDone(target, failure);
  }

  public String getName() {
    return "usb";
  }

  public boolean isEnabled() {
    return preferences.isUsbMethodEnabled();
  }

  @Override
  public Iterable<String> getTargets() {
    return openSockets.keySet();
  }

  private void startServer() {
    if (serverSocket == null) {
      Log.d(NotifierConstants.LOG_TAG, "Starting usb SocketServer");
      stopRequested = false;
      openSockets = new HashMap<String, LocalSocket>();
      try {
        serverSocket = new LocalServerSocket(SOCKET_NAME);
        serverThread = new Thread(new Runnable() {
          @Override
          public void run() {
            while (!stopRequested) {
              try {
                LocalSocket socket = serverSocket.accept();
                openSockets.put(Integer.toString(socket.hashCode()), socket);
              } catch (IOException e) {
                Log.e(NotifierConstants.LOG_TAG, "Error handling usb socket connection", e);
              }
            }
            for (LocalSocket socket : openSockets.values()) {
              try {
                socket.shutdownOutput();
              } catch (IOException e) {
                Log.w(NotifierConstants.LOG_TAG, "Error shutting down usb socket", e);
              } finally {
                close(socket);
              }
            }
          }
        }, "usb-server-thread");
        serverThread.start();
      } catch (IOException e) {
        serverSocket = null;
        Log.e(NotifierConstants.LOG_TAG, "Could not start usb ServerSocket, usb notifications will not work", e);
      }
    }
  }

  private void stopServer() {
    if (serverSocket != null) {
      Log.d(NotifierConstants.LOG_TAG, "Stopping usb SocketServer");
      stopRequested = true;
      try {
        serverThread.interrupt();
        serverThread.join(SHUTDOWN_TIMEOUT);
      } catch (InterruptedException e1) {
        Thread.currentThread().interrupt();
      } finally {
        openSockets.clear();
        try {
          serverSocket.close();
        } catch (IOException e) {
          Log.w(NotifierConstants.LOG_TAG, "Error closing usb ServerSocket", e);
        } finally {
          serverSocket = null;
        }
      }
    }
  }

  private void close(LocalSocket socket) {
    try {
      socket.close();
    } catch (IOException ce) {
      Log.e(NotifierConstants.LOG_TAG, "Error closing usb socket", ce);
    }
  }
}
