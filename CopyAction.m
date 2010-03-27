//
//  CopyAction.m
//  MacDroidNotifier
//
//  Created by Rodrigo Damazio on 27/03/10.
//

#import "CopyAction.h"

@implementation CopyAction

- (void)executeForNotification:(Notification *)notification {
  NSPasteboard *pasteboard = [NSPasteboard generalPasteboard];
  [pasteboard declareTypes:[NSArray arrayWithObjects:NSStringPboardType, nil]
                     owner:nil];
  [pasteboard setString:[notification description] forType:NSStringPboardType];
}

@end
