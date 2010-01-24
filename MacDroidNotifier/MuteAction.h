//
//  MuteAction.h
//  MacDroidNotifier
//
//  Created by Rodrigo Damazio on 07/01/10.
//

#import <Cocoa/Cocoa.h>
#import "Action.h"

@class SoundVolume;

@interface MuteAction : NSObject<Action> {
 @private
  SoundVolume *volume;
}

@end
