package org.damazio.notifier.locale.notify;

/**
 * Constants used by the notification plugin.
 *
 * @author rdamazio
 */
class Constants {
  static final String LOG_TAG = "AndroidNotifierLocale";
  static final String BROADCAST_ACTION = "org.damazio.notifier.service.UserReceiver.USER_MESSAGE";
  static final String EXTRA_TITLE = "title";
  static final String EXTRA_DESCRIPTION = "description";

  private Constants() {}
}
