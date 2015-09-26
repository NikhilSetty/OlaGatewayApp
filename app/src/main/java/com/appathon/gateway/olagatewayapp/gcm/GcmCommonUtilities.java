package com.appathon.gateway.olagatewayapp.gcm;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.StrictMode;
import android.util.Log;

import com.appathon.gateway.olagatewayapp.R;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.google.android.gms.iid.InstanceID;

import java.io.IOException;

/*
 *	Class contains utility info for GCM Server
 */
public class GcmCommonUtilities {


    // Google project id
//    public static final String SENDER_ID = "977048351640";

    /**
     * Tag used on log messages.
     */
    public static final String TAG = "Common Utilities";
    public static final String DISPLAY_MESSAGE_ACTION = "com.appathon.gateway.olagatewayapp.DISPLAY_MESSAGE";
    public static final String EXTRA_MESSAGE = "message";

    public final static int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;
    private static final String SHARED_PREF_KEY = "OLA_GCM_SETTINGS";
    public static final String PROPERTY_REG_ID = "registration_id";
    private static final String PROPERTY_APP_VERSION = "appVersion";

    /**
     * Notifies UI to display a message.
     * <p>
     * This method is defined in the common helper because it's used both by the
     * UI and the background service.
     *
     * @param context application's context.
     * @param message message to be displayed.
     */
    public static void displayMessage(Context context, String message) {
        Intent intent = new Intent(DISPLAY_MESSAGE_ACTION);
        intent.putExtra(EXTRA_MESSAGE, message);
        context.sendBroadcast(intent);
    }

    public static Dialog checkPlayServices(Activity activity) {
        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(activity);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {
                return GooglePlayServicesUtil.getErrorDialog(resultCode, activity,
                        PLAY_SERVICES_RESOLUTION_REQUEST);
            } else {
                Log.i(TAG, "This device is not supported.");
                return GooglePlayServicesUtil.getErrorDialog(ConnectionResult.SERVICE_MISSING, activity,
                        PLAY_SERVICES_RESOLUTION_REQUEST);
            }
        }
        return null;
    }

    public static String getRegistrationId(Context context) {
        final SharedPreferences prefs = getGCMPreferences(context);
        String registrationId = prefs.getString(PROPERTY_REG_ID, "");
        if (registrationId.isEmpty()) {
            Log.d(TAG, "Registration ID not found.");
            return "";
        }
        int registeredVersion = prefs.getInt(PROPERTY_APP_VERSION, Integer.MIN_VALUE);
        int currentVersion = getAppVersion(context);
        if (registeredVersion != currentVersion) {
            Log.d(TAG, "App version changed.");
            return "";
        }
        return registrationId;
    }

    public static SharedPreferences getGCMPreferences(Context context) {
        return context.getSharedPreferences(SHARED_PREF_KEY,
                Context.MODE_PRIVATE);
    }

    private static int getAppVersion(Context context) {
        try {
            PackageInfo packageInfo = context.getPackageManager()
                    .getPackageInfo(context.getPackageName(), 0);
            return packageInfo.versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            throw new RuntimeException("Could not get package name: " + e);
        }
    }

    public static void setStrictMode(){
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
    }

    // stores register id locally
    public static boolean registerWithGcm(Context context){
        try {
//            if(getRegistrationId(context).isEmpty()) {
                // Initially this call goes out to the network to retrieve the token, subsequent calls
                // are local.
                InstanceID instanceID = InstanceID.getInstance(context);
                String regId = instanceID.getToken(context.getString(R.string.gcm_defaultSenderId),
                        GoogleCloudMessaging.INSTANCE_ID_SCOPE, null);
                Log.i(TAG, "####GCM Registration Token: " + regId);
                if (regId == null || regId.isEmpty()) {
                    Log.d(TAG, "NO GOOGLE PLAY SERVICES AVAIALBLE");
                    return false;
                } else {
//                    UserData ud = UserData.getUserDataInstance(context);
//                    ud.setGCMRegId(regId);
//                    if(HttpUtils.registerNewUser(context, regId)) {
                        storeRegistrationId(context, regId);
                        return true;
//                    }
//                    return false;
                }
//            }else{
//                Log.d(TAG, "Already registered id:" + getRegistrationId(context));
//                return true;
//            }
        } catch (IOException ex) {
            Log.e(TAG, "Exception while registering with GSM:");
            ex.printStackTrace();
            return false;
        }
    }

    public static void storeRegistrationId(Context context, String regId) {
        final SharedPreferences prefs = getGCMPreferences(context);
        int appVersion = getAppVersion(context);
        Log.i(TAG, "Saving regId on app version " + appVersion);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(PROPERTY_REG_ID, regId);
        editor.putInt(PROPERTY_APP_VERSION, appVersion);
        editor.commit();
    }
}
