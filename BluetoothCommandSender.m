//
//  BluetoothCommandSender.m
//  MacDroidNotifier
//
//  Created by Rodrigo Damazio Bovendorp on 8/29/10.
//

#import "BluetoothCommandSender.h"
#import "Command.h"
#import <IOBluetooth/IOBluetooth.h>

// Our UUID: E8D515B4-47C1-4813-B6D6-3EAB32F8953E
static const char kCommandUuidBytes[] = {
  0xE8, 0xD5, 0x15, 0xB4, 0x47, 0xC1, 0x48, 0x13,
  0xB6, 0xD6, 0x3E, 0xAB, 0x32, 0xF8, 0x95, 0x3E };
static const int kCommandUuidSize = 16;

@interface BluetoothCommandSender (Private)
- (IOBluetoothSDPServiceRecord *)findRemoteServiceInDevice:(IOBluetoothDevice*) device;
- (BOOL)openChannelToService:(IOBluetoothSDPServiceRecord*)service
                    onDevice:(IOBluetoothDevice*)device;
- (void)sendPendingCommands;
@end

@implementation BluetoothCommandSender

- (BOOL)isEnabled {
  // TODO: From preferences
  return YES;
}

- (void)sendCommand:(Command *)cmd {
  // Enqueue the command
  @synchronized (pendingCommands) {
    [pendingCommands addObject:cmd];
  }
  [self sendPendingCommands];
}

- (void)sendPendingCommands {
  // Find the device to send to
  // TODO: Get device address from preferences
  NSString* addressStr = @"00:23:76:f5:b4:d0";
  BluetoothDeviceAddress deviceAddress;
  IOBluetoothNSStringToDeviceAddress(addressStr, &deviceAddress);
  IOBluetoothDevice* device = [IOBluetoothDevice withAddress:&deviceAddress];
  if (!device) {
    NSLog(@"Couldn't find device");
    return;
  }

  // Look up the service, if we don't find it try updating SDP
  IOBluetoothSDPServiceRecord *service = [self findRemoteServiceInDevice:device];
  if (!service) {
    [device performSDPQuery:self];
  }

  // If we can't open the channel, it could be because SDP is outdated (e.g. channel ID changed)
  if (![self openChannelToService:service onDevice:device]) {
    [device performSDPQuery:self];
  }
}

- (void)sdpQueryComplete:(IOBluetoothDevice *)device status:(IOReturn)status {
  if (status != kIOReturnSuccess) {
    NSLog(@"SDP query got status %d", status);
    return;
  }

  // After updating the SDP records, we either know the service, or it's not there
  IOBluetoothSDPServiceRecord *service = [self findRemoteServiceInDevice:device];
  if (service) {
    [self openChannelToService:service onDevice:device];
  } else {
    NSLog(@"Couldn't find service");
  }
}

- (IOBluetoothSDPServiceRecord *)findRemoteServiceInDevice:(IOBluetoothDevice*) device {
  IOBluetoothSDPUUID* uuid = [IOBluetoothSDPUUID uuidWithBytes:kCommandUuidBytes
                                                        length:kCommandUuidSize];
  return [device getServiceRecordForUUID:uuid];
}

- (BOOL)openChannelToService:(IOBluetoothSDPServiceRecord*)service
                    onDevice:(IOBluetoothDevice*)device {
  BluetoothRFCOMMChannelID channelId;
  if ([service getRFCOMMChannelID:&channelId] != kIOReturnSuccess) {
    NSLog(@"Couldn't get channel ID");
    return NO;
  }

  IOBluetoothRFCOMMChannel *channel;
  if ([device openRFCOMMChannelAsync:&channel withChannelID:channelId delegate:self] != kIOReturnSuccess) {
    NSLog(@"Couldn't open channel");
    return NO;
  }

  [channel closeChannel];
  return YES;
}

- (void)rfcommChannelOpenComplete:(IOBluetoothRFCOMMChannel*)rfcommChannel
                           status:(IOReturn)error {
  NSLog(@"Commands channel open, mtu=%d", [rfcommChannel getMTU]);
  NSLog(@"TODO: Write commands");
  @synchronized (pendingCommands) {
    [pendingCommands removeAllObjects];
  }
  [rfcommChannel closeChannel];
}

- (void)rfcommChannelClosed:(IOBluetoothRFCOMMChannel*)rfcommChannel {
}

// Not yet needed
//- (void)rfcommChannelData:(IOBluetoothRFCOMMChannel*)rfcommChannel data:(void *)dataPointer length:(size_t)dataLength;

- (void)rfcommChannelWriteComplete:(IOBluetoothRFCOMMChannel*)rfcommChannel
                            refcon:(void*)refcon
                            status:(IOReturn)error {
}


@end
