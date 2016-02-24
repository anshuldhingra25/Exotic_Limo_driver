package com.cabily.cabilydriver;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Messenger;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.app.service.ServiceRequest;
import com.app.xmpp.ChatingService;
import com.app.service.ServiceConstant;
import com.cabily.cabilydriver.Utils.AppController;
import com.cabily.cabilydriver.Utils.ConnectionDetector;
import com.cabily.cabilydriver.Utils.GPSTracker;
import com.cabily.cabilydriver.Utils.RoundedImageView;
import com.cabily.cabilydriver.Utils.SessionManager;
import com.cabily.cabilydriver.Utils.VolleyErrorResponse;
import com.cabily.cabilydriver.adapter.ContinuousRequestAdapter;
import com.cabily.cabilydriver.googlemappath.GMapV2GetRouteDirection;
import com.cabily.cabilydriver.subclass.SubclassActivity;
import com.cabily.cabilydriver.widgets.PkDialog;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.vision.barcode.Barcode;
import com.romainpiel.shimmer.Shimmer;
import com.romainpiel.shimmer.ShimmerButton;
import com.squareup.picasso.MemoryPolicy;
import com.squareup.picasso.Picasso;

import org.jivesoftware.smack.chat.Chat;
import org.json.JSONObject;
import org.w3c.dom.Document;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;


/**
 * Created by user88 on 10/29/2015.
 */
public class BeginTrip extends SubclassActivity implements com.google.android.gms.location.LocationListener, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, View.OnTouchListener {

    private static final String TAG = "swip";
    private String driver_id = "";
    private Boolean isInternetPresent = false;
    private ConnectionDetector cd;
    private Context context;
    private SessionManager session;
    private TextView Tv_name, Tv_name_header, Tv_mobile_no, Tv_Rideid;
    private ImageView callimg;
    private RoundedImageView profile_img;
    private GoogleApiClient mGoogleApiClient;
    private LocationRequest mLocationRequest;
    public static Location myLocation;
    public String locationAddressstring;
    public String locationAddressfinal = "";
    public String des_lat_lng = "";
    public String locationaddressstartingpoint;
    Shimmer shimmer;
    private String Str_name = "", Str_mobilno = "", Str_rideid = "", Str_profilpic = "", str_drop_location = "";

    private GoogleMap googleMap;
    GPSTracker gps;
    private double MyCurrent_lat = 0.0, MyCurrent_long = 0.0;

    public static double current_lat1, current_lon1;

    public double previous_lat1, previous_lon1, current_lat, current_lon, dis = 0.0;


    ArrayList<LatLng> points;

    private Marker currentMarker;
    private RelativeLayout alert_layout, Rl_layout_cancel, Rl_begin_trip_enter_droplocation;
    private int googlerequestcode = 100;

    private EditText Et_begintrip_enter_droplocation;
    private String Slattitude = "";
    private String Slongitude = "";

    private TextView alert_textview;
    //private RelativeLayout Bt_begin_trip;
    private ShimmerButton Bt_shimmer_begintrip;
    float initialX, initialY;

    private ServiceRequest mRequest;
    Dialog dialog;
    StringRequest postrequest;
    GMapV2GetRouteDirection v2GetRouteDirection;
    LatLng fromPosition;
    LatLng fromPosition2;
    LatLng toposition;
    MarkerOptions markerOptions;
    Document document;
    private String current_location = "";
    Double lat, lng;

    public BeginTrip() {
    }

    public static final int ActivityTwoRequestCode = 000;
    static Chat chat;

    private void enableChat() {
        ChatingService.startDriverAction(BeginTrip.this);
        // String sSenderID = "56b2f9d9219a4da531e0e59a";
        String sToID = ContinuousRequestAdapter.userID + "@" + ServiceConstant.XMPP_SERVICE_NAME;
        chat = ChatingService.createChat(sToID);
        ChatingService.setChatMessenger(new Messenger(new MessageHandler()));
        ChatingService.enableChat();
       /* try {
            chat.sendMessage("MI_MESSAGE");
        } catch (Exception e) {

        }
*/
    }

    public static class MessageHandler extends Handler {
        @Override
        public void handleMessage(Message message) {
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.begin_trip);
        initialize();

        try {
            setLocationRequest();
            buildGoogleApiClient();

            enableChat();

            initilizeMap();

            if (str_drop_location != null && !str_drop_location.equalsIgnoreCase("") && !str_drop_location.equalsIgnoreCase("Enter drop location")) {
                {
                    String dropLocation = Et_begintrip_enter_droplocation.getText().toString();
                    if (dropLocation != null && dropLocation.length() > 0) {
                        //String address1 = Et_begintrip_enter_droplocation.getText().toString();
                        GeocodingLocation locationAddress1 = new GeocodingLocation();
                        locationAddress1.getAddressFromLocation(str_drop_location,
                                getApplicationContext(), new GeocoderHandler());
                        getLocationFromAddress(str_drop_location);
                        // locationAddressstring = locationAddressfinal.toString();


                        // PostRequest(ServiceConstant.begintrip_url);


                    } else {
                        Alert(getResources().getString(R.string.alert_sorry_label_title), getResources().getString(R.string.alert_enter_drop_location));
                    }

                }
            }


        } catch (Exception e) {
        }


        // And From your main() method or any other method
        Timer timer = new Timer();
        timer.schedule(new SayHello(), 0, 5000);


        Rl_layout_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(BeginTrip.this, CancelTrip.class);
                intent.putExtra("RideId", Str_rideid);
                startActivity(intent);
                overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
            }
        });

        Bt_shimmer_begintrip.setVisibility(View.GONE);


        Et_begintrip_enter_droplocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                GPSTracker gps = new GPSTracker(BeginTrip.this);

                if (gps.canGetLocation()) {
                    double Dlatitude = gps.getLatitude();
                    double Dlongitude = gps.getLongitude();
                    Intent i = new Intent(BeginTrip.this, LocationSearch.class);
                    Bundle b = new Bundle();
                    b.putDouble("latitude", Dlatitude);
                    b.putDouble("longitude", Dlongitude);
                    i.putExtras(b);
                    startActivityForResult(i, ActivityTwoRequestCode);
                    overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
                } else {
                    Toast.makeText(BeginTrip.this, "Enable Gps", Toast.LENGTH_SHORT)
                            .show();
                }

            }
        });



      /*  Et_begintrip_enter_droplocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(BeginTrip.this, GooglePlaceSearch.class);
                startActivityForResult(intent, googlerequestcode);
                overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
            }
        });*/


        callimg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Str_mobilno != null) {
                    Intent callIntent = new Intent(Intent.ACTION_CALL);
                    callIntent.setData(Uri.parse("tel:" + Str_mobilno));
                    startActivity(callIntent);
                } else {
                    Alert(BeginTrip.this.getResources().getString(R.string.alert_sorry_label_title), BeginTrip.this.getResources().getString(R.string.arrived_alert_content1));
                }
            }
        });
    }


    public void getLocationFromAddress(String strAddress) {

        Geocoder coder = new Geocoder(this);
        List<Address> address;
        // GeoPoint p1 = null;

        try {
            address
                    = coder.getFromLocationName(strAddress, 5);
            if (address == null) {
                //  return null;
            }
            Address location = address.get(0);
            lat = location.getLatitude();
            lng = location.getLongitude();
            des_lat_lng = lat + "," + lng;
            Double ss1 = location.getLongitude();


            // p1 = new GeoPoint((int) (location.getLatitude() * 1E6),
            //       (int) (location.getLongitude() * 1E6));

            //return p1;
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private class GeocoderHandler extends Handler {

        @Override
        public void handleMessage(Message message) {

            switch (message.what) {
                case 1:
                    Bundle bundle = message.getData();
                    locationAddressfinal = bundle.getString("address");
                    break;
                default:
                    locationAddressfinal = null;
            }
            Log.d("TEXTview", locationAddressfinal);
        }
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
        if (Et_begintrip_enter_droplocation != null && !Et_begintrip_enter_droplocation.getText().toString().equalsIgnoreCase("") && !Et_begintrip_enter_droplocation.getText().toString().equalsIgnoreCase("Enter drop location")) {
            Bt_shimmer_begintrip.setVisibility(View.VISIBLE);
            Bt_shimmer_begintrip.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    int action = event.getActionMasked();
                    switch (action) {
                        case MotionEvent.ACTION_DOWN:
                            initialX = event.getX();
                            initialY = event.getY();

                            Log.d(TAG, "Action was DOWN");
                            break;

                        case MotionEvent.ACTION_MOVE:
                            Log.d(TAG, "Action was MOVE");
                            break;

                        case MotionEvent.ACTION_UP:
                            float finalX = event.getX();
                            float finalY = event.getY();

                            Log.d(TAG, "Action was UP");

                            if (initialX < finalX) {
                                cd = new ConnectionDetector(BeginTrip.this);
                                isInternetPresent = cd.isConnectingToInternet();
                                if (isInternetPresent) {
                                    String dropLocation = Et_begintrip_enter_droplocation.getText().toString();
                                    if (dropLocation != null && dropLocation.length() > 0) {
                                        String address = Et_begintrip_enter_droplocation.getText().toString();
                                        GeocodingLocation locationAddress = new GeocodingLocation();
                                        locationAddress.getAddressFromLocation(address,
                                                getApplicationContext(), new GeocoderHandler());
                                        getLocationFromAddress(address);
                                        locationAddressstring = locationAddressfinal.toString();

                                        Log.d("LOCATIONME@@@@", lat.toString());
                                        Log.d("LOCATIONME@!!!!@", lng.toString());
                                        PostRequest(ServiceConstant.begintrip_url);


                                    } else {
                                        Alert(getResources().getString(R.string.alert_sorry_label_title), getResources().getString(R.string.alert_enter_drop_location));
                                    }
                                    System.out.println("begin------------------" + ServiceConstant.begintrip_url);
                                } else {
                                    Alert(getResources().getString(R.string.alert_sorry_label_title), getResources().getString(R.string.alert_nointernet));
                                }
                                Log.d(TAG, "Left to Right swipe performed");
                            }
                            if (initialX > finalX) {
                                Log.d(TAG, "Right to Left swipe performed");
                            }
                            if (initialY < finalY) {
                                Log.d(TAG, "Up to Down swipe performed");
                            }
                            if (initialY > finalY) {
                                Log.d(TAG, "Down to Up swipe performed");
                            }
                            break;
                        case MotionEvent.ACTION_CANCEL:
                            Log.d(TAG, "Action was CANCEL");
                            break;

                        case MotionEvent.ACTION_OUTSIDE:
                            Log.d(TAG, "Movement occurred outside bounds of current screen element");
                            break;
                    }
                    return true;
                }
            });

        } else {
            Bt_shimmer_begintrip.setVisibility(View.GONE);
        }
    }

    @Override
    public void onDestroy() {

        ChatingService.disableChat();

        super.onDestroy();
    }

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

    private void initialize() {
        gps = new GPSTracker(BeginTrip.this);
        session = new SessionManager(BeginTrip.this);
        points = new ArrayList<LatLng>();
        //Starting Xmpp service
        ChatingService.startDriverAction(BeginTrip.this);
        // get user data from session
        HashMap<String, String> user = session.getUserDetails();
        driver_id = user.get(SessionManager.KEY_DRIVERID);

        Intent i = getIntent();
        Str_name = i.getStringExtra("user_name");
        Str_mobilno = i.getStringExtra("user_phoneno");
        Str_rideid = i.getStringExtra("rideid");
        Str_profilpic = i.getStringExtra("user_image");
        str_drop_location = i.getStringExtra("drop_location");

        Tv_name_header = (TextView) findViewById(R.id.begin_trip_nameTxt);
        Tv_name = (TextView) findViewById(R.id.begintrip_name);
        Tv_mobile_no = (TextView) findViewById(R.id.begintrip_mobilno);
        Tv_Rideid = (TextView) findViewById(R.id.begintrip_rideid);
        profile_img = (RoundedImageView) findViewById(R.id.profile_image);
        callimg = (ImageView) findViewById(R.id.begintrip_call);
        alert_textview = (TextView) findViewById(R.id.begintrip_alert_textView);
        alert_layout = (RelativeLayout) findViewById(R.id.begintrip_alert_layout);
        Rl_layout_cancel = (RelativeLayout) findViewById(R.id.layout_begin_trip_cancel);
        // Bt_begin_trip = (RelativeLayout) findViewById(R.id.layout_btn_begintrip);
        Bt_shimmer_begintrip = (ShimmerButton) findViewById(R.id.btn_begintrip);
        Rl_begin_trip_enter_droplocation = (RelativeLayout) findViewById(R.id.begin_trip_droplocation_layout);
        Et_begintrip_enter_droplocation = (EditText) findViewById(R.id.begin_trip_droplocation_Et);

        Et_begintrip_enter_droplocation.setText(str_drop_location);

        Et_begintrip_enter_droplocation.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                String dropLocation = Et_begintrip_enter_droplocation.getText().toString();
                if (dropLocation != null && dropLocation.length() > 0) {
                    String address = Et_begintrip_enter_droplocation.getText().toString();
                    GeocodingLocation locationAddress = new GeocodingLocation();
                    locationAddress.getAddressFromLocation(address,
                            getApplicationContext(), new GeocoderHandler());
                    getLocationFromAddress(address);
                    locationAddressstring = locationAddressfinal.toString();
                    //  PostRequest(ServiceConstant.begintrip_url);
                } else {
                    Alert(getResources().getString(R.string.alert_sorry_label_title), getResources().getString(R.string.alert_enter_drop_location));
                }

            }
        });
        Tv_name_header.setText(Str_name);
        Tv_mobile_no.setText(Str_mobilno);
        Tv_name.setText(Str_name);

        shimmer = new Shimmer();
        shimmer.start(Bt_shimmer_begintrip);

        Picasso.with(BeginTrip.this).load(String.valueOf(Str_profilpic)).placeholder(R.drawable.nouserimg).memoryPolicy(MemoryPolicy.NO_CACHE).into(profile_img);


        String address = Et_begintrip_enter_droplocation.getText().toString();


        GeocodingLocation locationAddress = new GeocodingLocation();
        locationAddress.getAddressFromLocation(address,
                getApplicationContext(), new GeocoderHandler());

        // if(Et_begintrip_enter_droplocation.getText().toString().equalsIgnoreCase())



       /* if(str_drop_location!=null && !str_drop_location.equalsIgnoreCase(""))
        {
            {
                String dropLocation = Et_begintrip_enter_droplocation.getText().toString();
                if (dropLocation != null && dropLocation.length() > 0) {
                    String address1 = Et_begintrip_enter_droplocation.getText().toString();
                    GeocodingLocation locationAddress1 = new GeocodingLocation();
                    locationAddress1.getAddressFromLocation(address1,
                            getApplicationContext(), new GeocoderHandler());
                    getLocationFromAddress(address1);
                    locationAddressstring = locationAddressfinal.toString();


                    PostRequest(ServiceConstant.begintrip_url);


                } else {
                    Alert(getResources().getString(R.string.alert_sorry_label_title), getResources().getString(R.string.alert_enter_drop_location));
                }

            }
        }*/
    }


    private void initilizeMap() {

        if (googleMap == null) {
            googleMap = ((MapFragment) BeginTrip.this.getFragmentManager().findFragmentById(R.id.arrived_trip_view_map)).getMap();
            // check if map is created successfully or not
            if (googleMap == null) {
                Toast.makeText(BeginTrip.this, getResources().getString(R.string.action_alert_unabletocreatemap), Toast.LENGTH_SHORT).show();
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
            CameraPosition cameraPosition = new CameraPosition.Builder().target(new LatLng(Dlatitude, Dlongitude)).zoom(17).build();
            googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
            // create marker double Dlatitude = gps.getLatitude();
            MarkerOptions marker = new MarkerOptions().position(new LatLng(Dlatitude, Dlongitude));
            marker.icon(BitmapDescriptorFactory.fromResource(R.drawable.flageimage2));
            currentMarker = googleMap.addMarker(marker);
            System.out.println("currntlat----------" + MyCurrent_lat);
            System.out.println("currntlon----------" + MyCurrent_long);

        } else {
            alert_layout.setVisibility(View.VISIBLE);
            alert_textview.setText(getResources().getString(R.string.alert_gpsEnable));
        }
        setLocationRequest();
        buildGoogleApiClient();
    }


    private final class GestureListener extends GestureDetector.SimpleOnGestureListener {

        private static final int SWIPE_THRESHOLD = 100;
        private static final int SWIPE_VELOCITY_THRESHOLD = 100;

        @Override
        public boolean onDown(MotionEvent e) {
            return true;
        }

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            boolean result = false;
            try {
                float diffY = e2.getY() - e1.getY();
                float diffX = e2.getX() - e1.getX();
                if (Math.abs(diffX) > Math.abs(diffY)) {
                    if (Math.abs(diffX) > SWIPE_THRESHOLD && Math.abs(velocityX) > SWIPE_VELOCITY_THRESHOLD) {
                        if (diffX > 0) {
                            onSwipeRight();
                        } else {
                            onSwipeLeft();
                        }
                    }
                    result = true;
                } else if (Math.abs(diffY) > SWIPE_THRESHOLD && Math.abs(velocityY) > SWIPE_VELOCITY_THRESHOLD) {
                    if (diffY > 0) {
                        onSwipeBottom();
                    } else {
                        onSwipeTop();
                    }
                }
                result = true;

            } catch (Exception exception) {
                exception.printStackTrace();
            }
            return result;
        }
    }

    public void onSwipeRight() {
    }

    public void onSwipeLeft() {
    }

    public void onSwipeTop() {
    }

    public void onSwipeBottom() {
    }


    private class GetRouteTask extends AsyncTask<String, Void, String> {

        String response = "";

        @Override
        protected void onPreExecute() {
        }

        @Override
        protected String doInBackground(String... urls) {
            //Get All Route values
            document = v2GetRouteDirection.getDocument(fromPosition, toposition, GMapV2GetRouteDirection.MODE_DRIVING);
            response = "Success";
            return response;
        }

        @Override
        protected void onPostExecute(String result) {
            googleMap.clear();
            ArrayList<LatLng> directionPoint = v2GetRouteDirection.getDirection(document);
            PolylineOptions rectLine = new PolylineOptions().width(5).color(
                    Color.RED);
            for (int i = 0; i < directionPoint.size(); i++) {
                rectLine.add(directionPoint.get(i));
            }

            // Adding route on the map
            googleMap.addPolyline(rectLine);
            markerOptions.position(toposition);
            markerOptions.position(fromPosition);
            markerOptions.draggable(true);

            //googleMap.addMarker(markerOptions);
            googleMap.addMarker(new MarkerOptions()
                    .position(toposition)
                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.flagimage)));
            googleMap.addMarker(new MarkerOptions()
                    .position(fromPosition)
                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.flageimage2)));
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // check if the request code is same as what is passed  here it is 2
        if (resultCode == RESULT_OK) {
            if (requestCode == googlerequestcode ) {
                String Saddress = data.getStringExtra("address");
                Slattitude = data.getStringExtra("Lattitude");
                Slongitude = data.getStringExtra("Longitude");
                System.out.println("msggoogleplace-------------" + Saddress);
                Et_begintrip_enter_droplocation.setText(Saddress);
            }
            if(requestCode  == ActivityTwoRequestCode){
                String Saddress = data.getStringExtra("Selected_Location");
                Slattitude = data.getStringExtra("Selected_Latitude");
                Slongitude = data.getStringExtra("Selected_Longitude");
                System.out.println("msggoogleplace-------------" + Saddress);
                Et_begintrip_enter_droplocation.setText(Saddress);
            }

        }
    }


    //--------------Alert Method-----------
    private void Alert(String title, String message) {
        final PkDialog mDialog = new PkDialog(BeginTrip.this);
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
            //  marker.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));
            currentMarker = googleMap.addMarker(marker);
            //postRequest(ServiceConstant.UPDATE_CURRENT_LOCATION);

            System.out.println("online------------------" + ServiceConstant.UPDATE_CURRENT_LOCATION);

        }

    }

    @Override
    public void onConnectionSuspended(int i) {
    }
    MarkerOptions mm = new MarkerOptions();
    Marker drivermarker;
    @Override
    public void onLocationChanged(Location location) {

        this.myLocation = location;
        System.out.println("locatbegintrip-----------" + location);
        if (myLocation != null && currentMarker != null) {


            try {
                if (chat != null) {
                    LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
                    current_lat = location.getLatitude();
                    current_lon = location.getLongitude();

                    String sendlat = Double.valueOf(current_lat).toString();
                    String sendlng = Double.valueOf(current_lon).toString();


                    JSONObject job = new JSONObject();

                    job.accumulate("action", "driver_loc");
                    job.accumulate("latitude", sendlat);
                    job.accumulate("longitude", sendlng);
                    job.accumulate("ride_id", "");


                    // String sSenderID = "56b2f9d9219a4da531e0e59a";
                    String sToID = ContinuousRequestAdapter.userID + "@" + ServiceConstant.XMPP_SERVICE_NAME;
                    chat = ChatingService.createChat(sToID);
                    chat.sendMessage(job.toString());

                    if(drivermarker!=null)
                    {
                        drivermarker.remove();
                    }



                    drivermarker =     googleMap.addMarker(new MarkerOptions().position(latLng).icon(BitmapDescriptorFactory.fromResource(R.drawable.orange)));

                }

            } catch (Exception e) {
            }
            System.out.println("mylocatiobegintrip-----------" + myLocation);
            LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());

            current_lat = location.getLatitude();
            current_lon = location.getLongitude();

            current_lat1 = current_lat;
            current_lon1 = current_lon;
            currentMarker.setPosition(latLng);
            // Toast.makeText(BeginTrip.this, "distance in metres:" + "MY current" , Toast.LENGTH_SHORT).show();
                /*fromPosition = new LatLng(previous_lat, previous_lon);
                float[] f = new float[1];
                if (current_lat != previous_lat || current_lon != previous_lon) {
                    // dis += getDistance(previous_lat, previous_lon, current_lat, current_lon);
                    Location.distanceBetween(previous_lat, previous_lon, current_lat, current_lon,f);

                    dis += Double.parseDouble( String.valueOf(f[0]));
                   // Toast.makeText(BeginTrip.this, "distance in metres1:" + String.valueOf(dis) , Toast.LENGTH_SHORT).show();
                    previous_lat = current_lat;
                    previous_lon = current_lon;
                    System.out.println("distanceinside----------------------" + dis);
                } else {
                    previous_lat = current_lat;
                    previous_lon = current_lon;
                    dis = dis;
                }
                previous_lat = current_lat;
                previous_lon = current_lon;*/
            //  Toast.makeText(BeginTrip.this, "distance in metres2:" + String.valueOf(dis), Toast.LENGTH_SHORT).show();
            if (googleMap != null) {
                googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(myLocation.getLatitude(), myLocation.getLongitude()),
                        16));
            }
            toposition = new LatLng(myLocation.getLatitude(), myLocation.getLongitude());

            if (fromPosition != null && toposition != null) {
                //GetRouteTask getRoute = new GetRouteTask();
                // getRoute.execute();
            }
        }

        myLocation = location;
        if (myLocation != null) {
            LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
            currentMarker.setPosition(latLng);
            System.out.println("latlaong---------------------------" + latLng);
            if (googleMap != null) {
                googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(myLocation.getLatitude(), myLocation.getLongitude()),
                        16));
            }
        }


     /*   myLocation = location;
        if (myLocation != null) {
            LatLng latLng = new LatLng(location.getLatitude(),location.getLongitude());
            currentMarker.setPosition(latLng);

            System.out.println("latlaong---------------------------"+latLng);

        }*/
    }


    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        return false;
    }

    class SayHello extends TimerTask {
        public void run() {
            System.out.println("hello");

        }
    }

    //-----------------------Code for begin trip post request-----------------
    private void PostRequest(String Url) {
        dialog = new Dialog(BeginTrip.this);
        dialog.getWindow();
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.custom_loading);
        dialog.setCanceledOnTouchOutside(false);
        dialog.show();


        String droplocation[] = locationAddressfinal.split(",");

       /* Log.d("Droplocation+++++",droplocation.toString());
         double droplatitude = Double.parseDouble(droplocation[0]);
       double droplongitude = Double.parseDouble(droplocation[1]);*/
        final TextView dialog_title = (TextView) dialog.findViewById(R.id.custom_loading_textview);
        dialog_title.setText(getResources().getString(R.string.action_loading));

        System.out.println("-------------begin----------------" + Url);

        HashMap<String, String> jsonParams = new HashMap<String, String>();
        jsonParams.put("driver_id", driver_id);
        jsonParams.put("ride_id", Str_rideid);
        jsonParams.put("pickup_lat", String.valueOf(MyCurrent_lat));
        jsonParams.put("pickup_lon", String.valueOf(MyCurrent_long));
        jsonParams.put("drop_lat", String.valueOf(lat));
        jsonParams.put("drop_lon", String.valueOf(lng));
        System.out
                .println("--------------driver_id-------------------"
                        + driver_id);
        System.out
                .println("--------------pickup_lat-------------------"
                        + String.valueOf(MyCurrent_lat));
        System.out
                .println("--------------pickup_lon-------------------"
                        + String.valueOf(MyCurrent_long));

        mRequest = new ServiceRequest(BeginTrip.this);
        mRequest.makeServiceRequest(Url, Request.Method.POST, jsonParams, new ServiceRequest.ServiceListener() {

            @Override
            public void onCompleteListener(String response) {
                Log.e("begin", response);

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
                    Intent intent = new Intent(BeginTrip.this, EndTrip.class);
                    intent.putExtra("name", Str_name);
                    intent.putExtra("rideid", Str_rideid);
                    intent.putExtra("mobilno", Str_mobilno);
                    if (locationAddressfinal != null && !locationAddressfinal.equalsIgnoreCase("")) {
                        locationaddressstartingpoint = String.valueOf(current_lat + "," + current_lon);
                        Log.d("myloc", locationaddressstartingpoint);
                        intent.putExtra("pickuplatlng", des_lat_lng);
                        intent.putExtra("startpoint", locationaddressstartingpoint);
                    }
                    startActivity(intent);

                    overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
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


/*        private void PostRequest1(String Url) {
        dialog = new Dialog(BeginTrip.this);
        dialog.getWindow();
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.custom_loading);
        dialog.setCanceledOnTouchOutside(false);
        dialog.show();

        System.out.println("loadin-----------");
        TextView dialog_title = (TextView) dialog.findViewById(R.id.custom_loading_textview);
        dialog_title.setText(getResources().getString(R.string.action_loading));


        postrequest = new StringRequest(Request.Method.POST, Url,
                new Response.Listener<String>() {

                    @Override
                    public void onResponse(String response) {
                        Log.e("begin", response);

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
                            Intent intent = new Intent(BeginTrip.this, EndTrip.class);
                            intent.putExtra("name", Str_name);
                            intent.putExtra("rideid", Str_rideid);
                            intent.putExtra("mobilno", Str_mobilno);
                            startActivity(intent);
                            overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
                        } else {
                            Alert(getResources().getString(R.string.alert_sorry_label_title), Str_response);


                        }
                    }
                }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                VolleyErrorResponse.VolleyError(BeginTrip.this, error);
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
                jsonParams.put("driver_id", driver_id);
                jsonParams.put("ride_id", Str_rideid);
                jsonParams.put("pickup_lat", String.valueOf(MyCurrent_lat));
                jsonParams.put("pickup_lon", String.valueOf(MyCurrent_long));
                System.out
                        .println("--------------driver_id-------------------"
                                + driver_id);
                System.out
                        .println("--------------pickup_lat-------------------"
                                + String.valueOf(MyCurrent_lat));
                System.out
                        .println("--------------pickup_lon-------------------"
                                + String.valueOf(MyCurrent_long));

                return jsonParams;
            }
        };
        postrequest.setRetryPolicy(new DefaultRetryPolicy(30000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        postrequest.setShouldCache(false);

        AppController.getInstance().addToRequestQueue(postrequest);
    }*/


}
