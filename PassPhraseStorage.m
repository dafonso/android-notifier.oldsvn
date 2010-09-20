//
//  PassPhraseStorage.m
//  MacDroidNotifier
//
//  Created by Rodrigo Damazio Bovendorp on 9/20/10.
//  Copyright 2010 __MyCompanyName__. All rights reserved.
//

#import "PassPhraseStorage.h"
#import <Security/Security.h>

static const char kServiceName[] = "MacDroidNotifier";
static const char kAccountName[] = "passphrase";

@implementation PassPhraseStorage

- (NSString *)passPhrase {
  void* password;
  UInt32 passwordLength;

  OSStatus status;
  status = SecKeychainFindGenericPassword(NULL,
                                          strlen(kServiceName), kServiceName,
                                          strlen(kAccountName), kAccountName,
                                          &passwordLength, &password,
                                          NULL);
  if (status != 0) return @"";

  NSString *passwordStr = [NSString stringWithUTF8String:password];
  SecKeychainItemFreeContent(NULL, password);
  return passwordStr;
}

- (void)setPassPhrase:(NSString *)passPhrase {
  const char* utf8pass = [passPhrase UTF8String];
  UInt32 len = (UInt32) strlen(utf8pass);
  OSStatus status;

  SecKeychainItemRef itemRef;
  status = SecKeychainFindGenericPassword(NULL,
                                         strlen(kServiceName), kServiceName,
                                         strlen(kAccountName), kAccountName,
                                         NULL, NULL,
                                         &itemRef);

  if (status == errSecItemNotFound) {
    // Doesn't exist yet, create it
    status = SecKeychainAddGenericPassword(NULL,
                                           strlen(kServiceName),
                                           kServiceName,
                                           strlen(kAccountName),
                                           kAccountName,
                                           len,
                                           utf8pass,
                                           NULL);
  } else if (status == 0) {
    // Exists and was successfully read
    status = SecKeychainItemModifyAttributesAndData(itemRef,
                                                    NULL,
                                                    len,
                                                    utf8pass);
  }  // else some other error occurred

  if (status != 0) {
    CFStringRef err = SecCopyErrorMessageString(status, NULL);
    NSLog(@"Error while saving pass phrase: %@", err);
    CFRelease(err);
  }

  if (itemRef) CFRelease(itemRef);
}

@end
