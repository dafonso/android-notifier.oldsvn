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
package org.damazio.notifier.command.listeners;

import java.io.Closeable;
import java.io.IOException;

import org.damazio.notifier.NotifierConstants;

import android.content.Context;
import android.net.LocalServerSocket;
import android.net.LocalSocket;
import android.util.Log;

/**
 * Command listener which receives commands over USB through adb.
 *
 * @author Rodrigo Damazio
 */
public class UsbCommandListener extends CommandListener {
  private static final String SOCKET_NAME = "androidnotifier-cmd";
  private LocalServerSocket serverSocket;

  public UsbCommandListener(Context context) {
    super(context);
  }

  @Override
  protected void initialize() throws IOException {
    serverSocket = new LocalServerSocket(SOCKET_NAME);
    Log.i(NotifierConstants.LOG_TAG, "Listening for commands over USB");
  }

  @Override
  protected void runOnce() throws IOException {
    final LocalSocket socket = serverSocket.accept();
    Log.d(NotifierConstants.LOG_TAG, "Accepted USB command connection");
    handleConnection(socket.getInputStream(), socket.getOutputStream(), new Closeable() {
      @Override
      public void close() throws IOException {
        // LocalSocket doesn't implement Closeable :( so we need this wrapper
        socket.close();
      }
    });
  }

  @Override
  public void shutdown() {
    Log.d(NotifierConstants.LOG_TAG, "No longer listening for USB commands");
    try {
      serverSocket.close();
    } catch (IOException e) {
      Log.e(NotifierConstants.LOG_TAG, "Error closing socket", e);
    }

    super.shutdown();
  }
}
