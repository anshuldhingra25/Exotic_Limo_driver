package com.cabily.cabilydriver;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;

import com.Hockeyapp.FragmentHockeyApp;
import com.android.volley.Request;
import com.app.xmpp.ChatingService;
import com.app.service.ServiceConstant;
import com.app.service.ServiceManager;
import com.cabily.cabilydriver.Utils.SessionManager;
import com.github.siyamed.shapeimageview.CircularImageView;
import com.google.android.gms.location.LocationRequest;
import com.special.ResideMenu.ResideMenu;
import com.squareup.picasso.Picasso;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;


import java.util.HashMap;

/**
 * Created by user14 on 9/21/2015.
 */
public class DriverHomepageDetails extends FragmentHockeyApp implements View.OnClickListener, com.google.android.gms.location.LocationListener, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    private View parentView;
    private ResideMenu resideMenu;
    private TextView user_name, car_name, car_no;
    private CircularImageView user_img;
    private Button goOnline;
    private SessionManager session;
    private String driver_img = "", driver_name = "", vehicle_name = "", vehicle_no = "";
    private LocationManager locationManager;
    private Location mLastLocation;
    private GoogleApiClient mGoogleApiClient;
    private LocationRequest mLocationRequest;
    private Dialog dialog;
    private ActionBar actionBar;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        parentView = inflater.inflate(R.layout.driver_home_page_details, container, false);
        parentView.findViewById(R.id.ham_home).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(resideMenu != null ){
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
        return parentView;
    }


    public void showDialog(String message) {
        dialog = new Dialog(getActivity());
        dialog.getWindow();
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.custom_loading);
        dialog.setCanceledOnTouchOutside(false);
        dialog.show();
    }

    public void dismissDialog() {
        if (dialog != null)
            dialog.dismiss();
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mGoogleApiClient != null && mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        if (mGoogleApiClient != null)
            mGoogleApiClient.connect();
    }

    private void initialize(View rootview) {
        buildGoogleApiClient();
        session = new SessionManager(getActivity());
        user_img = (CircularImageView) rootview.findViewById(R.id.home_user_img);
        user_name = (TextView) rootview.findViewById(R.id.home_user_name);
        car_name = (TextView) rootview.findViewById(R.id.home_car_name);
        car_no = (TextView) rootview.findViewById(R.id.home_car_no);
        goOnline = (Button) rootview.findViewById(R.id.home_online_btn);
        HashMap<String, String> user = session.getUserDetails();
        driver_img = user.get(SessionManager.KEY_DRIVER_IMAGE);
        driver_name = user.get(SessionManager.KEY_DRIVER_NAME);
        vehicle_no = user.get(SessionManager.KEY_VEHICLENO);
        vehicle_name = user.get(SessionManager.KEY_VEHICLE_MODEL);
        Picasso.with(getActivity()).load(driver_img).placeholder(R.drawable.splash_image).into(user_img);
        user_name.setText(driver_name);
        if (LoginPage.details != null) {
            car_name.setText(LoginPage.details.getVehicleModel());
        }else{
            car_name.setText(session.getUserVehicle());
        }
        car_no.setText(vehicle_no);
        goOnline.setOnClickListener(this);
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(10000);
        mLocationRequest.setFastestInterval(5000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        ActionBarActivity actionBarActivity = (ActionBarActivity) getActivity();
        actionBar = actionBarActivity.getSupportActionBar();
        actionBar.setHomeButtonEnabled(true);
        actionBar.setCustomView(R.layout.action_bar_home);
        actionBar.setBackgroundDrawable(new ColorDrawable(Color.parseColor("#31c3e7")));
        actionBar.setDisplayShowCustomEnabled(true);
        actionBar.hide();

    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.home_online_btn) {

            session.createSessionOnline("1");

            ChatingService.startDriverAction(getContext());
            showDialog("Loading..");
            HashMap<String, String> jsonParams = new HashMap<String, String>();
            HashMap<String, String> userDetails = session.getUserDetails();
            HashMap<String,String>onlinedetails = session.getOnlineDetails();


            String driverId = userDetails.get("driverid");
            jsonParams.put("driver_id", "" + driverId);
            jsonParams.put("availability", "" + "Yes");
            ServiceManager manager = new ServiceManager(getActivity(), updateAvailablityServiceListener);
            manager.makeServiceRequest(ServiceConstant.UPDATE_AVAILABILITY, Request.Method.POST, jsonParams);
        }
    }


    protected void startLocationUpdates() {
        LocationServices.FusedLocationApi.requestLocationUpdates(
                mGoogleApiClient, mLocationRequest, this);
    }

    @Override
    public void onResume() {
        if (mGoogleApiClient != null && mGoogleApiClient.isConnected()) {
            startLocationUpdates();
        }
        super.onResume();
    }


    private ServiceManager.ServiceListener updateAvailablityServiceListener = new ServiceManager.ServiceListener() {
        @Override
        public void onCompleteListener(Object object) {
            try {
                dismissDialog();
                String response = (String) object;
                Intent i = new Intent(getActivity(), DriverMapActivity.class);
                startActivity(i);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onErrorListener(Object obj) {
            dismissDialog();
        }
    };


    private void setUpViews() {
        NavigationDrawer parentActivity = (NavigationDrawer) getActivity();
        resideMenu = parentActivity.getResideMenu();
    }

    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(getActivity())
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
    }


    @Override
    public void onConnected(Bundle connectionHint) {
        mLastLocation = LocationServices.FusedLocationApi.getLastLocation(
                mGoogleApiClient);
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
        if (mLastLocation != null) {
        }
    }

    @Override
    public void onConnectionSuspended(int i) {
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
    }

    @Override
    public void onLocationChanged(Location location) {
        mLastLocation = location;
    }
}
