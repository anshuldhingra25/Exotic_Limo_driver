package com.cabily.cabilydriver;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

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
import com.cabily.cabilydriver.Utils.CurrencySymbolConverter;
import com.cabily.cabilydriver.Utils.SessionManager;
import com.cabily.cabilydriver.Utils.VolleyErrorResponse;
import com.cabily.cabilydriver.subclass.SubclassActivity;
import com.cabily.cabilydriver.widgets.PkDialog;

import org.json.JSONObject;

import java.text.NumberFormat;
import java.util.Currency;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import me.drakeet.materialdialog.MaterialDialog;

/**
 * Created by user88 on 11/5/2015.
 */
public class OtpPage extends SubclassActivity {
    private Boolean isInternetPresent = false;
    private ConnectionDetector cd;
    private SessionManager session;
          EditText Et_otp;
         Button BT_otp_confirm;

      Dialog dialog;
    private ServiceRequest mRequest;

    StringRequest postrequest;

    String Str_otp="",Str_amount="";
    private  String Str_rideId="",driver_id="";

    private String sCurrencySymbol="";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.otp_page);
        initialize();

        //Starting Xmpp service
        ChatingService.startDriverAction(OtpPage.this);

        BT_otp_confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(OtpPage.this,PaymentPage.class);
                intent.putExtra("amount",Str_amount);
                intent.putExtra("rideid",Str_rideId);
                startActivity(intent);
                overridePendingTransition(R.anim.fade_in,R.anim.fade_out);

            }
        });

    }

    private void initialize() {
        session = new SessionManager(OtpPage.this);

        HashMap<String, String> user = session.getUserDetails();
        driver_id = user.get(SessionManager.KEY_DRIVERID);
        Et_otp = (EditText)findViewById(R.id.otp_enter_code);
        BT_otp_confirm = (Button)findViewById(R.id.otp_request_btn);
        Intent i = getIntent();
        Str_rideId = i.getStringExtra("rideid");

        System.out.println("inside-------------"+Str_rideId);

       // Et_otp.setSelection(Et_otp.getText().length());

        Et_otp.setFocusable(false);

        cd = new ConnectionDetector(OtpPage.this);
        isInternetPresent = cd.isConnectingToInternet();
        if (isInternetPresent){
            PostRequest(ServiceConstant.receivecash_url);
            System.out.println("end------------------" +ServiceConstant.receivecash_url);
        }else {

            Alert(getResources().getString(R.string.alert_sorry_label_title), getResources().getString(R.string.alert_nointernet));
        }

    }


    //--------------Alert Method------------------
    private void Alert(String title, String message) {
        final PkDialog mDialog = new PkDialog(OtpPage.this);
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


    //-----------------------Code for arrived post request-----------------
    private void PostRequest(String Url) {
        dialog = new Dialog(OtpPage.this);
        dialog.getWindow();
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.custom_loading);
        dialog.setCanceledOnTouchOutside(false);
        dialog.show();

        final TextView dialog_title = (TextView) dialog.findViewById(R.id.custom_loading_textview);
        dialog_title.setText(getResources().getString(R.string.action_loading));

        System.out.println("-------------otp----------------" + Url);

        HashMap<String, String> jsonParams = new HashMap<String, String>();
        jsonParams.put("driver_id",driver_id);
        jsonParams.put("ride_id",Str_rideId);

        System.out.println("otp-------driver_id---------"+driver_id);

        System.out.println("otp-------ride_id---------"+Str_rideId);
        mRequest = new ServiceRequest(OtpPage.this);
        mRequest.makeServiceRequest(Url, Request.Method.POST, jsonParams, new ServiceRequest.ServiceListener() {

            @Override
            public void onCompleteListener(String response) {
                Log.e("otp", response);

                System.out.println("otp---------"+response);

                String Str_status = "",Str_response="",Str_otp_status="",Str_ride_id="",Str_currency="";

                try {
                    JSONObject object = new JSONObject(response);
                    Str_status = object.getString("status");

                    if (Str_status.equalsIgnoreCase("1")){

                        Str_response = object.getString("response");
                        Str_currency = object.getString("currency");
                        //    Currency currencycode = Currency.getInstance(getLocale(Str_currency));

                        sCurrencySymbol = CurrencySymbolConverter.getCurrencySymbol(Str_currency);

                        Str_otp_status = object.getString("otp_status");
                        Str_otp  = object.getString("otp");
                        Str_ride_id = object.getString("ride_id");
                        Str_amount = sCurrencySymbol + object.getString("amount");

                        System.out.println("otp--------"+Str_otp);

                        System.out.println("Str_otp_status--------"+Str_otp_status);

                    }else{

                        Str_response = object.getString("response");
                    }
                }catch (Exception e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                dialog.dismiss();


                if (Str_status.equalsIgnoreCase("1")){

                    if (Str_otp_status.equalsIgnoreCase("development")){
                        Et_otp.setText(Str_otp);
                    }

                }else{
                    Alert(getResources().getString(R.string.alert_sorry_label_title), Str_response);  }
            }

            @Override
            public void onErrorListener() {

                dialog.dismiss();
            }
        });

    }


/*
    private void PostRequest1(String Url) {
        dialog = new Dialog(OtpPage.this);
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
                        Log.e("otp", response);

                        System.out.println("otp---------"+response);

                        String Str_status = "",Str_response="",Str_otp_status="",Str_ride_id="",Str_currency="";

                        try {
                            JSONObject object = new JSONObject(response);
                            Str_status = object.getString("status");

                            if (Str_status.equalsIgnoreCase("1")){

                                Str_response = object.getString("response");
                                Str_currency = object.getString("currency");
                            //    Currency currencycode = Currency.getInstance(getLocale(Str_currency));

                                sCurrencySymbol = CurrencySymbolConverter.getCurrencySymbol(Str_currency);

                                Str_otp_status = object.getString("otp_status");
                                Str_otp  = object.getString("otp");
                                Str_ride_id = object.getString("ride_id");
                                Str_amount = sCurrencySymbol + object.getString("amount");

                                System.out.println("otp--------"+Str_otp);

                                System.out.println("Str_otp_status--------"+Str_otp_status);

                            }else{

                                Str_response = object.getString("response");

                            }



                        }catch (Exception e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }
                        dialog.dismiss();


                        if (Str_status.equalsIgnoreCase("1")){

                            if (Str_otp_status.equalsIgnoreCase("development")){
                                Et_otp.setText(Str_otp);
                            }

                        }else{
                            Alert(getResources().getString(R.string.alert_sorry_label_title), Str_response);  }

                    }
                }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                VolleyErrorResponse.VolleyError(OtpPage.this, error);
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
                jsonParams.put("ride_id",Str_rideId);

                System.out.println("otp-------driver_id---------"+driver_id);

                System.out.println("otp-------ride_id---------"+Str_rideId);



                return jsonParams;
            }
        };
        postrequest.setRetryPolicy(new DefaultRetryPolicy(30000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        postrequest.setShouldCache(false);

        AppController.getInstance().addToRequestQueue(postrequest);
    }
*/



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




}
