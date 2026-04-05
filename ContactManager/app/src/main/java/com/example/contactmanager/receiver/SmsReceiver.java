package com.example.contactmanager.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.telephony.SmsMessage;
import android.util.Log;

import com.example.contactmanager.database.ContactRepository;

/**
 * SmsReceiver: Intercepts incoming SMS messages and aborts delivery
 * for any sender whose number is in the blacklist.
 *
 * Registered with android:priority="999" so it runs before the system SMS app.
 *
 * NOTE: On Android 10+ (API 29+), only the default SMS application can fully
 * prevent message storage. This receiver still prevents notification / delivery
 * to other non-default SMS apps. For complete blocking, the user would need to
 * set this app as the default SMS application.
 *
 * Satisfies FR 2: Blacklisting a contact blocks incoming SMS messages.
 */
public class SmsReceiver extends BroadcastReceiver {

    private static final String TAG = "SmsReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (!"android.provider.Telephony.SMS_RECEIVED".equals(intent.getAction())) return;

        Bundle bundle = intent.getExtras();
        if (bundle == null) return;

        Object[] pdus = (Object[]) bundle.get("pdus");
        String format = bundle.getString("format");
        if (pdus == null) return;

        ContactRepository repository = ContactRepository.getInstance(context);

        for (Object pdu : pdus) {
            SmsMessage smsMessage;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                smsMessage = SmsMessage.createFromPdu((byte[]) pdu, format);
            } else {
                smsMessage = SmsMessage.createFromPdu((byte[]) pdu);
            }

            if (smsMessage != null) {
                String sender = smsMessage.getOriginatingAddress();
                if (sender != null && repository.isNumberBlacklisted(sender)) {
                    Log.i(TAG, "Blocking SMS from blacklisted number: " + sender);
                    abortBroadcast(); // Prevent delivery to other receivers
                    return;
                }
            }
        }
    }
}
