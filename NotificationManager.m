//
//  NotificationManager.m
//  MacDroidNotifier
//
//  Created by Rodrigo Damazio on 25/12/09.
//

#import "NotificationManager.h"

#import <CommonCrypto/CommonCryptor.h>
#import <CommonCrypto/CommonDigest.h>

#import "ActionDispatcher.h"
#import "BluetoothNotificationListener.h"
#import "Notification.h"
#import "PassPhraseStorage.h"
#import "Preferences.h"
#import "TcpNotificationListener.h"
#import "UdpNotificationListener.h"

const NSUInteger kLastNotificationCount = 10;

@implementation NotificationManager

- (id)init {
  if (self = [super init]) {
    lastNotifications =
        [[NSMutableArray arrayWithCapacity:kLastNotificationCount] retain];
    notificationCount = 0;

    listeners = [[NSArray arrayWithObjects:
                  [[[TcpNotificationListener alloc] init] autorelease],
                  [[[UdpNotificationListener alloc] init] autorelease],
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
  [lastNotifications release];

  [super dealloc];
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
    NSUInteger position = (notificationCount % kLastNotificationCount);
    [lastNotifications replaceObjectAtIndex:position withObject:notification];
  }

  notificationCount++;
  return NO;
}

- (BOOL)isDevicePaired:(NSString *)deviceId {
  NSUserDefaults *defaults = [NSUserDefaults standardUserDefaults];
  NSInteger pairingRequiredValue = [defaults integerForKey:kPreferencesPairingRequiredKey];
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

- (NSData*)decryptNotificationData:(NSData*)encryptedData {
  // Try decrypting it
  NSString* passPhrase = [passPhraseStorage passPhrase];
  if ([passPhrase length] == 0) return nil;

  // Hash the passphrase to get the key
  NSData* keyData = [passPhrase dataUsingEncoding:NSUTF8StringEncoding];
  unsigned char keyHash[CC_MD5_DIGEST_LENGTH];
  CC_MD5([keyData bytes], (CC_LONG) [keyData length], keyHash);

  // Initialization vector, also based on the key
  unsigned char iv[CC_MD5_DIGEST_LENGTH];
  CC_MD5(keyHash, CC_MD5_DIGEST_LENGTH, iv);

  // Prepare output
  size_t resultSize;
  char outBuffer[4096];

  // Decrypt
  CCCryptorStatus ccStatus;
  ccStatus = CCCrypt(kCCDecrypt,
                     kCCAlgorithmAES128,
                     kCCOptionPKCS7Padding,
                     keyHash, CC_MD5_DIGEST_LENGTH,
                     iv,
                     [encryptedData bytes], [encryptedData length],
                     (void*) outBuffer, 4096,
                     &resultSize);
  if (ccStatus != kCCSuccess) {
    NSLog(@"Failed to decrypt: %d", ccStatus);
    return nil;
  }

  return [NSData dataWithBytes:outBuffer length:resultSize];
}

- (BOOL)handlePlainNotificationData:(NSData *)data {
  // Discard the ending marker if present
  NSUInteger len = [data length];
  char* contents = (char *) [data bytes];
  if (contents[len - 1] == 0) len--;

  // Convert to a string
  NSString *notificationStr = [[NSString alloc] initWithBytes:contents
                                                       length:len
                                                     encoding:NSUTF8StringEncoding];
  if (notificationStr == nil) {
    return NO;
  }

  Notification *notification = [Notification notificationFromString:notificationStr];
  [notificationStr release];
  if (notification == nil) {
    return NO;
  }

  NSLog(@"Received notification %@", notification);

  @synchronized(self) {
    // Dispatch for regular handling
    if (![self isNotificationDuplicate:notification] &&
        [self isDevicePaired:[notification deviceId]]) {
      [dispatcher actOnNotification:notification];
    }

    // Dispatch for pairing handling
    if ([notification type] == PING) {
      [preferences handlePairingNotification:notification];
    }
  }
  return YES;
}

- (void)handleRawNotification:(NSData *)data {
  // First try handling the notification directly
  if (![self handlePlainNotificationData:data]) {
    // Didn't work, try decrypting it if enabled
    NSData *decryptedData = [self decryptNotificationData:data];
    if (decryptedData != nil) {
      [self handlePlainNotificationData:decryptedData];
    }
  }
}

@end
