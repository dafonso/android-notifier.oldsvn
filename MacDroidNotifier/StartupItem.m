//
//  StartupItem.m
//  BuildStatusItem
//
//  Created by Rodrigo Damazio on 25/12/09.
//
//  Pieces of code from http://rhult.github.com/preference-to-launch-on-login.html
//

#import "StartupItem.h"

@interface StartupItem (Private)

- (void)updateLoginItemState;

@end

static void loginItemsChanged(LSSharedFileListRef listRef, void *context) {
  StartupItem *controller = context;

  [controller willChangeValueForKey:@"startAtLogin"];
  [controller didChangeValueForKey:@"startAtLogin"];
}

@implementation StartupItem

@dynamic startAtLogin;

- (NSURL *)appURL {
  return [NSURL fileURLWithPath:[[NSBundle mainBundle] bundlePath]];
}

#pragma mark Initialization

- (id)init {
  if (self = [super init]) {
    loginItemsRef = LSSharedFileListCreate(kCFAllocatorDefault,
                                           kLSSharedFileListSessionLoginItems,
                                           NULL);

    LSSharedFileListAddObserver(loginItemsRef,
                                [[NSRunLoop mainRunLoop] getCFRunLoop],
                                kCFRunLoopCommonModes,
                                loginItemsChanged,
                                self);
  }
  return self;
}

- (void)dealloc {
  LSSharedFileListRemoveObserver(loginItemsRef,
                                 [[NSRunLoop mainRunLoop] getCFRunLoop],
                                 kCFRunLoopCommonModes,
                                 loginItemsChanged,
                                 self);
  CFRelease(loginItemsRef);

  [super dealloc];
}

# pragma mark Login item list manipulation

// Get an NSArray with the items.
- (NSArray *)loginItems {
  CFArrayRef snapshotRef = LSSharedFileListCopySnapshot(loginItemsRef, NULL);

  // Use toll-free bridging to get an NSArray with nicer API
  // and memory management.
  return [NSMakeCollectable(snapshotRef) autorelease];
}

// Return a CFRetained item for the app's bundle, if there is one.
- (LSSharedFileListItemRef)findLoginItem {
  NSArray *loginItems = [self loginItems];
  NSURL *bundleURL = [self appURL];

  for (id item in loginItems) {
    LSSharedFileListItemRef itemRef = (LSSharedFileListItemRef) item;
    CFURLRef itemURLRef;

    if (LSSharedFileListItemResolve(itemRef, 0, &itemURLRef, NULL) == noErr) {
      NSURL *itemURL = (NSURL *) [NSMakeCollectable(itemURLRef) autorelease];
      if ([itemURL isEqual:bundleURL]) {
        return itemRef;
      }
    }
  }
  
  return NULL;
}

- (void)addToLoginItems {
  // We use the URL to the app itself (i.e. the main bundle).
  NSURL *bundleURL = [self appURL];

  // Ask to be hidden on launch. The key name to use was a bit hard to find, but can
  // be found by inspecting the plist ~/Library/Preferences/com.apple.loginwindow.plist
  // and looking at some existing entries. Thanks to Anders for the hint!
  NSDictionary *properties =
  [NSDictionary dictionaryWithObject:[NSNumber numberWithBool:YES]
                              forKey:@"com.apple.loginitem.HideOnLaunch"];

  LSSharedFileListItemRef itemRef =
  LSSharedFileListInsertItemURL(loginItemsRef,
                                kLSSharedFileListItemLast,
                                NULL,
                                NULL,
                                (CFURLRef) bundleURL,
                                (CFDictionaryRef) properties,
                                NULL);

  if (itemRef) {
    CFRelease(itemRef);
  }
}

- (void)removeFromLoginItems {
  LSSharedFileListItemRef itemRef = [self findLoginItem];
  if (!itemRef)
    return;

  LSSharedFileListItemRemove(loginItemsRef, itemRef);
}

#pragma mark Property accessor methods

- (BOOL)startAtLogin {
  LSSharedFileListItemRef itemRef = [self findLoginItem];
  return (itemRef != NULL);
}

- (void)setStartAtLogin:(BOOL)value {
  if (!value) {
    [self removeFromLoginItems];
  } else {
    [self addToLoginItems];
  }
}

@end