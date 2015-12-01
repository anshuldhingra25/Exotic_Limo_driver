package com.cabily.cabilydriver;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.AsyncTask;
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
import com.android.volley.NetworkError;
import com.android.volley.NoConnectionError;
import com.android.volley.ParseError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.ServerError;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.app.xmpp.ChatingService;
import com.app.service.ServiceConstant;
import com.cabily.cabilydriver.Utils.AppController;
import com.cabily.cabilydriver.Utils.ConnectionDetector;
import com.cabily.cabilydriver.Utils.GPSTracker;
import com.cabily.cabilydriver.Utils.SessionManager;
import com.cabily.cabilydriver.Utils.VolleyErrorResponse;
import com.cabily.cabilydriver.googlemappath.GMapV2GetRouteDirection;
import com.cabily.cabilydriver.subclass.SubclassActivity;
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
import org.w3c.dom.Document;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import me.drakeet.materialdialog.MaterialDialog;

/**
 * Created by user88 on 10/28/2015.
 */
public class ArrivedTrip extends SubclassActivity {
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
    private Button Bt_Arrived;
    private TextView Tv_Address, Tv_RideId, Tv_usename;
    private RelativeLayout Rl_layout_userinfo, Rl_layout_arrived;
    private String ERROR_TAG = "Unknown Error Occured";
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.arrivedtrip);
        arrivedTrip_class = ArrivedTrip.this;
        initialize();
        initilizeMap();
        //Starting Xmpp service
        ChatingService.startDriverAction(ArrivedTrip.this);
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


        Bt_Arrived.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cd = new ConnectionDetector(ArrivedTrip.this);
                isInternetPresent = cd.isConnectingToInternet();
                if (isInternetPresent) {
                    PostRequest(ServiceConstant.arrivedtrip_url);
                    System.out.println("arrived------------------" + ServiceConstant.arrivedtrip_url);
                } else {

                    Alert(getResources().getString(R.string.alert_label_title), getResources().getString(R.string.alert_nointernet));
                }
            }
        });


        phone_call.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(Str_user_phoneno!=null)
                {
                    Intent callIntent = new Intent(Intent.ACTION_CALL);
                    callIntent.setData(Uri.parse("tel:"+Str_user_phoneno));
                    startActivity(callIntent);
                }
                else
                {
                    Alert(ArrivedTrip.this.getResources().getString(R.string.alert_label_title), ArrivedTrip.this.getResources().getString(R.string.arrived_alert_content1));
                }

            }
        });

    }


    private void initialize() {
        session = new SessionManager(ArrivedTrip.this);
        gps = new GPSTracker(ArrivedTrip.this);
        v2GetRouteDirection = new GMapV2GetRouteDirection();
        HashMap<String, String> user = session.getUserDetails();
        driver_id = user.get(SessionManager.KEY_DRIVERID);
        alert_textview = (TextView) findViewById(R.id.arrivd_Tripaccpt_alert_textView);
        alert_layout = (RelativeLayout) findViewById(R.id.arrivd_Tripaccpt_alert_layout);
        phone_call  = (ImageView)findViewById(R.id.user_phonecall);
        Intent i = getIntent();
        Str_address = i.getStringExtra("address");
        Str_RideId = i.getStringExtra("rideId");
        Str_pickUp_Lat = i.getStringExtra("pickuplat");
        Str_pickUp_Long = i.getStringExtra("pickup_long");
        Str_username = i.getStringExtra("username");
        Str_user_rating = i.getStringExtra("userrating");
        Str_user_phoneno = i.getStringExtra("phoneno");
        Str_user_img = i.getStringExtra("userimg");
        System.out.println("adres---------" + Str_address);
        System.out.println("id---------" + Str_RideId);
        Tv_Address = (TextView) findViewById(R.id.trip_arrived_user_address);
        Tv_RideId = (TextView) findViewById(R.id.trip_arrived_user_id);
        Tv_usename = (TextView) findViewById(R.id.trip_arrived_usernameTxt);
        Rl_layout_userinfo = (RelativeLayout) findViewById(R.id.layout_arrived_trip_userinfo);
        Rl_layout_arrived = (RelativeLayout) findViewById(R.id.layout_arrivedbtn);
        Bt_Arrived = (Button) findViewById(R.id.btn_arrived);
        Tv_Address.setText(Str_address);
        Tv_RideId.setText(Str_RideId);
        Tv_usename.setText(Str_username);
    }

    @Override
    public void onBackPressed() {
        //super.onBackPressed();
    }

    //--------------Alert Method-----------
    private void Alert(String title, String alert) {
        final MaterialDialog dialog = new MaterialDialog(ArrivedTrip.this);
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


    private void initilizeMap() {
        if (googleMap == null) {
            googleMap = ((MapFragment) ArrivedTrip.this.getFragmentManager().findFragmentById(R.id.arrived_trip_view_map)).getMap();
            if (googleMap == null) {
                Toast.makeText(ArrivedTrip.this, "Sorry! unable to create maps", Toast.LENGTH_SHORT).show();
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
        try {
            System.out.println("to----------" + toPosition);
            System.out.println("from----------" + fromPosition);
            System.out.println("pickiplat----------" + Double.parseDouble(Str_pickUp_Lat));
            System.out.println("picklong----------" + Double.parseDouble(Str_pickUp_Long));
            fromPosition = new LatLng(MyCurrent_lat, MyCurrent_long);
            toPosition = new LatLng(Double.parseDouble(Str_pickUp_Lat), Double.parseDouble(Str_pickUp_Long));
            getRout(fromPosition, toPosition);
            System.out.println("from------" + fromPosition);
            System.out.println("to------" + toPosition);
            if (fromPosition!=null&&toPosition!=null)
            {
                GetRouteTask getRoute = new GetRouteTask();
                getRoute.execute();
            }
        } catch (Exception e) {
            Toast.makeText(getApplicationContext(), ERROR_TAG, Toast.LENGTH_SHORT).show();
        }
    }


    public void getRout(LatLng start, LatLng end) {
        StringRequest stringRequest;
        String url = "http://maps.googleapis.com/maps/api/directions/xml?"
                + "origin=" + start.latitude + "," + start.longitude
                + "&destination=" + end.latitude + "," + end.longitude
                + "&sensor=false&units=metric&mode=driving";

        stringRequest = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                try {
                    if (error instanceof TimeoutError || error instanceof NoConnectionError) {
                        Toast.makeText(context, "Unable to fetch data from server", Toast.LENGTH_LONG).show();
                    } else if (error instanceof AuthFailureError) {
                        Toast.makeText(context, "AuthFailureError", Toast.LENGTH_LONG).show();
                    } else if (error instanceof ServerError) {
                        Toast.makeText(context, "ServerError", Toast.LENGTH_LONG).show();
                    } else if (error instanceof NetworkError) {
                        Toast.makeText(context, "No Internet Connection", Toast.LENGTH_LONG).show();
                    } else if (error instanceof ParseError) {
                        Toast.makeText(context, "ParseError", Toast.LENGTH_LONG).show();
                    }
                } catch (Exception e) {
                }
            }
        }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                return new HashMap<String, String>();
            }

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<String, String>();
                return headers;
            }
        };
        stringRequest.setRetryPolicy(new DefaultRetryPolicy(5000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

        RequestQueue mRequestQueue;
        mRequestQueue = Volley.newRequestQueue(ArrivedTrip.this);
        mRequestQueue.add(stringRequest);

    }


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
            googleMap.clear();
            ArrayList<LatLng> directionPoint = v2GetRouteDirection.getDirection(document);
            PolylineOptions rectLine = new PolylineOptions().width(5).color(
                    Color.RED);

            for (int i = 0; i < directionPoint.size(); i++) {
                rectLine.add(directionPoint.get(i));
            }
            // Adding route on the map
            googleMap.addPolyline(rectLine);
            markerOptions.position(toPosition);
            markerOptions.position(fromPosition);
            markerOptions.draggable(true);
            //googleMap.addMarker(markerOptions);


            googleMap.addMarker(new MarkerOptions()
                    .position(toPosition)
                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.man)));
            googleMap.addMarker(new MarkerOptions()
                    .position(fromPosition)
                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.cargreens)));
        }
    }

    //-----------------------Code for arrived post request-----------------
    private void PostRequest(String Url) {
        dialog = new Dialog(ArrivedTrip.this);
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
                            Intent intent = new Intent(ArrivedTrip.this, BeginTrip.class);
                            intent.putExtra("user_name", Str_username);
                            intent.putExtra("user_phoneno", Str_user_phoneno);
                            intent.putExtra("user_image", Str_user_img);
                            intent.putExtra("rideid", Str_RideId);
                            startActivity(intent);
                            overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
                            finish();
                        } else {
                            final MaterialDialog alertDialog = new MaterialDialog(ArrivedTrip.this);
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
                VolleyErrorResponse.VolleyError(ArrivedTrip.this, error);
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
                jsonParams.put("ride_id", Str_RideId);

                System.out
                        .println("--------------driver_id-------------------"
                                + driver_id);


                System.out
                        .println("--------------ride_id-------------------"
                                + Str_RideId);


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
