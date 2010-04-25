//
//  WifiNotificationListener.h
//  MacDroidNotifier
//
//  Created by Rodrigo Damazio on 25/04/10.
//

#import <Cocoa/Cocoa.h>
#import "NotificationListener.h"

@class AsyncSocket;

// Listener which receives notifications over Wifi using TCP.
@interface TcpNotificationListener : NSObject<NotificationListener> {
@private
  NSObject<NotificationListenerCallback> *callback;
  AsyncSocket *socket;
}

@end
