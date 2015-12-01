package com.cabily.cabilydriver;

import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.provider.Settings.Secure;
import android.support.v7.app.ActionBar;
import android.view.View;
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

import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import me.drakeet.materialdialog.MaterialDialog;

public class LoginPage extends BaseActivity implements View.OnClickListener {


    private String android_id;
    private SessionManager session;
    private RelativeLayout email_layout, password_layout;
    private EditText emailid, password;
    private Button signin;
    private String GCM_Id;
    private ActionBar actionBar;
    public static LoginDetails details;


    GPSTracker gps;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.signin);
        initialize();
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

    @Override
    public void onClick(View v) {
        if (v == signin) {
            final String pass = password.getText().toString();
            final String email = emailid.getText().toString();
            if (!isValidEmail(email)) {
                emailid.setError("Invalid Email");
            } else if (!isValidPassword(pass)) {
                password.setError("Invalid Password");
            } else {
                gps = new GPSTracker(LoginPage.this);
                if (gps.canGetLocation() && gps.isgpsenabled()) {
                    showDialog(getResources().getString(R.string.lablesigningin_Textview));
                    postRequest(LOGIN_URL);
                } else {
                    gpsDialog(getResources().getString(R.string.label_gps_textview));
                }

            }
        }
    }


    public void gpsDialog(String response){
        final MaterialDialog alertDialog = new MaterialDialog(LoginPage.this);
        alertDialog.setTitle("Error");
        alertDialog
                .setMessage(response)
                .setCanceledOnTouchOutside(false)
                .setPositiveButton(
                        "ENABLE", new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                alertDialog.dismiss();
                                startActivity(new Intent(Settings.ACTION_SETTINGS));
                            }
                        }
                ).show();

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
        jsonParams.put("email", emailid.getText().toString());
        jsonParams.put("password", password.getText().toString());
        jsonParams.put("gcm_id", GCM_Id);
        ServiceManager manager = new ServiceManager(this, mServiceListener);
        manager.makeServiceRequest(url, Request.Method.POST, jsonParams);
    }

    private ServiceManager.ServiceListener mServiceListener = new ServiceManager.ServiceListener() {
        @Override
        public void onCompleteListener(Object res) {
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

                Toast.makeText(getApplicationContext(), "Login successfully", Toast.LENGTH_LONG).show();
                session.createLoginSession(driver_img, driver_id, driver_name, email, vehicle_number, vehicle_model, key, sec_key);
                session.setUserVehicle(vehicle_model);
                ChatingService.startDriverAction(LoginPage.this);
                Intent i = new Intent(LoginPage.this, NavigationDrawer.class);
                startActivity(i);
                finish();
            } else {
            }
        }

        @Override
        public void onErrorListener(Object obj) {
            dismissDialog();
            if (obj instanceof LoginDetails) {
                LoginDetails details = (LoginDetails) obj;
                String status = details.getStatus();
                if (status.equalsIgnoreCase("0")) {
                    Toast.makeText(getApplicationContext(), "Invalid login", Toast.LENGTH_LONG).show();
                }
            }
        }
    };
}
