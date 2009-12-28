//
//  NotificationManager.m
//  MacDroidNotifier
//
//  Created by Rodrigo Damazio on 25/12/09.
//

#import "NotificationManager.h"
#import "Notification.h"
#import "BluetoothNotificationListener.h"
#import "WifiNotificationListener.h"

@implementation NotificationManager

- (id)initWithCallback:(NSObject<NotificationCallback> *)callbackParam {
  if (self = [super init]) {
    callback = [callbackParam retain];

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
  
  [super dealloc];
}

- (void)handleRawNotification:(NSData *)data {
  NSString *notificationStr = [[NSString alloc] initWithBytes:[data bytes]
                                                       length:[data length]
                                                     encoding:NSUTF8StringEncoding];
  Notification *notification = [Notification notificationFromString:notificationStr];
  [notificationStr release];

  [callback handleNotification:notification];
}

@end
