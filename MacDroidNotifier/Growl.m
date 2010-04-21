//
//  Growl.m
//  MacDroidNotifier
//
//  Created by Rodrigo Damazio on 26/12/09.
//

#import "Growl.h"

NSString *const kNeverAskForGrowl = @"neverAskForGrowl";
NSString *const kGrowlUrl = @"http://growl.info/";

@implementation Growl

- (void)checkGrowlInstalled {
  if (![GrowlApplicationBridge isGrowlInstalled]) {
    NSLog(@"Growl not found");
    
    NSUserDefaults *ud = [NSUserDefaults standardUserDefaults];
    BOOL neverAskForGrowl = [ud boolForKey:kNeverAskForGrowl];
    if (!neverAskForGrowl) {
      NSAlert *alert = [[[NSAlert alloc] init] autorelease];
      [alert addButtonWithTitle:NSLocalizedString(@"Open Growl website", @"Dialog button for opening Growl website")];
      [alert addButtonWithTitle:NSLocalizedString(@"Later", @"Dialog button for not opening Growl website now")];
      [alert addButtonWithTitle:NSLocalizedString(@"Never", @"Dialog button for never opening Growl website")];
      [alert setMessageText:NSLocalizedString(@"Growl not installed", @"Growl not installed message")];
      [alert setInformativeText:NSLocalizedString(@"If you don't install Growl from http://growl.info/, you won't see any notifications.", @"Detailed message explaining that Growl is required")];
      [alert setAlertStyle:NSWarningAlertStyle];

      NSInteger buttonClicked = [alert runModal];
      switch (buttonClicked) {
        case NSAlertFirstButtonReturn: {
          // Open the Growl website
          NSURL *growlUrl = [NSURL URLWithString:kGrowlUrl];
          [[NSWorkspace sharedWorkspace] openURL:growlUrl];
          break;
        }
        // NSAlertSecondButtonReturn is to ask again next time
        case NSAlertThirdButtonReturn:
          // User said we shouldn't ask again. Ever.
          [ud setBool:YES forKey:kNeverAskForGrowl];
          [ud synchronize];
          break;
      }
    }
  }
}

- (void)checkGrowlRunning {
  if (![GrowlApplicationBridge isGrowlRunning]) {
    NSLog(@"Growl not running");

    // Notify only once
    if (growlNotRunningNotified) return;
    growlNotRunningNotified = YES;

    NSAlert *alert = [[[NSAlert alloc] init] autorelease];
    [alert addButtonWithTitle:NSLocalizedString(@"Dismiss", @"Dialog button for dismissing Growl not running warning")];
    [alert setMessageText:NSLocalizedString(@"Growl not running", @"Growl not running message")];
    [alert setInformativeText:NSLocalizedString(@"Tried to display a notification, but Growl is not running - please start it...\nThis warning will only be displayed once.", @"Detailed message explaining that Growl must be running")];
    [alert setAlertStyle:NSWarningAlertStyle];

    [alert runModal];
  }
}

- (id)init {
  if (self = [super init]) {
    growlNotRunningNotified = NO;
  }
  return self;
}

- (void)awakeFromNib {
  [self checkGrowlInstalled];

  [GrowlApplicationBridge setGrowlDelegate:self];
}

- (NSString *)batteryIconNameFromDescription:(NSString *)description {
  // Expected: space + number + % at the end, where number is a multiple of 5
  if (![description hasSuffix:@"%%"]) {
    // Find last space
    NSRange lastSpace = [description rangeOfCharacterFromSet:[NSCharacterSet whitespaceCharacterSet]
                                                     options:NSBackwardsSearch];
    if (lastSpace.location != NSNotFound) {
      lastSpace.location++;
      lastSpace.length = [description length] - lastSpace.location - 1;
      NSString *valueStr = [description substringWithRange:lastSpace];
      NSInteger value = [valueStr integerValue];
      if (value % 5 == 0) {
        return [@"battery" stringByAppendingString:valueStr];
      }   
    }
  }

  NSLog(@"Unknown battery icon from description: %@", description);
  return @"battery_unknown";
}

- (NSDictionary *)dictionaryForNotification:(Notification *)notification {
  NSString *title = nil;
  NSString *description = [notification contents];
  NSString *name = nil;
  NSString *iconName = nil;
  switch ([notification type]) {
    case RING:
      title = NSLocalizedString(@"Phone is ringing", @"Phone ring title");
      name = @"PhoneRing";
      iconName = @"ring";
      break;
    case BATTERY:
      title = NSLocalizedString(@"Phone battery state", @"Battery state title");
      name = @"PhoneBattery";
      iconName = [self batteryIconNameFromDescription:description];
      break;
    case SMS:
      title = NSLocalizedString(@"Phone received an SMS", @"SMS received title");
      name = @"PhoneSMS";
      iconName = @"sms";
      break;
    case MMS:
      title = NSLocalizedString(@"Phone received an MMS", @"MMS received title");
      name = @"PhoneMMS";
      iconName = @"mms";
      break;
    case VOICEMAIL:
      title = NSLocalizedString(@"New voicemail", @"New voicemail title");
      name = @"PhoneVoicemail";
      iconName = @"voicemail";
      break;
    case PING:
      title = NSLocalizedString(@"Phone sent a ping", @"Ping title");
      name = @"PhonePing";
      break;
    default:
      return nil;
  }

  NSData *icon = nil;
  if (iconName) {
    NSImage *iconImage = [NSImage imageNamed:iconName];
    icon = [iconImage TIFFRepresentation];
  }

  return [NSDictionary dictionaryWithObjectsAndKeys:
          name, GROWL_NOTIFICATION_NAME,
          title, GROWL_NOTIFICATION_TITLE,
          description, GROWL_NOTIFICATION_DESCRIPTION,
          icon, GROWL_NOTIFICATION_ICON,
          0, GROWL_NOTIFICATION_PRIORITY,
          nil];
}

- (void)postGrowlNotification:(Notification *)notification {
  // We only check at the time of the first notification - during start up, if we're starting up as
  // a login item, Growl may not YET be running.
  [self checkGrowlRunning];

  [GrowlApplicationBridge notifyWithDictionary:[self dictionaryForNotification:notification]];
}

#pragma mark Delegate implementation

- (NSString *)applicationNameForGrowl {
  return [[NSBundle mainBundle] objectForInfoDictionaryKey: @"CFBundleName"];
}

- (NSDictionary *)registrationDictionaryForGrowl {
  return [NSDictionary dictionaryWithContentsOfFile:
             [[NSBundle mainBundle] pathForResource:@"MacDroidNotifier"
                                             ofType:@"growlRegDict"]];
}

@end
