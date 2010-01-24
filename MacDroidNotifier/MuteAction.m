//
//  MuteAction.m
//  MacDroidNotifier
//
//  Created by Rodrigo Damazio on 07/01/10.
//

#import "MuteAction.h"
#import "SoundVolume.h"


@implementation MuteAction

- (id)init {
  if (self = [super init]) {
    volume = [[SoundVolume alloc] init];
  }
  return self;
}

- (void)dealloc {
  [volume release];
  [super dealloc];
}

- (void)executeForNotification:(Notification *)notification {
  [volume setVolume:0.0f];
}

@end
