//
//  Growl.h
//  MacDroidNotifier
//
//  Created by Rodrigo Damazio on 26/12/09.
//

#import <Cocoa/Cocoa.h>
#import <Growl/Growl.h>
#import "Notification.h"

// Abstraction interface for displaying notifications on Growl.
@interface Growl : NSObject<GrowlApplicationBridgeDelegate> {
  @private
   BOOL growlNotRunningNotified;
}

- (void)postGrowlNotification:(Notification *)notification;

@end
