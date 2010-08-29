//
//  CommandController.h
//  MacDroidNotifier
//
//  Created by Rodrigo Damazio on 06/08/10.
//

#import <Cocoa/Cocoa.h>

@class CommandDispatcher;

@interface CommandController : NSObject {
 @private
  IBOutlet NSWindow *dialWindow;
  IBOutlet NSTextField *dialNumber;
  NSString *lastDialDeviceId;

  IBOutlet NSWindow *sendSmsWindow;
  IBOutlet NSTextField *sendSmsNumber;
  IBOutlet NSTextField *sendSmsContents;
  NSString *lastSmsDeviceId;

  IBOutlet CommandDispatcher *dispatcher;
}

- (IBAction)showStartCallDialog:(id)sender;
- (IBAction)showSendSmsDialog:(id)sender;
- (IBAction)startCall:(id)sender;
- (IBAction)sendSms:(id)sender;
- (IBAction)hangUp:(id)sender;

@end
