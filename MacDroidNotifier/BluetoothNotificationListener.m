//
//  BluetoothNotificationListener.m
//  MacDroidNotifier
//
//  Created by Rodrigo Damazio on 25/12/09.
//

#import "BluetoothNotificationListener.h"
#import "Preferences.h"

#import <IOBluetooth/objc/IOBluetoothDevice.h>

@interface BluetoothNotificationListener (Private) 
- (void)rfcommChannelData:(IOBluetoothRFCOMMChannel*)rfcommChannel data:(void *)dataPointer length:(size_t)dataLength;
- (void)rfcommChannelClosed:(IOBluetoothRFCOMMChannel*)rfcommChannelParam;
- (void) newRFCOMMChannelOpened:(IOBluetoothUserNotification *)inNotification
                        channel:(IOBluetoothRFCOMMChannel *)newChannel;
- (void)publishService;
- (void)unpublishService;
- (NSString *)localDeviceName;
@end

@implementation BluetoothNotificationListener

- (void)startWithCallback:(NSObject<NotificationListenerCallback> *)callbackParam {
  [self publishService];
  callback = callbackParam;

}

- (void)stop {
  if (rfcommChannel != nil) {
    IOBluetoothRFCOMMChannel *channel = rfcommChannel;
    IOBluetoothDevice *device = [channel getDevice];

    // And closes the channel:
    [channel closeChannel];

    // We do not need the channel anymore:
    [channel release];
    rfcommChannel = nil;

    // And closes the connection with the device:
    [device closeConnection];
  }

  [self unpublishService];
}

- (void)rfcommChannelData:(IOBluetoothRFCOMMChannel*)rfcommChannel data:(void *)dataPointer length:(size_t)dataLength {
	NSData *data = [NSData dataWithBytes:dataPointer length:dataLength];
  // TODO: Don't even listen for rfcomm if disabled
  if ([[NSUserDefaults standardUserDefaults] boolForKey:kListenWifiKey]) {
    [callback handleRawNotification:data];
  }
}

- (void)rfcommChannelClosed:(IOBluetoothRFCOMMChannel*)rfcommChannelParam {
  rfcommChannel = nil;
  [self publishService];
}

- (void) newRFCOMMChannelOpened:(IOBluetoothUserNotification *)inNotification
                        channel:(IOBluetoothRFCOMMChannel *)newChannel {
  // Make sure the channel is an incoming channel on the right channel ID.
  // This isn't strictly necessary since we only registered a notification for this case,
  // but it can't hurt to double-check.
  if (newChannel != nil && [newChannel isIncoming] && [newChannel getChannelID] == serverChannelID) {
    rfcommChannel = newChannel;

    // Retains the channel
    [rfcommChannel retain];

		// Set self as the channel's delegate: THIS IS THE VERY FIRST THING TO DO FOR A SERVER !!!!
		if ([rfcommChannel setDelegate:self] == kIOReturnSuccess) {
			// stop providing the services (this example only handles one chat connection at a time - but
			// there's no reason a well written app can't handle any number of connections)
			[self unpublishService];

			// And notify our UI client that we have a new chat connection:
//			[mConnectionTarget performSelector:mHandleRemoteConnectionSelector];
		} else {
			// The setDelegate: call failed. This is catastrophic for a server
			// Releases the channel:
			[rfcommChannel release];
			rfcommChannel = nil;
		}
  }
}

- (void)publishService {
  // Builds a string with the service name we wish to offer
  NSString *serviceName = [NSString stringWithFormat:@"%@ AndroidNotifierService", [self localDeviceName]];

  // Get the path for the dictonary we wish to publish.
  NSString *dictionaryPath = [[NSBundle mainBundle] pathForResource:@"BluetoothService" ofType:@"plist"];

  if (dictionaryPath != nil && serviceName != nil) {
    // Loads the dictionary from the path:
    NSMutableDictionary	*sdpEntries = [NSMutableDictionary dictionaryWithContentsOfFile:dictionaryPath];

    if (sdpEntries != nil) {
      IOBluetoothSDPServiceRecordRef serviceRecordRef;

      [sdpEntries setObject:serviceName forKey:@"0100 - ServiceName*"];

      // Add SDP dictionary, the rfcomm channel assigned to this service comes back in mServerChannelID.
      // While mServerHandle is going to be the record handle assigned to this service.
      if (IOBluetoothAddServiceDict((CFDictionaryRef) sdpEntries, &serviceRecordRef) == kIOReturnSuccess) {
        IOBluetoothSDPServiceRecord *serviceRecord =
            [IOBluetoothSDPServiceRecord withSDPServiceRecordRef:serviceRecordRef];

        [serviceRecord getRFCOMMChannelID:&serverChannelID];
        [serviceRecord getServiceRecordHandle:&serverHandle];

				IOBluetoothObjectRelease(serviceRecordRef);

        // Register a notification so we get notified when an incoming RFCOMM channel is opened
				// to the channel assigned to our chat service.
        incomingChannelNotification = [IOBluetoothRFCOMMChannel registerForChannelOpenNotifications:self selector:@selector(newRFCOMMChannelOpened:channel:) withChannelID:serverChannelID direction:kIOBluetoothUserNotificationChannelDirectionIncoming];

        NSLog(@"Started listening for Bluetooth notifications");
      } else {
        NSLog(@"Couldn't register bluetooth service dictionary");
      }
    }
  }
}

- (void)unpublishService {
  if (serverHandle != 0){
    // Removes the service:
    IOBluetoothRemoveServiceWithRecordHandle(serverHandle);
  }

  // Unregisters the notification:
  if (incomingChannelNotification != nil) {
    [incomingChannelNotification unregister];
		incomingChannelNotification = nil;
	}

	serverChannelID = 0;
  
  NSLog(@"No longer listening on Bluetooth");
}

// Returns the name of the local bluetooth device
- (NSString *)localDeviceName {
  BluetoothDeviceName localDeviceName;
  
  if (IOBluetoothLocalDeviceReadName( localDeviceName, NULL, NULL, NULL ) == kIOReturnSuccess) {
    return [NSString stringWithUTF8String:(const char*)localDeviceName];
  }
  
  return nil;
}

@end
