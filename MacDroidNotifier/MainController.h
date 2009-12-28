#import <Cocoa/Cocoa.h>

@class Growl;
@class NotificationView;
@class NotificationManager;

@interface MainController : NSObject {
 @private
  IBOutlet NSMenu *menu;
  IBOutlet Growl *growl;
  IBOutlet NSWindow *prefsWindow;

  NSStatusItem *statusItem;
  NotificationView *notificationView;
  NotificationManager *notificationManager;
}

- (IBAction)openPreferences:(id)sender;
- (IBAction)quit:(id)sender;

@end
