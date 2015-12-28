package com.app.service;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;
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
import com.app.dao.ServiceResponse;

import java.util.HashMap;
import java.util.Map;

/**
 */
public class ServiceManager {
    private static final String TAG = "ServiceManager TAG";
    private RequestQueue mRequestQueue;
    private Context context;
    private ServiceListener mServiceListener;
    private StringRequest stringRequest;
    private ObjectManager manager;
    public interface ServiceListener {
        void onCompleteListener(Object object);
        void onErrorListener(Object error);
    }

    public ServiceManager(Context context, ServiceListener listener) {
        this.context = context;
        this.mServiceListener = listener;
        init();
    }

    private void init() {
        manager = new ObjectManager();
    }

    public void makeServiceRequest(final String url, int method, final HashMap<String, String> param) {
        stringRequest = new StringRequest(method, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                System.out.println("----------response------------------"+response);
                try {
                    Object obj = manager.getObjectForUrl(url, response);
                    if (obj instanceof ServiceResponse) {
                        ServiceResponse mResponse = (ServiceResponse) obj;
                        if ("1".equalsIgnoreCase(mResponse.getStatus())) {
                            mServiceListener.onCompleteListener(obj);
                        } else {
                            if (obj instanceof ServiceResponse) {
                                ServiceResponse sr = (ServiceResponse) obj;
                            }
                            mServiceListener.onErrorListener(obj);
                        }
                    } else {
                        mServiceListener.onCompleteListener(obj);
                    }
                }catch (Exception e){
                    Toast.makeText(context, "Unknown error occurred", Toast.LENGTH_LONG).show();
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
                        Toast.makeText(context, "No Internet Connection", Toast.LENGTH_LONG).show();
                    } else if (error instanceof ParseError) {
                        Toast.makeText(context, "ParseError", Toast.LENGTH_LONG).show();
                    }
                }catch (Exception e){
                }
                mServiceListener.onErrorListener(error);
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
                headers.put("isapplication",ServiceConstant.isapplication);
                headers.put("applanguage",ServiceConstant.applanguage);
                System.out.println("isapplication------------" + ServiceConstant.isapplication);
                System.out.println("applanguage------------"+ServiceConstant.applanguage);
                return headers;
                }
        };
        stringRequest.setRetryPolicy(new DefaultRetryPolicy(5000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        getRequestQueue().add(stringRequest);
    }
    public RequestQueue getRequestQueue() {
        if (mRequestQueue == null) {
            mRequestQueue = Volley.newRequestQueue(context);
        }
        return mRequestQueue;
    }

    public <T> void addToRequestQueue(Request<T> req, String tag) {
        req.setTag(TextUtils.isEmpty(tag) ? TAG : tag);
        getRequestQueue().add(req);
    }

    public <T> void addToRequestQueue(Request<T> req) {
        req.setTag(TAG);
        getRequestQueue().add(req);
    }

    public void cancelPendingRequests(Object tag) {
        if (mRequestQueue != null) {
            mRequestQueue.cancelAll(tag);
        }
    }
}
