package com.appathon.gateway.olagatewayapp.gcm;

import android.app.IntentService;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import com.google.android.gms.gcm.GcmPubSub;

import java.io.IOException;

/**
 * Created by vikoo on 31/05/15.
 */
public class GcmRegistrationIntentService extends IntentService {

    private static final String TAG = GcmRegistrationIntentService.class.getSimpleName();
    private static final String[] TOPICS = {"global"};

    public GcmRegistrationIntentService() {
        super(TAG);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        try {
            // In the (unlikely) event that multiple refresh operations occur simultaneously,
            // ensure that they are processed sequentially.
            synchronized (TAG) {
                // [START get_token]
                String token ="";
                // registers with gcm
                if(GcmCommonUtilities.registerWithGcm(getApplicationContext())) {
                    token = GcmCommonUtilities.getRegistrationId(getApplicationContext());
                    if (sendRegistrationToServer(token)) {
                        // Subscribe to topic channels
                        subscribeTopics(token);
                    }
                }

//                // Subscribe to topic channels
//                subscribeTopics(token);
//
//                // You should store a boolean that indicates whether the generated token has been
//                // sent to your server. If the boolean is false, send the token to your server,
//                // otherwise your server should have already received the token.
//                sharedPreferences.edit().putBoolean(QuickstartPreferences.SENT_TOKEN_TO_SERVER, true).apply();
                // [END get_token]
            }
        } catch (Exception e) {
            Log.d(TAG, "Failed to complete token refresh");
            // If an exception happens while fetching the new token or updating our registration data
            // on a third-party server, this ensures that we'll attempt the update at a later time.
//            sharedPreferences.edit().putBoolean(QuickstartPreferences.SENT_TOKEN_TO_SERVER, false).apply();
        }

    }

    /**
     * Persist registration to third-party servers.
     *
     * Modify this method to associate the user's GCM registration token with any server-side account
     * maintained by your application.
     *
     * @param token The new token.
     */
    private boolean sendRegistrationToServer(String token) {
        // todo send reg id to server
//        return HttpUtils.updateRegIdForUser(UserData.getUserDataInstance(getApplicationContext()), getApplicationContext());
        return true;
    }

    /**
     * Subscribe to any GCM topics of interest, as defined by the TOPICS constant.
     *
     * @param token GCM token
     * @throws IOException if unable to reach the GCM PubSub service
     */
    // [START subscribe_topics]
    private void subscribeTopics(String token) throws IOException {
        for (String topic : TOPICS) {
            GcmPubSub pubSub = GcmPubSub.getInstance(this);
            pubSub.subscribe(token, "/topics/" + topic, null);
        }
    }
    // [END subscribe_topics]

}
