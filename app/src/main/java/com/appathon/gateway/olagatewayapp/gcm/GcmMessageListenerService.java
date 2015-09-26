package com.appathon.gateway.olagatewayapp.gcm;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.appathon.gateway.olagatewayapp.R;
import com.appathon.gateway.olagatewayapp.utilities.SmsSender;
import com.google.android.gms.gcm.GcmListenerService;


/**
 * This {@code IntentService} does the actual handling of the GCM message.
 * {@code GcmBroadcastReceiver} (a {@code WakefulBroadcastReceiver}) holds a
 * partial wake lock for this service while the service does its work. When the
 * service is finished, it calls {@code completeWakefulIntent()} to release the
 * wake lock.
 */
public class GcmMessageListenerService extends GcmListenerService {

    private static final String TAG = GcmMessageListenerService.class.getSimpleName();


    @Override
    public void onMessageReceived(String from, Bundle data) {

        Log.d(TAG, "From: " + from);
        String driverName = data.getString("driver_name");
        String driverNumber = data.getString("driver_number");
        String cabNumber = data.getString("cab_number");
        if(driverName != null) {
            //SmsSender.sendSms("", "Say Ola to your driver " + driverName + "(" + driverNumber + ") for CAB - (" + cabNumber + ")");
            sendNotification(data);
        }
    }
    // [END receive_message]


    // Put the message into a notification and post it.
    // This is just one simple example of what you might choose to do with
    // a GCM message.
    private void sendNotification(Bundle intent) {

        showLocationEnableNotification(getApplicationContext());
    }

    public void showLocationEnableNotification(Context context) {
        Intent resultIntent = new Intent(
                Settings.ACTION_LOCATION_SOURCE_SETTINGS);
        PendingIntent resultPendingIntent = PendingIntent.getActivity(context,
                0, resultIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(
                context).setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(context.getResources().getString(R.string.app_name))
                .setContentText("Cab Notification")
                .setContentIntent(resultPendingIntent);

        sendNotification(context, 1, mBuilder);
    }

    private void sendNotification(Context context, int id, NotificationCompat.Builder builder) {
        // Gets an instance of the NotificationManager service
        NotificationManager mNotifyMgr =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        Notification notification = builder.build();

        // default one
        notification.defaults |= Notification.DEFAULT_SOUND;
        notification.defaults |= Notification.DEFAULT_VIBRATE;
        mNotifyMgr.notify(id, notification);
    }
}
