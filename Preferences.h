//
//  Preferences.h
//  MacDroidNotifier
//
//  Created by Rodrigo Damazio on 27/12/09.
//

#import <Cocoa/Cocoa.h>

// Constants for preference keys.
extern NSString *const kPreferencesPairedDevicesKey;
extern NSString *const kPreferencesPairingRequiredKey;
extern NSString *const kPreferencesListenWifiKey;
extern NSString *const kPreferencesListenBluetoothKey;
extern NSString *const kPreferencesListenUsbKey;
extern NSString *const kPreferencesRingKey;
extern NSString *const kPreferencesSmsKey;
extern NSString *const kPreferencesMmsKey;
extern NSString *const kPreferencesBatteryKey;
extern NSString *const kPreferencesPingKey;
extern NSString *const kPreferencesDisplayKey;
extern NSString *const kPreferencesMuteKey;
extern NSString *const kPreferencesExecuteKey;

// Constants for preference values.
extern const NSInteger kPairingNotRequired;
extern const NSInteger kPairingRequired;

@class Notification;

// Object which wrapps handling of the app's preferences, including its UI.
@interface Preferences : NSObject {
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
- (IBAction)showDialog:(id)sender;

// Callback for when the "pairing required" option is changed
- (IBAction)pairingRequiredToggled:(id)sender;

// Callback when the user wants to add a paired device
- (IBAction)addPairedDeviceClicked:(id)sender;

// Callback when a pairing notification is received, even if not currently in
// pairing mode.
- (void)handlePairingNotification:(Notification *)notification;

// Callback for when the user cancels pairing
- (IBAction)cancelPairing:(id)sender;

// Callback to select what to execute
- (IBAction)selectExecuteAction:(id)sender;

@end
