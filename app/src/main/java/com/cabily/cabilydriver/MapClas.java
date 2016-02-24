package com.cabily.cabilydriver;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.Hockeyapp.ActionBarActivityHockeyApp;
import com.app.service.ServiceConstant;
import com.app.xmpp.ChatingService;
import com.cabily.cabilydriver.Utils.GPSTracker;
import com.cabily.cabilydriver.adapter.ContinuousRequestAdapter;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
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

import org.json.JSONObject;

import java.io.IOException;
import java.util.List;
import java.util.Locale;


/**
 * Created by user115 on 2/18/2016.
 */
public class MapClas extends ActionBarActivityHockeyApp implements GoogleMap.OnMapLongClickListener, View.OnClickListener, com.google.android.gms.location.LocationListener, GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener
{

    // Google Map
    private GoogleMap googleMap;

    MarkerOptions markerOptions;
    LatLng latLng;
    private Locale myLocale;
    private String UserEmail = "", Sphone_language = "";
    double latitude = 0.0;
    double longitude = 0.0;
    public String des_lat_lng = "";
    Double lat, lng;

    public String locationAddressstring;
    public String locationAddressfinal = "";

   // private Button mappage_select_location_layout;
    private RelativeLayout cancel;
    String current_addressText = "";
    private int search_status = 0;
    private int placeSearch_request_code = 200;
    private GoogleApiClient mGoogleApiClient;
    private LocationRequest mLocationRequest;
    private GPSTracker gps;
    public static Location myLocation;
    MarkerOptions marker;
    private Marker currentMarker;

    //EditText auto;
    // Button done;
    public static final int ActivityTwoRequestCode = 000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.map_class);
        try {
            initilizeMap();
            setLocationRequest();
            buildGoogleApiClient();
        } catch (Exception e) {
            e.printStackTrace();
        }
        cancel.setOnClickListener(new RelativeLayout.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
                overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
                finish();
            }
        });
       /* mappage_select_location_layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent returnIntent = new Intent();
                returnIntent.putExtra("Selected_location", current_addressText);
                setResult(RESULT_OK, returnIntent);
                onBackPressed();
                finish();
            }
        });*/
/*
*/
        googleMap.setOnMapLongClickListener(MapClas.this);
    }



    @Override
    protected void onResume() {
        super.onResume();
        startLocationUpdates();
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


    public void onMapLongClick (LatLng point){

        // Getting the Latitude and Longitude of the touched location
        latLng = point;
        System.out.println("------------latLng-------------------" + latLng);
        // Clears the previously touched position
        googleMap.clear();
        // Animating to the touched position
       // googleMap.animateCamera(CameraUpdateFactory.newLatLng(latLng));
        // Creating a marker
        markerOptions = new MarkerOptions();
        // Setting the position for the marker
        markerOptions.position(latLng);
        // Placing a marker on the touched position
        markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.flagimage));
        // Adding Marker on the touched location with address
        new ReverseGeocodingTask(getBaseContext()).execute(latLng);

    }


    /**
     * function to load map If map is not created it will create it for you
     */
    private void initilizeMap() {

        cancel = (RelativeLayout) findViewById(R.id.mappage_header_cancellayout);
       // mappage_select_location_layout = (Button) findViewById(R.id.mappage_select_location_button);


        if (googleMap == null) {
            googleMap = ((MapFragment) getFragmentManager().findFragmentById(
                    R.id.map)).getMap();
            // check if map is created successfully or not
            if (googleMap == null) {
               /* Toast.makeText(getApplicationContext(),
                        getResources().getString(R.string.map), Toast.LENGTH_SHORT)
                        .show();*/
            }


            Bundle b = getIntent().getExtras();
            try {
                latitude = b.getDouble("latitude");
                longitude = b.getDouble("longitude");
            }catch (Exception e){
                e.printStackTrace();
            }
            // Adding Marker on the touched location with address
            //new ReverseGeocodingTask(getBaseContext()).execute(latLng);
            First_ReverseGeocodingTask asyn = new First_ReverseGeocodingTask(MapClas.this, latitude, longitude);
            asyn.execute();

            Intent returnIntent = new Intent();
            returnIntent.putExtra("Selected_location", current_addressText);
            setResult(RESULT_OK, returnIntent);
        }
    }

    private void setLocationRequest() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(10000);
        mLocationRequest.setFastestInterval(5000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }




    @Override
    public void onStart() {
        super.onStart();
        if (mGoogleApiClient != null)
            mGoogleApiClient.connect();


    }


    public void onConnected(Bundle bundle) {

        if (gps != null && gps.canGetLocation() && gps.isgpsenabled()) {
        }
        myLocation = LocationServices.FusedLocationApi.getLastLocation(
                mGoogleApiClient);
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, (LocationListener) this);
        if (myLocation != null) {
            if (googleMap == null)
                googleMap = ((MapFragment) MapClas.this.getFragmentManager().findFragmentById(R.id.arrived_trip_view_map)).getMap();
            googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(myLocation.getLatitude(), myLocation.getLongitude()),
                    16));
            markerOptions = new MarkerOptions();
            marker = new MarkerOptions();
            marker.position(new LatLng(myLocation.getLatitude(), myLocation.getLongitude()));
            marker.icon(BitmapDescriptorFactory.fromResource(R.drawable.cargreens));
        }
    }

    public void onConnectionSuspended(int i) {
    }

    @Override
    public void onLocationChanged(Location location) {
        this.myLocation = location;
        System.out.println("locatbegintrip-----------" + location);
        myLocation = location;
        if (myLocation != null) {
           /* LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
            if(currentMarker != null){
                currentMarker.setPosition(latLng);
            }*/
            System.out.println("latlaong---------------------------" + latLng);
            if (googleMap != null) {
              /*  googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(myLocation.getLatitude(), myLocation.getLongitude()),
                        16));*/
            }
        }

    }

    @Override
    public void onClick(View view) {
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
    }


    private class ReverseGeocodingTask extends AsyncTask<LatLng, Void, String> {
        Context mContext;

        public ReverseGeocodingTask(Context context) {
            super();
            mContext = context;
        }

        // Finding address using reverse geocoding
        @Override
        protected String doInBackground(LatLng... params) {
            Geocoder geocoder = new Geocoder(mContext);
            double latitude = params[0].latitude;
            double longitude = params[0].longitude;

            List<Address> addresses = null;
            String addressText = "";

            try {
                addresses = geocoder.getFromLocation(latitude, longitude, 1);
            } catch (IOException e) {
                e.printStackTrace();
            }

            if (addresses != null && addresses.size() > 0) {

                Address address = addresses.get(0);

                addressText = String.format("%s",
                        address.getMaxAddressLineIndex() > 0 ? address.getAddressLine(1) : "");
                //address.getLocality()
                // address.getCountryName()
            }

            return addressText;
        }

        @Override
        protected void onPostExecute(String addressText) {
            // Setting the title for the marker.
            // This will be displayed on taping the marker
            //markerOptions.title(addressText);

            // Placing a marker on the touched position
            googleMap.addMarker(markerOptions);


            System.out.println("---------------addressText------------------" + addressText);

            if (addressText.length() > 0) {
                Intent returnIntent = new Intent();
                returnIntent.putExtra("Selected_location", addressText);
                setResult(RESULT_OK, returnIntent);
                onBackPressed();
                finish();
            }

        }
    }


    private class First_ReverseGeocodingTask extends AsyncTask<LatLng, Void, String> {
        Context mContext;
        double Dlatitude = 0.0, Dlongitude = 0.0;

        public First_ReverseGeocodingTask(Context context, double lat, double longitude) {
            super();
            mContext = context;
            Dlatitude = lat;
            Dlongitude = longitude;
        }

        // Finding address using reverse geocoding
        @Override
        protected String doInBackground(LatLng... params) {
            Geocoder geocoder = new Geocoder(mContext);
            double latitude = Dlatitude;
            double longitude = Dlongitude;

            List<Address> addresses = null;

            try {
                addresses = geocoder.getFromLocation(latitude, longitude, 1);


			/*	String address = addresses.get(0).getAddressLine(0); // If any additional address line present than only, check with max available address lines by getMaxAddressLineIndex()
                String city = addresses.get(0).getLocality();
				String state = addresses.get(0).getAdminArea();
				String country = addresses.get(0).getCountryName();
				String postalCode = addresses.get(0).getPostalCode();
				String knownName = addresses.get(0).getFeatureName(); */


			/*
                System.out.println("--------------address-----------------"+address);
				System.out.println("--------------city-----------------"+city);
				System.out.println("--------------state-----------------"+state);
				System.out.println("--------------country-----------------"+country);
				System.out.println("--------------postalCode-----------------"+postalCode);
				System.out.println("--------------knownName-----------------"+knownName);
				*/
            } catch (IOException e) {
                e.printStackTrace();
            }

            if (addresses != null && addresses.size() > 0) {

                Address address = addresses.get(0);

                current_addressText = String.format("%s",
                        address.getMaxAddressLineIndex() > 0 ? address.getAddressLine(1) : "");
                //address.getLocality()
                // address.getCountryName()
            }

            return current_addressText;
        }

        @Override
        protected void onPostExecute(String addressText) {
            // Setting the title for the marker.
            // This will be displayed on taping the marker
            //markerOptions.title(addressText);

            // Placing a marker on the touched position
            //googleMap.addMarker(markerOptions);

            System.out.println("---------------addressText------------------" + addressText);

        }
    }


    //---------------------------code to change language------------------------
    public void changeLang(String lang) {
        if (lang.equalsIgnoreCase(""))
            return;
        myLocale = new Locale(lang);
        saveLocale(lang);
        Locale.setDefault(myLocale);
        android.content.res.Configuration config = new android.content.res.Configuration();
        config.locale = myLocale;
        getBaseContext().getResources().updateConfiguration(config, getBaseContext().getResources().getDisplayMetrics());
    }

    public void saveLocale(String lang) {
        String langPref = "Language";
        SharedPreferences prefs = getSharedPreferences("CommonPrefs", Activity.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(langPref, lang);
        editor.commit();
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


}
