package org.damazio.notifier.service;

import org.damazio.notifier.NotifierConstants;
import org.damazio.notifier.R;

import android.content.Context;
import android.telephony.SmsMessage;
import android.util.Log;

/**
 * Class which handles decoding SMS messages in the proper way depending on the
 * version of Android being run.
 *
 * @author rdamazio
 */
public abstract class SmsDecoder {
  /**
   * Implementation for API levels 4 and above.
   */
  private static class DonutImpl extends SmsDecoder {
    @Override
    public String getSmsContents(Context context, Object pdu) {
      SmsMessage message = SmsMessage.createFromPdu((byte[]) pdu);

      String from = getSenderString(context, message.getOriginatingAddress());
      return context.getString(R.string.sms_contents, from, message.getMessageBody());
    }
  }

  /**
   * Implementation for API levels 3 and below.
   */
  private static class CupcakeImpl extends SmsDecoder {
    @SuppressWarnings("deprecation")
    @Override
    public String getSmsContents(Context context, Object pdu) {
      android.telephony.gsm.SmsMessage message =
          android.telephony.gsm.SmsMessage.createFromPdu((byte[]) pdu);

      String from = getSenderString(context, message.getOriginatingAddress());
      return context.getString(R.string.sms_contents, from, message.getMessageBody());
    }
  }

  private static SmsDecoder instance;

  /**
   * Decodes an SMS message.
   *
   * @param context the context in which decoding happens
   * @param pdu the PDU, extracted from the intent's "pdus" extras bundle
   * @return the string representation of the text message
   */
  public abstract String getSmsContents(Context context, Object pdu);

  /**
   * Returns the proper (singleton) instance of this class.
   */
  public static SmsDecoder getInstance() {
    if (instance == null) {
      if (NotifierConstants.ANDROID_SDK_INT >= 4) {
        Log.d(NotifierConstants.LOG_TAG, "Using donut SMS decoder");
        instance = new DonutImpl();
      } else {
        Log.d(NotifierConstants.LOG_TAG, "Using cupcake SMS decoder");
        instance = new CupcakeImpl();
      }
    }
    return instance;
  }

  /**
   * Creates and returns a complete string representing the sender, including
   * his name and phone type if found by the caller ID.
   *
   * @param context the current context
   * @param originatingAddress the phone number of the sender
   * @return the formatted sender string
   */
  protected String getSenderString(Context context, String originatingAddress) {
    return CallerId.create(context).buildCallerIdString(originatingAddress);
  }
}
