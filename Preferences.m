//
//  Preferences.m
//  MacDroidNotifier
//
//  Created by Rodrigo Damazio on 27/12/09.
//  Copyright 2009 Google Inc. All rights reserved.
//

#import "Preferences.h"

NSString *const kPairedWifiDevicesKey = @"pairedDevices";
NSString *const kListenWifiKey = @"listenWifi";
NSString *const kListenBluetoothKey = @"listenBluetooth";
NSString *const kListenUsbKey = @"listenUsb";
NSString *const kDisplayRingKey = @"displayRing";
NSString *const kDisplaySmsKey = @"displaySms";
NSString *const kDisplayMmsKey = @"displayMms";
NSString *const kDisplayBatteryKey = @"displayBattery";

@implementation Preferences

+ (void)load {
  NSAutoreleasePool *pool = [[NSAutoreleasePool alloc] init];
  NSDictionary *settings =
      [NSDictionary dictionaryWithObjectsAndKeys:
          [NSArray array], kPairedWifiDevicesKey,
          [NSNumber numberWithBool:YES], kListenWifiKey,
          [NSNumber numberWithBool:YES], kListenBluetoothKey,
          [NSNumber numberWithBool:NO], kListenUsbKey,
          [NSNumber numberWithBool:YES], kDisplayRingKey,
          [NSNumber numberWithBool:YES], kDisplaySmsKey,
          [NSNumber numberWithBool:YES], kDisplayMmsKey,
          [NSNumber numberWithBool:YES], kDisplayBatteryKey,
          nil];
  [[NSUserDefaults standardUserDefaults] registerDefaults:settings];
  [[NSUserDefaults standardUserDefaults] synchronize];
  [pool release];
}

- (void)showDialog:(id)sender {
  [NSApp activateIgnoringOtherApps:YES];
  [prefsWindow makeKeyAndOrderFront:sender];
}

- (void)windowWillClose:(NSNotification *)note {
  if ([note object] == prefsWindow) {
    [prefsWindow endEditingFor:nil];

    // Save the updated preferences
    NSLog(@"Saving preferences");
    NSUserDefaults *ud = [NSUserDefaults standardUserDefaults];
//    [ud setObject:[moduleController_ modules]
//           forKey:kPreferencesModuleArrayKey];
    [ud synchronize];
  }
}

@end
