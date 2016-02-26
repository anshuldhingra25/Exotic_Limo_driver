package com.cabily.cabilydriver;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.cabily.cabilydriver.Utils.ConnectionDetector;
import com.cabily.cabilydriver.Utils.GPSTracker;
import com.cabily.cabilydriver.widgets.PkDialog;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;

import java.util.List;
import java.util.Locale;

/**
 * Created by Prem Kumar and Anitha on 2/26/2016.
 */
public class DropLocationSelect extends Activity {
    RelativeLayout Rl_done, Rl_back;
    RelativeLayout Rl_selectDrop;
    private TextView Tv_dropLocation;

    private Boolean isInternetPresent = false;
    private ConnectionDetector cd;

    private GoogleMap googleMap;
    private GPSTracker gps;
    ProgressBar progressBar;

    private String sLatitude="";
    private String sLongitude="";

    public static final int ActivityDropRequestCode = 6000;
    private static final int REQUEST_CODE_RECOVER_PLAY_SERVICES = 1001;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.droplocation_select);
        initialize();
        initializeMap();

        Rl_done.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent returnIntent = new Intent();
                returnIntent.putExtra("Selected_Latitude", sLatitude);
                returnIntent.putExtra("Selected_Longitude", sLongitude);
                returnIntent.putExtra("Selected_Location", Tv_dropLocation.getText().toString());
                setResult(RESULT_OK, returnIntent);
                onBackPressed();
                finish();
            }
        });

        Rl_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
                finish();
                overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
            }
        });

        Rl_selectDrop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                GPSTracker gps = new GPSTracker(DropLocationSelect.this);
                if (gps.canGetLocation()) {
                    Intent i = new Intent(DropLocationSelect.this, LocationSearch.class);
                    startActivityForResult(i, ActivityDropRequestCode);
                    overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
                } else {
                    Toast.makeText(DropLocationSelect.this, "Enable Gps", Toast.LENGTH_SHORT)
                            .show();
                }
            }
        });
    }

    private void initialize() {
        cd = new ConnectionDetector(DropLocationSelect.this);
        isInternetPresent = cd.isConnectingToInternet();
        gps=new GPSTracker(DropLocationSelect.this);

        Rl_done = (RelativeLayout) findViewById(R.id.drop_location_select_done_layout);
        Rl_back = (RelativeLayout) findViewById(R.id.drop_location_select_back_layout);
        Rl_selectDrop = (RelativeLayout) findViewById(R.id.drop_location_select_dropLocation_layout);
        Tv_dropLocation = (TextView) findViewById(R.id.drop_location_select_drop_address);
        progressBar=(ProgressBar)findViewById(R.id.drop_location_select_progress_bar);

    }

    private void initializeMap() {
        if (googleMap == null) {
            googleMap = ((MapFragment) getFragmentManager().findFragmentById(R.id.drop_location_select_view_map)).getMap();
            if (googleMap == null) {
                Toast.makeText(DropLocationSelect.this, "Sorry! unable to create maps", Toast.LENGTH_SHORT).show();
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
        if (gps.canGetLocation() && gps.isgpsenabled()) {

            double Dlatitude = gps.getLatitude();
            double Dlongitude = gps.getLongitude();

            // Move the camera to last position with a zoom level
            CameraPosition cameraPosition = new CameraPosition.Builder().target(new LatLng(Dlatitude, Dlongitude)).zoom(17).build();
            googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));

        } else {
            Alert(getResources().getString(R.string.alert_sorry_label_title), getResources().getString(R.string.alert_gpsEnable));
        }


        if (CheckPlayService()) {
            googleMap.setOnCameraChangeListener(mOnCameraChangeListener);
            googleMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
                @Override
                public boolean onMarkerClick(Marker marker) {
                    String tittle = marker.getTitle();
                    return true;
                }
            });
        } else {
            Toast.makeText(DropLocationSelect.this, "Install Google Play service To View Location !!!", Toast.LENGTH_LONG).show();
        }

    }

    //--------------Alert Method-----------
    private void Alert(String title, String message) {
        final PkDialog mDialog = new PkDialog(DropLocationSelect.this);
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





    //-----------Check Google Play Service--------
    private boolean CheckPlayService() {
        final int connectionStatusCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(DropLocationSelect.this);
        if (GooglePlayServicesUtil.isUserRecoverableError(connectionStatusCode)) {
            showGooglePlayServicesAvailabilityErrorDialog(connectionStatusCode);
            return false;
        }
        return true;
    }


    void showGooglePlayServicesAvailabilityErrorDialog(final int connectionStatusCode) {
        DropLocationSelect.this.runOnUiThread(new Runnable() {
            public void run() {
                final Dialog dialog = GooglePlayServicesUtil.getErrorDialog(
                        connectionStatusCode,   DropLocationSelect.this, REQUEST_CODE_RECOVER_PLAY_SERVICES);
                if (dialog == null) {
                    Toast.makeText(  DropLocationSelect.this, "incompatible version of Google Play Services", Toast.LENGTH_LONG).show();
                }
            }
        });
    }




    //-------------------------------code for map marker moving-------------------------------
    GoogleMap.OnCameraChangeListener mOnCameraChangeListener = new GoogleMap.OnCameraChangeListener() {
        @Override
        public void onCameraChange(CameraPosition cameraPosition) {
            double latitude = cameraPosition.target.latitude;
            double longitude = cameraPosition.target.longitude;

            cd = new ConnectionDetector(DropLocationSelect.this);
            isInternetPresent = cd.isConnectingToInternet();

            Log.e("camerachange lat-->", "" + latitude);
            Log.e("on_camera_change lon-->", "" + longitude);

            if (googleMap != null) {
                googleMap.clear();

                if (isInternetPresent) {

                    sLatitude = String.valueOf(latitude);
                    sLongitude = String.valueOf(longitude);

                    Map_movingTask asynTask=new Map_movingTask(latitude,longitude);
                    asynTask.execute();
                } else {
                    Alert(getResources().getString(R.string.alert_sorry_label_title), getResources().getString(R.string.no_internet_connection));
                }
            }
        }
    };




    private class Map_movingTask extends AsyncTask<String, Void, String> {

        String response = "";
        private double dLatitude=0.0;
        private double dLongitude=0.0;
        Map_movingTask(double lat,double lng)
        {
            dLatitude=lat;
            dLongitude=lng;
        }
        @Override
        protected void onPreExecute() {
            progressBar.setVisibility(View.VISIBLE);
        }

        @Override
        protected String doInBackground(String... urls) {
            String address = getCompleteAddressString(dLatitude, dLongitude);
            return address;
        }

        @Override
        protected void onPostExecute(String result) {
            progressBar.setVisibility(View.GONE);
            if(result!=null)
            {
                Tv_dropLocation.setText(result);
                Rl_done.setVisibility(View.VISIBLE);
            }else
            {
                Rl_done.setVisibility(View.GONE);
            }
        }
    }


    //-------------Method to get Complete Address------------
    private String getCompleteAddressString(double LATITUDE, double LONGITUDE) {
        String strAdd = "";
        Geocoder geocoder = new Geocoder(DropLocationSelect.this, Locale.getDefault());
        try {
            List<Address> addresses = geocoder.getFromLocation(LATITUDE, LONGITUDE, 1);
            if (addresses != null) {
                Address returnedAddress = addresses.get(0);
                StringBuilder strReturnedAddress = new StringBuilder("");

                for (int i = 0; i < returnedAddress.getMaxAddressLineIndex(); i++) {
                    strReturnedAddress.append(returnedAddress.getAddressLine(i)).append("\n");
                }
                strAdd = strReturnedAddress.toString();
            } else {
                Log.e("Current loction address", "No Address returned!");
            }
        } catch (Exception e) {
            e.printStackTrace();
            Log.e("Current loction address", "Canont get Address!");
        }
        return strAdd;
    }





    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // check if the request code is same as what is passed  here it is 2
        if (resultCode == RESULT_OK) {
            if (requestCode == ActivityDropRequestCode) {
                String sAddress = data.getStringExtra("Selected_Location");
                sLatitude = data.getStringExtra("Selected_Latitude");
                sLongitude = data.getStringExtra("Selected_Longitude");
                Tv_dropLocation.setText(sAddress);

                // Move the camera to last position with a zoom level
                CameraPosition cameraPosition = new CameraPosition.Builder().target(new LatLng(Double.parseDouble(sLatitude), Double.parseDouble(sLongitude))).zoom(17).build();
                googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));

                Rl_done.setVisibility(View.VISIBLE);
            }
        }
    }

}
