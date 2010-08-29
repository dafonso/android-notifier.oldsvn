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
