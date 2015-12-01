package com.cabily.cabilydriver;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
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
import com.app.xmpp.ChatingService;
import com.app.service.ServiceConstant;
import com.cabily.cabilydriver.Utils.AppController;
import com.cabily.cabilydriver.Utils.ConnectionDetector;
import com.cabily.cabilydriver.Utils.GPSTracker;
import com.cabily.cabilydriver.Utils.RoundedImageView;
import com.cabily.cabilydriver.Utils.SessionManager;
import com.cabily.cabilydriver.Utils.VolleyErrorResponse;
import com.cabily.cabilydriver.googlemappath.GMapV2GetRouteDirection;
import com.cabily.cabilydriver.subclass.SubclassActivity;
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
import com.squareup.picasso.MemoryPolicy;
import com.squareup.picasso.Picasso;

import org.json.JSONObject;
import org.w3c.dom.Document;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import me.drakeet.materialdialog.MaterialDialog;

/**
 * Created by user88 on 10/29/2015.
 */
public class BeginTrip extends SubclassActivity implements com.google.android.gms.location.LocationListener, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener{

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

    private String Str_name = "", Str_mobilno = "", Str_rideid = "", Str_profilpic = "";

    private GoogleMap googleMap;
    GPSTracker gps;
    private double MyCurrent_lat = 0.0, MyCurrent_long = 0.0;

    ArrayList<LatLng> points;

    private Marker currentMarker;
    private RelativeLayout alert_layout, Rl_layout_cancel;
    private TextView alert_textview;
    private Button Bt_begin_trip;
    Dialog dialog;
    StringRequest postrequest;
    GMapV2GetRouteDirection v2GetRouteDirection;
    LatLng fromPosition;
    LatLng fromPosition2;
    LatLng toPosition;
    MarkerOptions markerOptions;
    Document document;
    private String current_location = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.begin_trip);
        initialize();
        initilizeMap();

        // And From your main() method or any other method
        Timer timer = new Timer();
        timer.schedule(new SayHello(), 0, 5000);


        Rl_layout_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
                overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
            }
        });


        Bt_begin_trip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cd = new ConnectionDetector(BeginTrip.this);
                isInternetPresent = cd.isConnectingToInternet();
                if (isInternetPresent) {
                    PostRequest(ServiceConstant.begintrip_url);
                    System.out.println("begin------------------" + ServiceConstant.begintrip_url);
                } else {

                    Alert(getResources().getString(R.string.alert_label_title), getResources().getString(R.string.alert_nointernet));
                }
            }
        });


        callimg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(Str_mobilno!=null)
                {
                    Intent callIntent = new Intent(Intent.ACTION_CALL);
                    callIntent.setData(Uri.parse("tel:"+Str_mobilno));
                    startActivity(callIntent);
                }
                else
                {
                    Alert(BeginTrip.this.getResources().getString(R.string.alert_label_title), BeginTrip.this.getResources().getString(R.string.arrived_alert_content1));
                }
            }
        });
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
        Tv_name_header = (TextView) findViewById(R.id.begin_trip_nameTxt);
        Tv_name = (TextView) findViewById(R.id.begintrip_name);
        Tv_mobile_no = (TextView) findViewById(R.id.begintrip_mobilno);
        Tv_Rideid = (TextView) findViewById(R.id.begintrip_rideid);
        profile_img = (RoundedImageView) findViewById(R.id.profile_image);
        callimg = (ImageView) findViewById(R.id.begintrip_call);
        alert_textview = (TextView) findViewById(R.id.begintrip_alert_textView);
        alert_layout = (RelativeLayout) findViewById(R.id.begintrip_alert_layout);
        Rl_layout_cancel = (RelativeLayout) findViewById(R.id.layout_begin_trip_cancel);
        Bt_begin_trip = (Button) findViewById(R.id.btn_begintrip);


        Tv_name_header.setText(Str_name);
        Tv_mobile_no.setText(Str_mobilno);
        Tv_name.setText(Str_name);

        Picasso.with(BeginTrip.this).load(String.valueOf(Str_profilpic)).placeholder(R.drawable.nouserimg).memoryPolicy(MemoryPolicy.NO_CACHE).into(profile_img);

    }

    private void initilizeMap() {

        if (googleMap == null) {
            googleMap = ((MapFragment) BeginTrip.this.getFragmentManager().findFragmentById(R.id.arrived_trip_view_map)).getMap();
            // check if map is created successfully or not
            if (googleMap == null) {
                Toast.makeText(BeginTrip.this, "Sorry! unable to create maps", Toast.LENGTH_SHORT).show();
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
            marker.icon(BitmapDescriptorFactory.fromResource(R.drawable.cargreens));
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

    //--------------Alert Method-----------
    private void Alert(String title, String alert) {
        final MaterialDialog dialog = new MaterialDialog(BeginTrip.this);
        dialog.setTitle(title)
                .setMessage(alert)
                .setPositiveButton(
                        "OK", new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                dialog.dismiss();
                            }
                        }
                )
                .show();
    }

    @Override
    public void onConnected(Bundle bundle) {
    }

    @Override
    public void onConnectionSuspended(int i) {
    }

    @Override
    public void onLocationChanged(Location location) {
        myLocation = location;
        if (myLocation != null) {
            LatLng latLng = new LatLng(location.getLatitude(),location.getLongitude());
            currentMarker.setPosition(latLng);

            System.out.println("latlaong---------------------------"+latLng);

        }
    }


    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
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
                            final MaterialDialog alertDialog = new MaterialDialog(BeginTrip.this);
                            alertDialog.setTitle("Error");
                            alertDialog
                                    .setMessage(Str_response)
                                    .setCanceledOnTouchOutside(false)
                                    .setPositiveButton(
                                            "OK", new View.OnClickListener() {
                                                @Override
                                                public void onClick(View v) {
                                                    alertDialog.dismiss();
                                                }
                                            }
                                    ).show();

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
    }


}
