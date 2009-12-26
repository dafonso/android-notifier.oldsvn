//
//  NotificationView.h
//  MacDroidNotifier
//
//  Created by Rodrigo Damazio on 25/12/09.
//

#import <Cocoa/Cocoa.h>
#import "NotificationManager.h"

@class Growl;

@interface NotificationView : NSObject<NotificationCallback> {
 @private
  Growl *growl;
}

- (id)initWithGrowl:(Growl *)growl;

@end
