//
//  NotificationManager.h
//  MacDroidNotifier
//
//  Created by Rodrigo Damazio on 25/12/09.
//

#import <Cocoa/Cocoa.h>
#import "NotificationListener.h"

@class ActionDispatcher;
@class Notification;
@class Preferences;

// Notification manager, which receives raw notifications from the listeners,
// decodes them, and forwards them to the given notification callback.
@interface NotificationManager : NSObject<NotificationListenerCallback> {
 @private
  // Dispatcher which takes action on notifications
  IBOutlet ActionDispatcher *dispatcher;

  // Preferences, for pairing with new devices
  IBOutlet Preferences *preferences;

  // Listeners which receive notifications and pass them to this object
  NSArray *listeners;

  // Last few received notifications, for eliminating duplicates
  NSMutableArray *lastNotifications;

  // Total count of received notifications
  NSUInteger notificationCount;
}

@end
