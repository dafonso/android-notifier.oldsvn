#import <Cocoa/Cocoa.h>

@class Growl;
@class NotificationView;
@class NotificationManager;
@class Preferences;

@interface MainController : NSObject {
 @private
  IBOutlet NSMenu *menu;
  IBOutlet Growl *growl;
  IBOutlet Preferences *preferences;

  NSStatusItem *statusItem;
  NotificationView *notificationView;
  NotificationManager *notificationManager;
}

- (IBAction)openPreferences:(id)sender;
- (IBAction)quit:(id)sender;

@end
