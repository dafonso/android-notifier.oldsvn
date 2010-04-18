package org.damazio.notifier;

import android.os.Build;

/**
 * Global constants for the application.
 *
 * @author rdamazio
 */
public class NotifierConstants {

  /**
   * The tag to use for all logging done in the application.
   */
  public static final String LOG_TAG = "RemoteNotifier";

  /**
   * The current Android API level.
   * This is available in {@link Build.VERSION#SDK_INT}, but only from
   * API level 4, and we want to support level 3 as well.
   */
  public static final int ANDROID_SDK_INT = Integer.parseInt(Build.VERSION.SDK);
}
