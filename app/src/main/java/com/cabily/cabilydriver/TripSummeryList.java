package com.cabily.cabilydriver;

import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.InflateException;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.Hockeyapp.FragmentHockeyApp;
import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.app.service.ServiceConstant;
import com.app.service.ServiceRequest;
import com.cabily.cabilydriver.Pojo.TripSummaryPojo;
import com.cabily.cabilydriver.Utils.AppController;
import com.cabily.cabilydriver.Utils.ConnectionDetector;
import com.cabily.cabilydriver.Utils.SessionManager;
import com.cabily.cabilydriver.Utils.VolleyErrorResponse;
import com.cabily.cabilydriver.adapter.TripSummeryAdapter;
import com.cabily.cabilydriver.widgets.PkDialog;
import com.special.ResideMenu.ResideMenu;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import me.drakeet.materialdialog.MaterialDialog;

/**
 * Created by user88 on 10/23/2015.
 */
public class TripSummeryList extends FragmentHockeyApp {
    private static View rootview;
    private Context context;
    private View parentView;
    private ResideMenu resideMenu;
    private StringRequest postrequest;
    private SessionManager session;
    private String driver_id = "";
    private String check = null;
    private LinearLayout all_layout, onride_layout, complete_layout;
    private TextView Tv_all, Tv_onride, Tv_complete;
    private ListView listview;
    private ActionBar actionBar;
    private TextView empty_Tv;
    Dialog dialog;
    private ArrayList<TripSummaryPojo> tripsummaryListall;
    private ArrayList<TripSummaryPojo> tripsummaryListonride;
    private ArrayList<TripSummaryPojo> tripsummaryListcompleted;
    private TripSummeryAdapter adapter;
    private TextView no_trip_summary;

    private Boolean isInternetPresent = false;
    private ConnectionDetector cd;
    private boolean show_progress_status = false;

    private ServiceRequest mRequest;

    BroadcastReceiver receiver;

    @Override
    public View onCreateView(final LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (rootview != null) {
            ViewGroup parent = (ViewGroup) rootview.getParent();
            if (parent != null)
                parent.removeView(rootview);
        }
        try {
            rootview = inflater.inflate(R.layout.trip_summary, container, false);
        } catch (InflateException e) {
        }

        ActionBarActivity actionBarActivity = (ActionBarActivity) getActivity();
        actionBar = actionBarActivity.getSupportActionBar();
        actionBar.setDisplayShowTitleEnabled(false);
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setHomeButtonEnabled(true);
        actionBar.hide();
        context = getActivity();
        initialize(rootview);

        //Code for broadcat receive
        IntentFilter filter = new IntentFilter();
        filter.addAction("com.finish.tripsummerylist");

        receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (intent.getAction().equals("com.finish.tripsummerylist")) {
                    getActivity().finish();
                }
            }
        };
        getActivity().registerReceiver(receiver, filter);


        rootview.findViewById(R.id.ham_home).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (resideMenu != null) {
                    resideMenu.openMenu(ResideMenu.DIRECTION_LEFT);
                }
            }
        });
        listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(getActivity(), TripSummaryDetail.class);
                intent.putExtra("ride_id", tripsummaryListall.get(position).getride_id());
                startActivity(intent);
                getActivity().overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
            }
        });


        all_layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                all_layout.setBackgroundColor(0xFF00B0FF);
                onride_layout.setBackgroundColor(0xFFFFFFFF);
                complete_layout.setBackgroundColor(0xFFFFFFFF);
                Tv_all.setTextColor(0xFFFFFFFF);
                Tv_onride.setTextColor(0xFF00B0FF);
                Tv_complete.setTextColor(0xFF00B0FF);
                adapter = new TripSummeryAdapter(getActivity(), tripsummaryListall);
                listview.setAdapter(adapter);
                adapter.notifyDataSetChanged();
                if (tripsummaryListall.size() > 0) {
                    empty_Tv.setVisibility(View.GONE);
                } else {
                    empty_Tv.setVisibility(View.VISIBLE);
                    listview.setEmptyView(empty_Tv);
                }
            }
        });

        onride_layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                all_layout.setBackgroundColor(0xFFFFFFFF);
                onride_layout.setBackgroundColor(0xFF00B0FF);
                complete_layout.setBackgroundColor(0xFFFFFFFF);
                Tv_all.setTextColor(0xFF00B0FF);
                Tv_complete.setTextColor(0xFF00B0FF);
                Tv_onride.setTextColor(0xFFFFFFFF);
                adapter = new TripSummeryAdapter(getActivity(), tripsummaryListonride);
                listview.setAdapter(adapter);
                adapter.notifyDataSetChanged();
                if (tripsummaryListonride.size() > 0) {
                    empty_Tv.setVisibility(View.GONE);
                } else {
                    empty_Tv.setVisibility(View.VISIBLE);
                    listview.setEmptyView(empty_Tv);
                }
            }
        });
        complete_layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                all_layout.setBackgroundColor(0xFFFFFFFF);
                onride_layout.setBackgroundColor(0xFFFFFFFF);
                complete_layout.setBackgroundColor(0xFF00B0FF);
                Tv_all.setTextColor(0xFF00B0FF);
                Tv_onride.setTextColor(0xFF00B0FF);
                Tv_complete.setTextColor(0xFFFFFFFF);
                adapter = new TripSummeryAdapter(getActivity(), tripsummaryListcompleted);
                listview.setAdapter(adapter);
                adapter.notifyDataSetChanged();
                if (tripsummaryListcompleted.size() > 0) {
                    empty_Tv.setVisibility(View.GONE);
                } else {
                    empty_Tv.setVisibility(View.VISIBLE);
                    listview.setEmptyView(empty_Tv);
                }
            }
        });
        setUpViews();
        return rootview;
    }

    private void setUpViews() {
        NavigationDrawer parentActivity = (NavigationDrawer) getActivity();
        resideMenu = parentActivity.getResideMenu();
    }


    private void initialize(View rootview) {
        tripsummaryListall = new ArrayList<TripSummaryPojo>();
        tripsummaryListonride = new ArrayList<TripSummaryPojo>();
        tripsummaryListcompleted = new ArrayList<TripSummaryPojo>();
        session = new SessionManager(getActivity());
        all_layout = (LinearLayout) rootview.findViewById(R.id.trip_summary_all_layout);
        onride_layout = (LinearLayout) rootview.findViewById(R.id.trip_summary_onride_layout);
        complete_layout = (LinearLayout) rootview.findViewById(R.id.trip_summary_completed_layout);
        listview = (ListView) rootview.findViewById(R.id.trip_summary_listview);
        no_trip_summary = (TextView) rootview.findViewById(R.id.no_trip_summary);
        Tv_all = (TextView) rootview.findViewById(R.id.trip_summary_all);
        Tv_onride = (TextView) rootview.findViewById(R.id.trip_summary_onride);
        Tv_complete = (TextView) rootview.findViewById(R.id.trip_summary_completed_button);
        empty_Tv = (TextView) rootview.findViewById(R.id.no_trip_summary);
        HashMap<String, String> user = session.getUserDetails();
        driver_id = user.get(SessionManager.KEY_DRIVERID);
        all_layout.setBackgroundColor(0xFF00B0FF);
        Tv_all.setTextColor(0xFFFFFFFF);
        cd = new ConnectionDetector(getActivity());
        isInternetPresent = cd.isConnectingToInternet();
        if (isInternetPresent) {
            PostRequest(ServiceConstant.tripsummery_list_url);
            System.out.println("triplists------------------" + ServiceConstant.tripsummery_list_url);
        } else {
            Alert(getResources().getString(R.string.alert_sorry_label_title), getResources().getString(R.string.alert_nointernet));

        }
    }

    //--------------Alert Method-----------
    private void Alert(String title, String message) {
        final PkDialog mDialog = new PkDialog(getActivity());
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

    //-----------------------Code for my rides post request-----------------

    private void PostRequest(String Url) {
        dialog = new Dialog(getActivity());
        dialog.getWindow();
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.custom_loading);
        dialog.setCanceledOnTouchOutside(false);
        dialog.show();

        TextView dialog_title = (TextView) dialog.findViewById(R.id.custom_loading_textview);
        dialog_title.setText(getResources().getString(R.string.action_loading));

        System.out.println("-------------triplist----------------" + Url);

        HashMap<String, String> jsonParams = new HashMap<String, String>();
        jsonParams.put("driver_id", driver_id);
        jsonParams.put("trip_type", "all");

        mRequest = new ServiceRequest(getActivity());
        mRequest.makeServiceRequest(Url, Request.Method.POST, jsonParams, new ServiceRequest.ServiceListener() {
            @Override
            public void onCompleteListener(String response) {
                System.out.println("--------------reponse-------------------" + response);
                Log.e("trip", response);
                String status = "", total_rides = "", type_group = "",Str_response="";

                try {
                    JSONObject object = new JSONObject(response);
                    status = object.getString("status");

                    System.out.println("triplist--status----"+status);

                    if (status.equalsIgnoreCase("1")){

                        JSONObject jsonObject = object.getJSONObject("response");

                        total_rides = jsonObject.getString("total_rides");
                        JSONArray jarry = jsonObject.getJSONArray("rides");

                        if (jarry.length() > 0) {

                            for (int i = 0; i < jarry.length(); i++) {
                                JSONObject jobjct = jarry.getJSONObject(i);
                                TripSummaryPojo items = new TripSummaryPojo();
                                items.setpickup(jobjct.getString("pickup"));
                                items.setdatetime(jobjct.getString("datetime"));
                                items.setride_id(jobjct.getString("ride_id"));
                                tripsummaryListall.add(items);

                                if (jobjct.getString("group").equalsIgnoreCase("completed")) {
                                    tripsummaryListcompleted.add(items);
                                } else if (jobjct.getString("group").equalsIgnoreCase("onride")) {
                                    tripsummaryListonride.add(items);
                                }

                            }
                            show_progress_status = true;
                        } else {
                            show_progress_status = false;
                        }
                    }else{

                        Str_response = object.getString("response");

                        System.out.println("triplist----------response---"+Str_response);

                    }

                } catch (Exception e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                dialog.dismiss();

                if (status.equalsIgnoreCase("1")){
                    adapter = new TripSummeryAdapter(getActivity(), tripsummaryListall);
                    listview.setAdapter(adapter);

                    if (show_progress_status) {
                        empty_Tv.setVisibility(View.GONE);
                    } else {
                        empty_Tv.setVisibility(View.VISIBLE);
                        listview.setEmptyView(empty_Tv);
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

     /*       private void PostRequest1(String Url) {

        dialog = new Dialog(getActivity());
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

                        System.out.println("--------------reponse-------------------" + response);

                        Log.e("trip", response);

                        String status = "", total_rides = "", type_group = "",Str_response="";

                        try {
                            JSONObject object = new JSONObject(response);
                            status = object.getString("status");

                            System.out.println("triplist--status----"+status);

                            if (status.equalsIgnoreCase("1")){

                                JSONObject jsonObject = object.getJSONObject("response");

                                total_rides = jsonObject.getString("total_rides");
                                JSONArray jarry = jsonObject.getJSONArray("rides");

                                if (jarry.length() > 0) {

                                    for (int i = 0; i < jarry.length(); i++) {
                                        JSONObject jobjct = jarry.getJSONObject(i);
                                        TripSummaryPojo items = new TripSummaryPojo();
                                        items.setpickup(jobjct.getString("pickup"));
                                        items.setdatetime(jobjct.getString("datetime"));
                                        items.setride_id(jobjct.getString("ride_id"));
                                        tripsummaryListall.add(items);

                                        if (jobjct.getString("group").equalsIgnoreCase("completed")) {
                                            tripsummaryListcompleted.add(items);
                                        } else if (jobjct.getString("group").equalsIgnoreCase("onride")) {
                                            tripsummaryListonride.add(items);
                                        }

                                    }
                                    show_progress_status = true;
                                } else {
                                    show_progress_status = false;
                                }
                            }else{

                                Str_response = object.getString("response");

                                System.out.println("triplist----------response---"+Str_response);

                            }

                        } catch (Exception e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }
                        dialog.dismiss();

                        if (status.equalsIgnoreCase("1")){
                            adapter = new TripSummeryAdapter(getActivity(), tripsummaryListall);
                            listview.setAdapter(adapter);

                            if (show_progress_status) {
                                empty_Tv.setVisibility(View.GONE);
                            } else {
                                empty_Tv.setVisibility(View.VISIBLE);
                                listview.setEmptyView(empty_Tv);
                            }
                        }else{
                            Alert(getResources().getString(R.string.alert_sorry_label_title),Str_response);
                        }
                    }
                }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                VolleyErrorResponse.VolleyError(getActivity(), error);
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
                jsonParams.put("trip_type", "all");

                return jsonParams;
            }
        };
        AppController.getInstance().addToRequestQueue(postrequest);
    }*/


}
