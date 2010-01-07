//
//  ActionDispatcher.m
//  MacDroidNotifier
//
//  Created by Rodrigo Damazio on 07/01/10.
//

#import "ActionDispatcher.h"

#import "Action.h"
#import "DisplayAction.h"
#import "ExecuteAction.h"
#import "MuteAction.h"
#import "Notification.h"
#import "Preferences.h"

@implementation ActionDispatcher

- (void)awakeFromNib {
  actions = [[NSDictionary dictionaryWithObjectsAndKeys:
                 [[[DisplayAction alloc] initWithGrowl:growl] autorelease], kPreferencesDisplayKey,
                 [[[MuteAction alloc] init] autorelease], kPreferencesMuteKey,
                 [[[ExecuteAction alloc] init] autorelease], kPreferencesExecuteKey,
                 nil] retain];
}

- (void)dealloc {
  [actions release];
  [super dealloc];
}

- (BOOL)isAction:(NSString *)actionKey enabledForNotificationType:(NotificationType)type
      inDefaults:(NSUserDefaults *)defaults {
  NSString *typeKey = [[Notification stringFromNotificationType:type] lowercaseString];
  NSString *fullKey = [NSString stringWithFormat:@"%@.%@", typeKey, actionKey];
  return [defaults boolForKey:fullKey];
}

- (void)actOnNotification:(Notification *)notification {
  NSUserDefaults *defaults = [NSUserDefaults standardUserDefaults];

  for (NSString *key in actions) {
    if ([self isAction:key enabledForNotificationType:[notification type] inDefaults:defaults]) {
      NSObject<Action> *action = [actions objectForKey:key];
      [action executeForNotification:notification];
    }
  }
}

@end
