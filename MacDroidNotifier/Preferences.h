//
//  Preferences.h
//  MacDroidNotifier
//
//  Created by Rodrigo Damazio on 27/12/09.
//  Copyright 2009 Google Inc. All rights reserved.
//

#import <Cocoa/Cocoa.h>

extern NSString *const kPairedWifiDevicesKey;
extern NSString *const kListenWifiKey;
extern NSString *const kListenBluetoothKey;
extern NSString *const kListenUsbKey;
extern NSString *const kDisplayRingKey;
extern NSString *const kDisplaySmsKey;
extern NSString *const kDisplayMmsKey;
extern NSString *const kDisplayBatteryKey;

@interface Preferences : NSObject {
 @private
  IBOutlet NSWindow *prefsWindow;
}

- (void)showDialog:(id)sender;

@end
