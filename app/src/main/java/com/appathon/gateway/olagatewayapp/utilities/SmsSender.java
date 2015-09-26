package com.appathon.gateway.olagatewayapp.utilities;

import android.telephony.SmsManager;

/**
 * Created by vikoo on 27/09/15.
 */
public class SmsSender {

    public static void sendSms(String phoneNum, String msg){
        SmsManager sms = SmsManager.getDefault();
        sms.sendTextMessage(phoneNum, null, msg, null, null);
    }
}
