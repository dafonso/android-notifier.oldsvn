//
//  ExecuteAction.m
//  MacDroidNotifier
//
//  Created by Rodrigo Damazio on 07/01/10.
//

#import "ExecuteAction.h"
#import "Notification.h"
#import "Preferences.h"

NSString *const kNotificationEnvironmentVar = @"ANDROID_NOTIFICATION";
NSString *const kOpenCommand = @"/usr/bin/open";

@implementation ExecuteAction

- (void)openExternalFile:(NSString *)filePath
           forNotification:(Notification *)notification {
  NSLog(@"Opening file %@", filePath);

  NSFileManager *fm = [NSFileManager defaultManager];
  BOOL isDir;
  if (![fm fileExistsAtPath:filePath isDirectory:&isDir]) {
    NSLog(@"Target executable not found");
    return;
  }

  // Set the notification as an environment variable for the other app
  // We pass it this way to satisfy:
  // 1. Any app can be run without having to understand the notification
  //    (so it can be just ignored)
  // 2. Applescript can be run and still read it
  NSMutableDictionary *environment =
      [[NSMutableDictionary alloc]
          initWithDictionary:[[NSProcessInfo processInfo] environment]];
  [environment setObject:[notification rawNotificationString]
                  forKey:kNotificationEnvironmentVar];

  NSTask *externalAction = [[NSTask alloc] init];
  [externalAction setEnvironment:environment];

  if (!isDir && [fm isExecutableFileAtPath:filePath]) {
    // Directly executable
    [externalAction setLaunchPath:filePath];
  } else {
    // Ask finder to open it
    [externalAction setLaunchPath:kOpenCommand];
    [externalAction setArguments:[NSArray arrayWithObject:filePath]];
  }

  [externalAction launch];

  [environment release];
  [externalAction release];
}

- (void)executeForNotification:(Notification *)notification {
  NSUserDefaults *ud = [NSUserDefaults standardUserDefaults];
  NSString *targetName = [ud objectForKey:kPreferencesExecuteActionKey];
  if (targetName != nil) {
    [self openExternalFile:targetName forNotification:notification];
  }
}

@end
