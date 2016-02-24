package com.app.service;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.NetworkError;
import com.android.volley.NoConnectionError;
import com.android.volley.ParseError;
import com.android.volley.Response;
import com.android.volley.ServerError;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.cabily.cabilydriver.HomePage;
import com.cabily.cabilydriver.R;
import com.cabily.cabilydriver.Utils.AppController;
import com.cabily.cabilydriver.Utils.SessionManager;
import com.cabily.cabilydriver.widgets.PkDialog;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Prem Kumar and Anitha on 11/26/2015.
 */
public class ServiceRequest {
    private Context context;
    private ServiceListener mServiceListener;
    private StringRequest stringRequest;
    private SessionManager sessionManager;
    private String userID = "", gcmID = "";

    public interface ServiceListener {
        void onCompleteListener(String response);

        void onErrorListener();
    }

    public ServiceRequest(Context context) {
        this.context = context;
        sessionManager = new SessionManager(context);

        HashMap<String, String> user = sessionManager.getUserDetails();
        userID = user.get(SessionManager.KEY_DRIVERID);
        gcmID = user.get(SessionManager.KEY_GCM_ID);

        Log.d("DRIVERID@@@@@@@", user.get(SessionManager.KEY_DRIVERID));
        Log.d("GCMID", user.get(SessionManager.KEY_GCM_ID));
    }

    public void cancelRequest() {
        if (stringRequest != null) {
            stringRequest.cancel();
        }
    }

    public void makeServiceRequest(final String url, int method, final HashMap<String, String> param, ServiceListener listener) {

        this.mServiceListener = listener;

        stringRequest = new StringRequest(method, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    mServiceListener.onCompleteListener(response);

                    JSONObject object = new JSONObject(response);
                    if (object.has("is_dead")) {
                        System.out.println("-----------is dead----------------");
                        final PkDialog mDialog = new PkDialog(context);
                        mDialog.setDialogTitle(context.getResources().getString(R.string.action_session_expired_title));
                        mDialog.setDialogMessage(context.getResources().getString(R.string.action_session_expired_message));
                        mDialog.setPositiveButton(context.getResources().getString(R.string.lbel_notification_ok), new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                mDialog.dismiss();
                                sessionManager.logoutUser();
                                Intent intent = new Intent(context, HomePage.class);
                                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                context.startActivity(intent);
                            }
                        });
                        mDialog.show();

                    }

                } catch (Exception e) {
                }
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
                        Toast.makeText(context, "NetworkError", Toast.LENGTH_LONG).show();
                    } else if (error instanceof ParseError) {
                        Toast.makeText(context, "ParseError", Toast.LENGTH_LONG).show();
                    }
                } catch (Exception e) {
                }
                mServiceListener.onErrorListener();
            }
        }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                return param;
            }

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {

                Map<String, String> headers = new HashMap<String, String>();
                headers.put("User-agent", ServiceConstant.useragent);
                // headers.put("isapplication", ServiceConstant.isapplication);
                //headers.put("applanguage", ServiceConstant.applanguage);
                headers.put("apptype", ServiceConstant.cabily_AppType);
                headers.put("driverid", userID);
                headers.put("apptoken", gcmID);


                return headers;
            }
        };

        //to avoid repeat request Multiple Time
        DefaultRetryPolicy retryPolicy = new DefaultRetryPolicy(0, -1, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
        stringRequest.setRetryPolicy(retryPolicy);
        stringRequest.setRetryPolicy(new DefaultRetryPolicy(30000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        stringRequest.setShouldCache(false);
        AppController.getInstance().addToRequestQueue(stringRequest);
    }


}
