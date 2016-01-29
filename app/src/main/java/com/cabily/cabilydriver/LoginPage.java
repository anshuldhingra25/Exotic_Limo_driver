package com.cabily.cabilydriver;

import android.content.Intent;
import android.content.IntentSender;
import android.os.Bundle;
import android.provider.Settings;
import android.provider.Settings.Secure;
import android.support.v7.app.ActionBar;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.android.volley.Request;
import com.app.dao.LoginDetails;
import com.app.xmpp.ChatingService;
import com.app.gcm.GCMIntializer;
import com.app.service.ServiceManager;
import com.cabily.cabilydriver.Utils.GPSTracker;
import com.cabily.cabilydriver.Utils.SessionManager;
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

import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import me.drakeet.materialdialog.MaterialDialog;

public class LoginPage extends BaseActivity implements View.OnClickListener,GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {


    private String android_id;
    private SessionManager session;
    private RelativeLayout email_layout, password_layout;
    private EditText emailid, password;
    private Button signin;
    private String GCM_Id;
    private ActionBar actionBar;
    public static LoginDetails details;
    GoogleApiClient mGoogleApiClient;
    LocationRequest mLocationRequest;
    PendingResult<LocationSettingsResult> result;
    final static int REQUEST_LOCATION = 199;
    GPSTracker gps;

    private Animation slideUp;
    private Animation slideLeft;

    private RelativeLayout layout_forgotpassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.signin);
        initialize();

        mGoogleApiClient = new GoogleApiClient.Builder(LoginPage.this)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this).build();
        mGoogleApiClient.connect();


        layout_forgotpassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(LoginPage.this,ForgotPassword.class);
                  startActivity(intent);
                overridePendingTransition(R.anim.fade_in,R.anim.fade_out);

            }
        });


    }

    private void initialize() {
        android_id = Secure.getString(getApplicationContext().getContentResolver(), Secure.ANDROID_ID);
        session = new SessionManager(this);
        gps = new GPSTracker(LoginPage.this);
        email_layout = (RelativeLayout) findViewById(R.id.email_layout);
        password_layout = (RelativeLayout) findViewById(R.id.password_layout);
        emailid = (EditText) findViewById(R.id.email);
        password = (EditText) findViewById(R.id.password);
        signin = (Button) findViewById(R.id.signin_main_button);
        layout_forgotpassword = (RelativeLayout)findViewById(R.id.layout_forgot_password);



        slideUp = AnimationUtils.loadAnimation(LoginPage.this,R.anim.slide_up);
        slideLeft = AnimationUtils.loadAnimation(LoginPage.this, R.anim.slide_left);

        signin.setOnClickListener(this);
        showDialog(getResources().getString(R.string.lablesigningin_Textview));

        GCMIntializer initializer = new GCMIntializer(LoginPage.this, new GCMIntializer.CallBack() {
            @Override
            public void onRegisterComplete(String id) {
                GCM_Id = id;
                dismissDialog();
            }
            @Override
            public void onError(String errorMsg) {
                dismissDialog();
            }
        });
        initializer.init();
    }


    private void slideLeft() {
        signin.startAnimation(slideLeft);
        signin.setVisibility(View.INVISIBLE);
    }


    //--------------Alert Method-----------
    private void Alert(String title, String message) {
        final PkDialog mDialog = new PkDialog(LoginPage.this);
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
                            status.startResolutionForResult(LoginPage.this, REQUEST_LOCATION);
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
    public void onClick(View v) {
        if (v == signin) {
            final String pass = password.getText().toString();
            final String email = emailid.getText().toString();
            if (!isValidEmail(email)) {
                emailid.setError(getResources().getString(R.string.action_alert_invalid_email));
            } else if (pass.length()==0) {
                password.setError(getResources().getString(R.string.action_alert_invalid_password));
            } else {
                gps = new GPSTracker(LoginPage.this);
                if (gps.canGetLocation() && gps.isgpsenabled()) {
                    showDialog(getResources().getString(R.string.lablesigningin_Textview));
                    postRequest(LOGIN_URL);
                } else {
                     enableGpsService();
                }

            }
        }
    }


    private boolean isValidEmail(String email) {
        String EMAIL_PATTERN = "^[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)*@"
                + "[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$";
        Pattern pattern = Pattern.compile(EMAIL_PATTERN);
        Matcher matcher = pattern.matcher(email);
        return matcher.matches();
    }
    // validating password with retype password
    public boolean isValidPassword(String pass) {
        if (pass != null && pass.length() > 5) {
            return true;
        }
        return false;
    }

    private void postRequest(final String url) {
        HashMap<String, String> jsonParams = new HashMap<String, String>();
        jsonParams.put("email",emailid.getText().toString());
        jsonParams.put("password",password.getText().toString());
        jsonParams.put("gcm_id",GCM_Id);
        ServiceManager manager = new ServiceManager(this, mServiceListener);
        manager.makeServiceRequest(url, Request.Method.POST, jsonParams);
    }

    private ServiceManager.ServiceListener mServiceListener = new ServiceManager.ServiceListener() {
        @Override
        public void onCompleteListener(Object res) {

            System.out.println("loginresponse-------------------"+res);
            dismissDialog();
            String status = "", driver_img = "", driver_id = "", driver_name = "", email = "", vehicle_number = "", vehicle_model = "", key = "";
            String sec_key = "";
            if (res instanceof LoginDetails) {
                LoginDetails details = (LoginDetails) res;
                LoginPage.details = details;
                status = details.getStatus();
                driver_img = details.getDriverImage();
                driver_id = details.getDriverId();
                driver_name = details.getDriverName();
                email = details.getEmail();
                vehicle_number = details.getVehicleNumber();
                vehicle_model = details.getVehicleModel();
                sec_key = details.getSec_key();
                key = details.getKey();

                System.out.println("key--------------"+sec_key);

                System.out.println("driverid--------------"+driver_id);

            }
            if (status.equalsIgnoreCase("1")) {

                Toast.makeText(getApplicationContext(), "Logged in  successfully", Toast.LENGTH_LONG).show();
                session.createLoginSession(driver_img, driver_id, driver_name, email, vehicle_number, vehicle_model, key, sec_key);
                session.setUserVehicle(vehicle_model);
                ChatingService.startDriverAction(LoginPage.this);
                Intent i = new Intent(LoginPage.this, NavigationDrawer.class);
                slideLeft();
                startActivity(i);
                finish();
            }
        }

        @Override
        public void onErrorListener(Object obj) {
            dismissDialog();
            if (obj instanceof LoginDetails) {
                LoginDetails details = (LoginDetails) obj;
                String status = details.getStatus();
                if (status.equalsIgnoreCase("0")) {
                    Alert(getResources().getString(R.string.action_alert_SigninFaild), getResources().getString(R.string.action_alert_signinfaildmsg));
                }
            }
        }
    };

    @Override
    public void onConnected(Bundle bundle) {

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }
}
