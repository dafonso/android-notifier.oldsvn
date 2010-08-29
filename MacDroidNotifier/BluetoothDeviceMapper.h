//
//  BluetoothDeviceMapper.h
//  MacDroidNotifier
//
//  Created by Rodrigo Damazio Bovendorp on 8/29/10.
//

#import <Cocoa/Cocoa.h>


@interface BluetoothDeviceMapper : NSObject {

}

- (void)cacheDevice:(NSString*)deviceId withMac:(NSString*)macAddress;
- (NSString*)findMacForDevice:(NSString*)deviceId;

@end
