package com.cabily.cabilydriver;

import android.app.Dialog;
import android.content.Intent;
import android.content.IntentSender;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.InflateException;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.app.service.ServiceConstant;
import com.app.service.ServiceManager;
import com.app.service.ServiceRequest;
import com.app.xmpp.ChatingService;
import com.cabily.cabilydriver.Pojo.Driver_Dashborad_Pojo;
import com.cabily.cabilydriver.Pojo.TripSummaryPojo;
import com.cabily.cabilydriver.Utils.AppController;
import com.cabily.cabilydriver.Utils.ConnectionDetector;
import com.cabily.cabilydriver.Utils.CurrencySymbolConverter;
import com.cabily.cabilydriver.Utils.GPSTracker;
import com.cabily.cabilydriver.Utils.RoundedImageView;
import com.cabily.cabilydriver.Utils.SessionManager;
import com.cabily.cabilydriver.Utils.VolleyErrorResponse;
import com.cabily.cabilydriver.widgets.PkDialog;
import com.github.paolorotolo.expandableheightlistview.ExpandableHeightListView;
import com.github.siyamed.shapeimageview.CircularImageView;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.romainpiel.shimmer.Shimmer;
import com.romainpiel.shimmer.ShimmerButton;
import com.special.ResideMenu.ResideMenu;
import com.squareup.picasso.MemoryPolicy;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONObject;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Currency;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Created by user88 on 12/22/2015.
 */
public class DashBoardDriver extends Fragment implements View.OnClickListener, com.google.android.gms.location.LocationListener, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {
    private static View parentView;
    private ResideMenu resideMenu;
    private SessionManager session;
    private RoundedImageView user_img;
    private String driver_img = "", driver_name = "", vehicle_name = "", vehicle_no = "";

    private Dialog dialog;
    private StringRequest postrequest;
    private String driver_id = "";

    private ServiceRequest mRequest;

      private  String Str_currencglobal="";
     private   Currency  currency_code;
    private String sCurrencySymbol="";

    private Boolean isInternetPresent = false;
    private ConnectionDetector cd;
    private Button Bt_Go_Online;
    private TextView Emty_Text;

    private ActionBar actionBar;

    private boolean isLastTripAvailable = false;
    private boolean isTodayEarningsAvailable = false;
    private boolean isTodayTipsAvailable = false;

    private  Currency currencycode1;
    private  Currency currencycode2;
    private   Currency currencycode;


    GPSTracker gps;
    private GoogleMap googleMap;
    private double MyCurrent_lat = 0.0, MyCurrent_long = 0.0;
    private RelativeLayout alert_layout;
    private TextView alert_textview;
    MarkerOptions markerOptions;

    private boolean show_progress_status = false;
    private String Str_currency_code = "";

    private TextView Tv_driver_name, Tv_Driver_Vechile_no, Tv_Driver_vechile_model,Tv_car_category;

    private TextView Tv_lasttrip_ridetime, Tv_lasttrip_ridedate, Tv_lasttrip_earnings;
    private TextView Tv_today_earnings_onlinehours, Tv_todayearnigs_trips, Tv_todayearnings_earnings;
    private TextView Tv_todaytips_trips, Tv_todaytips_tips;

    Shimmer shimmer;
    private RoundedImageView Im_driver_img;

    private double strlat, strlon;
    private RatingBar driver_rating;

    LocationRequest mLocationRequest;
    GoogleApiClient mGoogleApiClient;
    PendingResult<LocationSettingsResult> result;
    final static int REQUEST_LOCATION = 199;

    @Override
    public View onCreateView(final LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (parentView != null) {
            ViewGroup parent = (ViewGroup) parentView.getParent();
            if (parent != null)
                parent.removeView(parentView);
        }
        try {
            parentView = inflater.inflate(R.layout.driver_dash_board, container, false);
        } catch (InflateException e) {
        /* map is already there, just return view as it is */
        }
        parentView.findViewById(R.id.ham_home).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (resideMenu != null) {
                    resideMenu.openMenu(ResideMenu.DIRECTION_LEFT);
                }
            }
        });
        try {
            String home = getActivity().getResources().getString(R.string.home);
            getActivity().setTitle("" + home);
        } catch (Exception e) {
        }
        setUpViews();
        initialize(parentView);
        initilizeMap();

        Bt_Go_Online.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(gps.isgpsenabled()&&gps.canGetLocation())
                {
                    cd = new ConnectionDetector(getActivity());
                    isInternetPresent = cd.isConnectingToInternet();

                    if (isInternetPresent){
                        session.createSessionOnline("1");
                        session.createSessionOnline("1");
                        ChatingService.startDriverAction(getContext());
                        showDialog(getResources().getString(R.string.action_loading));
                        HashMap<String, String> jsonParams = new HashMap<String, String>();
                        HashMap<String, String> userDetails = session.getUserDetails();
                        HashMap<String, String> onlinedetails = session.getOnlineDetails();

                        String driverId = userDetails.get("driverid");
                        jsonParams.put("driver_id", "" + driverId);
                        jsonParams.put("availability", "" + "Yes");

                        System.out.println("availability-----" + "Yes");

                        System.out.println("driver_id-----"+driverId);

                        ServiceManager manager = new ServiceManager(getActivity(), updateAvailablityServiceListener);
                        manager.makeServiceRequest(ServiceConstant.UPDATE_AVAILABILITY, Request.Method.POST, jsonParams);

                        System.out.println("go_onlineurl-----" + ServiceConstant.UPDATE_AVAILABILITY);

                    }else{

                        Alert(getResources().getString(R.string.alert_sorry_label_title), getResources().getString(R.string.alert_nointernet));
                    }

                }
                else
                {
                    enableGpsService();
                }
            }
        });

        return parentView;
    }

    private void setUpViews() {
        NavigationDrawer parentActivity = (NavigationDrawer) getActivity();
        resideMenu = parentActivity.getResideMenu();
    }


    public void showDialog(String message) {
        dialog = new Dialog(getActivity());
        dialog.getWindow();
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.custom_loading);
        dialog.setCanceledOnTouchOutside(false);
        dialog.show();
    }


    private void initialize(View rootview) {
        session = new SessionManager(getActivity());
        gps = new GPSTracker(getActivity());
        shimmer = new Shimmer();

        HashMap<String, String> user = session.getUserDetails();
        driver_id = user.get(SessionManager.KEY_DRIVERID);
        driver_img = user.get(SessionManager.KEY_DRIVER_IMAGE);
        driver_name = user.get(SessionManager.KEY_DRIVER_NAME);
        vehicle_no = user.get(SessionManager.KEY_VEHICLENO);
        vehicle_name = user.get(SessionManager.KEY_VEHICLE_MODEL);

        Tv_lasttrip_ridetime = (TextView) rootview.findViewById(R.id.dashboard_ride_time);
        Tv_lasttrip_ridedate = (TextView) rootview.findViewById(R.id.dashboard_last_trip_ride_date);
        Tv_lasttrip_earnings = (TextView) rootview.findViewById(R.id.netAmount_price_last_trips);

        Tv_today_earnings_onlinehours = (TextView) rootview.findViewById(R.id.dashboard_today_earnings_onlinetime);
        Tv_todayearnigs_trips = (TextView) rootview.findViewById(R.id.dashboard_today_earnings_trips);
        Tv_todayearnings_earnings = (TextView) rootview.findViewById(R.id.netAmount_price_today_earnings);

        Tv_todaytips_trips = (TextView) rootview.findViewById(R.id.dashboard_todays_trips);
        Tv_todaytips_tips = (TextView) rootview.findViewById(R.id.netAmount_price_today_tips);


        Bt_Go_Online = (Button) rootview.findViewById(R.id.Bt_gonlinebutton);
        Tv_driver_name = (TextView) rootview.findViewById(R.id.home_user_name);
        Tv_Driver_vechile_model = (TextView) rootview.findViewById(R.id.home_car_name);
        user_img = (RoundedImageView) rootview.findViewById(R.id.dasboard_driverimg);
        Tv_Driver_Vechile_no = (TextView) rootview.findViewById(R.id.home_car_no);
        Tv_car_category = (TextView)rootview.findViewById(R.id.home_car_category);


        driver_rating = (RatingBar) rootview.findViewById(R.id.driver_dashboard_ratting);


       // shimmer = new Shimmer();
       // shimmer.start(Bt_Go_Online);

        Picasso.with(getActivity()).load(driver_img).placeholder(R.drawable.nouserimg).into(user_img);

        ActionBarActivity actionBarActivity = (ActionBarActivity) getActivity();
        actionBar = actionBarActivity.getSupportActionBar();
        actionBar.setHomeButtonEnabled(true);
        actionBar.setCustomView(R.layout.action_bar_home);
        actionBar.setBackgroundDrawable(new ColorDrawable(Color.parseColor("#31c3e7")));
        actionBar.setDisplayShowCustomEnabled(true);
        actionBar.hide();

        cd = new ConnectionDetector(getActivity());
        isInternetPresent = cd.isConnectingToInternet();

        if (isInternetPresent) {
            driverdashboard_PostRequest(ServiceConstant.driver_dashboard);
            System.out.println("driverdashboardurl------------" + ServiceConstant.driver_dashboard);
        } else {
            Alert(getResources().getString(R.string.alert_sorry_label_title), getResources().getString(R.string.alert_nointernet));
        }

    }


    private void initilizeMap() {
        if (googleMap == null) {
            googleMap = ((MapFragment) getActivity().getFragmentManager().findFragmentById(R.id.driver_dashboradsmain_map)).getMap();
            if (googleMap == null) {
                Toast.makeText(getActivity(), getResources().getString(R.string.action_alert_unabletocreatemap), Toast.LENGTH_SHORT).show();
            }
        }
        googleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        googleMap.setMyLocationEnabled(false);
        googleMap.getUiSettings().setZoomControlsEnabled(false);
        googleMap.getUiSettings().setMyLocationButtonEnabled(false);
        googleMap.getUiSettings().setCompassEnabled(false);
        googleMap.getUiSettings().setRotateGesturesEnabled(true);
        googleMap.getUiSettings().setZoomGesturesEnabled(true);
        googleMap.setMyLocationEnabled(false);
        googleMap.getUiSettings().setAllGesturesEnabled(false);


        if (gps != null && gps.canGetLocation()) {
            double Dlatitude = gps.getLatitude();
            double Dlongitude = gps.getLongitude();
            MyCurrent_lat = Dlatitude;
            MyCurrent_long = Dlongitude;
            CameraPosition cameraPosition = new CameraPosition.Builder().target(new LatLng(Dlatitude, Dlongitude)).zoom(18).build();
            googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));

        } else {
            alert_layout.setVisibility(View.VISIBLE);
            alert_textview.setText(getResources().getString(R.string.alert_gpsEnable));
        }

        markerOptions = new MarkerOptions();

    }

    private ServiceManager.ServiceListener updateAvailablityServiceListener = new ServiceManager.ServiceListener() {
        @Override
        public void onCompleteListener(Object object) {
            try {
                dismissDialog();
                String response = (String) object;
                Intent i = new Intent(getActivity(), DriverMapActivity.class);
                startActivity(i);
                System.out.println("onlineresponse-------"+response);

            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onErrorListener(Object obj) {
            dismissDialog();
        }
    };


    public void dismissDialog() {
        if (dialog != null)
            dialog.dismiss();
    }


    //Enabling Gps Service
    private void enableGpsService() {
        mLocationRequest = LocationRequest.create();
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setInterval(30 * 1000);
        mLocationRequest.setFastestInterval(5 * 1000);

        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(mLocationRequest);
        builder.setAlwaysShow(true);

        result = LocationServices.SettingsApi.checkLocationSettings(mGoogleApiClient, builder.build());

        result.setResultCallback(new ResultCallback<LocationSettingsResult>() {
            @Override
            public void onResult(LocationSettingsResult result) {
                final Status status = result.getStatus();
                //final LocationSettingsStates state = result.getLocationSettingsStates();
                switch (status.getStatusCode()) {
                    case LocationSettingsStatusCodes.SUCCESS:
                        // All location settings are satisfied. The client can initialize location
                        // requests here.
                        //...
                        break;
                    case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                        // Location settings are not satisfied. But could be fixed by showing the user
                        // a dialog.
                        try {
                            // Show the dialog by calling startResolutionForResult(),
                            // and check the result in onActivityResult().
                            status.startResolutionForResult(getActivity(), REQUEST_LOCATION);
                        } catch (IntentSender.SendIntentException e) {
                            // Ignore the error.
                        }
                        break;
                    case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                        // Location settings are not satisfied. However, we have no way to fix the
                        // settings so we won't show the dialog.
                        //...
                        break;
                }
            }
        });
    }

    private void Alert(String title, String message) {
        final PkDialog mDialog = new PkDialog(getActivity());
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


    private void driverdashboard_PostRequest(String Url) {
        dialog = new Dialog(getActivity());
        dialog.getWindow();
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.custom_loading);
        dialog.setCanceledOnTouchOutside(false);
        dialog.show();

        TextView dialog_title = (TextView) dialog.findViewById(R.id.custom_loading_textview);
        dialog_title.setText(getResources().getString(R.string.action_loading));

        System.out.println("-------------dashboard----------------" + Url);

        HashMap<String, String> jsonParams = new HashMap<String, String>();
        jsonParams.put("driver_id", driver_id);

        mRequest = new ServiceRequest(getActivity());
        mRequest.makeServiceRequest(Url, Request.Method.POST, jsonParams, new ServiceRequest.ServiceListener() {

            @Override
            public void onCompleteListener(String response) {

                Log.e("dashboards", response);
                System.out.println("dashboards---------" + response);
                String Str_status = "", Str_response = "", Str_driver_name = "", Str_driver_img = "",Str_driver_category="", Str_vechile_no = "", Str_vechile_model = "", Str_driver_id = "",
                        Str_driver_lattitude = "", Str_driver_longitude = "", Str_driver_ratting = "", Str_lasttrip_ridetime = "", Str_lasttrip_ridedate = "",
                        Str_lasttrip_earnings = "", Str_lasttrip_currencycode = "", Str_todayearnings_onlinehours = "", Str_todayearnings_trips = "",
                        Str_todayearnings_earnings = "", Str_todayearnings_currencycode = "", Str_todaytips_trips = "", Str_todaytips_tips = "", Str_todaytips_currencycode = "";

                try {

                    JSONObject jobject = new JSONObject(response);

                    Str_status = jobject.getString("status");

                    if (Str_status.equalsIgnoreCase("1")) {
                        JSONObject object = jobject.getJSONObject("response");

                        Str_currencglobal = object.getString("currency");
                        currency_code = Currency.getInstance(getLocale(Str_currencglobal));

                        Str_driver_id = object.getString("driver_id");
                        Str_driver_name = object.getString("driver_name");
                        Str_vechile_no = object.getString("vehicle_number");
                        Str_vechile_model = object.getString("vehicle_model");
                        Str_driver_img = object.getString("driver_image");
                        Str_driver_ratting = object.getString("driver_review");
                        Str_driver_category = object.getString("driver_category");

                        Str_driver_lattitude = object.getString("driver_lat");
                        Str_driver_longitude = object.getString("driver_lon");
                        strlat = Double.parseDouble(Str_driver_lattitude);
                        strlon = Double.parseDouble(Str_driver_longitude);

                        Object check_last_trip_object = object.get("last_trip");
                        if (check_last_trip_object instanceof JSONObject) {

                            JSONObject jobject1 = object.getJSONObject("last_trip");
                            if (jobject1.length() > 0) {
                                Str_lasttrip_ridetime = jobject1.getString("ride_time");
                                Str_lasttrip_ridedate = jobject1.getString("ride_date");
                                Str_lasttrip_currencycode = jobject1.getString("currency");
                                // currencycode = Currency.getInstance(getLocale(Str_lasttrip_currencycode));

                                sCurrencySymbol = CurrencySymbolConverter.getCurrencySymbol(Str_lasttrip_currencycode);

                                Str_lasttrip_earnings = sCurrencySymbol+ jobject1.getString("earnings");
                                isLastTripAvailable =true;

                                System.out.println("ridetim------"+Str_lasttrip_ridetime);
                                System.out.println("ridedate------"+Str_lasttrip_ridedate);
                                System.out.println("amount------"+Str_lasttrip_earnings);

                            }else{
                                isLastTripAvailable =false;
                            }
                        }else{
                            isLastTripAvailable =false;
                        }

                        Object check_today_earnings_object = object.get("today_earnings");
                        if (check_today_earnings_object instanceof JSONObject){
                            JSONObject jobject2 = object.getJSONObject("today_earnings");
                            if (jobject2.length() > 0) {
                                Str_todayearnings_onlinehours = jobject2.getString("online_hours");
                                Str_todayearnings_trips = jobject2.getString("trips");
                                Str_todayearnings_currencycode = jobject2.getString("currency");

                                // currencycode1= Currency.getInstance(getLocale(Str_todayearnings_currencycode));

                                sCurrencySymbol = CurrencySymbolConverter.getCurrencySymbol(Str_todayearnings_currencycode);

                                Str_todayearnings_earnings = sCurrencySymbol + jobject2.getString("earnings");
                                isTodayEarningsAvailable = true;
                            }else{
                                isTodayEarningsAvailable = false;
                            }
                        }else{
                            isTodayEarningsAvailable = false;
                        }

                        Object check_today_tips_object = object.get("today_tips");
                        if (check_today_tips_object instanceof JSONObject){
                            JSONObject jobject3 = object.getJSONObject("today_tips");
                            if (jobject3.length() > 0) {
                                Str_todaytips_trips = jobject3.getString("trips");
                                Str_todaytips_currencycode = jobject3.getString("currency");
                                //currencycode2 = Currency.getInstance(getLocale(Str_todaytips_currencycode));

                                sCurrencySymbol = CurrencySymbolConverter.getCurrencySymbol(Str_todaytips_currencycode);

                                Str_todaytips_tips = sCurrencySymbol + jobject3.getString("tips");
                                isTodayTipsAvailable = true;
                            }else{
                                isTodayTipsAvailable = false;
                            }
                        }else{
                            isTodayTipsAvailable = false;
                        }

                    } else {
                        Str_response = jobject.getString("response");
                    }

                    if (Str_status.equalsIgnoreCase("1")) {
                        Tv_Driver_vechile_model.setText(Str_vechile_model);
                        Tv_driver_name.setText(Str_driver_name);
                        Tv_Driver_Vechile_no.setText(Str_vechile_no);
                        driver_rating.setRating(Float.parseFloat(Str_driver_ratting));
                        Tv_car_category.setText(Str_driver_category);

                        if (isLastTripAvailable==true)
                        {
                            Tv_lasttrip_ridetime.setText(Str_lasttrip_ridetime);
                            Tv_lasttrip_ridedate.setText(Str_lasttrip_ridedate);
                            Tv_lasttrip_earnings.setText(Str_lasttrip_earnings);
                        }else {
                            Tv_lasttrip_ridetime.setText(getResources().getString(R.string.lasttrip_emtpy_label));
                            Tv_lasttrip_ridedate.setText(getResources().getString(R.string.lasttrip_emtpy_label));
                            Tv_lasttrip_earnings.setText(sCurrencySymbol+"0.00");
                        }

                        if (isTodayEarningsAvailable==true)
                        {
                            Tv_today_earnings_onlinehours.setText(Str_todayearnings_onlinehours);
                            Tv_todayearnigs_trips.setText(Str_todayearnings_trips+" "+"Trips");
                            Tv_todayearnings_earnings.setText(Str_todayearnings_earnings);
                        }else{
                            Tv_today_earnings_onlinehours.setText(getResources().getString(R.string.todayearning_no_online_label));
                            Tv_todayearnigs_trips.setText(getResources().getString(R.string.todayearning_no_trips_label));
                            Tv_todayearnings_earnings.setText(sCurrencySymbol+"0.00");
                        }

                        if (isTodayTipsAvailable==true)
                        {
                            Tv_todaytips_tips.setText(Str_todaytips_tips);
                            Tv_todaytips_trips.setText(Str_todaytips_trips+" "+"Trips");
                        }else{
                            Tv_todaytips_tips.setText(sCurrencySymbol+"0.00");
                            Tv_todaytips_trips.setText((getResources().getString(R.string.lasttrip_emtpy_label)));
                        }

                        //---------------map marker--------------
                        googleMap.addMarker(new MarkerOptions()
                                .position(new LatLng((strlat), (strlon)))
                                .icon(BitmapDescriptorFactory.fromResource(R.drawable.maps)));
                        // Move the camera to last position with a zoom level
                        CameraPosition cameraPosition = new CameraPosition.Builder().target(new LatLng((strlat), (strlon))).zoom(12).build();
                        googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));

                    } else {
                        Alert(getResources().getString(R.string.alert_sorry_label_title), Str_response);
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }
                dialog.dismiss();

            }

            @Override
            public void onErrorListener() {
                dialog.dismiss();
            }
        });


    }




   /* private void driverdashboard_PostRequest(String Url) {
        dialog = new Dialog(getActivity());
        dialog.getWindow();
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.custom_loading);
        dialog.setCanceledOnTouchOutside(false);
        dialog.show();

        TextView dialog_title = (TextView) dialog.findViewById(R.id.custom_loading_textview);
        dialog_title.setText(getResources().getString(R.string.action_loading));


        postrequest = new StringRequest(Request.Method.POST, Url,
                new Response.Listener<String>() {

                    @Override
                    public void onResponse(String response) {
                        Log.e("dashboards", response);
                        System.out.println("dashboards---------" + response);
                        String Str_status = "", Str_response = "", Str_driver_name = "", Str_driver_img = "",Str_driver_category="", Str_vechile_no = "", Str_vechile_model = "", Str_driver_id = "",
                                Str_driver_lattitude = "", Str_driver_longitude = "", Str_driver_ratting = "", Str_lasttrip_ridetime = "", Str_lasttrip_ridedate = "",
                                Str_lasttrip_earnings = "", Str_lasttrip_currencycode = "", Str_todayearnings_onlinehours = "", Str_todayearnings_trips = "",
                                Str_todayearnings_earnings = "", Str_todayearnings_currencycode = "", Str_todaytips_trips = "", Str_todaytips_tips = "", Str_todaytips_currencycode = "";

                        try {

                             JSONObject jobject = new JSONObject(response);

                              Str_status = jobject.getString("status");

                            if (Str_status.equalsIgnoreCase("1")) {
                                JSONObject object = jobject.getJSONObject("response");

                                Str_currencglobal = object.getString("currency");
                                currency_code = Currency.getInstance(getLocale(Str_currencglobal));

                                Str_driver_id = object.getString("driver_id");
                                Str_driver_name = object.getString("driver_name");
                                Str_vechile_no = object.getString("vehicle_number");
                                Str_vechile_model = object.getString("vehicle_model");
                                Str_driver_img = object.getString("driver_image");
                                Str_driver_ratting = object.getString("driver_review");
                                Str_driver_category = object.getString("driver_category");

                                Str_driver_lattitude = object.getString("driver_lat");
                                Str_driver_longitude = object.getString("driver_lon");
                                strlat = Double.parseDouble(Str_driver_lattitude);
                                strlon = Double.parseDouble(Str_driver_longitude);

                                Object check_last_trip_object = object.get("last_trip");
                                if (check_last_trip_object instanceof JSONObject) {

                                    JSONObject jobject1 = object.getJSONObject("last_trip");
                                    if (jobject1.length() > 0) {
                                        Str_lasttrip_ridetime = jobject1.getString("ride_time");
                                        Str_lasttrip_ridedate = jobject1.getString("ride_date");
                                        Str_lasttrip_currencycode = jobject1.getString("currency");
                                       // currencycode = Currency.getInstance(getLocale(Str_lasttrip_currencycode));

                                        sCurrencySymbol = CurrencySymbolConverter.getCurrencySymbol(Str_lasttrip_currencycode);

                                        Str_lasttrip_earnings = sCurrencySymbol+ jobject1.getString("earnings");
                                        isLastTripAvailable =true;

                                        System.out.println("ridetim------"+Str_lasttrip_ridetime);
                                        System.out.println("ridedate------"+Str_lasttrip_ridedate);
                                        System.out.println("amount------"+Str_lasttrip_earnings);

                                    }else{
                                        isLastTripAvailable =false;
                                    }
                                }else{
                                    isLastTripAvailable =false;
                                }

                                Object check_today_earnings_object = object.get("today_earnings");
                                if (check_today_earnings_object instanceof JSONObject){
                                    JSONObject jobject2 = object.getJSONObject("today_earnings");
                                    if (jobject2.length() > 0) {
                                        Str_todayearnings_onlinehours = jobject2.getString("online_hours");
                                        Str_todayearnings_trips = jobject2.getString("trips");
                                        Str_todayearnings_currencycode = jobject2.getString("currency");

                                       // currencycode1= Currency.getInstance(getLocale(Str_todayearnings_currencycode));

                                        sCurrencySymbol = CurrencySymbolConverter.getCurrencySymbol(Str_todayearnings_currencycode);

                                        Str_todayearnings_earnings = sCurrencySymbol + jobject2.getString("earnings");
                                        isTodayEarningsAvailable = true;
                                    }else{
                                        isTodayEarningsAvailable = false;
                                    }
                                }else{
                                    isTodayEarningsAvailable = false;
                                }

                                Object check_today_tips_object = object.get("today_tips");
                                if (check_today_tips_object instanceof JSONObject){
                                    JSONObject jobject3 = object.getJSONObject("today_tips");
                                    if (jobject3.length() > 0) {
                                        Str_todaytips_trips = jobject3.getString("trips");
                                        Str_todaytips_currencycode = jobject3.getString("currency");
                                        //currencycode2 = Currency.getInstance(getLocale(Str_todaytips_currencycode));

                                        sCurrencySymbol = CurrencySymbolConverter.getCurrencySymbol(Str_todaytips_currencycode);

                                        Str_todaytips_tips = sCurrencySymbol + jobject3.getString("tips");
                                        isTodayTipsAvailable = true;
                                    }else{
                                        isTodayTipsAvailable = false;
                                    }
                                }else{
                                    isTodayTipsAvailable = false;
                                }

                            } else {
                                Str_response = jobject.getString("response");
                            }

                            if (Str_status.equalsIgnoreCase("1")) {
                                Tv_Driver_vechile_model.setText(Str_vechile_model);
                                Tv_driver_name.setText(Str_driver_name);
                                Tv_Driver_Vechile_no.setText(Str_vechile_no);
                                driver_rating.setRating(Float.parseFloat(Str_driver_ratting));
                                Tv_car_category.setText(Str_driver_category);

                                if (isLastTripAvailable==true)
                                {
                                    Tv_lasttrip_ridetime.setText(Str_lasttrip_ridetime);
                                    Tv_lasttrip_ridedate.setText(Str_lasttrip_ridedate);
                                    Tv_lasttrip_earnings.setText(Str_lasttrip_earnings);
                                }else {
                                    Tv_lasttrip_ridetime.setText(getResources().getString(R.string.lasttrip_emtpy_label));
                                    Tv_lasttrip_ridedate.setText(getResources().getString(R.string.lasttrip_emtpy_label));
                                    Tv_lasttrip_earnings.setText(sCurrencySymbol+"0.00");
                                }

                                if (isTodayEarningsAvailable==true)
                                {
                                    Tv_today_earnings_onlinehours.setText(Str_todayearnings_onlinehours);
                                    Tv_todayearnigs_trips.setText(Str_todayearnings_trips+" "+"Trips");
                                    Tv_todayearnings_earnings.setText(Str_todayearnings_earnings);
                                }else{
                                    Tv_today_earnings_onlinehours.setText(getResources().getString(R.string.todayearning_no_online_label));
                                    Tv_todayearnigs_trips.setText(getResources().getString(R.string.todayearning_no_trips_label));
                                    Tv_todayearnings_earnings.setText(sCurrencySymbol+"0.00");
                                }

                                if (isTodayTipsAvailable==true)
                                {
                                    Tv_todaytips_tips.setText(Str_todaytips_tips);
                                    Tv_todaytips_trips.setText(Str_todaytips_trips+" "+"Trips");
                                }else{
                                    Tv_todaytips_tips.setText(sCurrencySymbol+"0.00");
                                    Tv_todaytips_trips.setText((getResources().getString(R.string.lasttrip_emtpy_label)));
                                }

                                //---------------map marker--------------
                                googleMap.addMarker(new MarkerOptions()
                                        .position(new LatLng((strlat), (strlon)))
                                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.maps)));
                                // Move the camera to last position with a zoom level
                                CameraPosition cameraPosition = new CameraPosition.Builder().target(new LatLng((strlat), (strlon))).zoom(12).build();
                                googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));

                            } else {
                                Alert(getResources().getString(R.string.alert_sorry_label_title), Str_response);
                            }

                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        dialog.dismiss();

                    }
                }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                VolleyErrorResponse.VolleyError(getActivity(), error);
            }

        }) {

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<String, String>();
                headers.put("User-agent", ServiceConstant.useragent);
                headers.put("isapplication", ServiceConstant.isapplication);
                headers.put("applanguage", ServiceConstant.applanguage);

                return headers;
            }

            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> jsonParams = new HashMap<String, String>();
                jsonParams.put("driver_id", driver_id);

                System.out.println("--------------driver_id-------------------" + driver_id);

                return jsonParams;
            }
        };
        postrequest.setRetryPolicy(new DefaultRetryPolicy(30000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        postrequest.setShouldCache(false);

        AppController.getInstance().addToRequestQueue(postrequest);
    }
*/

    @Override
    public void onConnected(Bundle bundle) {

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onLocationChanged(Location location) {

    }

    @Override
    public void onClick(View v) {

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

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


}
