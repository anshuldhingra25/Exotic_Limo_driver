package com.Hockeyapp;

import android.content.Context;
import android.os.Bundle;
import android.os.PowerManager;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;



/**
 * Created by Prem Kumar and Anitha on 11/12/2015.
 */
public class FragmentHockeyApp extends Fragment
{




    PowerManager.WakeLock mWakeLock;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        final PowerManager pm = (PowerManager)getActivity(). getSystemService(Context.POWER_SERVICE);
        this.mWakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "My Tag");
        this.mWakeLock.acquire();
    }





    @Override
    public void onDestroy() {
        super.onDestroy();

        if(mWakeLock != null){
            mWakeLock.release();
        }
    }

/*
  private static  String APP_ID = "9f8e1861d5cc413ba593e3367676bca3";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        checkForUpdates();
        return null;
    }

    @Override
    public void onResume() {
        super.onResume();
        checkForCrashes();
    }

    @Override
    public void onPause() {
        super.onPause();
        unregisterManagers();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unregisterManagers();
    }

    private void checkForCrashes() {
        CrashManager.register(getActivity(), APP_ID);
    }

    private void checkForUpdates() {
        // Remove this for store builds!
        UpdateManager.register(getActivity(), APP_ID);
    }

    private void unregisterManagers() {
        UpdateManager.unregister();
        // unregister other managers if necessary...
    }
*/
}

