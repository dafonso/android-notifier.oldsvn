#import <Cocoa/Cocoa.h>

@class Growl;
@class NotificationView;
@class NotificationManager;
@class Preferences;

// Main controller for the app's UI.
@interface MainController : NSObject {
 @private
  IBOutlet NSMenu *menu;
  IBOutlet Growl *growl;
  IBOutlet Preferences *preferences;
  IBOutlet NSWindow *aboutDialog;

  NSStatusItem *statusItem;
  NotificationView *notificationView;
  NotificationManager *notificationManager;
}

- (IBAction)openPreferences:(id)sender;
- (IBAction)showAboutDialog:(id)sender;
- (IBAction)quit:(id)sender;

@end
