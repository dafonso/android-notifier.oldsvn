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

import org.damazio.notifier.NotifierConstants;
import org.damazio.notifier.NotifierPreferences;
import org.damazio.notifier.command.CommandProtocol.CommandRequest;
import org.damazio.notifier.command.CommandProtocol.CommandRequest.SmsOptions;
import org.damazio.notifier.command.CommandProtocol.CommandResponse.Builder;

import android.telephony.SmsManager;
import android.util.Log;

/**
 * Command handler which sends SMSs.
 *
 * @author Rodrigo Damazio
 */
class SmsCommandHandler implements CommandHandler {
  @Override
  public boolean handleCommand(CommandRequest req, Builder responseBuilder) {
    if (!req.hasSmsOptions()) {
      responseBuilder.setErrorMessage("Incomplete command");  // i18n
      Log.e(NotifierConstants.LOG_TAG, "Missing SMS options");
      return false;
    }

    SmsOptions smsOptions = req.getSmsOptions();
    String destination = smsOptions.getPhoneNumber();
    String contents = smsOptions.getSmsMessage();
    if (destination.length() == 0 || contents.length() == 0) {
      responseBuilder.setErrorMessage("Missing number or contents");  // i18n
      Log.e(NotifierConstants.LOG_TAG, "Missing SMS number or contents");
      return false;
    }

    // TODO: Cupcake compatibility
    SmsManager smsManager = SmsManager.getDefault();

    // TODO: Notification when sms is delivered
    smsManager.sendTextMessage(destination, null, contents, null, null);

    return true;
  }

  @Override
  public boolean isEnabled(NotifierPreferences preferences) {
    return preferences.isSmsCommandEnabled();
  }
}
