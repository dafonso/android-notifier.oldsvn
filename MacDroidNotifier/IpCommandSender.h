//
//  IpCommandSender.h
//  MacDroidNotifier
//
//  Created by Rodrigo Damazio Bovendorp on 11/15/10.
//  Copyright 2010 __MyCompanyName__. All rights reserved.
//

#import <Cocoa/Cocoa.h>

#import "CommandSender.h"

@interface IpCommandSender : NSObject<CommandSender> {
 @private
  // Map of device ID to an array of pending commands
  NSMutableDictionary *pendingCommandMap;
}

@end
