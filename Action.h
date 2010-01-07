//
//  Action.h
//  MacDroidNotifier
//
//  Created by Rodrigo Damazio on 07/01/10.
//

@class Notification;

// Some action that can be taken about a notification.
@protocol Action

// Execute this action for the given notification.
- (void)executeForNotification:(Notification *)notification;

@end