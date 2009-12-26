//
//  BluetoothNotificationListener.h
//  MacDroidNotifier
//
//  Created by Rodrigo Damazio on 25/12/09.
//

#import <Cocoa/Cocoa.h>
#import "NotificationListener.h"

#import <IOBluetooth/objc/IOBluetoothSDPServiceRecord.h>
#import <IOBluetooth/objc/IOBluetoothRFCOMMChannel.h>

@interface BluetoothNotificationListener : NSObject<NotificationListener> {
 @private
  NSObject<NotificationListenerCallback> *callback;
  BluetoothRFCOMMChannelID serverChannelID;
  BluetoothSDPServiceRecordHandle serverHandle;
  IOBluetoothUserNotification *incomingChannelNotification;
  IOBluetoothRFCOMMChannel *rfcommChannel;
}

@end
