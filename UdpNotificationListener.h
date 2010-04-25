//
//  WifiNotificationListener.h
//  MacDroidNotifier
//
//  Created by Rodrigo Damazio on 25/12/09.
//

#import <Cocoa/Cocoa.h>
#import "NotificationListener.h"

@class AsyncUdpSocket;

// Listener which receives notifications over Wifi using UDP.
@interface UdpNotificationListener : NSObject<NotificationListener> {
 @private
  NSObject<NotificationListenerCallback> *callback;
  AsyncUdpSocket *socket;
}

@end
