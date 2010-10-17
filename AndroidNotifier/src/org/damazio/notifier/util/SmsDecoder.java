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
import android.telephony.SmsMessage;
import android.util.Log;

/**
 * Class which handles decoding SMS messages in the proper way depending on the
 * version of Android being run.
 *
 * @author Rodrigo Damazio
 */
public abstract class SmsDecoder {
  protected final Context context;
  protected final Object pdu;

  /**
   * Implementation for API levels 4 and above.
   */
  private static class DonutImpl extends SmsDecoder {
    private final String sender;
    private final String body;

    public DonutImpl(Context context, Object pdu) {
      super(context, pdu);

      SmsMessage message = null;
      try {
        message = SmsMessage.createFromPdu((byte[]) pdu);
      } catch (NullPointerException e) {
        // Workaround for Android bug
        // http://code.google.com/p/android/issues/detail?id=11345
        Log.e(NotifierConstants.LOG_TAG, "Invalid PDU", e);
      }

      if (message != null) {
        body = message.getMessageBody();
        sender = message.getOriginatingAddress();
      } else {
        body = null;
        sender = null;
      }
    }

    @Override
    public String getMessageBody() {
      return body;
    }

    @Override
    public String getSenderAddress() {
      return sender;
    }
  }

  /**
   * Implementation for API levels 3 and below.
   */
  @SuppressWarnings("deprecation")
  private static class CupcakeImpl extends SmsDecoder {
    private final String body;
    private final String sender;

    public CupcakeImpl(Context context, Object pdu) {
      super(context, pdu);

      android.telephony.gsm.SmsMessage message = null;
      try {
        message = android.telephony.gsm.SmsMessage.createFromPdu((byte[]) pdu);
      } catch (NullPointerException e) {
        // Workaround for Android bug
        // http://code.google.com/p/android/issues/detail?id=11345
        Log.e(NotifierConstants.LOG_TAG, "Invalid PDU", e);
      }

      if (message != null) {
        body = message.getMessageBody();
        sender = message.getOriginatingAddress();
      } else {
        body = null;
        sender = null;
      }
    }

    @Override
    public String getMessageBody() {
      return body;
    }

    @Override
    public String getSenderAddress() {
      return sender;
    }
  }

  /**
   * Formats the SMS in a human-readable way.
   *
   * @param context the context in which decoding happens
   * @param pdu the PDU, extracted from the intent's "pdus" extras bundle
   * @return the human-readable representation of the text message
   */
  public final String getSmsContents() {
    String sender = CallerId.create(context).buildCallerIdString(getSenderAddress());
    return context.getString(R.string.sms_contents, sender, getMessageBody());
  }

  /**
   * Returns the plain sender address (phone number) for the SMS.
   */
  public abstract String getSenderAddress();

  /**
   * Returns the plain SMS message body.
   */
  public abstract String getMessageBody();

  public boolean isValidMessage() {
    return getMessageBody() != null
        && getSenderAddress() != null;
  }

  protected SmsDecoder(Context context, Object pdu) {
    this.context = context;
    this.pdu = pdu;
  }

  /**
   * Returns a proper instance of this class.
   */
  public static SmsDecoder create(Context context, Object pdu) {
    if (NotifierConstants.ANDROID_SDK_INT >= 4) {
      Log.d(NotifierConstants.LOG_TAG, "Using donut SMS decoder");
      return new DonutImpl(context, pdu);
    } else {
      Log.d(NotifierConstants.LOG_TAG, "Using cupcake SMS decoder");
      return new CupcakeImpl(context, pdu);
    }
  }
}
