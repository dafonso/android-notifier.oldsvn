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
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.ref.WeakReference;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.damazio.notifier.NotifierConstants;
import org.damazio.notifier.command.CommandHistory;
import org.damazio.notifier.command.CommandStreamHandler;

import android.content.Context;
import android.util.Log;

/**
 * Abstract class for command listeners, which manages open connections
 * and their eventual shutdown.
 *
 * @author Rodrigo Damazio
 */
abstract class CommandListener extends Thread {
  private final Context context;
  private boolean shutdown;
  private Set<WeakReference<CommandStreamHandler>> streamHandlers =
      new HashSet<WeakReference<CommandStreamHandler>>();
  private final CommandHistory history;

  protected CommandListener(Context context, CommandHistory history) {
    this.context = context;
    this.history = history;
  }

  @Override
  public final void run() {
    shutdown = false;

    // Try to initialize for 5 minutes before giving up
    for (int i = 0; i < 300; i++) {
      try {
        initialize();
        break;
      } catch (IOException e) {
        if (i % 10 == 0) {
          Log.w(NotifierConstants.LOG_TAG, "Unable to initialize", e);
        }
      }

      try {
        sleep(1000);
      } catch (InterruptedException e1) {
        // Shutting down already? kthxbye
        return;
      }
    }

    while (!shutdown) {
      try {
        runOnce();
      } catch (IOException e) {
        Log.w(NotifierConstants.LOG_TAG, "Error accepting connection", e);
      }
    }
  }

  /**
   * Does any initialization necessary to start listening for command connections.
   *
   * @throws IOException if unable to listen
   */
  protected abstract void initialize() throws IOException;

  /**
   * Synchronously accepts one connection and calls {@link #handleConnection} to have it handled.
   * If interrupted, this method should return immediately.
   *
   * @throws IOException if unable to accept the connection
   */
  protected abstract void runOnce() throws IOException;

  /**
   * Handles commands sent through an open connection.
   * The connection will be automatically shutdown when {@link #shutdown()} is called.
   *
   * @param input the stream to read data from the connection
   * @param output the stream to write data to the connection
   * @param closeable the interface to close the connection
   */
  protected void handleConnection(InputStream input, OutputStream output, Closeable closeable) {
    CommandStreamHandler newHandler = new CommandStreamHandler(context, history, input, output, closeable);
    synchronized (streamHandlers) {
      streamHandlers.add(new WeakReference<CommandStreamHandler>(newHandler));
      newHandler.start();

      // Clean up handlers set
      cleanUpHandlers();
    }
  }

  /**
   * Cleans up any connection handlers which have been GCed.
   */
  private void cleanUpHandlers() {
    synchronized (streamHandlers) {
      for (Iterator<WeakReference<CommandStreamHandler>> it = streamHandlers.iterator(); it.hasNext();) {
        WeakReference<CommandStreamHandler> handlerRef = it.next();
        CommandStreamHandler handler = handlerRef.get();
        if (handler == null) {
          it.remove();
        }
      }
    }
  }

  /**
   * Shuts down the listener and closes all its open connections.
   */
  public void shutdown() {
    shutdown = true;
    interrupt();

    for (WeakReference<CommandStreamHandler> handlerRef : streamHandlers) {
      CommandStreamHandler handler = handlerRef.get();
      if (handler != null) {
        handler.shutdown();
      }
    }
  }
}
