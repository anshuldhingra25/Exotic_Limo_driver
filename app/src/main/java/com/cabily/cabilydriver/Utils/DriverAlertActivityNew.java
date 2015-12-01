package com.cabily.cabilydriver.Utils;

import android.app.Activity;
import android.os.Bundle;

import com.cabily.cabilydriver.R;
import com.cabily.cabilydriver.adapter.DriverAlertAdapterNew;

/**
 * Created by user88 on 10/13/2015.
 */
public class DriverAlertActivityNew extends Activity {

    DriverAlertAdapterNew adapter_new;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_driver_alert);


    }


}
