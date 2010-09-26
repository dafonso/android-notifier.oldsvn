//
//  CommandController.m
//  MacDroidNotifier
//
//  Copyright 2010 Rodrigo Damazio <rodrigo@damazio.org>
//
//  Redistribution and use in source and binary forms, with or without
//  modification, are permitted provided that the following conditions
//  are met:
//
//  1. Redistributions of source code must retain the above copyright
//     notice, this list of conditions and the following disclaimer.
//  2. Redistributions in binary form must reproduce the above copyright
//     notice, this list of conditions and the following disclaimer in the
//     documentation and/or other materials provided with the distribution.
//
//  THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR
//  IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
//  OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
//  IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT,
//  INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
//  NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
//  DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
//  THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
//  (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
//  THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.

#import "CommandController.h"

#import "Command.h"
#import "CommandDispatcher.h"

@implementation CommandController

- (IBAction)showStartCallDialog:(id)sender {
  NSMenuItem *item = sender;
  lastDialDeviceId = [item representedObject];
  [NSApp activateIgnoringOtherApps:YES];
  [dialWindow makeKeyAndOrderFront:sender];
}

- (IBAction)showSendSmsDialog:(id)sender {
  NSMenuItem *item = sender;
  lastSmsDeviceId = [item representedObject];

  [NSApp activateIgnoringOtherApps:YES];
  [sendSmsWindow makeKeyAndOrderFront:sender];
}

- (IBAction)startCall:(id)sender {
  NSString *number = [dialNumber stringValue];
  [dialWindow close];

  Command *cmd = [Command commandWithDeviceId:lastDialDeviceId
                                         type:DIAL
                                        data1:number
                                        data2:nil];
  [dispatcher dispatchCommand:cmd];
}

- (IBAction)hangUp:(id)sender {
  NSMenuItem *item = sender;
  NSString *deviceId = [item representedObject];

  Command *cmd = [Command commandWithDeviceId:deviceId
                                         type:HANGUP
                                        data1:nil
                                        data2:nil];
  [dispatcher dispatchCommand:cmd];
}

- (IBAction)sendSms:(id)sender {
  NSString *number = [sendSmsNumber stringValue];
  NSString *contents = [sendSmsContents stringValue];
  [sendSmsWindow close];

  Command *cmd = [Command commandWithDeviceId:lastSmsDeviceId
                                         type:SEND_SMS
                                        data1:number
                                        data2:contents];
  [dispatcher dispatchCommand:cmd];
}

@end
