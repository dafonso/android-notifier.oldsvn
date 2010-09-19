//
//  Notification.h
//  MacDroidNotifier
//
//  Created by Rodrigo Damazio on 25/12/09.
//

#import <Cocoa/Cocoa.h>

// Enum for the type of notification.
typedef enum {
  RING,
  BATTERY,
  SMS,
  MMS,
  VOICEMAIL,
  PING,
  USER
} NotificationType;

// Class which keeps data about a single notification.
@interface Notification : NSObject {
 @private
  NSString *deviceId;
  NSString *notificationId;
  NotificationType type;
  NSString *contents;
  NSString *data;
}

// The unique ID for the device that sent the notification
@property (nonatomic,readonly) NSString *deviceId;

// The unique ID for the notification
@property (nonatomic,readonly) NSString *notificationId;

// The type of notification
@property (nonatomic,readonly) NotificationType type;

// The textual (human-readable) contents of the notification
@property (nonatomic,readonly) NSString *contents;

// The non-human-readable data about the notification
@property (nonatomic,readonly) NSString *data;

// Parse a serialized notification string into a new notification object.
+ (Notification *)notificationFromString:(NSString *)serialized;

// Compares this notification to another and tells if they're equal.
- (BOOL)isEqualToNotification:(Notification *)notification;

// Returns the raw encoded form of the notification.
- (NSString *)rawNotificationString;

// Returns the string equivalent of the type enum.
+ (NSString *)stringFromNotificationType:(NotificationType)type;

// Sets the given type to the equivalent of the given string.
// Returns YES if the string was a valid type, NO otherwise.
+ (BOOL)setNotificationType:(NotificationType *)type
                 fromString:(NSString *)typeStr;

@end
