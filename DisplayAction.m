//
//  DisplayAction.m
//  MacDroidNotifier
//
//  Created by Rodrigo Damazio on 07/01/10.
//

#import "DisplayAction.h"

#import "Growl.h"

@implementation DisplayAction

- (id)initWithGrowl:(Growl *)growlParam {
  if (self = [super init]) {
    growl = [growlParam retain];
  }
  return self;
}

- (void)dealloc {
  [growl release];
  [super dealloc];
}

- (void)executeForNotification:(Notification *)notification {
  NSLog(@"Displaying with Growl");
  [growl postGrowlNotification:notification];
}

@end
