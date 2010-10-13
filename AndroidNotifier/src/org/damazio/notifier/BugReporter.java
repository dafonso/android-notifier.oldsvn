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
package org.damazio.notifier;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.net.Uri.Builder;
import android.preference.PreferenceManager;
import android.text.ClipboardManager;
import android.util.Log;
import android.widget.Toast;

/**
 * Shows the user a form to report a bug, giving him easy access to the
 * application's log.
 *
 * @author rdamazio
 */
public class BugReporter {
  /** Pattern to extract the tag */
  private static final Pattern TAG_PATTERN = Pattern.compile("[VDIWE]/([^(]+).*");

  // Patterns to match the relevant tags
  private static final String SYSTEM_TAG = "AndroidRuntime";
  private static final String BLUETOOTH_TAG_REGEX = "Bluetooth.*";
  private static final String WIFI_TAG_REGEX = "Wifi.*";
  private static final String CALL_TAG_REGEX = ".*Call.*";
  private static final String PHONE_TAG_REGEX = "Phone.*";
  private static final String[] FILTER_TAGS = {
      SYSTEM_TAG, BLUETOOTH_TAG_REGEX, WIFI_TAG_REGEX, CALL_TAG_REGEX, PHONE_TAG_REGEX,
      NotifierConstants.LOG_TAG };

  // Preferences to skip from reporting
  // These are usually of little use and may contain PII.
  private static final int[] EXCLUDE_PREFS = {
    R.string.encryption_pass_key,
    R.string.target_custom_ips_key,
    R.string.bluetooth_device_key,
    R.string.bluetooth_source_key,
  };

  // URL for reporting the issue
  // TODO(rdamazio): Use the project hosting GData api instead (how to get login?)
  private static final String ISSUE_URI_SCHEME = "http";
  private static final String ISSUE_URI_HOST = "code.google.com";
  private static final String ISSUE_URI_PATH = "/p/android-notifier/issues/entry";
  private static final String ISSUE_PHONE_TEMPLATE = "Defect report from phone";
  private static final String ISSUE_URI_TEMPLATE_PARAM = "template";

  public static void reportBug(Context context) {
    try {
      // Copy the log to the clipboard - it's likely too large to put in the URL
      String log = readLog(FILTER_TAGS);
      String preferences = readAllPreferences(context);
      String report = log + "\n\n" + preferences;
      Log.d(NotifierConstants.LOG_TAG, "Read log");

      ClipboardManager clipboard =
          (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
      clipboard.setText(report);

      Toast.makeText(context, R.string.report_bug_toast, Toast.LENGTH_LONG).show();
    } catch (IOException e) {
      Log.e(NotifierConstants.LOG_TAG, "Unable to read logs", e);
    }

    // Now open the bug report page
    Builder uriBuilder = new Uri.Builder();
    uriBuilder.scheme(ISSUE_URI_SCHEME);
    uriBuilder.authority(ISSUE_URI_HOST);
    uriBuilder.path(ISSUE_URI_PATH);
    uriBuilder.appendQueryParameter(ISSUE_URI_TEMPLATE_PARAM, ISSUE_PHONE_TEMPLATE);
    context.startActivity(new Intent(Intent.ACTION_VIEW, uriBuilder.build()));
  }

  private static String readAllPreferences(Context context) {
    Set<String> excludedKeys = new HashSet<String>(EXCLUDE_PREFS.length);
    for (int excludedKeyId : EXCLUDE_PREFS) {
      excludedKeys.add(context.getString(excludedKeyId));
    }

    StringBuilder prefStrBuilder = new StringBuilder("Preferences:\n");
    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
    Map<String, ?> allPrefs = prefs.getAll();
    for (Entry<String, ?> entry : allPrefs.entrySet()) {
      String key = entry.getKey();
      if (excludedKeys.contains(key)) continue;

      prefStrBuilder.append(key);
      prefStrBuilder.append(": ");
      prefStrBuilder.append(entry.getValue());
    }

    return prefStrBuilder.toString();
  }

  private static String readLog(String... tags) throws IOException {
    // Run logcat
    String[] args = new String[] { "logcat", "-d" };
    Process process = Runtime.getRuntime().exec(args);
    BufferedReader bufferedReader =
        new BufferedReader(new InputStreamReader(process.getInputStream()));

    // Get its output
    StringBuilder log = new StringBuilder();
    String line;
    while ((line = bufferedReader.readLine()) != null) {
      if (tags.length > 0) {
        Matcher tagMatcher = TAG_PATTERN.matcher(line);
        if (!tagMatcher.matches()) {
          // Logging here could cause an infinite logging loop
          continue;
        }
  
        // For each line, see if the tag matches what we're looking for
        String tagName = tagMatcher.group(1);
        for (String tag : tags) {
          if (tagName.matches(tag)) {
            log.append(line);
            log.append("\n");
            break;
          }
        }
      } else {
        log.append(line);
        log.append("\n");
      }
    }
    return log.toString();
  }
}
