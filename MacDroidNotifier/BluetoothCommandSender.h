//
//  BluetoothCommandSender.h
//  MacDroidNotifier
//
//  Created by Rodrigo Damazio Bovendorp on 8/29/10.
//

#import <Cocoa/Cocoa.h>
#import <IOBluetooth/IOBluetooth.h>

#import "CommandSender.h"

@interface BluetoothCommandSender : NSObject<CommandSender> {
 @private
  NSMutableArray *pendingCommands;
}

@end
