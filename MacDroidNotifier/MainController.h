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

// Open the preferences window.
- (IBAction)openPreferences:(id)sender;

// Open the about window.
- (IBAction)showAboutDialog:(id)sender;

// Quit the application.
- (IBAction)quit:(id)sender;

@end
