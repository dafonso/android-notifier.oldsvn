#import "MainController.h"

#import "growl.h"
#import "NotificationManager.h"
#import "NotificationView.h"
#import "Preferences.h"

@implementation MainController

- (NSImage *)prepareImageForMenubar:(NSString *)name
{
	NSImage *img = [NSImage imageNamed:name];
	[img setScalesWhenResized:YES];
	[img setSize:NSMakeSize(18, 18)];
  
	return img;
}

- (void)awakeFromNib {
  // Setup the menubar item
  statusItem = [[[NSStatusBar systemStatusBar] statusItemWithLength:NSVariableStatusItemLength] retain];
  [statusItem setHighlightMode:YES];
  [statusItem setImage:[self prepareImageForMenubar:@"menuicon"]];
  [statusItem setMenu:menu];

  notificationView = [[NotificationView alloc] initWithGrowl:growl];
  notificationManager =
      [[NotificationManager alloc] initWithCallback:notificationView
                                withPairingCallback:preferences];
}

- (void)dealloc {
  [notificationView release];
  [notificationManager release];
  [statusItem release];
  [super dealloc];
}

- (IBAction)openPreferences:(id)sender {
  [preferences showDialog:sender];
}

- (IBAction)quit:(id)sender {
  NSUserDefaults *ud = [NSUserDefaults standardUserDefaults];
  [ud synchronize];
  [NSApp terminate:self];
}

@end
