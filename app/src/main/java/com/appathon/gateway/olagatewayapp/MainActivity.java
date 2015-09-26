package com.appathon.gateway.olagatewayapp;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.appathon.gateway.olagatewayapp.gcm.GcmCommonUtilities;
import com.appathon.gateway.olagatewayapp.gcm.HttpUtils;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Dialog playServiceDialog = GcmCommonUtilities.checkPlayServices(MainActivity.this);
        if (playServiceDialog != null) {
            playServiceDialog.show();
            playServiceDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
                @Override
                public void onCancel(DialogInterface dialog) {
                    finish();
                }
            });
            playServiceDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                @Override
                public void onDismiss(DialogInterface dialog) {
                    finish();
                }
            });
            return;
        }

        register();
        sendPush();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void StartService(View v){
//        startService(new Intent(this, GatewayService.class));
    }

    public void register() {
        new AsyncTask<Void, Void, Boolean>() {
            @Override
            protected Boolean doInBackground(Void... params) {
                if (GcmCommonUtilities.getRegistrationId(getApplicationContext()).isEmpty()) {
                    return GcmCommonUtilities.registerWithGcm(getApplicationContext());

                } else {
                    return true;
                }
            }

            @Override
            protected void onPostExecute(Boolean isDone) {
                super.onPostExecute(isDone);
                if (isDone) {
                    Toast.makeText(getApplicationContext(),
                            "Registered...", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getApplicationContext(),
                            "Oopps Registration failed...", Toast.LENGTH_SHORT).show();
                }
            }
        }.execute();
    }

    public void sendPush() {
        new AsyncTask<Void, Void, Boolean>() {
            @Override
            protected Boolean doInBackground(Void... params) {
                Log.i("main activity", "sending push");
                HttpUtils.sendPush(GcmCommonUtilities.getRegistrationId(getApplicationContext()),
                        "", "4", getApplicationContext());
                Log.i("main activity", "push sent");
                return false;
            }

            @Override
            protected void onPostExecute(Boolean isDone) {
                super.onPostExecute(isDone);
//                if(isDone){
//                    Toast.makeText(getApplicationContext(),
//                            "Registered...", Toast.LENGTH_SHORT).show();
//                }else{
//                    Toast.makeText(getApplicationContext(),
//                            "Oopps Registration failed...", Toast.LENGTH_SHORT).show();
//                }
            }
        }.execute();
    }
}
