package com.cabily.cabilydriver.adapter;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.location.Location;
import android.media.MediaPlayer;
import android.os.CountDownTimer;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.android.volley.Request;
import com.app.dao.DriverAlertDetails;
import com.app.service.ServiceConstant;
import com.app.service.ServiceManager;
import com.cabily.cabilydriver.ArrivedTrip;
import com.cabily.cabilydriver.DriverAlertActivity;
import com.cabily.cabilydriver.DriverMapActivity;
import com.cabily.cabilydriver.R;
import com.cabily.cabilydriver.Utils.SessionManager;
import com.cabily.cabilydriver.widgets.PkDialog;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

import at.grabner.circleprogress.CircleProgressView;
import me.drakeet.materialdialog.MaterialDialog;

/**
 * Created by Administrator on 11/23/2015.
 */
public class ContinuousRequestAdapter {

    private SessionManager sm;
    private LayoutInflater mInflater;
    private Activity context;
    private LinearLayout listview;

    private CircleProgressView mCircleView;
    private CountDownTimer timer;
    private String KEY1 = "key1";
    private String KEY2 = "key2";
    private String KEY3 = "key3";
    private String rider_id = "";
    private Dialog dialog;
    private int seconds = 0;
    private MediaPlayer mediaPlayer;
    private Location myLocation;
    private DriverAlertActivity.TimerCompletCallback timerCompletCallback;
    public class ViewHolder {
        public  int count;
        public Button accept;
        public  Button decline;
        public  CircleProgressView circularProgressBar;
        public  TextView cabily_alert_address;
        public  JSONObject data;
    }

    public ContinuousRequestAdapter(SessionManager sm, Activity context, Location myLocation,LinearLayout listview) {
        this.sm = sm;
        this.context = context;
        mInflater = LayoutInflater.from(context);
        this.listview = listview;
    }

    public void setTimerCompleteCallBack(DriverAlertActivity.TimerCompletCallback callBack){
        this.timerCompletCallback = callBack;
    }

    public View getView(int i, JSONObject data) {
        View view;
        ViewHolder holder;
        String data1 = " ";
        // JSONObject data = (JSONObject) getItem(i);
        view = mInflater.inflate(R.layout.driver_alert, null, false);
        holder = new ViewHolder();
        holder.data = data;
        holder.count = i;
        holder.accept = (Button) view.findViewById(R.id.cabily_driver_alert_accept_btn);
        holder.decline = (Button) view.findViewById(R.id.cabily_driver_alert_reject_btn);
        holder.cabily_alert_address = (TextView) view.findViewById(R.id.cabily_alert_address);
        holder.circularProgressBar = (CircleProgressView) view.findViewById(R.id.timer_circleView);
        view.setTag(holder);
        holder.accept.setTag(holder);
        holder.decline.setTag(holder);
        holder.accept.setOnClickListener(acceptBtnlistener);
        holder.decline.setOnClickListener(declineBtnListener);
        holder.cabily_alert_address.setText("" + getDataForPosition(i, KEY3,data));
        holder.circularProgressBar.setEnabled(false);
        String position = getDataForPosition(i, KEY3,data);
        holder.circularProgressBar.setFocusable(false);
        holder.circularProgressBar.setMaxValue(15);
        holder.circularProgressBar.setValueAnimated(0);
        holder.circularProgressBar.setTextSize(12);
        holder.circularProgressBar.setAutoTextSize(true);
        holder.circularProgressBar.setTextScale(0.6f);
        holder.circularProgressBar.setTextColor(context.getResources().getColor(R.color.progress_ripplecolor));
        mHandler.post(new CircularHandler(holder));
        return view;
    }

    private Handler mHandler = new Handler();

    private class CircularHandler implements Runnable {
        ViewHolder holder;
        float value = 15;
        boolean isRunning;

        public CircularHandler(ViewHolder holder) {
            this.holder = holder;
            isRunning = true;
        }

        @Override
        public void run() {
            if (isRunning) {
                value = value - 1;
                holder.circularProgressBar.setValue(value);
                mHandler.postDelayed(this, 1000);
                if (value == 0) {
                    mHandler.removeCallbacks(this);
                    if (timerCompletCallback != null){
                       // timerCompletCallback.timerCompleteCallBack(holder);
                    }
                    isRunning = false;
                }
            }
        }
    }

    private String getDataForPosition(int i, String key,JSONObject data) {
        try {
            return (String) data.get(key);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }


    private View.OnClickListener acceptBtnlistener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            ViewHolder holder = (ViewHolder) view.getTag();
            HashMap<String, String> jsonParams = new HashMap<String, String>();
            HashMap<String, String> userDetails = sm.getUserDetails();
            String driverId = userDetails.get(SessionManager.KEY_DRIVERID);
            rider_id = getDataForPosition(holder.count, KEY1,holder.data);
            //String address = getDataForPosition(i,KEY3);
            jsonParams.put("ride_id", "" + rider_id);
            jsonParams.put("driver_id", "" + driverId);
            if (DriverMapActivity.myLocation != null) {
                jsonParams.put("driver_lat", "" + DriverMapActivity.myLocation.getLatitude());
                jsonParams.put("driver_lon", "" + DriverMapActivity.myLocation.getLongitude());
                System.out.println("rideid---------" + rider_id);
                System.out.println("driver_id---------" + driverId);
                System.out.println("driver_lat---------" + DriverMapActivity.myLocation.getLatitude());
                System.out.println("driver_lon---------" + DriverMapActivity.myLocation.getLongitude());
            }
            ServiceManager manager = new ServiceManager(context, acceptServicelistener);
            manager.makeServiceRequest(ServiceConstant.ACCEPTING_RIDE_REQUEST, Request.Method.POST, jsonParams);
            showDialog();
            stopPlayer();
        }
    };

    ServiceManager.ServiceListener acceptServicelistener = new ServiceManager.ServiceListener() {
        @Override
        public void onCompleteListener(Object object) {
            dismissDialog();
            String Sstatus = "",SResponse="", Str_Username = "", Str_User_email = "", Str_Userphoneno = "", Str_Userrating = "", Str_userimg = "", Str_pickuplocation = "", Str_pickuplat = "", Str_pickup_long = "", Str_pickup_time = "", Str_message = "";

            if (object instanceof String) {
                String jsonString = (String) object;
                System.out.println("Responseaccept---------" + jsonString);
                try {
                    JSONObject object1 = new JSONObject(jsonString);
                    Sstatus = object1.getString("status");
                    if (Sstatus.equalsIgnoreCase("1")){
                        System.out.println("status-----------" + Sstatus);
                        JSONObject jobject = object1.getJSONObject("response");
                        JSONObject jobject2 = jobject.getJSONObject("user_profile");
                        Str_message = jobject.getString("message");
                        Str_Username = jobject2.getString("user_name");
                        Str_User_email = jobject2.getString("user_email");
                        Str_Userphoneno = jobject2.getString("phone_number");
                        Str_Userrating = jobject2.getString("user_review");
                        Str_pickuplocation = jobject2.getString("pickup_location");
                        Str_pickuplat = jobject2.getString("pickup_lat");
                        Str_pickup_long = jobject2.getString("pickup_lon");
                        Str_pickup_time = jobject2.getString("pickup_time");
                        Str_userimg = jobject2.getString("user_image");
                    }
                    if (Sstatus.equalsIgnoreCase("1")) {
                        Intent intent = new Intent(context, ArrivedTrip.class);
                        intent.putExtra("address", Str_pickuplocation);
                        intent.putExtra("rideId", rider_id);
                        intent.putExtra("pickuplat", Str_pickuplat);
                        intent.putExtra("pickup_long", Str_pickup_long);
                        intent.putExtra("username", Str_Username);
                        intent.putExtra("userrating", Str_Userrating);
                        intent.putExtra("phoneno", Str_Userphoneno);
                        intent.putExtra("userimg", Str_userimg);
                        context.startActivity(intent);
                        context.finish();
                    } else {
                        SResponse = object1.getString("response");
                        Alert(context.getString(R.string.alert_sorry_label_title), SResponse);
                        final PkDialog mdialog = new PkDialog(context);
                        mdialog.setDialogTitle(context.getString(R.string.alert_sorry_label_title));
                        mdialog.setDialogMessage(SResponse);
                        mdialog.setPositiveButton(context.getString(R.string.alert_label_ok), new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        mdialog.dismiss();

                                        //--------remove list after
                                        listview.removeViewAt(0);
                                        context.finish();


                                    }
                                }
                        );
                        mdialog.show();
                    }

                } catch (JSONException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                } catch (Exception e) {
                }


            }
        }

        @Override
        public void onErrorListener(Object error) {
            dismissDialog();
            context.finish();
        }
    };

    public void showDialog() {
        dialog = new Dialog(context);
        dialog.getWindow();
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.custom_loading);
        dialog.setCanceledOnTouchOutside(false);
        dialog.show();
    }

    public void dismissDialog() {
        try {
            if (dialog != null)
                dialog.dismiss();
        } catch (Exception e) {
        }
    }

    private View.OnClickListener declineBtnListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            stopPlayer();
            ViewHolder holder = (ViewHolder) view.getTag();
            try {
                if (timerCompletCallback != null){
                  holder.count = holder.count-1;
                    timerCompletCallback.timerCompleteCallBack(holder);
                }
            } catch (Exception e) {
            }
            context.finish();
        }
    };

    private void stopPlayer() {
        try {
            if (DriverAlertActivity.mediaPlayer != null && DriverAlertActivity.mediaPlayer.isPlaying()) {
                DriverAlertActivity.mediaPlayer.stop();
            }
        } catch (Exception e) {
        }
    }



    //--------------Alert Method-----------
    private void Alert(String title, String message) {
        final PkDialog mDialog = new PkDialog(context);
        mDialog.setDialogTitle(title);
        mDialog.setDialogMessage(message);
        mDialog.setPositiveButton(context.getString(R.string.alert_label_ok), new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mDialog.dismiss();
            }
        });
        mDialog.show();
    }


}
