package com.cabily.cabilydriver;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.content.IntentSender;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationManager;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.toolbox.StringRequest;
import com.app.latlnginterpolation.LatLngInterpolator;
import com.app.latlnginterpolation.MarkerAnimation;
import com.app.service.ServiceConstant;
import com.app.service.ServiceRequest;
import com.app.xmpp.ChatingService;
import com.cabily.cabilydriver.Utils.ConnectionDetector;
import com.cabily.cabilydriver.Utils.CurrencySymbolConverter;
import com.cabily.cabilydriver.Utils.GPSTracker;
import com.cabily.cabilydriver.Utils.SessionManager;
import com.cabily.cabilydriver.adapter.ContinuousRequestAdapter;
import com.cabily.cabilydriver.googlemappath.GMapV2GetRouteDirection;
import com.cabily.cabilydriver.subclass.SubclassActivity;
import com.cabily.cabilydriver.widgets.PkDialog;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.maps.CameraUpdate;
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

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;

import me.drakeet.materialdialog.MaterialDialog;

/**
 * Created by user88 on 10/29/2015.
 */
public class EndTrip extends SubclassActivity implements com.google.android.gms.location.LocationListener, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, SeekBar.OnSeekBarChangeListener {
    private final static int REQUEST_LOCATION = 199;
    private PendingResult<LocationSettingsResult> result;
    private static final String TAG = "swipe";
    private String driver_id = "";
    private Boolean isInternetPresent = false;
    private ConnectionDetector cd;
    private SessionManager session;
    private TextView Tv_name, Tv_mobilno, Tv_rideid, Tv_start_wait, Tv_stop_wait;
    private RelativeLayout Rl_layout_back;
    private Button Bt_Endtrip;
    private String Str_name = "", Str_mobilno = "", Str_rideid = "";
    private RelativeLayout alert_layout;
    private TextView alert_textview;
    private float initialX, initialY;
    private Marker currentMarkerto;
    public MarkerOptions markerto;
    private String droplocation[];
    private String startlocation[];
    private MarkerOptions marker;
    private double previous_lat, previous_lon, current_lat, current_lon, dis = 0.0;
    public static Location myLocation;
    private GoogleApiClient mGoogleApiClient;
    private LocationRequest mLocationRequest;
    private Marker currentMarker;
    private ServiceRequest mRequest;
    private GoogleMap googleMap;
    private Dialog dialog;
    private StringRequest postrequest;
    private MediaPlayer mediaPlayer;
    private GMapV2GetRouteDirection v2GetRouteDirection;
    private Document document;
    private MarkerOptions markerOptions;
    private int mins;
    private int secs;
    private int milliseconds;
    private Button Bt_Enable_voice;
    private String Str_status = "", Str_response = "", Str_ridefare = "", Str_timetaken = "", Str_waitingtime = "", Str_need_payment = "", Str_currency = "", Str_ride_distance = "", str_recievecash = "";
    private GPSTracker gps;
    private LatLng fromPosition, toposition;
    private LatLng destlatlng, startlatlng;
    private double MyCurrent_lat = 0.0, MyCurrent_long = 0.0;
    private TextView timerValue;
    private RelativeLayout layout_timer;
    private long startTime = 0L;
    private Handler customHandler = new Handler();
    private long timeInMilliseconds = 0L;
    private long timeSwapBuff = 0L;
    private long updatedTime = 0L;
    private LatLng start = new LatLng(18.015365, -77.499382);
    LatLng waypoint = new LatLng(18.01455, -77.499333);
    LatLng end = new LatLng(18.012590, -77.500659);
    float[] results;
    LocationManager locationManager;
    Barcode.GeoPoint geoPoint;
    double location;
    private String beginAddress;
    private String endAddress;
    private String sCurrencySymbol = "";
    private String distance = "";
    private LatLng latLng;
    private PolylineOptions mPolylineOptions;
    private SeekBar sliderSeekBar;
    private ShimmerButton Bt_slider;
    private Shimmer shimmer;

    private String Str_Latitude = "", Str_longitude = "";

    private final static int INTERVAL = 45000;
    Handler mHandler;



    public static class MessageHandler extends Handler {
        @Override
        public void handleMessage(Message message) {
        }
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.endtrip);
        initialize();
        try {
            setLocationRequest();
            buildGoogleApiClient();
            initilizeMap();
        } catch (Exception e) {
        }
        ChatingService.startDriverAction(EndTrip.this);
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
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mHandler.removeCallbacks(mHandlerTask);
    }

    private void initialize() {
        session = new SessionManager(EndTrip.this);
        gps = new GPSTracker(EndTrip.this);
        mHandler=new Handler();

        // get user data from session
        HashMap<String, String> user = session.getUserDetails();
        driver_id = user.get(SessionManager.KEY_DRIVERID);
        v2GetRouteDirection = new GMapV2GetRouteDirection();
        Bundle b = getIntent().getExtras();
        if (b != null && b.containsKey("pickuplatlng")) {
            droplocation = b.getString("pickuplatlng").split(",");
            Log.d("LATLONG", droplocation.toString());
        }
        if (b != null && b.containsKey("startpoint")) {
            beginAddress = b.getString("startpoint");
            startlocation = b.getString("startpoint").split(",");
            Log.d("LATLONGccccccc", startlocation.toString());
            try {
                double latitude = Double.parseDouble(droplocation[0]);
                double longitude = Double.parseDouble(droplocation[1]);
                double startlatitude = Double.parseDouble(startlocation[0]);
                double startlongitude = Double.parseDouble(startlocation[1]);
                destlatlng = new LatLng(latitude, longitude);
                startlatlng = new LatLng(startlatitude, startlongitude);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
      /*  if (isInternetPresent=true)
        {
         }else {
            mediaPlayer = MediaPlayer.create(this,R.raw.jinngle);
        }
*/
        //endTripHandler.post(endTripRunnable);
        //---------------set polyline color and width----------------
        mPolylineOptions = new PolylineOptions();
        mPolylineOptions.color(Color.BLUE).width(10);
        Intent i = getIntent();
        Str_rideid = i.getStringExtra("rideid");
        Str_name = i.getStringExtra("name");
        Str_mobilno = i.getStringExtra("mobilno");
        Tv_name = (TextView) findViewById(R.id.end_trip_name);
        Tv_mobilno = (TextView) findViewById(R.id.end_trip_mobilno);
        Tv_rideid = (TextView) findViewById(R.id.beginendtrip_rideid);
        Tv_start_wait = (TextView) findViewById(R.id.begin_waitingtime_tv_start);
        Tv_stop_wait = (TextView) findViewById(R.id.begin_waitingtime_tv_stop);
        timerValue = (TextView) findViewById(R.id.timerValue);
        layout_timer = (RelativeLayout) findViewById(R.id.layout_timer);
        alert_layout = (RelativeLayout) findViewById(R.id.end_trip_alert_layout);
        alert_textview = (TextView) findViewById(R.id.end_trip_alert_textView);

        shimmer = new Shimmer();
        sliderSeekBar = (SeekBar) findViewById(R.id.end_Trip_seek);
        Bt_slider = (ShimmerButton) findViewById(R.id.end_Trip_slider_button);
        shimmer.start(Bt_slider);

        sliderSeekBar.setOnSeekBarChangeListener(this);


        Tv_name.setText(Str_name);
        Tv_mobilno.setText(Str_mobilno);


        cd = new ConnectionDetector(EndTrip.this);
        isInternetPresent = cd.isConnectingToInternet();
        if (isInternetPresent) {
            mHandlerTask.run();
        } else {
            Alert(getResources().getString(R.string.alert_sorry_label_title), getResources().getString(R.string.alert_nointernet));
        }


    }


    private void updatePolyline() {
        // googleMap.clear();
        Toast.makeText(EndTrip.this, "distance for endtrip " + String.valueOf(dis), Toast.LENGTH_SHORT).show();
        googleMap.addPolyline(mPolylineOptions.add(latLng));
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

                Intent broadcastIntent_begintrip = new Intent();
                broadcastIntent_begintrip.setAction("com.finish.com.finish.BeginTrip");
                sendBroadcast(broadcastIntent_begintrip);

                Intent broadcastIntent_arrivedtrip = new Intent();
                broadcastIntent_arrivedtrip.setAction("com.finish.ArrivedTrip");
                sendBroadcast(broadcastIntent_arrivedtrip);

                Intent broadcastIntent_endtrip = new Intent();
                broadcastIntent_endtrip.setAction("com.finish.EndTrip");
                sendBroadcast(broadcastIntent_endtrip);
                Intent intent = new Intent(EndTrip.this, LoadingPage.class);
                intent.putExtra("Driverid", driver_id);
                intent.putExtra("RideId", Str_rideid);
                startActivity(intent);
                overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
                finish();
            }
        });
        mDialog.show();
    }


    //------------------------------code for distance----------------------------
    @Override
    protected void onResume() {
        super.onResume();
        mHandlerTask.run();
        startLocationUpdates();

    }

    private void setLocationRequest() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(2000);
        mLocationRequest.setFastestInterval(2000);
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
    public void onStart() {
        super.onStart();
        if (mGoogleApiClient != null)
            mGoogleApiClient.connect();
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mGoogleApiClient != null)
            mGoogleApiClient.disconnect();
        mHandler.removeCallbacks(mHandlerTask);
    }


    public void onConnected(Bundle bundle) {

        if (gps != null && gps.canGetLocation() && gps.isgpsenabled()) {
        }
        myLocation = LocationServices.FusedLocationApi.getLastLocation(
                mGoogleApiClient);
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, (LocationListener) this);
        if (myLocation != null) {
            if (googleMap == null)
                googleMap = ((MapFragment) EndTrip.this.getFragmentManager().findFragmentById(R.id.arrived_trip_view_map)).getMap();
            googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(myLocation.getLatitude(), myLocation.getLongitude()),
                    16));
            markerOptions = new MarkerOptions();
            marker = new MarkerOptions();
            marker.position(new LatLng(myLocation.getLatitude(), myLocation.getLongitude()));
            marker.icon(BitmapDescriptorFactory.fromResource(R.drawable.cargreens));
            currentMarker = googleMap.addMarker(marker);
            System.out.println("online------------------" + ServiceConstant.UPDATE_CURRENT_LOCATION);
            toposition = new LatLng(myLocation.getLatitude(), myLocation.getLongitude());
            if (startlatlng != null && destlatlng != null) {
                GetRouteTask getRoute = new GetRouteTask();
                getRoute.execute();
            }
            fromPosition = new LatLng(myLocation.getLatitude(), myLocation.getLongitude());
//            connector = new MapPolyLineConnector(fromPosition, toposition, googleMap, marker);
//            connector.execute();
        }
    }

    public void onConnectionSuspended(int i) {

    }

    MarkerOptions mm = new MarkerOptions();
    Marker drivermarker;
    boolean isFirstTime;

    static LatLngInterpolator latLngInterpolator = new LatLngInterpolator.Spherical();

    public static void updateMap(LatLng latLng, Marker drivermarker) {
        try {
            MarkerAnimation.animateMarkerToICS(drivermarker, latLng, latLngInterpolator);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    JSONObject job;

    @Override
    public void onLocationChanged(Location location) {

        this.myLocation = location;
        System.out.println("locat-----------" + location);
        if (myLocation != null) {
            try {
                LatLng latLng = new LatLng(myLocation.getLatitude(), myLocation.getLongitude());
                if (drivermarker != null) {
                    drivermarker.remove();
                }
                drivermarker = googleMap.addMarker(new MarkerOptions().position(latLng).icon(BitmapDescriptorFactory.fromResource(R.drawable.carmove)));
                sendLocationToUser(myLocation);
            } catch (Exception e) {
            }

            if (currentMarker != null) {
                System.out.println("mylocatuon-----------" + myLocation);
                LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
                current_lat = location.getLatitude();
                current_lon = location.getLongitude();
                currentMarker.setPosition(latLng);
            }
            //  googleMap.addMarker(new MarkerOptions().position(latLng).icon(BitmapDescriptorFactory.fromResource(R.drawable.dot)));
            //markerOptionsnew = new MarkerOptions();
            //  markernew = new MarkerOptions();
            //  markernew.position((fromPosition));
            // markernew.icon(BitmapDescriptorFactory.fromResource(R.drawable.cargreens));
            // currentMarkerfrom = googleMap.addMarker(markernew);
            if (currentMarkerto != null)
                currentMarkerto.remove();
            //markerOptionsto = new MarkerOptions();
            markerto = new MarkerOptions();
            markerto.position((destlatlng));
            markerto.icon(BitmapDescriptorFactory.fromResource(R.drawable.flagimage));
            //currentMarkerto = googleMap.addMarker(markerto);
            System.out.println("online------------------" + ServiceConstant.UPDATE_CURRENT_LOCATION);
            toposition = new LatLng(myLocation.getLatitude(), myLocation.getLongitude());
            float[] f = new float[1];
            if (current_lat != previous_lat || current_lon != previous_lon) {
                //dis += getDistance(previous_lat, previous_lon, current_lat, current_lon);
                //previous_lat =13.052562;
                //previous_lon= 80.251086;
                //current_lat =13.054046;
                //current_lon   =80.253275;
                Location.distanceBetween(previous_lat, previous_lon, current_lat, current_lon, f);
                dis += Double.parseDouble(String.valueOf(f[0]));
                //Toast.makeText(EndTrip.this, "distance in metres2:" + String.valueOf(dis) , Toast.LENGTH_SHORT).show();
                previous_lat = current_lat;
                previous_lon = current_lon;
                System.out.println("distanceinside----------------------" + dis);
            } else {
                previous_lat = current_lat;
                previous_lon = current_lon;
                dis = dis;
            }
            previous_lat = current_lat;
            previous_lon = current_lon;
            // Toast.makeText(EndTrip.this, "distance in metres3:" + String.valueOf(dis), Toast.LENGTH_SHORT).show();
            if (googleMap != null) {
                // googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(myLocation.getLatitude(), myLocation.getLongitude()),
                //        16));
            }
            toposition = new LatLng(myLocation.getLatitude(), myLocation.getLongitude());
        }
    }
    Chat chat;
    private void sendLocationToUser(Location location) throws JSONException {
        String sendlat = Double.valueOf(location.getLatitude()).toString();
        String sendlng = Double.valueOf( location.getLongitude()).toString();
        if (job == null) {
            job = new JSONObject();
        }
        job.put("action", "driver_loc");
        job.put("latitude", sendlat);
        job.put("longitude", sendlng);
        job.put("ride_id", "");
        String sToID = ContinuousRequestAdapter.userID + "@" + ServiceConstant.XMPP_SERVICE_NAME;
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
        }


    }

    public static void midPoint(double lat1, double lon1, double lat2, double lon2) {

        double dLon = Math.toRadians(lon2 - lon1);
        //convert to radians
        lat1 = Math.toRadians(lat1);
        lat2 = Math.toRadians(lat2);
        lon1 = Math.toRadians(lon1);

        double Bx = Math.cos(lat2) * Math.cos(dLon);
        double By = Math.cos(lat2) * Math.sin(dLon);
        double lat3 = Math.atan2(Math.sin(lat1) + Math.sin(lat2), Math.sqrt((Math.cos(lat1) + Bx) * (Math.cos(lat1) + Bx) + By * By));
        double lon3 = lon1 + Math.atan2(By, Math.cos(lat1) + Bx);

        //print out in degrees
        System.out.println(Math.toDegrees(lat3) + " " + Math.toDegrees(lon3));
    }


    private class GetRouteTask extends AsyncTask<String, Void, String> {

        String response = "";

        @Override
        protected void onPreExecute() {
        }

        @Override
        protected String doInBackground(String... urls) {
            //Get All Route values
            document = v2GetRouteDirection.getDocument(startlatlng, destlatlng, GMapV2GetRouteDirection.MODE_DRIVING);
            response = "Success";
            return response;
        }

        @Override
        protected void onPostExecute(String result) {
            googleMap.clear();
            ArrayList<LatLng> directionPoint = v2GetRouteDirection.getDirection(document);
            PolylineOptions rectLine = new PolylineOptions().width(15).color(getResources().getColor(R.color.app_color));
            for (int i = 0; i < directionPoint.size(); i++) {
                rectLine.add(directionPoint.get(i));
            }
            Marker m[] = new Marker[2];
            m[0] = googleMap.addMarker(new MarkerOptions().position(startlatlng).icon(BitmapDescriptorFactory.fromResource(R.drawable.flageimage2)));
            m[1] = googleMap.addMarker(new MarkerOptions().position(destlatlng).icon(BitmapDescriptorFactory.fromResource(R.drawable.flagimage)));


            LatLngBounds.Builder builder = new LatLngBounds.Builder();
            for (Marker marker : m) {
                builder.include(marker.getPosition());
            }
            LatLngBounds bounds = builder.build();


            int padding = 100; // offset from edges of the map in pixels
            CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds, padding);


            googleMap.moveCamera(cu);


            googleMap.animateCamera(cu);

           /* markerOptions = new MarkerOptions();
            marker = new MarkerOptions();
            marker.position(new LatLng(myLocation.getLatitude(), myLocation.getLongitude()));
            marker.icon(BitmapDescriptorFactory.fromResource(R.drawable.cargreens));
            currentMarker = googleMap.addMarker(marker);*/


            // Adding route on the map
            googleMap.addPolyline(rectLine);
            markerOptions.position(destlatlng);
            markerOptions.position(startlatlng);
            markerOptions.draggable(true);

            //googleMap.addMarker(markerOptions);
         /*   googleMap.addMarker(new MarkerOptions()
                    .position(toposition)
                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.man)));
            googleMap.addMarker(new MarkerOptions()
                    .position(fromPosition)
                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.red_car)));*/
        }
    }


    public double getDistance(double lat1, double lon1, double lat2, double lon2) {
        double latA = Math.toRadians(lat1);
        double lonA = Math.toRadians(lon1);
        double latB = Math.toRadians(lat2);
        double lonB = Math.toRadians(lon2);
        double cosAng = (Math.cos(latA) * Math.cos(latB) * Math.cos(lonB - lonA)) +
                (Math.sin(latA) * Math.sin(latB));
        double ang = Math.acos(cosAng);
        double dist = ang * 6371;
        return dist;
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


    public void onConnectionFailed(ConnectionResult connectionResult) {
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

    Runnable mHandlerTask = new Runnable()
    { @Override public void run() {

            gps = new GPSTracker(EndTrip.this);
            cd = new ConnectionDetector(EndTrip.this);
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
                Toast.makeText(EndTrip.this, getResources().getString(R.string.no_internet_connection), Toast.LENGTH_SHORT).show();
            }

            mHandler.postDelayed(mHandlerTask, INTERVAL);
        }
    };


    private void initilizeMap() {
        /// myLocation = googleMap.getMyLocation();
        if (googleMap == null) {
            googleMap = ((MapFragment) EndTrip.this.getFragmentManager().findFragmentById(R.id.arrived_trip_view_map)).getMap();
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

            previous_lat = MyCurrent_lat;
            previous_lon = MyCurrent_long;
            // Move the camera to last position with a zoom level
            CameraPosition cameraPosition = new CameraPosition.Builder().target(new LatLng(Dlatitude, Dlongitude)).zoom(17).build();
            googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));

            // create marker double Dlatitude = gps.getLatitude();

            //----------------------set marker------------------
            marker = new MarkerOptions().position(new LatLng(Dlatitude, Dlongitude));
            marker.icon(BitmapDescriptorFactory.fromResource(R.drawable.red_car));

            currentMarker = googleMap.addMarker(marker);

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
        final TextView Tv_reqest = (TextView) view.findViewById(R.id.requst);
        TextView tv_fare_totalamount = (TextView) view.findViewById(R.id.fare_summery_total_amount);
        TextView tv_ridedistance = (TextView) view.findViewById(R.id.fare_summery_ride_distance_value);
        TextView tv_timetaken = (TextView) view.findViewById(R.id.fare_summery_ride_timetaken_value);
        TextView tv_waittime = (TextView) view.findViewById(R.id.fare_summery_wait_time_value);
        RelativeLayout layout_request_payment = (RelativeLayout) view.findViewById(R.id.layout_faresummery_requstpayment);
        RelativeLayout layout_receive_cash = (RelativeLayout) view.findViewById(R.id.fare_summery_receive_cash_layout);
        tv_fare_totalamount.setText(Str_ridefare);
        tv_ridedistance.setText(Str_ride_distance);
        tv_timetaken.setText(Str_timetaken);
        tv_waittime.setText(Str_waitingtime);
        dialog.setView(view).show();
        //if (Str_need_payment.equalsIgnoreCase("YES")){
        layout_receive_cash.setVisibility(View.VISIBLE);
        layout_request_payment.setVisibility(View.VISIBLE);
        Tv_reqest.setText(EndTrip.this.getResources().getString(R.string.lbel_fare_summery_requestpayment));

        //}else{
        //  layout_receive_cash.setVisibility(View.GONE);
        // Tv_reqest.setText(EndTrip.this.getResources().getString(R.string.alert_label_ok));

        //}

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

                    if (Tv_reqest.getText().toString().equalsIgnoreCase(EndTrip.this.getResources().getString(R.string.lbel_fare_summery_requestpayment))) {
                        postRequest_Reqqustpayment(ServiceConstant.request_paymnet_url);
                        System.out.println("arrived------------------" + ServiceConstant.request_paymnet_url);
                    } else {
                        Intent intent = new Intent(EndTrip.this, RatingsPage.class);
                        startActivity(intent);
                        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
                    }

                } else {
                    Alert(getResources().getString(R.string.alert_sorry_label_title), getResources().getString(R.string.alert_nointernet));
                }
            }
        });

    }

    private void showfaresummerydetails1() {

        final MaterialDialog dialog = new MaterialDialog(EndTrip.this);
        View view = LayoutInflater.from(EndTrip.this).inflate(R.layout.fare_summery_alert_dialog, null);
        final TextView Tv_reqest = (TextView) view.findViewById(R.id.requst);
        TextView tv_fare_totalamount = (TextView) view.findViewById(R.id.fare_summery_total_amount);
        TextView tv_ridedistance = (TextView) view.findViewById(R.id.fare_summery_ride_distance_value);
        TextView tv_timetaken = (TextView) view.findViewById(R.id.fare_summery_ride_timetaken_value);
        TextView tv_waittime = (TextView) view.findViewById(R.id.fare_summery_wait_time_value);
        RelativeLayout layout_request_payment = (RelativeLayout) view.findViewById(R.id.layout_faresummery_requstpayment);
        RelativeLayout layout_receive_cash = (RelativeLayout) view.findViewById(R.id.fare_summery_receive_cash_layout);
        tv_fare_totalamount.setText(Str_ridefare);
        tv_ridedistance.setText(Str_ride_distance);
        tv_timetaken.setText(Str_timetaken);
        tv_waittime.setText(Str_waitingtime);
        dialog.setView(view).show();


        // if (Str_need_payment.equalsIgnoreCase("YES")){

        layout_receive_cash.setVisibility(View.GONE);
        layout_request_payment.setVisibility(View.VISIBLE);
        Tv_reqest.setText(EndTrip.this.getResources().getString(R.string.lbel_fare_summery_requestpayment));

        //  }else{
        // layout_receive_cash.setVisibility(View.GONE);
        //  Tv_reqest.setText(EndTrip.this.getResources().getString(R.string.alert_label_ok));

        // }
//
        /*layout_receive_cash.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(EndTrip.this, OtpPage.class);
                intent.putExtra("rideid", Str_rideid);
                startActivity(intent);
                overridePendingTransition(R.anim.fade_in, R.anim.fade_out);

            }
        });*/

        layout_request_payment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cd = new ConnectionDetector(EndTrip.this);
                isInternetPresent = cd.isConnectingToInternet();
                if (isInternetPresent) {

                    if (Tv_reqest.getText().toString().equalsIgnoreCase(EndTrip.this.getResources().getString(R.string.lbel_fare_summery_requestpayment))) {
                        postRequest_Reqqustpayment(ServiceConstant.request_paymnet_url);
                        System.out.println("arrived------------------" + ServiceConstant.request_paymnet_url);
                    } else {
                        Intent intent = new Intent(EndTrip.this, RatingsPage.class);
                        startActivity(intent);
                        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
                    }

                } else {
                    Alert(getResources().getString(R.string.alert_sorry_label_title), getResources().getString(R.string.alert_nointernet));
                }
            }
        });

    }


    private void showfaresummerydetails2() {

        final MaterialDialog dialog = new MaterialDialog(EndTrip.this);
        View view = LayoutInflater.from(EndTrip.this).inflate(R.layout.fare_summery_alert_dialog, null);
        final TextView Tv_reqest = (TextView) view.findViewById(R.id.requst);
        TextView tv_fare_totalamount = (TextView) view.findViewById(R.id.fare_summery_total_amount);
        TextView tv_ridedistance = (TextView) view.findViewById(R.id.fare_summery_ride_distance_value);
        TextView tv_timetaken = (TextView) view.findViewById(R.id.fare_summery_ride_timetaken_value);
        TextView tv_waittime = (TextView) view.findViewById(R.id.fare_summery_wait_time_value);
        RelativeLayout layout_request_payment = (RelativeLayout) view.findViewById(R.id.layout_faresummery_requstpayment);
        RelativeLayout layout_receive_cash = (RelativeLayout) view.findViewById(R.id.fare_summery_receive_cash_layout);
        tv_fare_totalamount.setText(Str_ridefare);
        tv_ridedistance.setText(Str_ride_distance);
        tv_timetaken.setText(Str_timetaken);
        tv_waittime.setText(Str_waitingtime);
        dialog.setView(view).show();


        // if (Str_need_payment.equalsIgnoreCase("YES")){

        layout_receive_cash.setVisibility(View.GONE);
        layout_request_payment.setVisibility(View.VISIBLE);
        Tv_reqest.setText(EndTrip.this.getResources().getString(R.string.lbel_notification_ok));

        //  }else{
        // layout_receive_cash.setVisibility(View.GONE);
        //  Tv_reqest.setText(EndTrip.this.getResources().getString(R.string.alert_label_ok));

        // }
//
        /*layout_receive_cash.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(EndTrip.this, OtpPage.class);
                intent.putExtra("rideid", Str_rideid);
                startActivity(intent);
                overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
            }
        });*/

        layout_request_payment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cd = new ConnectionDetector(EndTrip.this);
                isInternetPresent = cd.isConnectingToInternet();
                if (isInternetPresent) {


                    Intent intent = new Intent(EndTrip.this, RatingsPage.class);
                    startActivity(intent);
                    overridePendingTransition(R.anim.fade_in, R.anim.fade_out);


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

        final TextView dialog_title = (TextView) dialog.findViewById(R.id.custom_loading_textview);
        dialog_title.setText(getResources().getString(R.string.action_loading));

        System.out.println("-------------endtrip----------------" + Url);

        HashMap<String, String> jsonParams = new HashMap<String, String>();
        jsonParams.put("driver_id", driver_id);
        jsonParams.put("ride_id", Str_rideid);
        jsonParams.put("drop_lat", String.valueOf(MyCurrent_lat));
        jsonParams.put("drop_lon", String.valueOf(MyCurrent_long));
        jsonParams.put("distance", String.valueOf(dis / 1000));
        jsonParams.put("wait_time", "0");

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
                .println("--------------postdistance-------------------"
                        + String.valueOf(dis / 1000));
        mRequest = new ServiceRequest(EndTrip.this);
        mRequest.makeServiceRequest(Url, Request.Method.POST, jsonParams, new ServiceRequest.ServiceListener() {

            @Override
            public void onCompleteListener(String response) {

                Log.e("end", response);

                System.out.println("endtrip---------" + response);

                //  String Str_status = "",Str_response="",Str_ridefare="",Str_timetaken="",Str_waitingtime="",Str_currency="",Str_ride_distance="";

                try {
                    JSONObject object = new JSONObject(response);
                    Str_status = object.getString("status");
                    Str_response = object.getString("response");

                    JSONObject jsonObject = object.getJSONObject("response");
                    JSONObject jobject = jsonObject.getJSONObject("fare_details");
                    Str_need_payment = jsonObject.getString("need_payment");
                    str_recievecash = jsonObject.getString("receive_cash");
                    Str_currency = jobject.getString("currency");

                    //Currency currencycode = Currency.getInstance(getLocale(Str_currency));
                    sCurrencySymbol = CurrencySymbolConverter.getCurrencySymbol(Str_currency);

                    Str_ridefare = sCurrencySymbol + jobject.getString("ride_fare");
                    Str_timetaken = jobject.getString("ride_duration");
                    Str_waitingtime = jobject.getString("waiting_duration");
                    Str_ride_distance = jobject.getString("ride_distance");
                    Str_need_payment = jobject.getString("need_payment");


                    Log.d("RECEIVE", str_recievecash);


                } catch (Exception e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                dialog.dismiss();


                if (Str_status.equalsIgnoreCase("1")) {

                    //  endTripHandler.removeCallbacks(endTripRunnable);


                    if (Str_need_payment.equalsIgnoreCase("YES")) {
                        System.out.println("sucess------------" + Str_need_payment);
                        if (str_recievecash.matches("Enable")) {
                            showfaresummerydetails();
                        } else {
                            showfaresummerydetails1();
                        }

                    } else {
                        showfaresummerydetails2();
                    }

                } else {
                    Alert(getResources().getString(R.string.alert_sorry_label_title), Str_response);
                }


                dialog.dismiss();

            }

            @Override
            public void onErrorListener() {
                dialog.dismiss();
            }

        });
    }

 /*           private void PostRequest1(String Url) {
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

                           //Currency currencycode = Currency.getInstance(getLocale(Str_currency));
                           sCurrencySymbol = CurrencySymbolConverter.getCurrencySymbol(Str_currency);

                           Str_ridefare = sCurrencySymbol + jobject.getString("ride_fare");
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

                            endTripHandler.removeCallbacks(endTripRunnable);

                            if (Str_need_payment.equalsIgnoreCase("YES")){
                                System.out.println("sucess------------"+Str_need_payment);
                                showfaresummerydetails();
                            }else{
                                showfaresummerydetails();
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
                headers.put("isapplication",ServiceConstant.isapplication);
                headers.put("applanguage",ServiceConstant.applanguage);
                return headers;
            }

            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> jsonParams = new HashMap<String, String>();
                jsonParams.put("driver_id",driver_id);
                jsonParams.put("ride_id",Str_rideid);
                jsonParams.put("drop_lat",String.valueOf(MyCurrent_lat));
                jsonParams.put("drop_lon",String.valueOf(MyCurrent_long));
                jsonParams.put("distance",String.valueOf(dis/1000));
                jsonParams.put("wait_time","0");

                //jsonParams.put("wait_time",String.valueOf(mins).replace(":","."));
               *//* jsonParams.put("wait_time",String.valueOf( String.valueOf("" + mins + ":"
                        + String.format("%02d", secs) + ":"
                        + String.format("%03d", milliseconds))).replace(":","."));*//*


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
                        .println("--------------postdistance-------------------"
                                +String.valueOf(dis/1000));




                return jsonParams;
            }
        };
         postrequest.setRetryPolicy(new DefaultRetryPolicy(30000,
                 DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                 DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
         postrequest.setShouldCache(false);

        AppController.getInstance().addToRequestQueue(postrequest);
    }*/


    //-----------------------Code for arrived post request-----------------
    private void postRequest_Reqqustpayment(String Url) {
        dialog = new Dialog(EndTrip.this);
        dialog.getWindow();
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.custom_loading);
        dialog.setCanceledOnTouchOutside(false);
        dialog.show();

        final TextView dialog_title = (TextView) dialog.findViewById(R.id.custom_loading_textview);

        dialog_title.setText(getResources().getString(R.string.action_loading));
     /*  LinearLayout main = (LinearLayout)findViewById(R.id.main_layout);
        View view = getLayoutInflater().inflate(R.layout.waiting, main,false);
        main.addView(view);
*/

        System.out.println("-------------endtrip----------------" + Url);

        HashMap<String, String> jsonParams = new HashMap<String, String>();
        jsonParams.put("driver_id", driver_id);
        jsonParams.put("ride_id", Str_rideid);

        System.out
                .println("--------------driver_id-------------------"
                        + driver_id);


        System.out
                .println("--------------ride_id-------------------"
                        + Str_rideid);

        mRequest = new ServiceRequest(EndTrip.this);
        mRequest.makeServiceRequest(Url, Request.Method.POST, jsonParams, new ServiceRequest.ServiceListener() {

            @Override
            public void onCompleteListener(String response) {

                Log.e("requestpayment", response);

                System.out.println("response---------" + response);

                String Str_status = "", Str_response = "", Str_currency = "", Str_rideid = "", Str_action = "";

                try {
                    JSONObject object = new JSONObject(response);
                    Str_response = object.getString("response");
                    Str_status = object.getString("status");

                } catch (Exception e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }

                if (Str_status.equalsIgnoreCase("0")) {
                    Alert(getResources().getString(R.string.alert_sorry_label_title), Str_response);

                } else {
                    Alert(getResources().getString(R.string.label_pushnotification_cashreceived), Str_response);
                }
            }

            @Override
            public void onErrorListener() {

                dialog.dismiss();
            }

        });

    }


/*            private void postRequest_Reqqustpayment1(String Url) {
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
                headers.put("isapplication",ServiceConstant.isapplication);
                headers.put("applanguage",ServiceConstant.applanguage);
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
    }*/

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


    public void onRoutingSuccess(PolylineOptions mPolyOptions) {
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
                            status.startResolutionForResult(EndTrip.this, REQUEST_LOCATION);
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
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_LOCATION) {
            System.out.println("----------inside request location------------------");

            switch (resultCode) {
                case Activity.RESULT_OK: {
                    Toast.makeText(EndTrip.this, "Location enabled!", Toast.LENGTH_LONG).show();
                    break;
                }
                case Activity.RESULT_CANCELED: {
                    enableGpsService();
                    break;
                }
                default: {
                    break;
                }
            }

        }
        super.onActivityResult(requestCode, resultCode, data);
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
            sliderSeekBar.setBackgroundResource(R.drawable.red_slide_to_unlock_bg);
            Bt_slider.setVisibility(View.VISIBLE);
            Bt_slider.setText(getResources().getString(R.string.lbel_endtrip));
            shimmer.start(Bt_slider);
        } else if (seekBar.getProgress() > 90) {
            seekBar.setProgress(100);
            Bt_slider.setVisibility(View.VISIBLE);
            Bt_slider.setText(getResources().getString(R.string.lbel_endtrip));
            shimmer.start(Bt_slider);
            sliderSeekBar.setVisibility(View.VISIBLE);
            System.out.println("------------------sliding completed----------------");

            cd = new ConnectionDetector(EndTrip.this);
            isInternetPresent = cd.isConnectingToInternet();

            if (isInternetPresent) {
                PostRequest(ServiceConstant.endtrip_url);
                System.out.println("end------------------" + ServiceConstant.endtrip_url);
            } else {

                Alert(getResources().getString(R.string.alert_sorry_label_title), getResources().getString(R.string.alert_nointernet));
            }
        }
    }




    //-----------------------Update current Location for notification  Post Request-----------------
    private void postRequest_UpdateProviderLocation(String Url) {

        HashMap<String, String> jsonParams = new HashMap<String, String>();
        jsonParams.put("ride_id",Str_rideid);
        jsonParams.put("latitude",Str_Latitude );
        jsonParams.put("longitude",Str_longitude );
        jsonParams.put("driver_id",driver_id);

        System.out.println("-------------Endtripride_id----------------" + Str_longitude);
        System.out.println("-------------Endtriplatitude----------------" + Str_Latitude);
        System.out.println("-------------Endtriplongitude----------------" + Str_longitude);

        System.out.println("-------------latlongupdate----------------" + Url);
        mRequest = new ServiceRequest(EndTrip.this);
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









}
