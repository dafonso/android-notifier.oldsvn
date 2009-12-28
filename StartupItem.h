//
//  StartupItem.h
//  BuildStatusItem
//
//  Created by Rodrigo Damazio on 25/12/09.
//

#import <Cocoa/Cocoa.h>

// Class which handles adding or removing this application as a login item.
@interface StartupItem : NSObject {
 @private
  // Reference to the login items list
  LSSharedFileListRef loginItemsRef;
}

// Whether or not this application will start at login. This reads from and
// writes to the launch services' login items list.
@property (readwrite, assign) BOOL startAtLogin;

@end
