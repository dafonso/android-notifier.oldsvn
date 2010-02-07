package org.damazio.notifier.notification;

import org.damazio.notifier.NotifierConstants;

import android.os.CountDownTimer;
import android.util.Log;

/**
 * This timer is used to enable a notification method, then periodically retry
 * sending the given notification until the method is ready to send it.
 *
 * @author rdamazio
 */
abstract class MethodEnablingNotifier extends CountDownTimer {
  private static final long MAX_WIFI_WAIT_TIME_MS = 60000;
  private static final long WIFI_CHECK_INTERVAL_MS = 500;

  private final Notification notification;
  private final boolean previousEnabledState;
  private boolean notificationSent = false;
  private final NotificationMethod method;

  MethodEnablingNotifier(Notification notification, boolean previousEnabledState, NotificationMethod method) {
    super(MAX_WIFI_WAIT_TIME_MS, WIFI_CHECK_INTERVAL_MS);

    this.notification = notification;
    this.previousEnabledState = previousEnabledState;
    this.method = method;

    acquireLock();
    setMediumEnabled(true);
  }

  @Override
  public void onTick(long millisUntilFinished) {
    if (!notificationSent && isMediumReady()) {
      Log.d(NotifierConstants.LOG_TAG, "Wifi connected, sending delayed notification after " + (MAX_WIFI_WAIT_TIME_MS - millisUntilFinished) + "ms");

      // Ignore next ticks
      notificationSent = true;

      // Send notification
      method.sendNotification(notification);

      restorePreviousEnabledState();
    }
  }

  @Override
  public void onFinish() {
    // Give it one last chance
    onTick(0);

    if (!notificationSent) {
      Log.e(NotifierConstants.LOG_TAG, "Timed out while waiting for medium to connect");
      restorePreviousEnabledState();
    }
  }

  private void restorePreviousEnabledState() {
    releaseLock();
    setMediumEnabled(previousEnabledState);
  }

  /**
   * Acquires a lock of the method's medium to ensure it won't be disabled by
   * the power manager while we're retrying.
   */
  protected abstract void acquireLock();

  /**
   * Releases the lock acquired over the method's medium.
   */
  protected abstract void releaseLock();

  /**
   * @return whether the method is ready to send the notification
   */
  protected abstract boolean isMediumReady();

  /**
   * Sets the method medium's enabled state.
   */
  protected abstract void setMediumEnabled(boolean enabled);
}
