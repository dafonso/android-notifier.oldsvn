//
//  Notification.m
//  MacDroidNotifier
//
//  Created by Rodrigo Damazio on 25/12/09.
//

#import "Notification.h"


@implementation Notification

@synthesize deviceId;
@synthesize type;
@synthesize contents;

- (id)initWithDeviceId:(NSString *)deviceIdParam
              withType:(NotificationType)typeParam
          withContents:(NSString *)contentsParam {
  if (self = [super init]) {
    deviceId = [deviceIdParam copy];
    type = typeParam;
    contents = [contentsParam copy];
  }
  return self;
}

- (void)dealloc {
  [deviceId release];
  [contents release];
  [super dealloc];
}

+ (Notification *)notificationFromString:(NSString *)serialized {
  NSArray *parts = [serialized pathComponents];

  NSString *deviceId = [parts objectAtIndex:0];
  NSString *typeStr = [parts objectAtIndex:1];
  NSString *contents;

  int numParts = [parts count];
  if (numParts < 3) {
    NSLog(@"Malformed notification: '%@'", serialized);
    return nil;
  } else if (numParts > 3) {
    // Oops, we broke down the description, put it back together as the last item
    NSArray *contentsParts = [parts subarrayWithRange:NSMakeRange(2, numParts - 2)];
    contents = [NSString pathWithComponents:contentsParts];
  } else {
    contents = [parts objectAtIndex:2];
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
                                        withType:type
                                    withContents:contents] autorelease];
}

@end
