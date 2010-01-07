//
//  DisplayAction.h
//  MacDroidNotifier
//
//  Created by Rodrigo Damazio on 07/01/10.
//

#import <Cocoa/Cocoa.h>
#import "Action.h"

@class Growl;

@interface DisplayAction : NSObject<Action> {
 @private
  Growl *growl;
}

- (id)initWithGrowl:(Growl *)growl;

@end
