//
//  WifiNotificationListener.m
//  MacDroidNotifier
//
//  Created by Rodrigo Damazio on 25/04/10.
//

#import "TcpNotificationListener.h"

#import "AsyncSocket.h"
#import "Preferences.h"

static const UInt16 kPortNumber = 10600;
static const NSTimeInterval kReadTimeout = 10.0;

@implementation TcpNotificationListener

- (void)startWithCallback:(NSObject<NotificationListenerCallback> *)callbackParam {
  if (socket) {
    NSLog(@"Tried to start service twice");
    return;
  }
  
  callback = [callbackParam retain];
  
  NSLog(@"started listening for TCP notifications");
  socket = [[AsyncSocket alloc] initWithDelegate:self];
  
  // Advanced options - enable the socket to contine operations even during modal dialogs, and menu browsing
  [socket setRunLoopModes:[NSArray arrayWithObject:NSRunLoopCommonModes]];

  NSError *err;
  BOOL bound = [socket acceptOnPort:kPortNumber error:&err];
  if (bound != YES) {
    NSLog(@"Error when listening on socket: %@", err);
  }
}

- (void)onSocket:(AsyncSocket *)sock didAcceptNewSocket:(AsyncSocket *)newSocket {
  if ([[NSUserDefaults standardUserDefaults] boolForKey:kPreferencesListenTcpKey]) {
    [newSocket readDataToData:[AsyncSocket ZeroData]
                  withTimeout:kReadTimeout
                          tag:1L];
  } else {
    [newSocket disconnect];
  }
}

- (NSTimeInterval)onSocket:(AsyncSocket *)sock
  shouldTimeoutReadWithTag:(long)tag
                   elapsed:(NSTimeInterval)elapsed
                 bytesDone:(CFIndex)length {
  [sock disconnectAfterReading];
  return 0;
}

- (void)onSocket:(AsyncSocket *)sock didReadData:(NSData *)data withTag:(long)tag {
  [callback handleRawNotification:data];
}

- (void)stop {
  if (!socket) {
    return;
  }
  
  [socket disconnect];
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
