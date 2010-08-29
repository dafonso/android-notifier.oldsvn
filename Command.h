//
//  Command.h
//  MacDroidNotifier
//
//  Created by Rodrigo Damazio on 06/08/10.
//

#import <Cocoa/Cocoa.h>

typedef enum {
  DIAL,
  HANGUP,
  SEND_SMS,
  QUERY
} CommandType;

@interface Command : NSObject {
 @private
  NSString *deviceId;
  NSString *commandId;
  CommandType type;
  NSString *data1;
  NSString *data2;
}

// The unique ID for the device that sent the notification
@property (nonatomic,readonly) NSString *deviceId;

// The unique ID for the command
@property (nonatomic,readonly) NSString *commandId;

// The type of notification
@property (nonatomic,readonly) CommandType type;

// The textual (human-readable) contents of the notification
@property (nonatomic,readonly) NSString *data1;

// The non-human-readable data about the notification
@property (nonatomic,readonly) NSString *data2;

- (NSString *)serialized;

+ (Command *)commandWithDeviceId:(NSString *)deviceId
                            type:(CommandType)type
                           data1:(NSString *)data1
                           data2:(NSString *)data2;

@end
