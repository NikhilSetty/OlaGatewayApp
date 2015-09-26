package com.appathon.gateway.olagatewayapp;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
import android.telephony.SmsMessage;
import android.util.Log;
import android.widget.Toast;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class GatewayService extends Service {

    private SMSReceiver mSMSreceiver;
    private IntentFilter mIntentFilter;

    Thread workThread;

    public static boolean isMessageReceived = false;
    public static List <RequestModel> requestModels;

    public static SmsMessage[] smsMessages = null;

    public GatewayService() {
    }

    @Override
    public void onCreate(){
        mSMSreceiver = new SMSReceiver();
        mIntentFilter = new IntentFilter();
        mIntentFilter.addAction("android.provider.Telephony.SMS_RECEIVED");
        registerReceiver(mSMSreceiver, mIntentFilter);
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d("SERVICE", "Started....");
        workThread = new Thread(){
            @Override
            public void run(){
                try {
                    Worker();
                }catch (Exception ex){
                    ex.printStackTrace();
                }
            }
        };
        workThread.start();
        Log.d("SERVICE", "This work is done.");
        return Service.START_STICKY;
    }

    private void Worker() throws InterruptedException {
        try {
            while (true) {
                while(!isMessageReceived){
                    if(smsMessages != null){
                        InformServerOnNewRequest();
                    }
                }
            }
        }catch(Exception ex){
            Log.e("SERVICE", "ERROR : " + ex.getMessage());
        }
    }

    private void InformServerOnNewRequest() {
        try{
            for (SmsMessage smsMessage : smsMessages) {

                String message = smsMessage.getMessageBody();
                requestModels = new ArrayList<RequestModel>();

                if (message != null && !message.isEmpty()) {
                    try {
                        List<String> items = Arrays.asList(message.split("\\s*,\\s*"));

                        if(items.get(0).equals("OLA")){
                            RequestModel request = new RequestModel();
                            request.AppAuthCode = items.get(3);
                            request.Lattitude = items.get(1);
                            request.Longitude = items.get(2);
                            request.Type = items.get(4);
                            request.Destination = items.get(5);
                            request.Counter = items.get(6);
                            Log.d("SERVICE", "recieved items - " + items.get(0) + "," + items.get(1) + "," + items.get(2) + "," + items.get(3) + ",");

                            requestModels.add(request);
                        }else{
                            Log.d("SERVICE", "Message not from OLA.");
                        }

                        if(requestModels.size() > 0){
                            PostToServer post = new PostToServer();
                            post.execute("");
                        }

                    }catch(Exception ex){
                        Log.e("SERVICE", ex.getMessage());
                    }
                }
            }

        }catch (Exception ex){
            Log.e("SERVICE", ex.getMessage());
        }

    }

    @Override
    public void onDestroy()
    {
        super.onDestroy();
        unregisterReceiver(mSMSreceiver);
    }

    public String POST(String url){
        InputStream inputStream = null;
        String result = "";
        try {

            HttpClient httpclient = new DefaultHttpClient();
            HttpPost httpPost = new HttpPost(url);
            String json = "";

            JSONObject jsonObject = new JSONObject();

            json = jsonObject.toString();

            StringEntity se = new StringEntity(json);

            httpPost.setEntity(se);

            httpPost.setHeader("Accept", "application/json");
            httpPost.setHeader("Content-type", "application/json");


            HttpResponse httpResponse = httpclient.execute(httpPost);

            inputStream = httpResponse.getEntity().getContent();

            if(inputStream != null)
                result = convertInputStreamToString(inputStream);
            else
                result = "Did not work!";

            return result;

        } catch (Exception e) {
            Log.v("Getter", e.getLocalizedMessage());
        }

        return result;
    }

    private static String convertInputStreamToString(InputStream inputStream) throws IOException {
        BufferedReader bufferedReader = new BufferedReader( new InputStreamReader(inputStream));
        String line = "";
        String result = "";
        while((line = bufferedReader.readLine()) != null)
            result += line;

        inputStream.close();
        return result;
    }


    private class PostToServer extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... urls) {
            return POST(urls[0]);
        }

        @Override
        protected void onPostExecute(String result) {
        }
    }

}

class SMSReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        Bundle myBundle = intent.getExtras();
        SmsMessage[] messages = null;
        String strMessage = "";

        if (myBundle != null)
        {
            Object [] pdus = (Object[]) myBundle.get("pdus");
            messages = new SmsMessage[pdus.length];

            for (int i = 0; i < messages.length; i++)
            {
                messages[i] = SmsMessage.createFromPdu((byte[]) pdus[i]);
                strMessage += "SMS From: " + messages[i].getOriginatingAddress();
                strMessage += " : ";
                strMessage += messages[i].getMessageBody();
                strMessage += "\n";
            }

            GatewayService.smsMessages = messages;
            GatewayService.isMessageReceived = true;
            Log.d("SERVICE", strMessage);
        }
    }
}
