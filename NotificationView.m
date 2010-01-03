//
//  NotificationView.m
//  MacDroidNotifier
//
//  Created by Rodrigo Damazio on 25/12/09.
//

#import "NotificationView.h"
#import "Growl.h"

@implementation NotificationView

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

- (void)handleNotification:(Notification *)notification {
  NSLog(@"Handling notification: %@", notification);
  [growl postGrowlNotification:notification];
}

@end
