package com.cabily.cabilydriver;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.Hockeyapp.ActivityHockeyApp;
import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.app.service.ServiceConstant;
import com.app.service.ServiceRequest;
import com.cabily.cabilydriver.Pojo.CancelReasonPojo;
import com.cabily.cabilydriver.Utils.AppController;
import com.cabily.cabilydriver.Utils.ConnectionDetector;
import com.cabily.cabilydriver.Utils.SessionManager;
import com.cabily.cabilydriver.Utils.VolleyErrorResponse;
import com.cabily.cabilydriver.adapter.CancelReasonAdapter;
import com.cabily.cabilydriver.widgets.PkDialog;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.sql.SQLOutput;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.jar.JarException;

import me.drakeet.materialdialog.MaterialDialog;

/**
 * Created by user88 on 10/28/2015.
 */
public class CancelTrip extends ActivityHockeyApp {

    private Boolean isInternetPresent = false;
    private ConnectionDetector cd;
    private Context context;
    private SessionManager session;
    private ListView cancel_listview;
    private ArrayAdapter<String> listAdapter;
    private ArrayList<CancelReasonPojo> Cancelreason_arraylist;
    private CancelReasonAdapter adapter;
    private StringRequest canceltrip_postrequest;
    private Dialog dialog;
    private String driver_id;
    private TextView Tv_Emtytxt;
    private boolean show_progress_status = false;
    private String Str_rideId;

    private ServiceRequest mRequest;

    private RelativeLayout Rl_layout_cancel_back;

    private String Str_reason = "";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ridecancel_reasons_dialog);
        initialize();

        Rl_layout_cancel_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
                overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
            }
        });

        cancel_listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Str_reason = Cancelreason_arraylist.get(position).getCancelreason_id();
                System.out.println("reasonm-----------" + Cancelreason_arraylist.get(position).getCancelreason_id());
                cancelTripAlert();
            }
        });

    }

    private void initialize() {
        session = new SessionManager(CancelTrip.this);
        // get user data from session
        HashMap<String, String> user = session.getUserDetails();
        driver_id = user.get(SessionManager.KEY_DRIVERID);
        Cancelreason_arraylist = new ArrayList<CancelReasonPojo>();
        cancel_listview = (ListView) findViewById(R.id.cancelreason_listView);
        Tv_Emtytxt = (TextView) findViewById(R.id.emtpy_cancelreason);
        Rl_layout_cancel_back = (RelativeLayout) findViewById(R.id.layouts_cancel_reasons);

        Intent i = getIntent();
        Str_rideId = i.getStringExtra("RideId");


        cd = new ConnectionDetector(CancelTrip.this);
        isInternetPresent = cd.isConnectingToInternet();

        if (isInternetPresent) {
            postRequest_Cancelreason(ServiceConstant.ridecancel_reason_url);
        } else {
            Alert(getResources().getString(R.string.alert_sorry_label_title), getResources().getString(R.string.alert_nointernet));
        }
    }

    //--------------Alert Method-----------
    private void Alert(String title, String message) {
        final PkDialog mDialog = new PkDialog(CancelTrip.this);
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

    //--------------------code for cancel reason diaolg--------------------
    public void cancelTripAlert() {
        ConnectionDetector cd = new ConnectionDetector(CancelTrip.this);
        final boolean isInternetPresent = cd.isConnectingToInternet();
        final PkDialog mDialog = new PkDialog(CancelTrip.this);
        mDialog.setDialogTitle(getResources().getString(R.string.confirmdelete));
        mDialog.setDialogMessage(getResources().getString(R.string.surewanttodelete));

        mDialog.setPositiveButton(getResources().getString(R.string.label_yes), new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isInternetPresent) {
                    postRequest_Cancelride(ServiceConstant.ridecancel_url);
                } else {
                    Alert(getResources().getString(R.string.alert_sorry_label_title), getResources().getString(R.string.alert_nointernet));
                }
                mDialog.dismiss();
            }
        });

        mDialog.setNegativeButton(getResources().getString(R.string.label_no), new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mDialog.dismiss();

            }
        });

        mDialog.show();

    }


    //---------------------code for cancel ride-----------------
    private void postRequest_Cancelreason(String Url) {
        dialog = new Dialog(CancelTrip.this);
        dialog.getWindow();
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.custom_loading);
        dialog.setCanceledOnTouchOutside(false);
        dialog.show();

        TextView dialog_title = (TextView) dialog.findViewById(R.id.custom_loading_textview);
        dialog_title.setText(getResources().getString(R.string.action_loading));

        System.out.println("-------------cancel----------------" + Url);

        HashMap<String, String> jsonParams = new HashMap<String, String>();
        jsonParams.put("driver_id", driver_id);

        System.out.println("driver_id-------------" + driver_id);

        mRequest = new ServiceRequest(CancelTrip.this);
        mRequest.makeServiceRequest(Url, Request.Method.POST, jsonParams, new ServiceRequest.ServiceListener() {

            @Override
            public void onCompleteListener(String response) {
                System.out.println("-------------cancelreason Response----------------" + response);
                String Sstatus = "",Str_response="";
                try {

                    JSONObject jobject = new JSONObject(response);
                    Sstatus = jobject.getString("status");

                    if (Sstatus.equalsIgnoreCase("1")){
                        JSONObject object = jobject.getJSONObject("response");
                        JSONArray jarry = object.getJSONArray("reason");

                        if (jarry.length() > 0) {
                            for (int i = 0; i < jarry.length(); i++) {

                                JSONObject object1 = jarry.getJSONObject(i);

                                CancelReasonPojo items = new CancelReasonPojo();

                                items.setReason(object1.getString("reason"));
                                items.setCancelreason_id(object1.getString("id"));

                                System.out.println("reason----------" + object1.getString("reason"));

                                Cancelreason_arraylist.add(items);

                            }
                            show_progress_status = true;

                        } else {
                            show_progress_status = false;
                        }
                    }else{
                        Str_response = jobject.getString("response");
                    }

                } catch (JSONException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                dialog.dismiss();

                if (Sstatus.equalsIgnoreCase("1")){
                    System.out.println("secnd-----------" + Cancelreason_arraylist.get(0).getReason());
                    adapter = new CancelReasonAdapter(CancelTrip.this, Cancelreason_arraylist);
                    cancel_listview.setAdapter(adapter);

                    if (show_progress_status) {
                        Tv_Emtytxt.setVisibility(View.GONE);
                    } else {
                        Tv_Emtytxt.setVisibility(View.VISIBLE);
                        cancel_listview.setEmptyView(Tv_Emtytxt);
                    }
                }else{

                    Alert(getResources().getString(R.string.alert_sorry_label_title),Str_response);
                }

            }

            @Override
            public void onErrorListener() {

                dialog.dismiss();
            }

        });

    }


/*
            private void postRequest_Cancelreason1(String Url) {
        dialog = new Dialog(CancelTrip.this);
        dialog.getWindow();
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.custom_loading_cancel);
        dialog.setCanceledOnTouchOutside(false);
        dialog.show();

        TextView dialog_title = (TextView) dialog.findViewById(R.id.custom_loading_textview_cancel);
        dialog_title.setText(getResources().getString(R.string.action_loading_cancel));

        System.out.println("-------------cancel Url----------------" + Url);

        canceltrip_postrequest = new StringRequest(Request.Method.POST, Url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {

                        System.out.println("-------------cancelreason Response----------------" + response);

                        String Sstatus = "",Str_response="";
                        try {

                            JSONObject jobject = new JSONObject(response);
                            Sstatus = jobject.getString("status");

                            if (Sstatus.equalsIgnoreCase("1")){
                                JSONObject object = jobject.getJSONObject("response");
                                JSONArray jarry = object.getJSONArray("reason");

                                if (jarry.length() > 0) {
                                    for (int i = 0; i < jarry.length(); i++) {

                                        JSONObject object1 = jarry.getJSONObject(i);

                                        CancelReasonPojo items = new CancelReasonPojo();

                                        items.setReason(object1.getString("reason"));
                                        items.setCancelreason_id(object1.getString("id"));

                                        System.out.println("reason----------" + object1.getString("reason"));

                                        Cancelreason_arraylist.add(items);

                                    }
                                    show_progress_status = true;

                                } else {
                                    show_progress_status = false;
                                }
                            }else{
                                Str_response = jobject.getString("response");
                            }

                        } catch (JSONException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }
                        dialog.dismiss();

                        if (Sstatus.equalsIgnoreCase("1")){
                            System.out.println("secnd-----------" + Cancelreason_arraylist.get(0).getReason());
                            adapter = new CancelReasonAdapter(CancelTrip.this, Cancelreason_arraylist);
                            cancel_listview.setAdapter(adapter);

                            if (show_progress_status) {
                                Tv_Emtytxt.setVisibility(View.GONE);
                            } else {
                                Tv_Emtytxt.setVisibility(View.VISIBLE);
                                cancel_listview.setEmptyView(Tv_Emtytxt);
                            }
                        }else{

                            Alert(getResources().getString(R.string.alert_sorry_label_title),Str_response);
                        }
                    }
                }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                dialog.dismiss();
                VolleyErrorResponse.VolleyError(CancelTrip.this, error);
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

                System.out.println("driver_id-------------" + driver_id);

                return jsonParams;
            }
        };
        canceltrip_postrequest.setRetryPolicy(new DefaultRetryPolicy(20000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        canceltrip_postrequest.setShouldCache(false);
        AppController.getInstance().addToRequestQueue(canceltrip_postrequest);
    }
*/

    //---------------------code for cancel ride-----------------
    private void postRequest_Cancelride(String Url) {
        dialog = new Dialog(CancelTrip.this);
        dialog.getWindow();
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.custom_loading);
        dialog.setCanceledOnTouchOutside(false);
        dialog.show();

        TextView dialog_title = (TextView) dialog.findViewById(R.id.custom_loading_textview);
        dialog_title.setText(getResources().getString(R.string.action_loading));

        System.out.println("-------------cancelling----------------" + Url);

        HashMap<String, String> jsonParams = new HashMap<String, String>();
        jsonParams.put("driver_id", driver_id);
        jsonParams.put("ride_id", Str_rideId);
        jsonParams.put("reason", Str_reason);

        System.out.println("ride_id-------------" + Str_rideId);
        System.out.println("driver_id-------------" + driver_id);
        System.out.println("reason-------------" + Str_reason);

        mRequest = new ServiceRequest(CancelTrip.this);
        mRequest.makeServiceRequest(Url, Request.Method.POST, jsonParams, new ServiceRequest.ServiceListener() {

            @Override
            public void onCompleteListener(String response) {
                System.out.println("------------- Response----------------" + response);
                String Str_status = "", Str_message = "", Str_Id = "";
                try {
                    JSONObject object = new JSONObject(response);
                    Str_status = object.getString("status");
                    JSONObject jobject = object.getJSONObject("response");
                    Str_message = jobject.getString("message");
                    Str_Id = jobject.getString("ride_id");

                } catch (JSONException e) {
                    e.printStackTrace();
                }
                if (Str_status.equalsIgnoreCase("1")) {
                    final PkDialog mdialog = new PkDialog(CancelTrip.this);
                    mdialog.setDialogTitle(getResources().getString(R.string.action_loading_sucess));
                    mdialog.setDialogMessage(Str_message);
                    mdialog.setPositiveButton(getResources().getString(R.string.lbel_notification_ok), new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    mdialog.dismiss();

                                    Intent broadcastIntent = new Intent();
                                    broadcastIntent.setAction("com.finish.ArrivedTrip");
                                    sendBroadcast(broadcastIntent);

                                    Intent broadcastIntent_userinfo = new Intent();
                                    broadcastIntent_userinfo.setAction("com.finish.UserInfo");
                                    sendBroadcast(broadcastIntent_userinfo);

                                    Intent broadcastIntent_tripdetail = new Intent();
                                    broadcastIntent_tripdetail.setAction("com.finish.tripsummerydetail");
                                    sendBroadcast(broadcastIntent_tripdetail);

                                    Intent broadcastIntent_drivermap = new Intent();
                                    broadcastIntent_drivermap.setAction("com.finish.canceltrip.DriverMapActivity");
                                    sendBroadcast(broadcastIntent_drivermap);

                                    finish();
                                    onBackPressed();
                                }
                            }
                    );
                    mdialog.show();

                } else {
                    Alert(getResources().getString(R.string.alert_sorry_label_title),Str_message);
                }
                dialog.dismiss();

            }

            @Override
            public void onErrorListener() {

                dialog.dismiss();
            }

        });

    }


/*            private void postRequest_Cancelride1(String Url) {
        dialog = new Dialog(CancelTrip.this);
        dialog.getWindow();
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.custom_loading_cancel);
        dialog.setCanceledOnTouchOutside(false);
        dialog.show();

        TextView dialog_title = (TextView) dialog.findViewById(R.id.custom_loading_textview_cancel);
        dialog_title.setText(getResources().getString(R.string.action_loading_cancel));

        System.out.println("-------------cancelling----------------" + Url);

        canceltrip_postrequest = new StringRequest(Request.Method.POST, Url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        System.out.println("------------- Response----------------" + response);
                        String Str_status = "", Str_message = "", Str_Id = "";
                        try {
                            JSONObject object = new JSONObject(response);
                            Str_status = object.getString("status");
                            JSONObject jobject = object.getJSONObject("response");
                            Str_message = jobject.getString("message");
                            Str_Id = jobject.getString("ride_id");

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        if (Str_status.equalsIgnoreCase("1")) {
                          final PkDialog mdialog = new PkDialog(CancelTrip.this);
                            mdialog.setDialogTitle(getResources().getString(R.string.action_loading_sucess));
                            mdialog.setDialogMessage(Str_message);
                            mdialog.setPositiveButton(getResources().getString(R.string.lbel_notification_ok), new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                            mdialog.dismiss();

                                            Intent broadcastIntent = new Intent();
                                            broadcastIntent.setAction("com.finish.ArrivedTrip");
                                            sendBroadcast(broadcastIntent);

                                            Intent broadcastIntent_userinfo = new Intent();
                                            broadcastIntent_userinfo.setAction("com.finish.UserInfo");
                                            sendBroadcast(broadcastIntent_userinfo);

                                            Intent broadcastIntent_tripdetail = new Intent();
                                            broadcastIntent_tripdetail.setAction("com.finish.tripsummerydetail");
                                            sendBroadcast(broadcastIntent_tripdetail);

                                            Intent broadcastIntent_drivermap = new Intent();
                                            broadcastIntent_drivermap.setAction("com.finish.canceltrip.DriverMapActivity");
                                            sendBroadcast(broadcastIntent_drivermap);

                                            finish();
                                            onBackPressed();
                                        }
                                    }
                            );
                                    mdialog.show();

                        } else {
                            Alert(getResources().getString(R.string.alert_sorry_label_title),Str_message);
                        }
                        dialog.dismiss();

                    }
                }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                dialog.dismiss();
                VolleyErrorResponse.VolleyError(CancelTrip.this, error);
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
                jsonParams.put("ride_id", Str_rideId);
                jsonParams.put("reason", Str_reason);

                System.out.println("ride_id-------------" + Str_rideId);
                System.out.println("driver_id-------------" + driver_id);
                System.out.println("reason-------------" + Str_reason);

                return jsonParams;
            }
        };
        canceltrip_postrequest.setRetryPolicy(new DefaultRetryPolicy(20000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        canceltrip_postrequest.setShouldCache(false);
        AppController.getInstance().addToRequestQueue(canceltrip_postrequest);
    }*/


}
