#import <Cocoa/Cocoa.h>

@class Growl;
@class NotificationView;
@class NotificationManager;

@interface MainController : NSObject {
 @private
  IBOutlet NSMenu *menu;
  IBOutlet Growl *growl;

  NSStatusItem *statusItem;
  NotificationView *notificationView;
  NotificationManager *notificationManager;
}

@end
