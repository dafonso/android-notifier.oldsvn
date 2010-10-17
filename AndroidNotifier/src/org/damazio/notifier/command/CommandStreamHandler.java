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
package org.damazio.notifier.command;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.damazio.notifier.NotifierConstants;
import org.damazio.notifier.NotifierPreferences;
import org.damazio.notifier.command.CommandProtocol.CommandRequest;
import org.damazio.notifier.command.CommandProtocol.CommandResponse;
import org.damazio.notifier.command.handlers.CommandHandler;
import org.damazio.notifier.command.handlers.CommandHandlerFactory;
import org.damazio.notifier.notification.DeviceIdProvider;

import android.content.Context;
import android.util.Log;

/**
 * Handler for the command protocol.
 *
 * @author Rodrigo Damazio
 */
public class CommandStreamHandler extends Thread {

  private final InputStream input;
  private final OutputStream output;
  private final Closeable source;
  private final Context context;
  private final NotifierPreferences preferences;
  private final CommandHandlerFactory handlerFactory;

  public CommandStreamHandler(Context context, InputStream input, OutputStream output, Closeable source) {
    this.context = context;
    this.input = input;
    this.output = output;
    this.source = source;
    this.preferences = new NotifierPreferences(context);
    this.handlerFactory = new CommandHandlerFactory(context);
  }

  @Override
  public void run() {
    long deviceId = Long.parseLong(DeviceIdProvider.getDeviceId(context), 16);

    while (true) {
      try {
        CommandRequest req = CommandRequest.parseFrom(input);
        CommandResponse.Builder responseBuilder = CommandResponse.newBuilder()
            .setCommandId(req.getCommandId())
            .setDeviceId(deviceId);

        if (!req.isInitialized()) {
          writeFailure(req, responseBuilder, "Incomplete command");  // i18n
          continue;
        }

        Log.d(NotifierConstants.LOG_TAG, "Handling command: " + req);
        if (req.getDeviceId() != deviceId) {
          Log.e(NotifierConstants.LOG_TAG,
              "Wrong device id: " + req.getDeviceId() + "; this=" + deviceId);
          writeFailure(req, responseBuilder, "Wrong device ID");  // i18n
          continue;
        }

        CommandHandler handler = handlerFactory.createHandlerFor(req.getCommandType());
        if (handler == null) {
          Log.e(NotifierConstants.LOG_TAG, "No handler for command: " + req);
          writeFailure(req, responseBuilder, "No handler");  // i18n
          continue;
        }
        if (!handler.isEnabled(preferences)) {
          Log.w(NotifierConstants.LOG_TAG, "Not handling disabled command: " + req);
          writeFailure(req, responseBuilder, "Command disabled");  // i18n
          continue;
        }

        boolean success = handler.handleCommand(req, responseBuilder);
        responseBuilder.setSuccess(success);

        if (!success) {
          Log.e(NotifierConstants.LOG_TAG, "Command handling failed: " + req);
        }

        responseBuilder.build().writeTo(output);
      } catch (IOException e) {
        Log.w(NotifierConstants.LOG_TAG, "Error writing command output", e);
        break;
      }
    }

    closeSource();
  }

  private void writeFailure(CommandRequest req, CommandResponse.Builder responseBuilder, String errorMessage) throws IOException {
    CommandResponse response = responseBuilder.setSuccess(false)
        .setErrorMessage(errorMessage)
        .build();
    response.writeTo(output);
  }

  public void shutdown() {
    closeSource();
  }

  private void closeSource() {
    try {
      source.close();
    } catch (IOException e) {
      Log.w(NotifierConstants.LOG_TAG, "Error closing source", e);
    }
  }
}
