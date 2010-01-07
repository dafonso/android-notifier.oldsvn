#import <Cocoa/Cocoa.h>

@class ActionDispatcher;
@class Growl;
@class NotificationManager;
@class Preferences;

// Main controller for the app's UI.
@interface MainController : NSObject {
 @private
  IBOutlet NSMenu *menu;
  IBOutlet NSWindow *aboutDialog;

  IBOutlet NotificationManager *notificationManager;

  NSStatusItem *statusItem;
}

// Open the about window.
- (IBAction)showAboutDialog:(id)sender;

// Quit the application.
- (IBAction)quit:(id)sender;

@end
