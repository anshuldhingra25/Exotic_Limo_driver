package com.cabily.cabilydriver.subclass;

import android.os.Bundle;

import com.app.service.ServiceConstant;
import com.app.xmpp.ChatConfigurationBuilder;
import com.cabily.cabilydriver.adapter.ContinuousRequestAdapter;

/**
 * Created by Administrator on 3/17/2016.
 */
public class RealTimeActivity extends SubclassActivity {

    protected ChatConfigurationBuilder builder;
    protected String chatID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void onResume() {
        super.onResume();
        chatID = ContinuousRequestAdapter.userID + "@" + ServiceConstant.XMPP_SERVICE_NAME;
        builder = new ChatConfigurationBuilder(this);
        builder.createConnection();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (builder != null)
            builder.closeConnection();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (builder != null)
            builder.closeConnection();
    }
}
