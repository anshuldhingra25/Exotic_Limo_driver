package com.Hockeyapp;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.PowerManager;


/**
 * Created by Prem Kumar and Anitha on 11/12/2015.
 */
public class ActivityHockeyApp extends Activity
{


    PowerManager.WakeLock mWakeLock;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        this.mWakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "My Tag");
        this.mWakeLock.acquire();
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();

        if(mWakeLock != null){
            mWakeLock.release();
        }
    }
}
