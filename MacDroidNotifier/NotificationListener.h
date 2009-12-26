//
//  NotificationListener.h
//  MacDroidNotifier
//
//  Created by Rodrigo Damazio on 25/12/09.
//

#import <Cocoa/Cocoa.h>

@protocol NotificationListenerCallback
- (void)handleRawNotification:(NSData *)data;
@end

@protocol NotificationListener

- (void)startWithCallback:(NSObject<NotificationListenerCallback> *)callback;
- (void)stop;

@end
