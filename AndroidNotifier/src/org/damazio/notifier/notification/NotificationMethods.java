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
package org.damazio.notifier.notification;

import java.util.HashSet;
import java.util.Set;

import org.damazio.notifier.NotifierPreferences;

import android.content.Context;

/**
 * Factory for notification methods.
 *
 * @author rdamazio
 */
class NotificationMethods {
  private NotificationMethods() { }

  /**
   * Create and return a set of all valid notification methods for the current
   * environment.
   *
   * @param context the context to get information from
   * @param preferences the preferences for the methods to use
   * @return the set of notification methods
   */
  public static Set<NotificationMethod> getAllValidMethods(
      Context context, NotifierPreferences preferences) {
    HashSet<NotificationMethod> methods = new HashSet<NotificationMethod>();

    // Methods supported in all versions
    methods.add(new IpNotificationMethod(context, preferences));
    methods.add(new UsbNotificationMethod());

    // Methods supported only in 2.0 and above
    if (BluetoothDeviceUtils.isBluetoothMethodSupported()) {
      methods.add(new BluetoothNotificationMethod(context, preferences));
    }

    return methods;
  }
}
