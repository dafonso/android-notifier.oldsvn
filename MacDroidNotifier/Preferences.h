//
//  Preferences.h
//  MacDroidNotifier
//
//  Created by Rodrigo Damazio on 27/12/09.
//  Copyright 2009 Google Inc. All rights reserved.
//

#import <Cocoa/Cocoa.h>

#import "NotificationManager.h"

// Constants for preference keys.
extern NSString *const kPreferencesPairedDevicesKey;
extern NSString *const kPreferencesPairingRequiredKey;
extern NSString *const kPreferencesListenWifiKey;
extern NSString *const kPreferencesListenBluetoothKey;
extern NSString *const kPreferencesListenUsbKey;
extern NSString *const kPreferencesDisplayRingKey;
extern NSString *const kPreferencesDisplaySmsKey;
extern NSString *const kPreferencesDisplayMmsKey;
extern NSString *const kPreferencesDisplayBatteryKey;

// Constants for preference values.
extern const int kPairingNotRequired;
extern const int kPairingRequired;

// Object which wrapps handling of the app's preferences, including its UI.
@interface Preferences : NSObject<NotificationCallback> {
 @private
  IBOutlet NSWindow *prefsWindow;
  IBOutlet NSWindow *pairingSheet;
  IBOutlet NSMatrix *pairingRadioGroup;
  IBOutlet NSTableView *pairedDevicesView;
  IBOutlet NSArrayController *pairedDevicesModel;
  IBOutlet NSButton *addPairedDeviceButton;
  IBOutlet NSButton *removePairedDeviceButton;

  BOOL isPairing;
}

// Shows the preferences dialog.
- (void)showDialog:(id)sender;

// Callback for when the "pairing required" option is changed
- (IBAction)pairingRequiredToggled:(id)sender;

// Callback when the user wants to add a paired device
- (IBAction)addPairedDeviceClicked:(id)sender;

// Callback for when the user cancels pairing
- (IBAction)cancelPairing:(id)sender;

@end
