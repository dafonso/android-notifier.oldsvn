//
//  NotificationManager.m
//  MacDroidNotifier
//
//  Created by Rodrigo Damazio on 25/12/09.
//

#import "NotificationManager.h"

#import "BluetoothNotificationListener.h"
#import "Notification.h"
#import "Preferences.h"
#import "WifiNotificationListener.h"

const int kLastNotificationCount = 10;

@implementation NotificationManager

- (id)initWithCallback:(NSObject<NotificationCallback> *)callbackParam
   withPairingCallback:(NSObject<NotificationCallback> *)pairingCallbackParam {
  if (self = [super init]) {
    callback = [callbackParam retain];
    pairingCallback = [pairingCallbackParam retain];

    lastNotifications =
        [[NSMutableArray arrayWithCapacity:kLastNotificationCount] retain];
    notificationCount = 0;

    listeners = [[NSArray arrayWithObjects:
        [[[WifiNotificationListener alloc] init] autorelease],
        [[[BluetoothNotificationListener alloc] init] autorelease],
        nil] retain];
    for (id<NotificationListener> listener in listeners) {
      // TODO: Only start if enabled in preferences
      [listener startWithCallback:self];
    }
  }
  return self;
}

- (void)dealloc {
  for (id<NotificationListener> listener in listeners) {
    [listener stop];
  }
  [listeners release];
  [callback release];
  [pairingCallback release];
  
  [super dealloc];
}

- (BOOL)isNotificationTypeEnabled:(NotificationType)type {
  NSUserDefaults *defaults = [NSUserDefaults standardUserDefaults];
  switch (type) {
    case RING:
      return [defaults boolForKey:kPreferencesDisplayRingKey];
    case SMS:
      return [defaults boolForKey:kPreferencesDisplaySmsKey];
    case MMS:
      return [defaults boolForKey:kPreferencesDisplayMmsKey];
    case BATTERY:
      return [defaults boolForKey:kPreferencesDisplayBatteryKey];
    case PING:
      return YES;
    default:
      return NO;
  }
}

- (BOOL)isNotificationDuplicate:(Notification *)notification {
  for (Notification *lastNotification in lastNotifications) {
    if ([notification isEqualToNotification:lastNotification]) {
      return YES;
    }
  }

  if ([lastNotifications count] < kLastNotificationCount) {
    [lastNotifications addObject:notification];
  } else {
    int position = (notificationCount % kLastNotificationCount);
    [lastNotifications replaceObjectAtIndex:position withObject:notification];
  }

  notificationCount++;
  return NO;
}

- (BOOL)isDevicePaired:(NSString *)deviceId {
  NSUserDefaults *defaults = [NSUserDefaults standardUserDefaults];
  int pairingRequiredValue = [defaults integerForKey:kPreferencesPairingRequiredKey];
  BOOL pairingRequired = (pairingRequiredValue == kPairingRequired) ? YES : NO;

  if (!pairingRequired) {
    return YES;
  }

  NSArray *pairedDevices = [defaults arrayForKey:kPreferencesPairedDevicesKey];
  for (NSDictionary *pairedDevice in pairedDevices) {
    NSString *pairedDeviceId = [pairedDevice objectForKey:@"deviceId"];
    if ([deviceId isEqualToString:pairedDeviceId]) {
      return YES;
    }
  }

  return NO;
}

- (void)handleRawNotification:(NSData *)data {
  NSString *notificationStr = [[NSString alloc] initWithBytes:[data bytes]
                                                       length:[data length]
                                                     encoding:NSUTF8StringEncoding];
  Notification *notification = [Notification notificationFromString:notificationStr];
  [notificationStr release];

  @synchronized(self) {
    if ([self isNotificationTypeEnabled:[notification type]] &&
        ![self isNotificationDuplicate:notification] &&
        [self isDevicePaired:[notification deviceId]]) {
      [callback handleNotification:notification];
    }

    if ([notification type] == PING) {
      [pairingCallback handleNotification:notification];
    }
  }
}

@end
