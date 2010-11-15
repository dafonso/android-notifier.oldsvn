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
package org.damazio.notifier.util;

import java.util.ArrayList;

import org.damazio.notifier.NotifierConstants;

import android.telephony.SmsManager;

/**
 * API-level abstraction for sending SMS messages.
 *
 * @author Rodrigo Damazio
 */
public abstract class SmsSender {
  /**
   * Cupcake version of the SMS sender.
   * Uses the deprecated {@link android.telephony.gsm.SmsManager} class.
   */
  @SuppressWarnings("deprecation")
  private static class CupcakeSmsSender extends SmsSender {
    private final android.telephony.gsm.SmsManager manager =
        android.telephony.gsm.SmsManager.getDefault();

    @Override
    public void sendSms(String destination, String contents) {
      ArrayList<String> messageParts = manager.divideMessage(contents);
      if (messageParts.size() > 1) {
        manager.sendMultipartTextMessage(destination, null, messageParts, null, null);
      } else {
        manager.sendTextMessage(destination, null, contents, null, null);
      }
    }
  }

  /**
   * Donut+ version of the SMS sender.
   */
  private static class DonutSmsSender extends SmsSender {
    private final SmsManager manager = SmsManager.getDefault();

    @Override
    public void sendSms(String destination, String contents) {
      ArrayList<String> messageParts = manager.divideMessage(contents);
      if (messageParts.size() > 1) {
        manager.sendMultipartTextMessage(destination, null, messageParts, null, null);
      } else {
        manager.sendTextMessage(destination, null, contents, null, null);
      }
    }
  }

  /**
   * Sends an SMS text message.
   *
   * @param destination the destination phone number
   * @param contents the text message contents
   */
  public abstract void sendSms(String destination, String contents);

  /**
   * Creates an appropriate instance for the current Android version.
   */
  public static SmsSender create() {
    if (NotifierConstants.ANDROID_SDK_INT >= 4) {
      return new DonutSmsSender();
    } else {
      return new CupcakeSmsSender();
    }
  }
}
