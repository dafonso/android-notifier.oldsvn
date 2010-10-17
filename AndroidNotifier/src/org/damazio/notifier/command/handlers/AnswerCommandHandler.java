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

import org.damazio.notifier.NotifierPreferences;
import org.damazio.notifier.command.CommandProtocol.CommandRequest;
import org.damazio.notifier.command.CommandProtocol.CommandResponse.Builder;

import android.content.Context;
import android.content.Intent;

/**
 * Command handler which answers phone calls.
 *
 * @author Rodrigo Damazio
 */
class AnswerCommandHandler implements CommandHandler {

  private final Context context;

  public AnswerCommandHandler(Context context) {
    this.context = context;
  }

  @Override
  public boolean handleCommand(CommandRequest req, Builder responseBuilder) {
    context.startActivity(new Intent(Intent.ACTION_ANSWER));
    return true;
  }

  @Override
  public boolean isEnabled(NotifierPreferences preferences) {
    return preferences.isCallCommandEnabled();
  }
}
