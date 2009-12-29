//
//  Notification.m
//  MacDroidNotifier
//
//  Created by Rodrigo Damazio on 25/12/09.
//

#import "Notification.h"


@implementation Notification

@synthesize deviceId;
@synthesize notificationId;
@synthesize type;
@synthesize contents;

- (id)initWithDeviceId:(NSString *)deviceIdParam
    withNotificationId:(NSString *)notificationIdParam
              withType:(NotificationType)typeParam
          withContents:(NSString *)contentsParam {
  if (self = [super init]) {
    deviceId = [deviceIdParam copy];
    notificationId = [notificationIdParam copy];
    type = typeParam;
    contents = [contentsParam copy];
  }
  return self;
}

- (void)dealloc {
  [deviceId release];
  [notificationId release];
  [contents release];
  [super dealloc];
}

+ (Notification *)notificationFromString:(NSString *)serialized {
  NSArray *parts = [serialized pathComponents];

  NSString *deviceId = [parts objectAtIndex:0];
  NSString *notificationId = [parts objectAtIndex:1];
  NSString *typeStr = [parts objectAtIndex:2];
  NSString *contents;

  int numParts = [parts count];
  if (numParts < 4) {
    NSLog(@"Malformed notification: '%@'", serialized);
    return nil;
  } else if (numParts > 4) {
    // Oops, we broke down the description, put it back together as the last item
    NSArray *contentsParts = [parts subarrayWithRange:NSMakeRange(3, numParts - 3)];
    contents = [NSString pathWithComponents:contentsParts];
  } else {
    contents = [parts objectAtIndex:3];
  }

  NotificationType type;
  if ([typeStr isEqualToString:@"RING"]) {
    type = RING;
  } else if ([typeStr isEqualToString:@"BATTERY"]) {
    type = BATTERY;
  } else if ([typeStr isEqualToString:@"SMS"]) {
    type = SMS;
  } else if ([typeStr isEqualToString:@"MMS"]) {
    type = MMS;
  } else if ([typeStr isEqualToString:@"PING"]) {
    type = PING;
  } else {
    NSLog(@"Malformed notification: '%@'", serialized);
    return nil;
  }

  return [[[Notification alloc] initWithDeviceId:deviceId
                              withNotificationId:notificationId
                                        withType:type
                                    withContents:contents] autorelease];
}

- (BOOL)isEqualToNotification:(Notification *)notification {
  return [[notification deviceId] isEqualToString:[self deviceId]] &&
         [[notification notificationId] isEqualToString:[self notificationId]] &&
         ([notification type] == type) &&
         [[notification contents] isEqualToString:[self contents]];
}

@end
