package org.damazio.notifier.notification;

import org.damazio.notifier.NotifierConstants;
import org.damazio.notifier.notification.NotificationMethod.NotificationCallback;

import android.os.CountDownTimer;
import android.util.Log;

/**
 * This timer is used to enable a notification method, then periodically retry
 * sending the given notification until the method is ready to send it.
 *
 * @param <T> is the target type
 * @author rdamazio
 */
abstract class MethodEnablingNotifier<T> extends CountDownTimer {
  private static final long MAX_MEDIUM_WAIT_TIME_MS = 60000;
  private static final long MEDIUM_CHECK_INTERVAL_MS = 500;

  private final byte[] payload;
  private final boolean previousEnabledState;
  private boolean notificationSent = false;
  private final NotificationMethod method;
  private final NotificationCallback callback;
  private final T target;
  private final boolean isForeground;

  MethodEnablingNotifier(byte[] payload, T target, boolean isForeground,
      NotificationCallback callback, boolean previousEnabledState, NotificationMethod method) {
    super(MAX_MEDIUM_WAIT_TIME_MS, MEDIUM_CHECK_INTERVAL_MS);

    this.payload = payload;
    this.target = target;
    this.isForeground = isForeground;
    this.callback = callback;
    this.previousEnabledState = previousEnabledState;
    this.method = method;

    acquireLock();
    setMediumEnabled(true);
  }

  @Override
  public void onTick(long millisUntilFinished) {
    if (!notificationSent && isMediumReady()) {
      Log.d(NotifierConstants.LOG_TAG, "Method " + method.getName()
          + " connected, sending delayed notification after "
          + (MAX_MEDIUM_WAIT_TIME_MS - millisUntilFinished) + "ms");

      // Ignore next ticks
      cancel();
      notificationSent = true;

      // Send notification
      method.sendNotification(payload, target, callback, isForeground);

      restorePreviousEnabledState();
    }
  }

  @Override
  public void onFinish() {
    // Give it one last chance
    onTick(0);

    if (!notificationSent) {
      Log.e(NotifierConstants.LOG_TAG, "Timed out while waiting for medium to connect");
      callback.notificationDone(target, null);
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
