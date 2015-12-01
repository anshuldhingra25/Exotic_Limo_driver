package com.app.gcm;

import android.app.IntentService;
import android.content.Intent;
import android.os.Bundle;

import com.google.android.gms.gcm.GoogleCloudMessaging;

/**
 *
 */

public class GCMNotificationIntentService extends IntentService {

    GCMIntentManager  notificationManager ;

    public GCMNotificationIntentService() {
        super("GCMNotificationIntentService");
    }

    public GCMNotificationIntentService(String name) {
        super(name);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Bundle extras = intent.getExtras();
        GoogleCloudMessaging gcm = GoogleCloudMessaging.getInstance(this);
        String messageType = gcm.getMessageType(intent);
        notificationManager = new GCMIntentManager(getApplicationContext());
        if (!extras.isEmpty() && GoogleCloudMessaging.MESSAGE_TYPE_MESSAGE.equals(messageType)) {
            notificationManager.sendNotification(extras.toString());
        }
        GcmBroadcastReceiver.completeWakefulIntent(intent);
    }


}