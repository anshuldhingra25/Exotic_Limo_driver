package com.app.gcm;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.WakefulBroadcastReceiver;

import com.cabily.cabilydriver.DriverAlertActivity;
import com.google.android.gms.gcm.GoogleCloudMessaging;

/**
 */
public class GcmBroadcastReceiver extends WakefulBroadcastReceiver {


    @Override
    public void onReceive(Context context, Intent intent) {
        try {
            Intent i = new Intent(context, DriverAlertActivity.class);
            Bundle mBundle = intent.getExtras();
            String data = mBundle.toString();
            String key1 = (String) mBundle.get("Key1");
            String key2 = (String) mBundle.get("Key2");
            GoogleCloudMessaging gcm = GoogleCloudMessaging.getInstance(context);
            String messageType = gcm.getMessageType(intent);
            i.putExtra("extra", messageType);
            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(i);
        } catch (Exception e) {
        }
    }
}
