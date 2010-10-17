/*
 * Copyright 2010 Rodrigo Damazio <rodrigo@damazio.org>
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * 
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR
 * IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 * IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
 * NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
 * THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.damazio.notifier.util;

import org.damazio.notifier.NotifierConstants;
import org.damazio.notifier.R;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.Contacts;
import android.provider.Contacts.Phones;
import android.provider.ContactsContract.PhoneLookup;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.util.Log;

/**
 * Service which can look up information about an incoming caller,
 * and prepare it for display in the notification.
 *
 * @author Rodrigo Damazio
 */
@SuppressWarnings("deprecation")
public abstract class CallerId {

  /**
   * Structure to contain caller information.
   */
  private static class CallerInfo {
    public String number;
    public String typeName;
    public String displayName;
  }

  /**
   * Full implementation for API level 4.
   */
  private static class CallerIdApi4 extends CallerId {
    private CallerIdApi4(Context context) {
      super(context);
    }

    @Override
    protected CallerInfo getCallerInfo(String number) {
      return getCallerInfo(Contacts.Phones.CONTENT_FILTER_URL,
          Phones.DISPLAY_NAME, Phones.TYPE, Phones.LABEL,
          number);
    }

    @Override
    protected String getTypeName(int type, String label) {
      return Phones.getDisplayLabel(context, type, label).toString();
    }
  }

  /**
   * Full implementation for API level 5 and above.
   */
  private static class CallerIdApi5 extends CallerId {
    private CallerIdApi5(Context context) {
      super(context);
    }

    @Override
    public CallerInfo getCallerInfo(String number) {
      return getCallerInfo(PhoneLookup.CONTENT_FILTER_URI,
          PhoneLookup.DISPLAY_NAME, PhoneLookup.TYPE, PhoneLookup.LABEL,
          number);
    }

    @Override
    protected String getTypeName(int type, String label) {
      return Phone.getTypeLabel(context.getResources(), type, label).toString();
    }
  }

  /**
   * Creates a new instance of {@link CallerId}, appropriate for the current
   * version of Android.
   */
  public static CallerId create(Context context) {
    if (NotifierConstants.ANDROID_SDK_INT >= 5) {
      Log.d(NotifierConstants.LOG_TAG, "Using level 5 caller ID");
      return new CallerIdApi5(context);
    } else {
      Log.d(NotifierConstants.LOG_TAG, "Using level 4 caller ID");
      return new CallerIdApi4(context);
    }
  }

  protected final Context context;

  protected CallerId(Context context) {
    this.context = context;
  }
  
  /**
   * Get information about the caller.
   *
   * @param number the number of the caller
   * @return the information about the caller, or null if not found
   */
  protected abstract CallerInfo getCallerInfo(String number);

  /**
   * Convert a phone type + label into a user-visible string.
   *
   * @param type the phone type
   * @param label the phone label
   * @return a user-visible string for phone type
   */
  protected abstract String getTypeName(int type, String label);

  /**
   * Build a user-visible caller ID string from the given number.
   *
   * @param number the caller number
   * @return a user-visible caller ID string for the number
   */
  public String buildCallerIdString(String number) {
    if (number == null) {
      return context.getString(R.string.unknown_number);
    }

    CallerInfo callerInfo = getCallerInfo(number);
    if (callerInfo != null) {
      return buildCallerIdString(callerInfo);
    }

    // Couldn't find the information for some reason, return just the number 
    Log.i(NotifierConstants.LOG_TAG, "Couldn't find caller information");
    return number;
  }

  /**
   * Do the actual query for the caller information.
   * The source of the information, as well as the column names, are abstracted
   * in order to suit multiple API levels.
   *
   * @param filterUri the base URI of a contact
   * @param displayNameColumn the display name column
   * @param typeColumn the phone type column
   * @param labelColumn the phone type label column
   * @param number the calling number
   * @return information about the caller, or null if not found
   */
  protected CallerInfo getCallerInfo(Uri filterUri, String displayNameColumn,
      String typeColumn, String labelColumn, String number) {
    // Do the contact lookup by number
    Uri uri = Uri.withAppendedPath(filterUri, Uri.encode(number));
    Cursor cursor;
    try {
      cursor = context.getContentResolver().query(uri,
          new String[] { displayNameColumn, typeColumn, labelColumn },
          null, null, null);
    } catch (IllegalArgumentException e) {
      Log.e(NotifierConstants.LOG_TAG, "Unable to look up caller ID", e);
      return null;
    }

    // Take the first match only
    if (cursor != null && cursor.moveToFirst()) {
      int nameIndex = cursor.getColumnIndex(displayNameColumn);
      int typeIndex = cursor.getColumnIndex(typeColumn);
      int labelIndex = cursor.getColumnIndex(labelColumn);

      if (nameIndex != -1) {
        String displayName = cursor.getString(nameIndex);

        // Get the phone type if possible
        String typeStr = null;
        if (typeIndex != -1) {
          int numberType = cursor.getInt(typeIndex);
          String label = "";
          if (labelIndex != -1) {
            label = cursor.getString(labelIndex);
          }

          typeStr = getTypeName(numberType, label);
        }

        CallerInfo callerInfo = new CallerInfo();
        callerInfo.number = number;
        callerInfo.typeName = typeStr;
        callerInfo.displayName = displayName;
        return callerInfo;
      }
    }

    return null;
  }

  /**
   * Build the caller ID string from the caller information.
   *
   * @param number the number calling
   * @param numberType the type of the calling number (can be null)
   * @param displayName the name of the caller
   * @return the fully formatted caller ID
   */
  private String buildCallerIdString(CallerInfo info) {
    StringBuilder contentsBuilder = new StringBuilder();
    contentsBuilder.append(info.displayName);
    if (info.typeName != null) {
      contentsBuilder.append(" - ");
      contentsBuilder.append(info.typeName);
    }
    contentsBuilder.append(" (");
    contentsBuilder.append(info.number);
    contentsBuilder.append(")");
    return contentsBuilder.toString();
  }
}
