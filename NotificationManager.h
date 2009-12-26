//
//  NotificationManager.h
//  MacDroidNotifier
//
//  Created by Rodrigo Damazio on 25/12/09.
//

#import <Cocoa/Cocoa.h>
#import "NotificationListener.h"

@class Notification;

@protocol NotificationCallback
- (void)handleNotification:(Notification *)notification;
@end

@interface NotificationManager : NSObject<NotificationListenerCallback> {
 @private
  NSObject<NotificationCallback> *callback;
  NSArray *listeners;
}

- (id)initWithCallback:(NSObject<NotificationCallback> *)callback;

@end
