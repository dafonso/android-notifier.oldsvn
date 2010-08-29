//
//  CommandDispatcher.m
//  MacDroidNotifier
//
//  Created by Rodrigo Damazio on 05/08/10.
//

#import "CommandDispatcher.h"

#import "Command.h"
#import "CommandSender.h"
#import "BluetoothCommandSender.h"

@implementation CommandDispatcher

- (id)init {
  if (self = [super init]) {
    senders = [[NSArray arrayWithObjects:
        [[[BluetoothCommandSender alloc] init] autorelease],
        nil] retain];
  }
  return self;
}

- (void)dealloc {
  [senders release];
  [super dealloc];
}

- (void)dispatchCommand:(Command *)cmd {
  NSLog(@"Dispatching command: %@ (serialized=%@)", cmd, [cmd serialized]);

  for (id<CommandSender> sender in senders) {
    if ([sender isEnabled]) {
      [sender sendCommand:cmd];
    }
  }
}

@end
