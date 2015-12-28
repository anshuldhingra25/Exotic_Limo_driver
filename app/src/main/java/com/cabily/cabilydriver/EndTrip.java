package com.cabily.cabilydriver;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.provider.Settings;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.app.xmpp.ChatingService;
import com.app.service.ServiceConstant;
import com.cabily.cabilydriver.Utils.AppController;
import com.cabily.cabilydriver.Utils.ConnectionDetector;
import com.cabily.cabilydriver.Utils.GPSTracker;
import com.cabily.cabilydriver.Utils.SessionManager;
import com.cabily.cabilydriver.Utils.VolleyErrorResponse;
import com.cabily.cabilydriver.subclass.SubclassActivity;
import com.cabily.cabilydriver.widgets.PkDialog;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.vision.barcode.Barcode;

import org.json.JSONObject;

import java.text.NumberFormat;
import java.util.Currency;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import me.drakeet.materialdialog.MaterialDialog;

/**
 * Created by user88 on 10/29/2015.
 */
public class EndTrip extends SubclassActivity {
    private String driver_id = "";
    private Boolean isInternetPresent = false;
    private ConnectionDetector cd;
    private SessionManager session;
    private TextView Tv_name,Tv_mobilno,Tv_rideid,Tv_start_wait,Tv_stop_wait;
    private RelativeLayout Rl_layout_back;
    private Button Bt_Endtrip;
    private String Str_name="",Str_mobilno="",Str_rideid="";
    private RelativeLayout alert_layout;
    private TextView alert_textview;
    private  Button Bt_End_trip;
    private GoogleMap googleMap;
    Dialog dialog;
    StringRequest postrequest;
    MediaPlayer mediaPlayer;

    private int mins;
    private  int secs;
    private  int milliseconds;

    private Button Bt_Enable_voice;


    private  String Str_status = "",Str_response="",Str_ridefare="",Str_timetaken="",Str_waitingtime="",Str_need_payment="",Str_currency="",Str_ride_distance="";
    GPSTracker gps;
    LatLng fromPosition,toposition;
    private double MyCurrent_lat = 0.0, MyCurrent_long = 0.0;
    private TextView timerValue;
    private  RelativeLayout layout_timer;
    private long startTime = 0L;
    private Handler customHandler = new Handler();
    long timeInMilliseconds = 0L;
    long timeSwapBuff = 0L;
    long updatedTime = 0L;
    LatLng start = new LatLng(18.015365, -77.499382);
    LatLng waypoint= new LatLng(18.01455, -77.499333);
    LatLng end = new LatLng(18.012590, -77.500659);
    float[] results;
    LocationManager locationManager;
       Barcode.GeoPoint geoPoint;

    double location;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.endtrip);
        initialize();
        initilizeMap();

        //Starting Xmpp service
        ChatingService.startDriverAction(EndTrip.this);

        Bt_End_trip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cd = new ConnectionDetector(EndTrip.this);
                isInternetPresent = cd.isConnectingToInternet();
                if (isInternetPresent) {
                    PostRequest(ServiceConstant.endtrip_url);
                    System.out.println("end------------------" + ServiceConstant.endtrip_url);
                } else {

                    Alert(getResources().getString(R.string.alert_sorry_label_title), getResources().getString(R.string.alert_nointernet));
                }
            }
        });


        Tv_start_wait.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Tv_stop_wait.setVisibility(View.VISIBLE);
                Tv_start_wait.setVisibility(View.GONE);

                layout_timer.setVisibility(View.VISIBLE);

                startTime = SystemClock.uptimeMillis();
                customHandler.postDelayed(updateTimerThread, 0);


            }
        });

       Tv_stop_wait.setOnClickListener(new View.OnClickListener() {
           @Override
           public void onClick(View v) {
               Tv_start_wait.setVisibility(View.VISIBLE);
               Tv_stop_wait.setVisibility(View.GONE);

               timeSwapBuff += timeInMilliseconds;
               customHandler.removeCallbacks(updateTimerThread);

           }
       });


        Bt_Enable_voice.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(android.content.Intent.ACTION_VIEW, Uri.parse("http://maps.google.com/maps?saddr=20.344,34.34&daddr=20.5666,45.345")); startActivity(intent);
            }
        });


    }

    private void initialize() {
        session = new SessionManager(EndTrip.this);
        gps = new GPSTracker(EndTrip.this);
        // get user data from session
        HashMap<String, String> user = session.getUserDetails();
        driver_id = user.get(SessionManager.KEY_DRIVERID);

      /*  if (isInternetPresent=true)
        {
         }else {
            mediaPlayer = MediaPlayer.create(this,R.raw.jinngle);
        }
*/
        Intent i = getIntent();
        Str_rideid = i.getStringExtra("rideid");
        Str_name  = i.getStringExtra("name");
        Str_mobilno = i.getStringExtra("mobilno");

        Tv_name = (TextView)findViewById(R.id.end_trip_name);
        Tv_mobilno = (TextView)findViewById(R.id.end_trip_mobilno);
        Tv_rideid = (TextView)findViewById(R.id.beginendtrip_rideid);
        Bt_End_trip = (Button)findViewById(R.id.btn_end_trip);
        Tv_start_wait = (TextView)findViewById(R.id.begin_waitingtime_tv_start);
        Tv_stop_wait = (TextView)findViewById(R.id.begin_waitingtime_tv_stop);
        timerValue = (TextView)findViewById(R.id.timerValue);
        layout_timer = (RelativeLayout)findViewById(R.id.layout_timer);
        Bt_Enable_voice = (Button)findViewById(R.id.Enable_voice_button);



        alert_layout = (RelativeLayout)findViewById(R.id.end_trip_alert_layout);
        alert_textview = (TextView)findViewById(R.id.end_trip_alert_textView);

        Tv_name.setText(Str_name);
        Tv_mobilno.setText(Str_mobilno);
        //Tv_rideid.setText(Str_rideid);

    }

    //--------------Alert Method-----------
    private void Alert(String title, String message) {
        final PkDialog mDialog = new PkDialog(EndTrip.this);
        mDialog.setDialogTitle(title);
        mDialog.setDialogMessage(message);
        mDialog.setPositiveButton(getResources().getString(R.string.alert_label_ok), new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mDialog.dismiss();
            }
        });
        mDialog.show();
    }


    private Runnable updateTimerThread = new Runnable() {

        public void run() {

            timeInMilliseconds = SystemClock.uptimeMillis() - startTime;

            updatedTime = timeSwapBuff + timeInMilliseconds;

            secs = (int) (updatedTime / 1000);
            mins = secs / 60;
            secs = secs % 60;
            milliseconds = (int) (updatedTime % 1000);
            timerValue.setText("" + mins + ":"
                    + String.format("%02d", secs));

           /* timerValue.setText("" + mins + ":"
                    + String.format("%02d", secs) + ":"
                    + String.format("%03d", milliseconds));*/

            customHandler.postDelayed(this, 0);
        }

    };


    private void initilizeMap() {
        if (googleMap == null) {
            googleMap = ((MapFragment)EndTrip.this.getFragmentManager().findFragmentById(R.id.arrived_trip_view_map)).getMap();
            // check if map is created successfully or not
            if (googleMap == null) {
                Toast.makeText(EndTrip.this, getResources().getString(R.string.action_alert_unabletocreatemap), Toast.LENGTH_SHORT).show();
            }
        }
        // Changing map type
        googleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        // Showing / hiding your current location
        googleMap.setMyLocationEnabled(false);
        // Enable / Disable zooming controls
        googleMap.getUiSettings().setZoomControlsEnabled(false);
        // Enable / Disable my location button
        googleMap.getUiSettings().setMyLocationButtonEnabled(false);
        // Enable / Disable Compass icon
        googleMap.getUiSettings().setCompassEnabled(false);
        // Enable / Disable Rotate gesture
        googleMap.getUiSettings().setRotateGesturesEnabled(true);
        // Enable / Disable zooming functionality
        googleMap.getUiSettings().setZoomGesturesEnabled(true);
        googleMap.setMyLocationEnabled(false);


        if (gps.canGetLocation()) {
            double Dlatitude = gps.getLatitude();
            double Dlongitude = gps.getLongitude();

            MyCurrent_lat = Dlatitude;
            MyCurrent_long = Dlongitude;

            // Move the camera to last position with a zoom level
            CameraPosition cameraPosition = new CameraPosition.Builder().target(new LatLng(Dlatitude, Dlongitude)).zoom(17).build();
            googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));

            // create marker double Dlatitude = gps.getLatitude();
            MarkerOptions marker = new MarkerOptions().position(new LatLng(Dlatitude, Dlongitude));
            marker.icon(BitmapDescriptorFactory.fromResource(R.drawable.cargreens));

            fromPosition = new LatLng(MyCurrent_lat, MyCurrent_long);

            // adding marker
            googleMap.addMarker(marker);

            System.out.println("currntlat----------" + MyCurrent_lat);
            System.out.println("currntlon----------" + MyCurrent_long);

        } else {
            alert_layout.setVisibility(View.VISIBLE);
            alert_textview.setText(getResources().getString(R.string.alert_gpsEnable));
        }
    }



    public static final float[] calculateDistanceTo(Location fromLocation, Location toLocation) {
        float[] results = new float[0];

        double startLatitude = fromLocation.getLatitude();
        double startLongitude = fromLocation.getLongitude();

        double endLatitude = toLocation.getLatitude();
        double endLongitude = toLocation.getLongitude();

        fromLocation.distanceBetween(startLatitude, startLongitude, endLatitude, endLongitude, results);

        return results;
    }


    /*
    public double getDistance(double a[]) {
        double earthRadius = 6371; //kilometers
        double dLat = Math.toRadians(a[2] -a[0] );
        double dLng = Math.toRadians(a[3] - a[1]);
        double b = Math.sin(dLat/2) * Math.sin(dLat/2) +
                Math.cos(Math.toRadians(a[0])) * Math.cos(Math.toRadians(a[2])) *
                        Math.sin(dLng/2) * Math.sin(dLng/2);
        double c = 2 * Math.atan2(Math.sqrt(b), Math.sqrt(1-b));
        float dist = (float) (earthRadius * c);

        return dist;
    }
*/




    //-------------------Show Summery fare  Method--------------------
    private void showfaresummerydetails() {

            final MaterialDialog dialog = new MaterialDialog(EndTrip.this);
            View view = LayoutInflater.from(EndTrip.this).inflate(R.layout.fare_summery_alert_dialog, null);

            TextView tv_fare_totalamount = (TextView) view.findViewById(R.id.fare_summery_total_amount);
            TextView tv_ridedistance = (TextView) view.findViewById(R.id.fare_summery_ride_distance_value);
            TextView tv_timetaken = (TextView) view.findViewById(R.id.fare_summery_ride_timetaken_value);
            TextView tv_waittime = (TextView) view.findViewById(R.id.fare_summery_wait_time_value);
            RelativeLayout layout_request_payment = (RelativeLayout)view.findViewById(R.id.layout_faresummery_requstpayment);
            RelativeLayout layout_receive_cash = (RelativeLayout)view.findViewById(R.id.fare_summery_receive_cash_layout);

         tv_fare_totalamount.setText(Str_ridefare);
         tv_ridedistance.setText(Str_ride_distance);
         tv_timetaken.setText(Str_timetaken);
         tv_waittime.setText(Str_waitingtime);
         dialog.setView(view).show();

        layout_receive_cash.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(EndTrip.this, OtpPage.class);
                intent.putExtra("rideid", Str_rideid);
                startActivity(intent);
                overridePendingTransition(R.anim.fade_in, R.anim.fade_out);

            }
        });

        layout_request_payment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cd = new ConnectionDetector(EndTrip.this);
                isInternetPresent = cd.isConnectingToInternet();
                if (isInternetPresent) {
                    postRequest_Reqqustpayment(ServiceConstant.request_paymnet_url);
                    System.out.println("arrived------------------" + ServiceConstant.request_paymnet_url);
                } else {
                    Alert(getResources().getString(R.string.alert_sorry_label_title), getResources().getString(R.string.alert_nointernet));
                }
            }
        });


    }

     //-----------------------Code for begin trip post request-----------------
     private void PostRequest(String Url) {
        dialog = new Dialog(EndTrip.this);
        dialog.getWindow();
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.custom_loading);
        dialog.setCanceledOnTouchOutside(false);
        dialog.show();

        TextView dialog_title=(TextView)dialog.findViewById(R.id.custom_loading_textview);
        dialog_title.setText(getResources().getString(R.string.action_loading));


        postrequest = new StringRequest(Request.Method.POST, Url,
                new Response.Listener<String>() {

                    @Override
                    public void onResponse(String response) {
                        Log.e("end", response);

                        System.out.println("endtrip---------"+response);

                      //  String Str_status = "",Str_response="",Str_ridefare="",Str_timetaken="",Str_waitingtime="",Str_currency="",Str_ride_distance="";

                       try {
                            JSONObject object = new JSONObject(response);
                            Str_status = object.getString("status");
                           Str_response = object.getString("response");

                           JSONObject jsonObject= object.getJSONObject("response");
                           JSONObject jobject = jsonObject.getJSONObject("fare_details");

                           Str_currency = jobject.getString("currency");

                           Currency currencycode = Currency.getInstance(getLocale(Str_currency));
                           Str_ridefare = currencycode.getSymbol()+jobject.getString("ride_fare");
                           Str_timetaken = jobject.getString("ride_duration");
                           Str_waitingtime = jobject.getString("waiting_duration");
                           Str_ride_distance = jobject.getString("ride_distance");
                           Str_need_payment = jobject.getString("need_payment");

                        }catch (Exception e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }
                        dialog.dismiss();
                        if (Str_status.equalsIgnoreCase("1")){
                            if (Str_need_payment.equalsIgnoreCase("YES")){
                                System.out.println("sucess------------"+Str_need_payment);
                                showfaresummerydetails();
                            }else{

                                Intent intent= new Intent(EndTrip.this,RatingsPage.class);
                                startActivity(intent);
                                overridePendingTransition(R.anim.fade_in,R.anim.fade_out);
                            }

                        }else {
                            Alert(getResources().getString(R.string.alert_sorry_label_title), Str_response);

                        }

                        dialog.dismiss();

                    }
                }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                VolleyErrorResponse.VolleyError(EndTrip.this, error);
            }

        }) {

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<String, String>();
                headers.put("User-agent", ServiceConstant.useragent);
                return headers;
            }

            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> jsonParams = new HashMap<String, String>();
                jsonParams.put("driver_id",driver_id);
                jsonParams.put("ride_id",Str_rideid);
                jsonParams.put("drop_lat",String.valueOf(MyCurrent_lat));
                jsonParams.put("drop_lon",String.valueOf(MyCurrent_long));
                jsonParams.put("distance","0");
                jsonParams.put("wait_time","0");

                //jsonParams.put("wait_time",String.valueOf(mins).replace(":","."));
               /* jsonParams.put("wait_time",String.valueOf( String.valueOf("" + mins + ":"
                        + String.format("%02d", secs) + ":"
                        + String.format("%03d", milliseconds))).replace(":","."));*/


                System.out
                        .println("--------------driver_id-------------------"
                                + driver_id);
                System.out
                        .println("--------------drop_lat-------------------"
                                + String.valueOf(MyCurrent_lat));
                System.out
                        .println("--------------drop_lon-------------------"
                                + String.valueOf(MyCurrent_long));

                System.out
                        .println("--------------distance-------------------"
                                +6.4);




                return jsonParams;
            }
        };
         postrequest.setRetryPolicy(new DefaultRetryPolicy(30000,
                 DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                 DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
         postrequest.setShouldCache(false);

        AppController.getInstance().addToRequestQueue(postrequest);
    }


    //-----------------------Code for arrived post request-----------------
    private void postRequest_Reqqustpayment(String Url) {
        dialog = new Dialog(EndTrip.this);
        dialog.getWindow();
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.custom_loading);
        dialog.setCanceledOnTouchOutside(false);
        dialog.show();

        System.out.println("loadin-----------");
        TextView dialog_title=(TextView)dialog.findViewById(R.id.custom_loading_textview);
        dialog_title.setText(getResources().getString(R.string.action_loading));


        postrequest = new StringRequest(Request.Method.POST, Url,
                new Response.Listener<String>() {

                    @Override
                    public void onResponse(String response) {
                        Log.e("requestpayment", response);

                        System.out.println("response---------"+response);

                        String Str_status = "",Str_response="",Str_currency="",Str_rideid="",Str_action="";

                        try {
                            JSONObject object = new JSONObject(response);
                            Str_response = object.getString("response");
                            Str_status = object.getString("status");

                        }catch (Exception e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }

                        if (Str_status.equalsIgnoreCase("0"))
                        {
                            Alert(getResources().getString(R.string.alert_sorry_label_title), Str_response);

                        }else{
                            Alert(getResources().getString(R.string.label_pushnotification_cashreceived), Str_response);

                        }

                    }
                }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                VolleyErrorResponse.VolleyError(EndTrip.this, error);
            }

        }) {

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<String, String>();
                headers.put("User-agent",ServiceConstant.useragent);
                return headers;
            }

            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> jsonParams = new HashMap<String, String>();
                jsonParams.put("driver_id", driver_id);
                jsonParams.put("ride_id", Str_rideid);

                System.out
                        .println("--------------driver_id-------------------"
                                + driver_id);


                System.out
                        .println("--------------ride_id-------------------"
                                + Str_rideid);


                return jsonParams;
            }
        };
        postrequest.setRetryPolicy(new DefaultRetryPolicy(30000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        postrequest.setShouldCache(false);

        AppController.getInstance().addToRequestQueue(postrequest);
    }

    //method to convert currency code to currency symbol
    private static Locale getLocale(String strCode) {

        for (Locale locale : NumberFormat.getAvailableLocales()) {
            String code = NumberFormat.getCurrencyInstance(locale).getCurrency().getCurrencyCode();
            if (strCode.equals(code)) {
                return locale;
            }
        }
        return null;
    }


    public void onRoutingSuccess(PolylineOptions mPolyOptions)
    {
        PolylineOptions polyoptions = new PolylineOptions();
        polyoptions.color(Color.BLUE);
        polyoptions.width(10);
        polyoptions.addAll(mPolyOptions.getPoints());
        googleMap.addPolyline(polyoptions);
    }


    //-----------------Move Back on  phone pressed  back button------------------
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if ((keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0)) {
            // nothing
            return true;
        }
        return false;
    }


}
