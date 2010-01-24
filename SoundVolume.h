//
//  SoundVolume.h
//  MacDroidNotifier
//
//  Created by Rodrigo Damazio on 24/01/10.
//

#import <Cocoa/Cocoa.h>

#import <AudioToolbox/AudioServices.h>

@interface SoundVolume : NSObject

@property float volume;

- (AudioDeviceID)defaultOutputDeviceID;

@end
