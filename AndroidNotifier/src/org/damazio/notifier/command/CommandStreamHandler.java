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

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;

import org.damazio.notifier.NotifierConstants;

import android.util.Log;

/**
 * Handler for the command protocol.
 *
 * @author rdamazio
 */
class CommandStreamHandler extends Thread {

  private static final int BUFFER_SIZE = 256;
  private final BufferedReader input;
  private final OutputStream output;
  private final Closeable source;

  CommandStreamHandler(InputStream input, OutputStream output, Closeable source) {
    this.input = new BufferedReader(new InputStreamReader(input), BUFFER_SIZE);
    this.output = output;
    this.source = source;
  }

  @Override
  public void run() {
    try {
      String line;
      while ((line = input.readLine()) != null) {
        handleCommandLine(line);
      }
    } catch (IOException e) {
      // TODO
    }

    try {
      source.close();
    } catch (IOException e) {
      Log.w(NotifierConstants.LOG_TAG, "Error closing source", e);
    }
  }

  private void handleCommandLine(String line) {
    // TODO
    Log.d(NotifierConstants.LOG_TAG, "Got command line: " + line);
  }
}
