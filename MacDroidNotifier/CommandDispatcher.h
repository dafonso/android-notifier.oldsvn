//
//  CommandDispatcher.h
//  MacDroidNotifier
//
//  Created by Rodrigo Damazio on 05/08/10.
//

#import <Cocoa/Cocoa.h>

@class Command;

@interface CommandDispatcher : NSObject {
 @private
  NSArray *senders;
}

- (void)dispatchCommand:(Command *)cmd;

@end
