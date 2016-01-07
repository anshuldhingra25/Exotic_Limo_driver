package com.cabily.cabilydriver.Utils;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.widget.Toast;

import com.app.xmpp.ChatingService;

/**
 * Created by user88 on 1/6/2016.
 */
public class NetworkChangeReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if(isOnline(context)){
            ChatingService.startDriverAction(context);
            Toast.makeText(context, "Internet connected", Toast.LENGTH_SHORT).show();
        }
        else
        {
            Toast.makeText(context,"Internet not connected",Toast.LENGTH_SHORT).show();
        }
    }
    public boolean isOnline(Context context) {

        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
//should check null because in air plan mode it will be null
        return (netInfo != null && netInfo.isConnected());

    }

}