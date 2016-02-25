package com.cabily.cabilydriver.adapter;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.location.Location;
import android.media.MediaPlayer;
import android.os.CountDownTimer;
import android.os.Handler;
import android.util.Log;
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
import at.grabner.circleprogress.TextMode;
import me.drakeet.materialdialog.MaterialDialog;

/**
 * Created by Administrator on 11/23/2015.
 */
public class ContinuousRequestAdapter {
    private SessionManager sm;
    private LayoutInflater mInflater;
    private Activity context;
    private LinearLayout listview;
    public int req_count;
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
    private Handler mHandler = new Handler();
    public static String userID;

    public class ViewHolder {
        public  int count;
        public Button accept;
        public  Button decline;
        public  CircleProgressView circularProgressBar;
        public  TextView cabily_alert_address;
        private  LinearLayout Ll_ride_Requst_layout;
        public  JSONObject data;


    }

    public ContinuousRequestAdapter(Activity context, Location myLocation,LinearLayout listview) {
        this.context = context;
        sm = new SessionManager(context);
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
        holder.Ll_ride_Requst_layout  = (LinearLayout)view.findViewById(R.id.Ll_ride_request_layout);
        view.setTag(holder);

        HashMap<String, Integer> user = sm.getRequestCount();
        req_count = user.get(SessionManager.KEY_COUNT);

        System.out.println("---------------req_count top-------------------"+req_count);

        holder.accept.setTag(holder);
        holder.decline.setTag(holder);
        holder.accept.setOnClickListener(acceptBtnlistener);
        holder.decline.setOnClickListener(new DeclineBtnListener(i));
        holder.cabily_alert_address.setText("" + getDataForPosition(i, KEY3,data));
        holder.circularProgressBar.setEnabled(false);
        String position = getDataForPosition(i, KEY3,data);
        holder.circularProgressBar.setFocusable(false);
        holder.circularProgressBar.setMaxValue(Integer.parseInt(getDataForPosition(i, KEY2,data)));
        holder.circularProgressBar.setValueAnimated(0);
        holder.circularProgressBar.setTextSize(30);
        holder.circularProgressBar.setAutoTextSize(true);
        holder.circularProgressBar.setTextScale(0.6f);
        holder.circularProgressBar.setTextColor(context.getResources().getColor(R.color.progress_ripplecolor));
        mHandler.post(new CircularHandler(holder,Integer.parseInt(getDataForPosition(i, KEY2,data))));
        return view;
    }

    private class CircularHandler implements Runnable {
        ViewHolder holder;
        Integer value ;
        boolean isRunning;

        public CircularHandler(ViewHolder holder,Integer val) {
            this.holder = holder;
            isRunning = true;
            value=val;
        }

        @Override
        public void run() {
            if (isRunning) {
                value = value - 1;

                holder.circularProgressBar.setText(String.valueOf(Math.abs(value)));
                holder.circularProgressBar.setTextMode(TextMode.TEXT);
                holder.circularProgressBar.setValueAnimated(value, 500);
                mHandler.postDelayed(this, 1000);

                if (value == 0) {
                    mHandler.removeCallbacks(this);
                    if (timerCompletCallback != null){
                        holder.Ll_ride_Requst_layout.setVisibility(View.GONE);

                        System.out.println("requestcount2 above------------------"+req_count);

                        req_count=req_count-1;
                        SessionManager session=new SessionManager(context);
                        session.setRequestCount(req_count);

                        System.out.println("requestcount2 below------------------"+req_count);

                        if(req_count==0)
                        {

                            System.out.println("activity  finished------------------"+req_count);
                            sm.setRequestCount(0);
                            context.finish();
                            stopPlayer();
                            context.overridePendingTransition(R.anim.fade_in,R.anim.fade_out);
                        }
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
            System.out.println("acceptrideurl------------"+ServiceConstant.ACCEPTING_RIDE_REQUEST);
            showDialog();
            stopPlayer();
        }
    };

    ServiceManager.ServiceListener acceptServicelistener = new ServiceManager.ServiceListener() {
        @Override
        public void onCompleteListener(Object object) {
            dismissDialog();
            String Sstatus = "",SResponse="", Str_Username = "",Str_UserId="", Str_User_email = "", Str_Userphoneno = "", Str_Userrating = "", Str_userimg = "", Str_pickuplocation = "", Str_pickuplat = "", Str_pickup_long = "",
                    Str_pickup_time = "", Str_message = "",Str_droplat="",Str_droplon="",str_drop_location="";

            if (object instanceof String) {
                String jsonString = (String) object;
                System.out.println("Responseaccept---------" + jsonString);

                Log.e("accept",jsonString);

                try {
                    JSONObject object1 = new JSONObject(jsonString);
                    Sstatus = object1.getString("status");
                    if (Sstatus.equalsIgnoreCase("1")){
                        System.out.println("status-----------" + Sstatus);
                        JSONObject jobject = object1.getJSONObject("response");
                        JSONObject jobject2 = jobject.getJSONObject("user_profile");
                        Str_message = jobject.getString("message");
                        Str_Username = jobject2.getString("user_name");

                       //Str_UserId = jobject2.getString("user_id");
                        userID = jobject2.getString("user_id");

                        Str_User_email = jobject2.getString("user_email");
                        Str_Userphoneno = jobject2.getString("phone_number");
                        Str_Userrating = jobject2.getString("user_review");
                        Str_pickuplocation = jobject2.getString("pickup_location");
                        Str_pickuplat = jobject2.getString("pickup_lat");
                        Str_pickup_long = jobject2.getString("pickup_lon");
                        Str_pickup_time = jobject2.getString("pickup_time");
                        Str_userimg = jobject2.getString("user_image");
                        Str_droplat = jobject2.getString("drop_lat");
                        Str_droplon = jobject2.getString("drop_lon");
                        str_drop_location = jobject2.getString("drop_loc");
                       // System.out.println("userid-------------"+Str_UserId);

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
                        intent.putExtra("UserId",Str_UserId);
                        intent.putExtra("drop_lat",Str_droplat);
                        intent.putExtra("drop_lon",Str_droplon);
                        intent.putExtra("drop_location",str_drop_location);

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




    public class DeclineBtnListener implements View.OnClickListener
    {

        int mPosition;
        DeclineBtnListener(int position)
        {
            mPosition=position;
        }
        @Override
        public void onClick(View v) {

            ViewHolder holder = (ViewHolder) v.getTag();
            try {

                if (timerCompletCallback != null){
                    holder.count = holder.count-1;

                    HashMap<String, Integer> user = sm.getRequestCount();
                    int req_count = user.get(SessionManager.KEY_COUNT);
                    req_count=req_count-1;

                    System.out.println("----------inside declineBtnListener req_count----------------"+req_count);

                    sm.setRequestCount(req_count);
                    holder.Ll_ride_Requst_layout.setVisibility(View.GONE);

                    if(req_count==0)
                    {
                        sm.setRequestCount(0);
                        context.finish();
                        if (DriverAlertActivity.mediaPlayer != null && DriverAlertActivity.mediaPlayer.isPlaying()) {
                            DriverAlertActivity.mediaPlayer.stop();
                        }
                    }

                    //req_count=req_count-1;
                    // sm.setRequestCount(req_count);

                    // timerCompletCallback.timerCompleteCallBack(holder);

                }
            } catch (Exception e) {
            }
        }
    }


}
