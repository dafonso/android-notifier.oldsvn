//
//  Growl.m
//  MacDroidNotifier
//
//  Created by Rodrigo Damazio on 26/12/09.
//

#import "Growl.h"


@implementation Growl

- (void)awakeFromNib {
  [GrowlApplicationBridge setGrowlDelegate:self];
}

- (NSDictionary *)dictionaryForNotification:(Notification *)notification {
  NSString *title = nil;
  NSString *description = [notification contents];
  NSString *name = nil;
  NSData *icon = nil;  // TODO
  switch ([notification type]) {
    case RING:
      title = NSLocalizedString(@"Phone is ringing", @"Phone ring title");
      name = @"PhoneRing";
      break;
    case BATTERY:
      title = NSLocalizedString(@"Phone battery is low", @"Low battery title");
      name = @"PhoneBattery";
      break;
    case SMS:
      title = NSLocalizedString(@"Phone received an SMS", @"SMS received title");
      name = @"PhoneSms";
      break;
    case MMS:
      title = NSLocalizedString(@"Phone received an MMS", @"MMS received title");
      name = @"PhoneMms";
      break;
    case PING:
      title = NSLocalizedString(@"Phone sent a ping", @"Ping title");
      name = @"PhonePing";
      break;
    default:
      return nil;
  }

  return [NSDictionary dictionaryWithObjectsAndKeys:
          name, GROWL_NOTIFICATION_NAME,
          title, GROWL_NOTIFICATION_TITLE,
          description, GROWL_NOTIFICATION_DESCRIPTION,
          icon, GROWL_NOTIFICATION_ICON,
          0, GROWL_NOTIFICATION_PRIORITY];
}

- (void)postGrowlNotification:(Notification *)notification {
  NSLog(@"Notifying growl");
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
