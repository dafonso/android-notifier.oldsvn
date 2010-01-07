#import <Cocoa/Cocoa.h>

@class ActionDispatcher;
@class Growl;
@class NotificationManager;
@class Preferences;

// Main controller for the app's UI.
@interface MainController : NSObject {
 @private
  IBOutlet NSMenu *menu;
  IBOutlet Preferences *preferences;
  IBOutlet NSWindow *aboutDialog;

  IBOutlet ActionDispatcher *actionDispatcher;
  NSStatusItem *statusItem;
  NotificationManager *notificationManager;
}

// Open the about window.
- (IBAction)showAboutDialog:(id)sender;

// Quit the application.
- (IBAction)quit:(id)sender;

@end
