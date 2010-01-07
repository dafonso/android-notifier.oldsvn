//
//  ActionDispatcher.h
//  MacDroidNotifier
//
//  Created by Rodrigo Damazio on 07/01/10.
//

#import <Cocoa/Cocoa.h>

@class Growl;
@class Notification;

@interface ActionDispatcher : NSObject {
 @private
  IBOutlet Growl *growl;

  NSDictionary *actions;
}

- (void)actOnNotification:(Notification *)notification;

@end
