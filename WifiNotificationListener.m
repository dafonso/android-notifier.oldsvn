//
//  WifiNotificationListener.m
//  MacDroidNotifier
//
//  Created by Rodrigo Damazio on 25/12/09.
//

#import "WifiNotificationListener.h"

#import "AsyncUdpSocket.h"
#import "Preferences.h"

static const unsigned short kPortNumber = 10600;
static const double kReceiveTimeout = 10.0;

@implementation WifiNotificationListener

- (void)startWithCallback:(NSObject<NotificationListenerCallback> *)callbackParam {
  if (socket) {
    NSLog(@"Tried to start service twice");
    return;
  }

  callback = [callbackParam retain];

  NSLog(@"started listening for WiFi notifications");
  socket = [[AsyncUdpSocket alloc] initWithDelegate:self];

  // Advanced options - enable the socket to contine operations even during modal dialogs, and menu browsing
  [socket setRunLoopModes:[NSArray arrayWithObject:NSRunLoopCommonModes]];

  NSError *err;
  BOOL bound = [socket bindToPort:kPortNumber error:&err];
  if (bound != YES) {
    NSLog(@"Error when binding socket: %@", err);
  }

  [socket receiveWithTimeout:kReceiveTimeout tag:1];
}

- (BOOL)onUdpSocket:(AsyncUdpSocket *)sock
     didReceiveData:(NSData *)data
            withTag:(long)tag
           fromHost:(NSString *)host
               port:(UInt16)port {
  // TODO(rdamazio): Don't even listen on the UDP port if disabled
  if ([[NSUserDefaults standardUserDefaults] boolForKey:kPreferencesListenWifiKey]) {
    [callback handleRawNotification:data];
  }
  return NO;
}

- (void)onUdpSocket:(AsyncUdpSocket *)sock
         didNotReceiveDataWithTag:(long)tag
         dueToError:(NSError *)error {
  // That's ok, keep listening
  [socket receiveWithTimeout:kReceiveTimeout tag:1];
}

- (void)stop {
  if (!socket) {
    return;
  }

  [socket close];
  [socket release];
  socket = nil;
  [callback release];
  callback = nil;
}

- (void)dealloc {
  [self stop];
  [super dealloc];
}

@end
