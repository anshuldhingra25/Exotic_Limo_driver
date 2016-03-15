package com.cabily.cabilydriver;import android.app.Dialog;import android.content.Context;import android.content.Intent;import android.location.Location;import android.net.Uri;import android.os.AsyncTask;import android.os.Bundle;import android.os.Handler;import android.os.Message;import android.os.Messenger;import android.util.Log;import android.view.MotionEvent;import android.view.View;import android.view.Window;import android.widget.ImageView;import android.widget.RelativeLayout;import android.widget.SeekBar;import android.widget.TextView;import android.widget.Toast;import com.android.volley.Request;import com.android.volley.toolbox.StringRequest;import com.app.service.ServiceConstant;import com.app.service.ServiceRequest;import com.app.xmpp.ChatingService;import com.cabily.cabilydriver.Utils.ConnectionDetector;import com.cabily.cabilydriver.Utils.GPSTracker;import com.cabily.cabilydriver.Utils.RoundedImageView;import com.cabily.cabilydriver.Utils.SessionManager;import com.cabily.cabilydriver.adapter.ContinuousRequestAdapter;import com.cabily.cabilydriver.googlemappath.GMapV2GetRouteDirection;import com.cabily.cabilydriver.subclass.SubclassActivity;import com.cabily.cabilydriver.widgets.PkDialog;import com.google.android.gms.common.ConnectionResult;import com.google.android.gms.common.api.GoogleApiClient;import com.google.android.gms.location.LocationRequest;import com.google.android.gms.location.LocationServices;import com.google.android.gms.maps.CameraUpdateFactory;import com.google.android.gms.maps.GoogleMap;import com.google.android.gms.maps.MapFragment;import com.google.android.gms.maps.model.BitmapDescriptorFactory;import com.google.android.gms.maps.model.LatLng;import com.google.android.gms.maps.model.LatLngBounds;import com.google.android.gms.maps.model.Marker;import com.google.android.gms.maps.model.MarkerOptions;import com.google.android.gms.maps.model.PolylineOptions;import com.romainpiel.shimmer.Shimmer;import com.romainpiel.shimmer.ShimmerButton;import com.squareup.picasso.MemoryPolicy;import com.squareup.picasso.Picasso;import org.jivesoftware.smack.SmackException;import org.jivesoftware.smack.chat.Chat;import org.json.JSONException;import org.json.JSONObject;import org.w3c.dom.Document;import java.util.ArrayList;import java.util.HashMap;/** */public class BeginTrip extends SubclassActivity implements com.google.android.gms.location.LocationListener, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, View.OnTouchListener, SeekBar.OnSeekBarChangeListener {    private static final String TAG = "swip";    private String driver_id = "";    private Boolean isInternetPresent = false;    private ConnectionDetector cd;    private Context context;    private SessionManager session;    private TextView Tv_name, Tv_name_header, Tv_mobile_no, Tv_Rideid;    private ImageView callimg;    private RoundedImageView profile_img;    private GoogleApiClient mGoogleApiClient;    private LocationRequest mLocationRequest;    public static Location myLocation;    public String locationAddressstring;    public String locationAddressfinal = "";    public String des_lat_lng = "";    public String locationaddressstartingpoint;    private String Str_name = "", Str_mobilno = "", Str_rideid = "", Str_profilpic = "", str_drop_location = "";    private String str_drop_Latitude = "", str_drop_Longitude = "";    private GoogleMap googleMap;    private GPSTracker gps;    private double MyCurrent_lat = 0.0, MyCurrent_long = 0.0;    public static double current_lat1, current_lon1;    public double previous_lat1, previous_lon1, current_lat, current_lon, dis = 0.0;    public ArrayList<LatLng> points;    private Marker currentMarker;    private RelativeLayout alert_layout, Rl_layout_cancel, Rl_begin_trip_enter_droplocation;    private String Slattitude = "";    private String Slongitude = "";    private TextView alert_textview;    //private RelativeLayout Bt_begin_trip;    float initialX, initialY;    private ServiceRequest mRequest;    Dialog dialog;    StringRequest postrequest;    GMapV2GetRouteDirection v2GetRouteDirection;    LatLng fromPosition;    LatLng fromPosition2;    LatLng toposition;    MarkerOptions markerOptions;    private Document document;    private String current_location = "";    MarkerOptions mm = new MarkerOptions();    Marker drivermarker;    JSONObject job;    private TextView Drop_address_Tv;    //Slider Design Declaration    SeekBar sliderSeekBar;    ShimmerButton Bt_slider;    Shimmer shimmer;    private RelativeLayout Rl_beginTrip;    public static final int DropLocationRequestCode = 5000;    Chat chat;    public static class MessageHandler extends Handler {        @Override        public void handleMessage(Message message) {        }    }    @Override    protected void onCreate(Bundle savedInstanceState) {        super.onCreate(savedInstanceState);        setContentView(R.layout.begin_trip);        initialize();        try {           /* setLocationRequest();            buildGoogleApiClient();            enableChat();*/            initilizeMap();        } catch (Exception e) {        }        Rl_layout_cancel.setOnClickListener(new View.OnClickListener() {            @Override            public void onClick(View v) {                Intent intent = new Intent(BeginTrip.this, CancelTrip.class);                intent.putExtra("RideId", Str_rideid);                startActivity(intent);                overridePendingTransition(R.anim.fade_in, R.anim.fade_out);            }        });        Rl_begin_trip_enter_droplocation.setOnClickListener(new View.OnClickListener() {            @Override            public void onClick(View v) {                GPSTracker gps = new GPSTracker(BeginTrip.this);                if (gps.canGetLocation()) {                    Intent intent = new Intent(BeginTrip.this, DropLocationSelect.class);                    startActivityForResult(intent, DropLocationRequestCode);                    overridePendingTransition(R.anim.fade_in, R.anim.fade_out);                } else {                    Toast.makeText(BeginTrip.this, "Enable Gps", Toast.LENGTH_SHORT)                            .show();                }            }        });        callimg.setOnClickListener(new View.OnClickListener() {            @Override            public void onClick(View v) {                if (Str_mobilno != null) {                    Intent callIntent = new Intent(Intent.ACTION_CALL);                    callIntent.setData(Uri.parse("tel:" + Str_mobilno));                    startActivity(callIntent);                } else {                    Alert(BeginTrip.this.getResources().getString(R.string.alert_sorry_label_title), BeginTrip.this.getResources().getString(R.string.arrived_alert_content1));                }            }        });    }    @Override    public void onStart() {        super.onStart();        if (mGoogleApiClient != null)            mGoogleApiClient.connect();    }    @Override    protected void onResume() {        super.onResume();        startLocationUpdates();    }    @Override    public void onDestroy() {        super.onDestroy();        System.gc();        if(chat != null){            chat.close();        }    }    private void setLocationRequest() {        mLocationRequest = new LocationRequest();        mLocationRequest.setInterval(20000);        mLocationRequest.setFastestInterval(20000);        mLocationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);    }    protected void startLocationUpdates() {        if (mGoogleApiClient != null && mGoogleApiClient.isConnected()) {            LocationServices.FusedLocationApi.requestLocationUpdates(                    mGoogleApiClient, mLocationRequest, this);        }    }    protected synchronized void buildGoogleApiClient() {        mGoogleApiClient = new GoogleApiClient.Builder(this)                .addConnectionCallbacks(this)                .addOnConnectionFailedListener(this)                .addApi(LocationServices.API)                .build();    }    private void initialize() {        gps = new GPSTracker(BeginTrip.this);        session = new SessionManager(BeginTrip.this);        points = new ArrayList<LatLng>();        //Starting Xmpp service        ChatingService.startDriverAction(BeginTrip.this);        // get user data from session        HashMap<String, String> user = session.getUserDetails();        driver_id = user.get(SessionManager.KEY_DRIVERID);        Intent i = getIntent();        Str_name = i.getStringExtra("user_name");        Str_mobilno = i.getStringExtra("user_phoneno");        Str_rideid = i.getStringExtra("rideid");        Str_profilpic = i.getStringExtra("user_image");        str_drop_location = i.getStringExtra("drop_location");        str_drop_Latitude = i.getStringExtra("DropLatitude");        str_drop_Longitude = i.getStringExtra("DropLongitude");        Tv_name_header = (TextView) findViewById(R.id.begin_trip_nameTxt);        Tv_name = (TextView) findViewById(R.id.begintrip_name);        Tv_mobile_no = (TextView) findViewById(R.id.begintrip_mobilno);        Tv_Rideid = (TextView) findViewById(R.id.begintrip_rideid);        profile_img = (RoundedImageView) findViewById(R.id.profile_image);        callimg = (ImageView) findViewById(R.id.begintrip_call);        alert_textview = (TextView) findViewById(R.id.begintrip_alert_textView);        alert_layout = (RelativeLayout) findViewById(R.id.begintrip_alert_layout);        Rl_layout_cancel = (RelativeLayout) findViewById(R.id.layout_begin_trip_cancel);        Rl_begin_trip_enter_droplocation = (RelativeLayout) findViewById(R.id.begin_trip_droplocation_layout);        Drop_address_Tv = (TextView) findViewById(R.id.location_drop_address);        shimmer = new Shimmer();        sliderSeekBar = (SeekBar) findViewById(R.id.begin_Trip_seek);        Bt_slider = (ShimmerButton) findViewById(R.id.begin_Trip_slider_button);        Rl_beginTrip = (RelativeLayout) findViewById(R.id.layout_begintrip);        shimmer.start(Bt_slider);        sliderSeekBar.setOnSeekBarChangeListener(this);        Tv_name_header.setText(Str_name);        Tv_mobile_no.setText(Str_mobilno);        Tv_name.setText(Str_name);        Picasso.with(BeginTrip.this).load(String.valueOf(Str_profilpic)).placeholder(R.drawable.nouserimg).memoryPolicy(MemoryPolicy.NO_CACHE).into(profile_img);    }    private void initilizeMap() {        if (googleMap == null) {            googleMap = ((MapFragment) BeginTrip.this.getFragmentManager().findFragmentById(R.id.arrived_trip_view_map)).getMap();            // check if map is created successfully or not            if (googleMap == null) {                Toast.makeText(BeginTrip.this, getResources().getString(R.string.action_alert_unabletocreatemap), Toast.LENGTH_SHORT).show();            }        }        // Changing map type        googleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);        // Showing / hiding your current location        googleMap.setMyLocationEnabled(false);        // Enable / Disable zooming controls        googleMap.getUiSettings().setZoomControlsEnabled(false);        // Enable / Disable my location button        googleMap.getUiSettings().setMyLocationButtonEnabled(false);        // Enable / Disable Compass icon        googleMap.getUiSettings().setCompassEnabled(false);        // Enable / Disable Rotate gesture        googleMap.getUiSettings().setRotateGesturesEnabled(true);        // Enable / Disable zooming functionality        googleMap.getUiSettings().setZoomGesturesEnabled(true);        googleMap.setMyLocationEnabled(false);        if (gps.canGetLocation()) {        } else {            alert_layout.setVisibility(View.VISIBLE);            alert_textview.setText(getResources().getString(R.string.alert_gpsEnable));        }        setLocationRequest();        buildGoogleApiClient();        markerOptions = new MarkerOptions();        if (str_drop_Latitude != null && str_drop_Latitude.length() > 0) {            Rl_beginTrip.setVisibility(View.VISIBLE);            Drop_address_Tv.setText(str_drop_location);            GPSTracker gps = new GPSTracker(BeginTrip.this);            if (gps.canGetLocation()) {                double Dlatitude = gps.getLatitude();                double Dlongitude = gps.getLongitude();                MyCurrent_lat = Dlatitude;                MyCurrent_long = Dlongitude;                LatLng fromLat = new LatLng(MyCurrent_lat, MyCurrent_long);                LatLng toLat = new LatLng(Double.parseDouble(str_drop_Latitude), Double.parseDouble(str_drop_Longitude));                GetRouteTask getRouteTask = new GetRouteTask(fromLat, toLat);                getRouteTask.execute();            }        } else {            Rl_beginTrip.setVisibility(View.GONE);            Drop_address_Tv.setText(getResources().getString(R.string.action_enter_drop_location));        }    }    //---------------AsyncTask to Draw PolyLine Between Two Point--------------    public class GetRouteTask extends AsyncTask<String, Void, String> {        String response = "";        GMapV2GetRouteDirection v2GetRouteDirection = new GMapV2GetRouteDirection();        Document document;        LatLng from_LatLng, to_LatLng;        GetRouteTask(LatLng from, LatLng to) {            from_LatLng = to;            to_LatLng = from;            System.out.println("------------begin trip from-----------------"+from_LatLng);            System.out.println("------------begin trip to-----------------"+to_LatLng);        }        @Override        protected void onPreExecute() {        }        @Override        protected String doInBackground(String... urls) {            //Get All Route values            document = v2GetRouteDirection.getDocument(to_LatLng, from_LatLng, GMapV2GetRouteDirection.MODE_DRIVING);            response = "Success";            return response;        }        @Override        protected void onPostExecute(String result) {            if (result.equalsIgnoreCase("Success")) {                googleMap.clear();                try {                    ArrayList<LatLng> directionPoint = v2GetRouteDirection.getDirection(document);                    PolylineOptions rectLine = new PolylineOptions().width(18).color(getResources().getColor(R.color.app_color));                    for (int i = 0; i < directionPoint.size(); i++) {                        rectLine.add(directionPoint.get(i));                    }                    // Adding route on the map                    googleMap.addPolyline(rectLine);                    markerOptions.position(to_LatLng);                    markerOptions.position(from_LatLng);                    markerOptions.draggable(true);                  //  googleMap.addMarker(markerOptions);                    googleMap.addMarker(new MarkerOptions()                            .position(from_LatLng)                            .icon(BitmapDescriptorFactory.fromResource(R.drawable.dark_green_flag)));                    googleMap.addMarker(new MarkerOptions()                            .position(to_LatLng)                            .icon(BitmapDescriptorFactory.fromResource(R.drawable.light_green_flag)));                    googleMap.addMarker(new MarkerOptions()                            .position(to_LatLng)                            .icon(BitmapDescriptorFactory.fromResource(R.drawable.carmove)));                    //Show path in                    LatLngBounds.Builder builder = new LatLngBounds.Builder();                    builder.include(from_LatLng);                    builder.include(to_LatLng);                    LatLngBounds bounds = builder.build();                    googleMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, 162));                }catch (Exception e){                    e.printStackTrace();                }            }        }    }    //--------------Alert Method-----------    private void Alert(String title, String message) {        final PkDialog mDialog = new PkDialog(BeginTrip.this);        mDialog.setDialogTitle(title);        mDialog.setDialogMessage(message);        mDialog.setPositiveButton(getResources().getString(R.string.alert_label_ok), new View.OnClickListener() {            @Override            public void onClick(View v) {                mDialog.dismiss();            }        });        mDialog.show();    }    @Override    public void onConnected(Bundle bundle) {        if (gps != null && gps.canGetLocation() && gps.isgpsenabled()) {        }        myLocation = LocationServices.FusedLocationApi.getLastLocation(                mGoogleApiClient);        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);        if (myLocation != null) {            googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(myLocation.getLatitude(), myLocation.getLongitude()),                    16));            MarkerOptions marker = new MarkerOptions();            marker.position(new LatLng(myLocation.getLatitude(), myLocation.getLongitude()));            //  marker.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));            //currentMarker = googleMap.addMarker(marker);            //postRequest(ServiceConstant.UPDATE_CURRENT_LOCATION);            System.out.println("online------------------" + ServiceConstant.UPDATE_CURRENT_LOCATION);        }    }    @Override    public void onConnectionSuspended(int i) {    }    double session_lat =0.0;    double session_long= 0.0;    @Override    public void onLocationChanged(Location location) {        this.myLocation = location;        System.out.println("locatbegintrip-----------" + location);        if (myLocation != null && session_lat != location.getLatitude() && session_long != location.getLongitude()  ) {            try {                LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());                if (drivermarker != null) {                    drivermarker.remove();                }                drivermarker = googleMap.addMarker(new MarkerOptions().position(latLng).icon(BitmapDescriptorFactory.fromResource(R.drawable.carmove)));                //sendLocationToTheUser(myLocation);            } catch (Exception e) {            }            System.out.println("mylocatiobegintrip-----------" + myLocation);            LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());            current_lat = location.getLatitude();            current_lon = location.getLongitude();            current_lat1 = current_lat;            current_lon1 = current_lon;            //currentMarker.setPosition(latLng);            // Toast.makeText(BeginTrip.this, "distance in metres:" + "MY current" , Toast.LENGTH_SHORT).show();                /*fromPosition = new LatLng(previous_lat, previous_lon);                float[] f = new float[1];                if (current_lat != previous_lat || current_lon != previous_lon) {                    // dis += getDistance(previous_lat, previous_lon, current_lat, current_lon);                    Location.distanceBetween(previous_lat, previous_lon, current_lat, current_lon,f);                    dis += Double.parseDouble( String.valueOf(f[0]));                   // Toast.makeText(BeginTrip.this, "distance in metres1:" + String.valueOf(dis) , Toast.LENGTH_SHORT).show();                    previous_lat = current_lat;                    previous_lon = current_lon;                    System.out.println("distanceinside----------------------" + dis);                } else {                    previous_lat = current_lat;                    previous_lon = current_lon;                    dis = dis;                }                previous_lat = current_lat;                previous_lon = current_lon;*/            //  Toast.makeText(BeginTrip.this, "distance in metres2:" + String.valueOf(dis), Toast.LENGTH_SHORT).show();            if (googleMap != null) {                //googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(myLocation.getLatitude(), myLocation.getLongitude()),                //       16));            }            toposition = new LatLng(myLocation.getLatitude(), myLocation.getLongitude());            if (fromPosition != null && toposition != null) {                //GetRouteTask getRoute = new GetRouteTask();                // getRoute.execute();            }        }    }    private void sendLocationToTheUser(Location location) throws JSONException {        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());        current_lat = location.getLatitude();        current_lon = location.getLongitude();        String sendlat = Double.valueOf(current_lat).toString();        String sendlng = Double.valueOf(current_lon).toString();        if (job == null) {            job = new JSONObject();        }        job.put("action", "driver_loc");        job.put("latitude", sendlat);        job.put("longitude", sendlng);        job.put("ride_id", "");        String sToID = ContinuousRequestAdapter.userID + "@" + ServiceConstant.XMPP_SERVICE_NAME;        try {            if(chat  != null){                chat.sendMessage(job.toString());            }else{                chat = ChatingService.createChat(sToID);                chat.sendMessage(job.toString());            }        } catch (SmackException.NotConnectedException e) {            try {                chat = ChatingService.createChat(sToID);                chat.sendMessage(job.toString());            }catch (SmackException.NotConnectedException e1){                //Toast.makeText(this,"Not Able to send data to the user",Toast.LENGTH_SHORT).show();            }        }    }    @Override    public void onConnectionFailed(ConnectionResult connectionResult) {    }    @Override    public boolean onTouch(View v, MotionEvent event) {        return false;    }    //-----------------------Code for begin trip post request-----------------    private void PostRequest(String Url) {        dialog = new Dialog(BeginTrip.this);        dialog.getWindow();        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);        dialog.setContentView(R.layout.custom_loading);        dialog.setCanceledOnTouchOutside(false);        dialog.show();        String droplocation[] = locationAddressfinal.split(",");       /* Log.d("Droplocation+++++",droplocation.toString());         double droplatitude = Double.parseDouble(droplocation[0]);       double droplongitude = Double.parseDouble(droplocation[1]);*/        final TextView dialog_title = (TextView) dialog.findViewById(R.id.custom_loading_textview);        dialog_title.setText(getResources().getString(R.string.action_loading));        System.out.println("-------------begin----------------" + Url);        HashMap<String, String> jsonParams = new HashMap<String, String>();        jsonParams.put("driver_id", driver_id);        jsonParams.put("ride_id", Str_rideid);        jsonParams.put("pickup_lat", String.valueOf(MyCurrent_lat));        jsonParams.put("pickup_lon", String.valueOf(MyCurrent_long));        jsonParams.put("drop_lat", String.valueOf(str_drop_Latitude));        jsonParams.put("drop_lon", String.valueOf(str_drop_Longitude));        System.out                .println("--------------driver_id-------------------"                        + driver_id);        System.out                .println("--------------pickup_lat-------------------"                        + String.valueOf(MyCurrent_lat));        System.out                .println("--------------pickup_lon-------------------"                        + String.valueOf(MyCurrent_long));        mRequest = new ServiceRequest(BeginTrip.this);        mRequest.makeServiceRequest(Url, Request.Method.POST, jsonParams, new ServiceRequest.ServiceListener() {            @Override            public void onCompleteListener(String response) {                Log.e("begin", response);                System.out.println("response---------" + response);                String Str_status = "", Str_response = "";                try {                    JSONObject object = new JSONObject(response);                    Str_status = object.getString("status");                    Str_response = object.getString("response");                } catch (Exception e) {                    // TODO Auto-generated catch block                    e.printStackTrace();                }                dialog.dismiss();                if (Str_status.equalsIgnoreCase("1")) {                    locationaddressstartingpoint = String.valueOf(MyCurrent_lat + "," + MyCurrent_long);                    String sDropLocation=str_drop_Latitude+","+str_drop_Longitude;                    Intent intent = new Intent(BeginTrip.this, EndTrip.class);                    intent.putExtra("name", Str_name);                    intent.putExtra("rideid", Str_rideid);                    intent.putExtra("mobilno", Str_mobilno);                    intent.putExtra("pickuplatlng", sDropLocation);                    intent.putExtra("startpoint", locationaddressstartingpoint);                    startActivity(intent);                    overridePendingTransition(R.anim.fade_in, R.anim.fade_out);                    finish();                } else {                    Alert(getResources().getString(R.string.alert_sorry_label_title), Str_response);                }            }            @Override            public void onErrorListener() {                dialog.dismiss();            }        });    }    @Override    protected void onPause() {        super.onPause();        System.gc();        if(chat != null){            chat.close();        }    }    @Override    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {        if (progress > 95) {            seekBar.setThumb(getResources().getDrawable(R.drawable.slidetounlock_arrow));        }    }    @Override    public void onStartTrackingTouch(SeekBar seekBar) {        Bt_slider.setVisibility(View.INVISIBLE);    }    @Override    public void onStopTrackingTouch(SeekBar seekBar) {        shimmer = new Shimmer();        if (seekBar.getProgress() < 80) {            seekBar.setProgress(0);            sliderSeekBar.setBackgroundResource(R.drawable.blue_slide_to_unlock_bg);            Bt_slider.setVisibility(View.VISIBLE);            Bt_slider.setText(getResources().getString(R.string.lbel_begintrip));            shimmer.start(Bt_slider);        } else if (seekBar.getProgress() > 90) {            seekBar.setProgress(100);            Bt_slider.setVisibility(View.VISIBLE);            Bt_slider.setText(getResources().getString(R.string.lbel_begintrip));            shimmer.start(Bt_slider);            sliderSeekBar.setVisibility(View.VISIBLE);            System.out.println("------------------sliding completed----------------");            cd = new ConnectionDetector(BeginTrip.this);            isInternetPresent = cd.isConnectingToInternet();            if (isInternetPresent) {                String dropLocation = Drop_address_Tv.getText().toString();                if (dropLocation != null && dropLocation.length() > 0) {                    PostRequest(ServiceConstant.begintrip_url);                } else {                    Alert(getResources().getString(R.string.alert_sorry_label_title), getResources().getString(R.string.alert_enter_drop_location));                }                System.out.println("begin------------------" + ServiceConstant.begintrip_url);            } else {                Alert(getResources().getString(R.string.alert_sorry_label_title), getResources().getString(R.string.alert_nointernet));            }        }    }    @Override    protected void onActivityResult(int requestCode, int resultCode, Intent data) {        super.onActivityResult(requestCode, resultCode, data);        if (resultCode == RESULT_OK) {            if (requestCode == DropLocationRequestCode) {                String sAddress = data.getStringExtra("Selected_Location");                Slattitude = data.getStringExtra("Selected_Latitude");                Slongitude = data.getStringExtra("Selected_Longitude");                Drop_address_Tv.setText(sAddress);                str_drop_Latitude=Slattitude;                str_drop_Longitude=Slongitude;                if (Drop_address_Tv.getText().toString().length() > 0) {                    Rl_beginTrip.setVisibility(View.VISIBLE);                    GPSTracker gps = new GPSTracker(BeginTrip.this);                    if (gps.canGetLocation()) {                        double dLatitude = gps.getLatitude();                        double dLongitude = gps.getLongitude();                        MyCurrent_lat = dLatitude;                        MyCurrent_long = dLongitude;                        LatLng fromLat = new LatLng(MyCurrent_lat, MyCurrent_long);                        LatLng toLat = new LatLng(Double.parseDouble(Slattitude), Double.parseDouble(Slongitude));                        GetRouteTask getRouteTask = new GetRouteTask(fromLat, toLat);                        getRouteTask.execute();                    }                } else {                    Rl_beginTrip.setVisibility(View.GONE);                }            }        }    }    @Override    protected void onStop() {        super.onStop();        if (mGoogleApiClient != null)            mGoogleApiClient.disconnect();    }}