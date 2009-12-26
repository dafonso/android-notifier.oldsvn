//
//  WifiNotificationListener.h
//  MacDroidNotifier
//
//  Created by Rodrigo Damazio on 25/12/09.
//

#import <Cocoa/Cocoa.h>
#import "NotificationListener.h"

@class AsyncUdpSocket;

@interface WifiNotificationListener : NSObject<NotificationListener> {
 @private
  NSObject<NotificationListenerCallback> *callback;
  AsyncUdpSocket *socket;
}

@end
