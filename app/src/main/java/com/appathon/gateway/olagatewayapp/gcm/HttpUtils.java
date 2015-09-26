package com.appathon.gateway.olagatewayapp.gcm;

import android.content.Context;
import android.telephony.TelephonyManager;
import android.util.Log;

import com.appathon.gateway.olagatewayapp.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;

public class HttpUtils {

    private static final String TAG = "HttpUtils";
    private static final String serverVersion = "dev-1/";
    private static final String serverUrl = "http://172.16.1.62/SSFM/api/SSFM/";
    private static String registrationUrl = serverUrl;


    public static String makeRequest(String uri, String json) {
        HttpURLConnection urlConnection;
        String url;
        String data = json;
        String result = null;
        try {
            //Connect
            urlConnection = (HttpURLConnection) ((new URL(uri).openConnection()));
            urlConnection.setDoOutput(true);
            urlConnection.setRequestProperty("Content-Type", "application/json");
            urlConnection.setRequestProperty("Accept", "application/json");
            urlConnection.setConnectTimeout(40000);
            urlConnection.setReadTimeout(40000);
            urlConnection.setRequestMethod("POST");
            urlConnection.connect();

            //Write
            OutputStream outputStream = urlConnection.getOutputStream();
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(outputStream, "UTF-8"));
            writer.write(data);
            writer.close();
            outputStream.close();

            //Read
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream(), "UTF-8"));

            String line = null;
            StringBuilder sb = new StringBuilder();

            while ((line = bufferedReader.readLine()) != null) {
                sb.append(line);
            }

            bufferedReader.close();
            result = sb.toString();

        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return result;
    }


    // this function will register new user to server
    public static boolean registerNewUser(Context ctx, String id) {
        JSONObject request = new JSONObject();
        TelephonyManager telephonyManager = (TelephonyManager)ctx.getSystemService(Context.TELEPHONY_SERVICE);
        String imei = telephonyManager.getDeviceId();
        try {

            request.put("_id", id);
            request.put("_imei", imei);
//            registrationUrl += ""
            Log.d(TAG, "registerNewUser:" + request.toString());
            String receivedResponse = makeRequest(registrationUrl, request.toString());

            Log.d(TAG, "RECEIVED RESPONSE FOR registerNewUser request = " + receivedResponse);
            if (receivedResponse != null) {
                if(receivedResponse.contains("true")) {
                    return true;
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return false;
    }


    public static void sendPush(String gcmId, String apiKey, String id, Context context){
        String gcmUrl = "https://gcm-http.googleapis.com/gcm/send";
        gcmId = GcmCommonUtilities.getRegistrationId(context);
        apiKey = context.getString(R.string.api_key);
        JSONObject mainObject = new JSONObject();
        JSONObject dataObject = new JSONObject();
        try {
            dataObject.put("id", id);
            mainObject.put("data", dataObject);
            mainObject.put("to", gcmId);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        String gcmPush = mainObject.toString();
        Log.i(TAG, "push:"+gcmPush);
        Log.i(TAG, "url:"+gcmUrl);
        HttpURLConnection urlConnection;
        try {
            //Connect
            urlConnection = (HttpURLConnection) ((new URL(gcmUrl).openConnection()));
            urlConnection.setDoOutput(true);
            urlConnection.setRequestMethod("POST");
            urlConnection.setRequestProperty("Content-Type", "application/json");
            urlConnection.setRequestProperty("Authorization", "key="+apiKey);
            urlConnection.connect();

//            //Write
            OutputStream outputStream = urlConnection.getOutputStream();
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(outputStream, "UTF-8"));
            writer.write(gcmPush);
            writer.close();
            outputStream.close();

            int status = urlConnection.getResponseCode();
            Log.i(TAG, "code" +status);
            //Read
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));

            String line = null;
            StringBuilder sb = new StringBuilder();

            while ((line = bufferedReader.readLine()) != null) {
                sb.append(line);
            }

            bufferedReader.close();
//            result = sb.toString();
            Log.i(TAG, "result" +sb.toString());

        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

}
