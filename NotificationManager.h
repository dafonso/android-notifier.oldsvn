//
//  NotificationManager.h
//  MacDroidNotifier
//
//  Created by Rodrigo Damazio on 25/12/09.
//

#import <Cocoa/Cocoa.h>
#import "NotificationListener.h"

@class Notification;

// The callback which receives notifications from this manager.
@protocol NotificationCallback
- (void)handleNotification:(Notification *)notification;
@end

// Notification manager, which receives raw notifications from the listeners,
// decodes them, and forwards them to the given notification callback.
@interface NotificationManager : NSObject<NotificationListenerCallback> {
 @private
  NSObject<NotificationCallback> *callback;
  NSObject<NotificationCallback> *pairingCallback;
  NSArray *listeners;

  NSMutableArray *lastNotifications;
  int notificationCount;
}

// Initializes the notification manager, with one regular |callback| which is
// called for every notification, and a |pairingCallback| which is called only
// for test notifications, used for pairing.
- (id)initWithCallback:(NSObject<NotificationCallback> *)callback
   withPairingCallback:(NSObject<NotificationCallback> *)pairingCallback;

@end
