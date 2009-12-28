//
//  NotificationListener.h
//  MacDroidNotifier
//
//  Created by Rodrigo Damazio on 25/12/09.
//

#import <Cocoa/Cocoa.h>

// Listener which handles raw data as received by the notification listeners.
@protocol NotificationListenerCallback
- (void)handleRawNotification:(NSData *)data;
@end

// Listener which receives notifications over some medium.
@protocol NotificationListener
- (void)startWithCallback:(NSObject<NotificationListenerCallback> *)callback;
- (void)stop;
@end
