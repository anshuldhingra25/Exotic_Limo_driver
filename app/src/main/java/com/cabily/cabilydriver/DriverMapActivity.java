package com.cabily.cabilydriver;

import android.app.Activity;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentSender;
import android.location.Location;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.Hockeyapp.ActivityHockeyApp;
import com.android.volley.Request;
import com.app.xmpp.ChatingService;
import com.app.service.ServiceConstant;
import com.app.service.ServiceManager;
import com.cabily.cabilydriver.Utils.GPSTracker;
import com.cabily.cabilydriver.Utils.SessionManager;
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
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONObject;

import java.util.HashMap;

import me.drakeet.materialdialog.MaterialDialog;

/**
 * Created by user14 on 9/22/2015.
 */
public class DriverMapActivity extends ActivityHockeyApp implements View.OnClickListener, com.google.android.gms.location.LocationListener, GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {

    // Google Map
    private GoogleMap googleMap;
    private Location location = null;
    public static Location myLocation;
    private SessionManager session;
    private Dialog dialog;
    private Marker currentMarker;
    private RelativeLayout Rl_layout_available_status;
     private  String Str_rideId="";
    private String driver_id ="";
    boolean isGpsEnabled;
    BroadcastReceiver receiver;
    GPSTracker gps;


    LocationRequest mLocationRequest;
    GoogleApiClient mGoogleApiClient;
    PendingResult<LocationSettingsResult> result;
    final static int REQUEST_LOCATION = 199;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.roadmap);
        session = new SessionManager(DriverMapActivity.this);
        gps = new GPSTracker(this);

        // get user data from session
        HashMap<String, String> user = session.getUserDetails();
        driver_id = user.get(SessionManager.KEY_DRIVERID);

        //Code for broadcat receive
        IntentFilter filter = new IntentFilter();
        filter.addAction("com.finish.canceltrip.DriverMapActivity");
        receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (intent.getAction().equals("com.finish.canceltrip.DriverMapActivity")) {
                    Rl_layout_available_status.setVisibility(View.GONE);
                }
            }
        };


        registerReceiver(receiver, filter);

        //Starting Xmpp service
        ChatingService.startDriverAction(DriverMapActivity.this);
        final Button goOffline = (Button) findViewById(R.id.go_offline);
        Rl_layout_available_status = (RelativeLayout)findViewById(R.id.layout_available_status);
        Rl_layout_available_status.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(DriverMapActivity.this,TripSummaryDetail.class);
                intent.putExtra("ride_id",Str_rideId);
                System.out.println("StrRideID---------"+Str_rideId);
                startActivity(intent);
                overridePendingTransition(R.anim.fade_in,R.anim.fade_out);
            }
        });

        goOffline.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                goOffLine();
            }

        });

        try {
            session = new SessionManager(this);
            setLocationRequest();
            buildGoogleApiClient();
            initilizeMap();
        } catch (Exception e) {
        }
        initView();



        if (gps != null && gps.canGetLocation() && gps.isgpsenabled()) {
        } else {
            enableGpsService();
            //showGpsDisableDialog(getResources().getString(R.string.label_gps_textview));
        }
    }


    @Override
    public void onBackPressed() {
        //   super.onBackPressed();
        showBackPressedDialog();
    }

    private void showBackPressedDialog() {
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
        builder.setMessage(R.string.label_sure_go_offline)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        finish();
                    }
                })
                .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.dismiss();
                    }
                });
        Dialog dialog = builder.create();
        dialog.show();
    }

    @Override
    public void onStart() {
        super.onStart();
        if (mGoogleApiClient != null)
            mGoogleApiClient.connect();

    }


    @Override
    protected void onResume() {
        super.onResume();
        startLocationUpdates();
    }

    private void initView() {
    }

    public void showDialog(String message) {
        dialog = new Dialog(this);
        dialog.getWindow();
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.custom_loading);
        dialog.setCanceledOnTouchOutside(false);
        dialog.show();
    }



    public void dismissDialog() {
        dialog.dismiss();
    }

    private void postRequest(final String url) {
        if (myLocation != null) {
            HashMap<String, String> jsonParams = new HashMap<String, String>();
            HashMap<String, String> userDetails = session.getUserDetails();
            String driverId = userDetails.get("driverid");

            System.out.println("driverId-------------"+driverId);
            System.out.println("latitude-------------"+myLocation.getLatitude());
            System.out.println("longitude-------------"+myLocation.getLongitude());

            jsonParams.put("driver_id", "" + driverId);
            jsonParams.put("latitude", "" + myLocation.getLatitude());
            jsonParams.put("longitude", "" + myLocation.getLongitude());
            ServiceManager manager = new ServiceManager(DriverMapActivity.this, mServiceListener);
            manager.makeServiceRequest(url, Request.Method.POST, jsonParams);
        } else {
          Toast.makeText(DriverMapActivity.this,"Location Not Update",Toast.LENGTH_SHORT).show();
        }
    }

    private ServiceManager.ServiceListener mServiceListener = new ServiceManager.ServiceListener() {


        private String Str_status="",Str_availablestaus="",Str_message="";
        @Override
        public void onCompleteListener(Object object) {
            try {
                String response = (String) object;

                JSONObject jobject = new JSONObject(response);
                Str_status = jobject.getString("status");
                if("0".equalsIgnoreCase(Str_status)){
                }
                JSONObject jobject2  =jobject.getJSONObject("response");
                Str_availablestaus = jobject2.getString("availability");
                Str_message = jobject2.getString("message");
                Str_rideId = jobject2.getString("ride_id");

                System.out.println("rideIDDresponse----------" + Str_rideId);

                System.out.println("online----------" + response);

                if (Str_availablestaus.equalsIgnoreCase("Unavailable"))
                {
                    Rl_layout_available_status.setVisibility(View.VISIBLE);
                }else{
                    Rl_layout_available_status.setVisibility(View.GONE);
                }
            } catch (Exception e) {
            }
        }

        @Override
        public void onErrorListener(Object obj) {

        }
    };


    private void initilizeMap() {
        // latitude and longitude
       /// gps = new GPSTracker(this);
        double latitude;
        double longitude;
        if (googleMap == null) {
            googleMap = ((MapFragment) getFragmentManager().findFragmentById(
                    R.id.map)).getMap();
            googleMap.setMyLocationEnabled(true);
            myLocation = googleMap.getMyLocation();
            googleMap.getUiSettings().setMyLocationButtonEnabled(true);
            googleMap.getUiSettings().setRotateGesturesEnabled(true);
            googleMap.getUiSettings().setCompassEnabled(true);
            if (googleMap == null) {
                Toast.makeText(getApplicationContext(),
                        "Sorry! unable to create maps", Toast.LENGTH_SHORT)
                        .show();
            }
        }
    }


    public void goOffLine() {
        showDialog("");
        HashMap<String, String> jsonParams = new HashMap<String, String>();
        HashMap<String, String> userDetails = session.getUserDetails();
        String driverId = userDetails.get("driverid");
        jsonParams.put("driver_id", "" + driverId);
        jsonParams.put("availability", "" + "No");

        System.out.println("availability-------------" +"No");
        System.out.println("offline driver_id-------------"+driverId);

        ServiceManager manager = new ServiceManager(this, updateAvailabilityServiceListener);
        manager.makeServiceRequest(ServiceConstant.UPDATE_AVAILABILITY, Request.Method.POST, jsonParams);
    }

    private ServiceManager.ServiceListener updateAvailabilityServiceListener = new ServiceManager.ServiceListener() {
        @Override
        public void onCompleteListener(Object object) {
            try {
                dismissDialog();
                String response = (String) object;
                System.out.println("goofflineresponse---------"+response);

                finish();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onErrorListener(Object obj) {
            dismissDialog();
            finish();
        }
    };

    private void setLocationRequest() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(10000);
        mLocationRequest.setFastestInterval(5000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    protected void startLocationUpdates() {
        if (mGoogleApiClient != null && mGoogleApiClient.isConnected()) {
            LocationServices.FusedLocationApi.requestLocationUpdates(
                    mGoogleApiClient, mLocationRequest, this);
        }
    }


    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
    }


    @Override
    public void onConnected(Bundle bundle) {
        if (gps != null && gps.canGetLocation() && gps.isgpsenabled()) {
        }

            myLocation = LocationServices.FusedLocationApi.getLastLocation(
                mGoogleApiClient);
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
        if (myLocation != null) {
            googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(myLocation.getLatitude(), myLocation.getLongitude()),
                    16));
            MarkerOptions marker = new MarkerOptions();
            marker.position(new LatLng(myLocation.getLatitude(), myLocation.getLongitude()));
            marker.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));
            currentMarker =  googleMap.addMarker(marker);
            postRequest(ServiceConstant.UPDATE_CURRENT_LOCATION);

            System.out.println("online------------------"+ServiceConstant.UPDATE_CURRENT_LOCATION);

        }
    }

    @Override
    public void onConnectionSuspended(int i) {
    }

    @Override
    public void onLocationChanged(Location location) {
        this.myLocation = location;
        System.out.println("locat-----------" + location);
        if (myLocation != null && currentMarker != null) {
            LatLng latLng = new LatLng(location.getLatitude(),location.getLongitude());
            currentMarker.setPosition(latLng);
        }
    }

    @Override
    public void onClick(View view) {

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
    }

    //Enabling Gps Service
    private void enableGpsService()
    {
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
                            status.startResolutionForResult(DriverMapActivity.this,REQUEST_LOCATION);
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


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        switch (requestCode)
        {
            case REQUEST_LOCATION:
                switch (resultCode)
                {
                    case Activity.RESULT_OK:
                    {
                        break;
                    }
                    case Activity.RESULT_CANCELED:
                    {
                        enableGpsService();
                        break;
                    }
                    default:
                    {
                        break;
                    }
                }
                break;
        }
    }






}
