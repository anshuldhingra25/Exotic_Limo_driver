package com.cabily.cabilydriver;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.toolbox.StringRequest;
import com.app.service.ServiceConstant;
import com.app.service.ServiceRequest;
import com.app.xmpp.ChatingService;
import com.cabily.cabilydriver.Utils.ConnectionDetector;
import com.cabily.cabilydriver.Utils.GPSTracker;
import com.cabily.cabilydriver.Utils.SessionManager;
import com.cabily.cabilydriver.adapter.ContinuousRequestAdapter;
import com.cabily.cabilydriver.googlemappath.GMapV2GetRouteDirection;
import com.cabily.cabilydriver.subclass.RealTimeActivity;
import com.cabily.cabilydriver.subclass.SubclassActivity;
import com.cabily.cabilydriver.widgets.PkDialog;
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
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.vision.barcode.Barcode;
import com.romainpiel.shimmer.Shimmer;
import com.romainpiel.shimmer.ShimmerButton;

import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.chat.Chat;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Document;

import java.util.ArrayList;
import java.util.HashMap;


/**
 * Created by user88 on 10/28/2015.
 */
public class ArrivedTrip extends RealTimeActivity implements com.google.android.gms.location.LocationListener, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, SeekBar.OnSeekBarChangeListener {
    private static final String TAG = "swipe";
    private Context context;
    private SessionManager session;
    private String driver_id = "";
    private String Str_RideId = "";
    private String Str_address = "";
    private String Str_pickUp_Lat = "";
    private String Str_pickUp_Long = "";
    private String Str_username = "";
    private String Str_user_rating = "";
    private String Str_user_phoneno = "";
    private String Str_user_img = "";
    private String Str_droplat = "";
    private String Str_droplon = "";
    private String str_drop_location = "";
    private TextView Tv_Address, Tv_RideId, Tv_usename;
    private RelativeLayout Rl_layout_userinfo, Rl_layout_arrived;
    private String ERROR_TAG = "Unknown Error Occured";
    private RelativeLayout Rl_layout_enable_voicenavigation;
    float[] results;
    final static int REQUEST_LOCATION = 199;
    float initialX, initialY;
    // List<Overlay> mapOverlays;
    private Barcode.GeoPoint point1, point2;
    private LocationManager locManager;
    Drawable drawable;
    Document document;
    GMapV2GetRouteDirection v2GetRouteDirection;
    LatLng fromPosition;
    LatLng toPosition;
    MarkerOptions markerOptions;
    Location location;
    private StringRequest postrequest;
    private Dialog dialog;
    private Boolean isInternetPresent = false;
    private ConnectionDetector cd;
    private GoogleMap googleMap;
    private GPSTracker gps;
    private double MyCurrent_lat = 0.0, MyCurrent_long = 0.0;
    private RelativeLayout alert_layout;
    private TextView alert_textview;
    private ImageView phone_call;
    public static ArrivedTrip arrivedTrip_class;
    private ServiceRequest mRequest;
    private String Suser_Id = "";
    private LocationRequest mLocationRequest;
    public static Location myLocation;
    private GoogleApiClient mGoogleApiClient;
    PendingResult<LocationSettingsResult> result;
    private Marker currentMarker;
    MarkerOptions marker;
    private LatLng latLng;
    double previous_lat, previous_lon, current_lat, current_lon;

    private String Str_Latitude = "", Str_longitude = "";

    //Slider Design Declaration
    SeekBar sliderSeekBar;
    ShimmerButton Bt_slider;
    Shimmer shimmer;

    private final static int INTERVAL = 45000;
    Handler mHandler;


    //-----------------------------code for car moving handler------------
    private Handler arrivedTripHandler = new Handler();
    private int count = 0;

    private Runnable arrivedTripRunnable = new Runnable() {
        @Override
        public void run() {
            gps = new GPSTracker(ArrivedTrip.this);
            if (gps != null && gps.canGetLocation()) {
            } else {
                enableGpsService();
            }
            arrivedTripHandler.postDelayed(this, 600);
        }
    };
    Chat chat;

    public static class MessageHandler extends Handler {
        @Override
        public void handleMessage(Message message) {
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.arrivedtrip);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        arrivedTrip_class = ArrivedTrip.this;
        initialize();
        try {
            setLocationRequest();
            buildGoogleApiClient();
            initilizeMap();
        } catch (Exception e) {
        }

        //Starting Xmpp service
        Rl_layout_userinfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ArrivedTrip.this, UserInfo.class);
                intent.putExtra("user_name", Str_username);
                intent.putExtra("user_phoneno", Str_user_phoneno);
                intent.putExtra("user_rating", Str_user_rating);
                intent.putExtra("user_image", Str_user_img);
                intent.putExtra("RideId", Str_RideId);
                startActivity(intent);
                overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
            }
        });


        phone_call.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (Str_user_phoneno != null) {
                    Intent callIntent = new Intent(Intent.ACTION_CALL);
                    callIntent.setData(Uri.parse("tel:" + Str_user_phoneno));
                    startActivity(callIntent);
                } else {
                    Alert(ArrivedTrip.this.getResources().getString(R.string.alert_sorry_label_title), ArrivedTrip.this.getResources().getString(R.string.arrived_alert_content1));
                }

            }
        });


        Rl_layout_enable_voicenavigation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String voice_curent_lat_long = MyCurrent_lat + "," + MyCurrent_long;
                String voice_destination_lat_long = Str_pickUp_Lat + "," + Str_pickUp_Long;
                System.out.println("----------fromPosition---------------" + voice_curent_lat_long);
                System.out.println("----------toPosition---------------" + voice_destination_lat_long);
                String locationUrl = "http://maps.google.com/maps?saddr=" + voice_curent_lat_long + "&daddr=" + voice_destination_lat_long;
                System.out.println("----------locationUrl---------------" + locationUrl);
                Intent intent = new Intent(android.content.Intent.ACTION_VIEW, Uri.parse(locationUrl));
                startActivity(intent);
            }
        });


    }



    private void initialize() {
        session = new SessionManager(ArrivedTrip.this);
        gps = new GPSTracker(ArrivedTrip.this);
        v2GetRouteDirection = new GMapV2GetRouteDirection();
        arrivedTripHandler.post(arrivedTripRunnable);
        mHandler=new Handler();

        HashMap<String, String> user = session.getUserDetails();
        driver_id = user.get(SessionManager.KEY_DRIVERID);
        alert_textview = (TextView) findViewById(R.id.arrivd_Tripaccpt_alert_textView);
        alert_layout = (RelativeLayout) findViewById(R.id.arrivd_Tripaccpt_alert_layout);
        phone_call = (ImageView) findViewById(R.id.user_phonecall);
        Intent i = getIntent();
        Str_address = i.getStringExtra("address");
        Str_RideId = i.getStringExtra("rideId");
        Str_pickUp_Lat = i.getStringExtra("pickuplat");
        Str_pickUp_Long = i.getStringExtra("pickup_long");
        Str_username = i.getStringExtra("username");
        Str_user_rating = i.getStringExtra("userrating");
        Str_user_phoneno = i.getStringExtra("phoneno");
        Str_user_img = i.getStringExtra("userimg");
        Str_droplat = i.getStringExtra("drop_lat");
        Str_droplon = i.getStringExtra("drop_lon");
        str_drop_location = i.getStringExtra("drop_location");
        System.out.println("KKKKKKK---------" + Str_droplat);
        Suser_Id = i.getStringExtra("UserId");

        System.out.println("DERiverID----------------"+driver_id);

        System.out.println("UserId---------" + Suser_Id);
        System.out.println("adres---------" + Str_address);
        System.out.println("id---------" + Str_RideId);
        Tv_Address = (TextView) findViewById(R.id.trip_arrived_user_address);
        // Tv_RideId = (TextView) findViewById(R.id.trip_arrived_user_id);
        Tv_usename = (TextView) findViewById(R.id.trip_arrived_usernameTxt);
        Rl_layout_userinfo = (RelativeLayout) findViewById(R.id.layout_arrived_trip_userinfo);
        Rl_layout_arrived = (RelativeLayout) findViewById(R.id.layout_arrivedbtn);
        Rl_layout_enable_voicenavigation = (RelativeLayout) findViewById(R.id.layout_arrived_Enable_voice);

        shimmer = new Shimmer();
        sliderSeekBar = (SeekBar) findViewById(R.id.arrived_Trip_seek);
        Bt_slider = (ShimmerButton) findViewById(R.id.arrived_Trip_slider_button);
        shimmer.start(Bt_slider);
        sliderSeekBar.setOnSeekBarChangeListener(this);
        Tv_Address.setText(Str_address);
        // Tv_RideId.setText(Str_RideId);
        Tv_usename.setText(Str_username);
        cd = new ConnectionDetector(ArrivedTrip.this);
        isInternetPresent = cd.isConnectingToInternet();
        if (isInternetPresent) {
            mHandler.post(mHandlerTask);
        } else {
            Alert(getResources().getString(R.string.alert_sorry_label_title), getResources().getString(R.string.alert_nointernet));
        }

    }

    @Override
    public void onBackPressed() {
        //super.onBackPressed();
    }

    //--------------Alert Method-----------
    private void Alert(String title, String message) {
        final PkDialog mDialog = new PkDialog(ArrivedTrip.this);
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


    Runnable mHandlerTask = new Runnable()
    { @Override public void run() {

            gps = new GPSTracker(ArrivedTrip.this);
            cd = new ConnectionDetector(ArrivedTrip.this);
            isInternetPresent = cd.isConnectingToInternet();
            if(isInternetPresent)
            {
                if (gps != null && gps.canGetLocation() && gps.isgpsenabled()) {

                    Str_Latitude = String.valueOf(gps.getLatitude());
                    Str_longitude = String.valueOf(gps.getLongitude());

                    postRequest_UpdateProviderLocation(ServiceConstant.UPDATE_CURRENT_LOCATION);
                }
            }else
            {
                Toast.makeText(ArrivedTrip.this, getResources().getString(R.string.no_internet_connection), Toast.LENGTH_SHORT).show();
            }

            mHandler.postDelayed(mHandlerTask, INTERVAL);
        }
    };


    private void initilizeMap() {
        if (googleMap == null) {
            googleMap = ((MapFragment) ArrivedTrip.this.getFragmentManager().findFragmentById(R.id.arrived_trip_view_map)).getMap();
            if (googleMap == null) {
                Toast.makeText(ArrivedTrip.this, getResources().getString(R.string.action_alert_unabletocreatemap), Toast.LENGTH_SHORT).show();
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
        if (gps != null && gps.canGetLocation()) {
            double Dlatitude = gps.getLatitude();
            double Dlongitude = gps.getLongitude();
            MyCurrent_lat = Dlatitude;
            MyCurrent_long = Dlongitude;
            CameraPosition cameraPosition = new CameraPosition.Builder().target(new LatLng(Dlatitude, Dlongitude)).zoom(18).build();
            googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
            System.out.println("currntlat----------" + MyCurrent_lat);
            System.out.println("currntlon----------" + MyCurrent_long);
        } else {
            alert_layout.setVisibility(View.VISIBLE);
            alert_textview.setText(getResources().getString(R.string.alert_gpsEnable));
        }
        markerOptions = new MarkerOptions();
    }


    @Override
    public void onStart() {
        super.onStart();
        if (mGoogleApiClient != null)
            mGoogleApiClient.connect();
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
        }

    }

    MarkerOptions mm = new MarkerOptions();
    Marker drivermarker;
    JSONObject job = new JSONObject();

    @Override
    public void onLocationChanged(Location location) {
        this.myLocation = location;
        System.out.println("locatbegintrip-----------" + location);
        if (myLocation != null) {
            try {
                LatLng latLng = new LatLng(myLocation.getLatitude(), myLocation.getLongitude());
                drawRouteInMap();
                sendLocationToTheUser(myLocation);
                if (drivermarker != null) {
                    drivermarker.remove();
                }
                drivermarker = googleMap.addMarker(new MarkerOptions().position(latLng).icon(BitmapDescriptorFactory.fromResource(R.drawable.carmove)));
            } catch (Exception e) {
            }
        }

    }

    private void drawRouteInMap() {
        try {
            if(!isDrawnOnMap){
                fromPosition = new LatLng(myLocation.getLatitude(), myLocation.getLongitude());
                toPosition = new LatLng(Double.parseDouble(Str_pickUp_Lat), Double.parseDouble(Str_pickUp_Long));
                marker = new MarkerOptions().position(new LatLng(MyCurrent_lat, MyCurrent_long));
                marker.icon(BitmapDescriptorFactory.fromResource(R.drawable.dark_green_flag));
                currentMarker = googleMap.addMarker(marker);
                if (fromPosition != null && toPosition != null) {
                    GetRouteTask getRoute = new GetRouteTask();
                    getRoute.execute();
                }
            }else{
            }
        } catch (Exception e) {
            Toast.makeText(getApplicationContext(), ERROR_TAG, Toast.LENGTH_SHORT).show();
        }
    }

    private void sendLocationToTheUser(Location location) throws JSONException {
        String sendlat = Double.valueOf(location.getLatitude()).toString();
        String sendlng = Double.valueOf(location.getLongitude()).toString();
        if(job == null){
            job = new JSONObject();
        }

        job.put("action", "driver_loc");
        job.put("latitude", sendlat);
        job.put("longitude", sendlng);
        job.put("ride_id", Str_RideId);
        builder.sendMessage(chatID,job.toString());
        /*String sToID = ContinuousRequestAdapter.userID + "@" + ServiceConstant.XMPP_SERVICE_NAME;
        try {
            if(chat  != null){
                chat.sendMessage(job.toString());
            }else{
                chat = ChatingService.createChat(sToID);
                chat.sendMessage(job.toString());
            }
        } catch (SmackException.NotConnectedException e) {
            try {
                chat = ChatingService.createChat(sToID);
                chat.sendMessage(job.toString());
            }catch (SmackException.NotConnectedException e1){
                Toast.makeText(this,"Not Able to send data to the user Network Error",Toast.LENGTH_SHORT).show();
            }
        }*/
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
    }
    protected void onStop() {
        if (mGoogleApiClient != null) {
            mGoogleApiClient.disconnect();
        }
        super.onStop();
        mHandler.removeCallbacks(mHandlerTask);
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        if(chat != null){
            chat.close();
        }
        mHandler.removeCallbacks(mHandlerTask);

    }
    boolean isDrawnOnMap = false;
    private class GetRouteTask extends AsyncTask<String, Void, String> {
        String response = "";
        @Override
        protected void onPreExecute() {
        }
        @Override
        protected String doInBackground(String... urls) {
            //Get All Route values
            document = v2GetRouteDirection.getDocument(fromPosition, toPosition, GMapV2GetRouteDirection.MODE_DRIVING);
            response = "Success";
            return response;
        }

        @Override
        protected void onPostExecute(String result) {
            // googleMap.clear();
            ArrayList<LatLng> directionPoint = v2GetRouteDirection.getDirection(document);
            PolylineOptions rectLine = new PolylineOptions().width(15).color(getResources().getColor(R.color.app_color));

            for (int i = 0; i < directionPoint.size(); i++) {
                rectLine.add(directionPoint.get(i));
            }
            // Adding route on the map
            googleMap.addPolyline(rectLine);
            markerOptions.position(fromPosition);
            markerOptions.position(toPosition);
            markerOptions.draggable(true);
            //googleMap.addMarker(markerOptions);
            isDrawnOnMap = true;
            googleMap.addMarker(new MarkerOptions()
                    .position(toPosition)
                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.light_green_flag)));
            LatLngBounds.Builder builder = new LatLngBounds.Builder();
            builder.include(toPosition);
            builder.include(fromPosition);
            LatLngBounds bounds = builder.build();
            googleMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, 50));

        }
    }


    @Override
    protected void onResume() {
        super.onResume();
        startLocationUpdates();
        mHandlerTask.run();
    }

    private void setLocationRequest() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setSmallestDisplacement(5);
        mLocationRequest.setInterval(1000);
        mLocationRequest.setFastestInterval(1000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    protected void startLocationUpdates() {
        if (mGoogleApiClient != null && mGoogleApiClient.isConnected()) {
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
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
    public void onConnectionSuspended(int i) {

    }


    //-----------------------Code for arrived post request-----------------
    private void PostRequest(String Url) {
        dialog = new Dialog(ArrivedTrip.this);
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
        jsonParams.put("ride_id", Str_RideId);
        System.out
                .println("--------------driver_id-------------------"
                        + driver_id);
        System.out
                .println("--------------ride_id-------------------"
                        + Str_RideId);


        mRequest = new ServiceRequest(ArrivedTrip.this);
        mRequest.makeServiceRequest(Url, Request.Method.POST, jsonParams, new ServiceRequest.ServiceListener() {

            @Override
            public void onCompleteListener(String response) {

                Log.e("arrived", response);

                System.out.println("response---------" + response);

                String Str_status = "", Str_response = "";

                try {
                    JSONObject object = new JSONObject(response);
                    Str_status = object.getString("status");
                    Str_response = object.getString("response");
                } catch (Exception e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                dialog.dismiss();
                if (Str_status.equalsIgnoreCase("1")) {
                    if (Str_droplat != null && !Str_droplon.equalsIgnoreCase("") && Str_droplat != null && !Str_droplon.equalsIgnoreCase("")) {
                        Intent intent = new Intent(ArrivedTrip.this, BeginTrip.class);
                        intent.putExtra("user_name", Str_username);
                        intent.putExtra("rideid", Str_RideId);
                        intent.putExtra("user_image", Str_user_img);
                        intent.putExtra("user_phoneno", Str_user_phoneno);
                        intent.putExtra("drop_location", str_drop_location);
                        intent.putExtra("DropLatitude", Str_droplat);
                        intent.putExtra("DropLongitude", Str_droplon);
                        String locationaddressstartingpoint = String.valueOf(MyCurrent_lat + "," + MyCurrent_long);
                        Log.d("myloc", locationaddressstartingpoint);
                        intent.putExtra("pickuplatlng", Str_droplat + "," + Str_droplon);
                        intent.putExtra("startpoint", locationaddressstartingpoint);
                        startActivity(intent);
                        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
                    } else {
                        Intent intent = new Intent(ArrivedTrip.this, BeginTrip.class);
                        intent.putExtra("user_name", Str_username);
                        intent.putExtra("user_phoneno", Str_user_phoneno);
                        intent.putExtra("user_image", Str_user_img);
                        intent.putExtra("rideid", Str_RideId);
                        startActivity(intent);
                        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
                        finish();
                    }
                } else {
                    Alert(getResources().getString(R.string.alert_sorry_label_title), Str_response);
                }

            }

            @Override
            public void onErrorListener() {

                dialog.dismiss();
            }

        });


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
                            status.startResolutionForResult(ArrivedTrip.this, REQUEST_LOCATION);
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
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        if (progress > 95) {
            seekBar.setThumb(getResources().getDrawable(R.drawable.slidetounlock_arrow));
        }
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
        Bt_slider.setVisibility(View.INVISIBLE);
    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {

        shimmer = new Shimmer();
        if (seekBar.getProgress() < 80) {
            seekBar.setProgress(0);
            sliderSeekBar.setBackgroundResource(R.drawable.blue_slide_to_unlock_bg);
            Bt_slider.setVisibility(View.VISIBLE);
            Bt_slider.setText(getResources().getString(R.string.arrivedtrip_arrivedtriptv_label));
            shimmer.start(Bt_slider);
        } else if (seekBar.getProgress() > 90) {
            seekBar.setProgress(100);
            Bt_slider.setVisibility(View.VISIBLE);
            Bt_slider.setText(getResources().getString(R.string.arrivedtrip_arrivedtriptv_label));
            shimmer.start(Bt_slider);
            sliderSeekBar.setVisibility(View.VISIBLE);

            cd = new ConnectionDetector(ArrivedTrip.this);
            isInternetPresent = cd.isConnectingToInternet();
            if (isInternetPresent) {
                PostRequest(ServiceConstant.arrivedtrip_url);
                System.out.println("arrived------------------" + ServiceConstant.arrivedtrip_url);
            } else {
                Alert(getResources().getString(R.string.alert_sorry_label_title), getResources().getString(R.string.alert_nointernet));
            }
        }
    }

    //-----------------------Update current Location for notification  Post Request-----------------
    private void postRequest_UpdateProviderLocation(String Url) {

        HashMap<String, String> jsonParams = new HashMap<String, String>();
        jsonParams.put("ride_id",Str_RideId);
        jsonParams.put("latitude",Str_Latitude );
        jsonParams.put("longitude",Str_longitude );
        jsonParams.put("driver_id",driver_id);

        System.out.println("-------------Arrivedsendrequestride_id----------------" + Str_RideId);
        System.out.println("-------------Arrivedsendrequestlatitude----------------" + Str_Latitude );
        System.out.println("-------------Arrivedsendrequestlongitude----------------" + Str_longitude);

        System.out.println("-------------latlongupdate----------------" + Url);
        mRequest = new ServiceRequest(ArrivedTrip.this);
        mRequest.makeServiceRequest(Url, Request.Method.POST, jsonParams, new ServiceRequest.ServiceListener() {
            @Override
            public void onCompleteListener(String response) {

                Log.e("updatelocation", response);

                System.out.println("-------------latlongupdate----------------" + response);


            }

            @Override
            public void onErrorListener() {

            }
        });
    }



    @Override
    protected void onPause() {
        super.onPause();
        System.gc();
        if(chat != null){
            chat.close();
        }
        mHandler.removeCallbacks(mHandlerTask);
    }

}