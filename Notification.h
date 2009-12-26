//
//  Notification.h
//  MacDroidNotifier
//
//  Created by Rodrigo Damazio on 25/12/09.
//

#import <Cocoa/Cocoa.h>

typedef enum {
  RING,
  BATTERY,
  SMS,
  MMS,
  PING
} NotificationType;

@interface Notification : NSObject {
 @private
  NSString *deviceId;
  NotificationType type;
  NSString *contents;
}

@property (nonatomic,readonly) NSString *deviceId;
@property (nonatomic,readonly) NotificationType type;
@property (nonatomic,readonly) NSString *contents;

+ (Notification *)notificationFromString:(NSString *)serialized;

@end
