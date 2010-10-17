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
package org.damazio.notifier.notification.methods;

import org.damazio.notifier.NotifierConstants;
import org.damazio.notifier.notification.methods.NotificationMethod.NotificationCallback;

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
      try {
        Log.d(NotifierConstants.LOG_TAG, "Method " + method.getName()
            + " connected, sending delayed notification after "
            + (MAX_MEDIUM_WAIT_TIME_MS - millisUntilFinished) + "ms");
  
        // Ignore next ticks
        cancel();
        notificationSent = true;
  
        // Send notification
        method.sendNotification(payload, target, callback, isForeground);
      } finally {
        restorePreviousEnabledState();
      }
    }
  }

  @Override
  public void onFinish() {
    // Give it one last chance
    onTick(0);

    if (!notificationSent) {
      Log.e(NotifierConstants.LOG_TAG, "Timed out while waiting for medium to connect");
      try {
        callback.notificationDone(target, null);
      } finally {
        restorePreviousEnabledState();
      }
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
