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
package org.damazio.notifier.command.handlers;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.damazio.notifier.NotifierConstants;
import org.damazio.notifier.NotifierPreferences;
import org.damazio.notifier.command.CommandProtocol.CommandRequest;
import org.damazio.notifier.command.CommandProtocol.CommandResponse;

import android.content.Context;
import android.os.IBinder;
import android.util.Log;

/**
 * Command handler which hangs up phone calls.
 *
 * @author Rodrigo Damazio
 */
class HangupCommandHandler implements CommandHandler {

  private Object telephonyService;
  private Method endCallMethod;

  public HangupCommandHandler(Context context) {
    try {
      // Obtain the real telephony service
      // TODO: Vomit all over this code
      Class<?> serviceManager = Class.forName("android.os.ServiceManager");
      Method getServiceMethod = serviceManager.getMethod("getService", String.class);
      Object telephonyBinder = getServiceMethod.invoke(serviceManager, Context.TELEPHONY_SERVICE);
      Class<?> iTelephonyClass = Class.forName("com.android.internal.telephony.ITelephony.Stub");
      Method asInterfaceMethod = iTelephonyClass.getMethod("asInterface", IBinder.class);
      telephonyService = asInterfaceMethod.invoke(iTelephonyClass, telephonyBinder);
      endCallMethod = telephonyService.getClass().getMethod("endCall");
    } catch (InvocationTargetException e) {
      Log.e(NotifierConstants.LOG_TAG, "Failed to call hangup method", e);
    } catch (ClassNotFoundException e) {
      Log.e(NotifierConstants.LOG_TAG, "Failed to call hangup method", e);
    } catch (SecurityException e) {
      Log.e(NotifierConstants.LOG_TAG, "Failed to call hangup method", e);
    } catch (NoSuchMethodException e) {
      Log.e(NotifierConstants.LOG_TAG, "Failed to call hangup method", e);
    } catch (IllegalArgumentException e) {
      Log.e(NotifierConstants.LOG_TAG, "Failed to call hangup method", e);
    } catch (IllegalAccessException e) {
      Log.e(NotifierConstants.LOG_TAG, "Failed to call hangup method", e);
    }
  }

  @Override
  public boolean handleCommand(CommandRequest req, CommandResponse.Builder respBuilder) {
    if (endCallMethod == null || telephonyService == null) {
      return false;
    }

    try {
      boolean hungUp = (Boolean) endCallMethod.invoke(telephonyService);
      if (hungUp) {
        return true;
      }

      Log.w(NotifierConstants.LOG_TAG, "Hangup failed");
      respBuilder.setErrorMessage("Hangup failed");  // TODO: i18n
    } catch (IllegalArgumentException e) {
      Log.w(NotifierConstants.LOG_TAG, "Hangup failed", e);
      respBuilder.setErrorMessage("Hangup failed: bad argument");  // TODO: i18n
    } catch (IllegalAccessException e) {
      Log.w(NotifierConstants.LOG_TAG, "Hangup failed", e);
      respBuilder.setErrorMessage("Hangup failed: bad access");  // TODO: i18n
    } catch (InvocationTargetException e) {
      Log.w(NotifierConstants.LOG_TAG, "Hangup failed", e);
      respBuilder.setErrorMessage("Hangup failed: bad call");  // TODO: i18n
    }

    // If we got here, something failed
    return false;
  }

  @Override
  public boolean isEnabled(NotifierPreferences preferences) {
    return preferences.isCallCommandEnabled();
  }
}
