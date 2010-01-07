//
//  Preferences.m
//  MacDroidNotifier
//
//  Created by Rodrigo Damazio on 27/12/09.
//

#import "Preferences.h"
#import "Notification.h"

NSString *const kPreferencesPairedDevicesKey = @"pairedDevices";
NSString *const kPreferencesPairingRequiredKey = @"pairingRequired";
NSString *const kPreferencesListenWifiKey = @"listenWifi";
NSString *const kPreferencesListenBluetoothKey = @"listenBluetooth";
NSString *const kPreferencesListenUsbKey = @"listenUsb";

NSString *const kPreferencesRingKey = @"ring";
NSString *const kPreferencesSmsKey = @"sms";
NSString *const kPreferencesMmsKey = @"mms";
NSString *const kPreferencesBatteryKey = @"battery";
NSString *const kPreferencesPingKey = @"ping";

NSString *const kPreferencesDisplayKey = @"display";
NSString *const kPreferencesMuteKey = @"mute";
NSString *const kPreferencesExecuteKey = @"execute";

const int kPairingNotRequired = 0;
const int kPairingRequired = 1;

@implementation Preferences

+ (void)setBool:(BOOL)value
     forTypeKey:(NSString *)typeKey
   forActionKey:(NSString *)actionKey
   inDictionary:(NSMutableDictionary *)dict {
  NSString *key = [NSString stringWithFormat:@"%@.%@", typeKey, actionKey];
  [dict setObject:[NSNumber numberWithBool:value] forKey:key];
}

+ (void)initialize {
  NSAutoreleasePool *pool = [[NSAutoreleasePool alloc] init];

  NSMutableDictionary *settings =
      [NSMutableDictionary dictionaryWithObjectsAndKeys:
          [NSArray array], kPreferencesPairedDevicesKey,
          [NSNumber numberWithInt:kPairingNotRequired], kPreferencesPairingRequiredKey,
          [NSNumber numberWithBool:YES], kPreferencesListenWifiKey,
          [NSNumber numberWithBool:YES], kPreferencesListenBluetoothKey,
          [NSNumber numberWithBool:NO],  kPreferencesListenUsbKey,
          nil];

  // Set defaults for actions
  [self setBool:YES forTypeKey:kPreferencesRingKey    forActionKey:kPreferencesDisplayKey inDictionary:settings];
  [self setBool:YES forTypeKey:kPreferencesRingKey    forActionKey:kPreferencesMuteKey    inDictionary:settings];
  [self setBool:YES forTypeKey:kPreferencesSmsKey     forActionKey:kPreferencesDisplayKey inDictionary:settings];
  [self setBool:YES forTypeKey:kPreferencesMmsKey     forActionKey:kPreferencesDisplayKey inDictionary:settings];
  [self setBool:YES forTypeKey:kPreferencesBatteryKey forActionKey:kPreferencesDisplayKey inDictionary:settings];
  [self setBool:YES forTypeKey:kPreferencesPingKey    forActionKey:kPreferencesDisplayKey inDictionary:settings];

  [[NSUserDefaults standardUserDefaults] registerDefaults:settings];
  [[NSUserDefaults standardUserDefaults] synchronize];
  [pool release];
}

- (id)init {
  if (self = [super init]) {
    isPairing = NO;
  }
  return self;
}

- (void)updatePairingUI {
  int selectedTag = [pairingRadioGroup selectedTag];
  BOOL enablePairingUI = (selectedTag == kPairingRequired) ? YES : NO;

  [pairedDevicesView setEnabled:enablePairingUI];
  [addPairedDeviceButton setEnabled:enablePairingUI];
  [removePairedDeviceButton setEnabled:enablePairingUI];
}

- (void)showDialog:(id)sender {
  [NSApp activateIgnoringOtherApps:YES];
  [prefsWindow makeKeyAndOrderFront:sender];
  [self updatePairingUI];
}

- (void)windowWillClose:(NSNotification *)note {
  if ([note object] == prefsWindow) {
    [prefsWindow endEditingFor:nil];

    // Save the updated preferences
    NSLog(@"Saving preferences");
    NSUserDefaults *ud = [NSUserDefaults standardUserDefaults];
    [ud synchronize];
  }
}

- (IBAction)pairingRequiredToggled:(id)sender {
  [self updatePairingUI];
}

- (IBAction)addPairedDeviceClicked:(id)sender {
  // Save notification settings since we'll now listen for notifications
  [[NSUserDefaults standardUserDefaults] synchronize];

  isPairing = YES;
  [NSApp beginSheet:pairingSheet
     modalForWindow:prefsWindow
      modalDelegate:self
     didEndSelector:@selector(didEndPairing:returnCode:contextInfo:)
        contextInfo:0];
}

- (void)handlePairingNotification:(Notification *)notification {
  // Only let through if we're in pairing mode
  if (!isPairing) {
    return;
  }

  // Only let ping notifications through
  if ([notification type] != PING) {
    return;
  }

  @synchronized(self) {
    // Ensure the device is not already in the paired devices list
    NSString *deviceId = [notification deviceId];
    for (id pairedDeviceId in [pairedDevicesModel arrangedObjects]) {
      NSDictionary *pairedDevice = pairedDeviceId;
      if ([[pairedDevice objectForKey:@"deviceId"] isEqualToString:deviceId]) {
        NSLog(@"Tried to pair with device %@ - already paired.", deviceId);
        return;
      }
    }

    NSDictionary *newDevice =
        [NSDictionary dictionaryWithObjectsAndKeys:deviceId, @"deviceId",
                                              @"New device", @"deviceName",
                                              nil];
    [pairedDevicesModel addObject:newDevice];
  }
  [NSApp endSheet:pairingSheet];
}

- (IBAction)cancelPairing:(id)sender {
  [NSApp endSheet:pairingSheet];
}

- (void)didEndPairing:(NSWindow *)sheet
          returnCode:(int)success
          contextInfo:(void *)rowInfo {
  [sheet orderOut:self];
  isPairing = NO;
}

- (IBAction)selectExecuteAction:(id)sender {
  // TODO(rdamazio): Implement
}

@end
