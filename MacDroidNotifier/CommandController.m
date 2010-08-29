//
//  CommandController.m
//  MacDroidNotifier
//
//  Created by Rodrigo Damazio on 06/08/10.
//

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
