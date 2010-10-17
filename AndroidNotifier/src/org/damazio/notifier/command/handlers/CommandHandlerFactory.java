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

import org.damazio.notifier.command.CommandProtocol.CommandRequest.CommandType;

import android.content.Context;

/**
 * Factory for command handlers.
 *
 * @author Rodrigo Damazio
 */
public class CommandHandlerFactory {

  private final Context context;

  public CommandHandlerFactory(Context context) {
    this.context = context;
  }

  /**
   * Creates and returns a command handler for the given command type.
   *
   * @param commandType the command type
   * @return the handler, or null if unable to handle the type
   */
  public CommandHandler createHandlerFor(CommandType commandType) {
    switch (commandType) {
      case CALL:
        return new CallCommandHandler(context);
      case ANSWER:
        return new AnswerCommandHandler(context);
      case HANG_UP:
        return new HangupCommandHandler(context);
      case SEND_SMS:
        return new SmsCommandHandler();
      case DISCOVER:
        return new DiscoveryCommandHandler(context);
      // TODO: Other types
      default:
        return null;
    }
  }
}
