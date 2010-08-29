//
//  CommandSender.h
//  MacDroidNotifier
//
//  Created by Rodrigo Damazio Bovendorp on 8/29/10.
//

#import <Cocoa/Cocoa.h>

@class Command;

@protocol CommandResponseListener
  // TODO
@end

@protocol CommandSender

- (BOOL)isEnabled;
- (void)sendCommand:(Command *)cmd;

@end
