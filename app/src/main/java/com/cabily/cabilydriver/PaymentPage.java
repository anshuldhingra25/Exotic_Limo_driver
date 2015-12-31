package com.cabily.cabilydriver;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
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
 * Created by user88 on 11/4/2015.
 */
public class PaymentPage extends SubclassActivity {

    private String driver_id = "";
    private Boolean isInternetPresent = false;
    private ConnectionDetector cd;
    private Context context;
    private SessionManager session;

    public static String EXTRA = "EXTRA";
    private TextView Tv_amount;
    private Button Bt_receivecash;
    private String Str_amount = "", Str_rideid = "",Str_currencycode="";
    Dialog dialog;
    StringRequest postrequest;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.receive_cash);
        initialize();
        //Starting Xmpp service
        ChatingService.startDriverAction(PaymentPage.this);

        Bt_receivecash.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cd = new ConnectionDetector(PaymentPage.this);
                isInternetPresent = cd.isConnectingToInternet();
                if (isInternetPresent) {
                    PostRequest(ServiceConstant.receivedbill_amounr_cash_url);
                    System.out.println("end------------------" + ServiceConstant.receivedbill_amounr_cash_url);
                } else {
                    Alert(getResources().getString(R.string.alert_sorry_label_title), getResources().getString(R.string.alert_nointernet));
                }
                Toast.makeText(getApplicationContext(), getResources().getString(R.string.waitfortransaction_label_title), Toast.LENGTH_SHORT).show();
            }
        });

    }

    private void initialize() {
        session = new SessionManager(PaymentPage.this);
        // get user data from session
        HashMap<String, String> user = session.getUserDetails();
        driver_id = user.get(SessionManager.KEY_DRIVERID);
        Intent i = getIntent();
        Str_amount = i.getStringExtra("amount");
        Str_rideid = i.getStringExtra("rideid");
        Str_currencycode = i.getStringExtra("CurrencyCode");

        System.out.println("rideid---paymneinituliz-------------" + Str_rideid);


        System.out.println("amount-------------" + Str_amount);
        Tv_amount = (TextView) findViewById(R.id.Receive_cash_amount);
        Bt_receivecash = (Button) findViewById(R.id.Receive_cash_receive_btn);
        Tv_amount.setText(Str_amount);

    }


    //--------------Alert Method------------------
    private void Alert(String title, String alert) {
        final MaterialDialog dialogs = new MaterialDialog(PaymentPage.this);
        dialogs.setTitle(title)
                .setMessage(alert)
                .setPositiveButton(
                        "OK", new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                dialogs.dismiss();
                            }
                        }
                )
                .show();
    }

    //-----------------------Code for begin trip post request-----------------
    private void PostRequest(String Url) {
        dialog = new Dialog(PaymentPage.this);
        dialog.getWindow();
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.custom_loading);
        dialog.setCanceledOnTouchOutside(false);
        dialog.show();

        TextView dialog_title = (TextView) dialog.findViewById(R.id.custom_loading_textview);
        dialog_title.setText(getResources().getString(R.string.action_loading));


        postrequest = new StringRequest(Request.Method.POST, Url,
                new Response.Listener<String>() {

                    @Override
                    public void onResponse(String response) {
                        Log.e("recev", response);

                        System.out.println("responsepayment---------" + response);

                        String Str_status = "", Str_response = "";

                        try {
                            JSONObject object = new JSONObject(response);
                            Str_status = object.getString("status");
                            Str_response = object.getString("response");

                            System.out.println("response----------" + object.getString("response"));

                            System.out.println("status----------" + object.getString("status"));

                            if (Str_status.equalsIgnoreCase("1")){

                                final PkDialog mdialog = new PkDialog(PaymentPage.this);
                                mdialog.setDialogTitle(getResources().getString(R.string.action_loading_sucess));
                                mdialog.setDialogMessage(Str_response);
                                mdialog.setPositiveButton(
                                        "OK", new View.OnClickListener() {
                                            @Override
                                            public void onClick(View v) {
                                                mdialog.dismiss();

                                                Intent broadcastIntent_otp = new Intent();
                                                broadcastIntent_otp.setAction("com.finish.OtpPage");
                                                sendBroadcast(broadcastIntent_otp);

                                                Intent broadcastIntent = new Intent();
                                                broadcastIntent.setAction("com.finish.EndTrip");
                                                sendBroadcast(broadcastIntent);
                                                finish();

                                                Intent intent = new Intent(PaymentPage.this, RatingsPage.class);
                                                intent.putExtra("rideid", Str_rideid);
                                                startActivity(intent);
                                                //onBackPressed();
                                            }
                                        }
                                );
                                mdialog.show();
                            }else {
                                final PkDialog mdialog = new PkDialog(PaymentPage.this);
                                mdialog.setDialogTitle(getResources().getString(R.string.alert_sorry_label_title));
                                mdialog.setDialogMessage(Str_response);
                                mdialog.setCancelOnTouchOutside(false);
                                mdialog.setPositiveButton(getResources().getString(R.string.alert_label_ok), new View.OnClickListener() {
                                            @Override
                                            public void onClick(View v) {
                                                mdialog.dismiss();
                                            }
                                        }
                                );
                                mdialog.show();

                            }

                        } catch (Exception e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }
                        dialog.dismiss();


                    }
                }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                VolleyErrorResponse.VolleyError(PaymentPage.this, error);

                dialog.dismiss();
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
                jsonParams.put("amount",Str_amount);

                System.out
                        .println("--------------postdriver_id-------------------"
                                + driver_id);

                System.out
                        .println("--------------posstdriver_id-------------------"
                                + Str_amount);

                System.out
                        .println("--------------postrideid-------------------"
                                + Str_rideid);


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
