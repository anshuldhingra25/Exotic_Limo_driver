package com.cabily.cabilydriver;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ListView;
import android.widget.RatingBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.Hockeyapp.ActivityHockeyApp;
import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.app.service.ServiceConstant;
import com.cabily.cabilydriver.Pojo.Reviwes_Pojo;
import com.cabily.cabilydriver.Utils.AppController;
import com.cabily.cabilydriver.Utils.ConnectionDetector;
import com.cabily.cabilydriver.Utils.SessionManager;
import com.cabily.cabilydriver.Utils.VolleyErrorResponse;
import com.cabily.cabilydriver.adapter.Reviwes_adapter;
import com.cabily.cabilydriver.widgets.PkDialog;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import me.drakeet.materialdialog.MaterialDialog;

/**
 * Created by user88 on 11/4/2015.
 */
public class RatingsPage extends ActivityHockeyApp
{
    private String driver_id = "";
    private Boolean isInternetPresent = false;
    private ConnectionDetector cd;
    private SessionManager session;
    StringRequest postrequest;
    private TextView empty_Tv;
    Reviwes_adapter adapter;
    private ArrayList<Reviwes_Pojo>reivweslist;
    private boolean show_progress_status=false;
    Dialog dialog;

    private TextView Tv_skip;
    ListView listview;
    private String Str_rideid="";

    Button Bt_rate_rider;

    private RelativeLayout Rl_layout_rating;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.reviwes_list);
        initialize();

        Tv_skip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent broadcastIntent_tripdetail = new Intent();
                broadcastIntent_tripdetail.setAction("com.finish.endtripenterdetail");
                sendBroadcast(broadcastIntent_tripdetail);

                Intent broadcastIntent_drivermap = new Intent();
                broadcastIntent_drivermap.setAction("com.finish.canceltrip.DriverMapActivity");
                sendBroadcast(broadcastIntent_drivermap);
                finish();


            }
        });

       Bt_rate_rider.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                boolean isRatingEmpty=false;

                System.out.println("btnclick------------------");

                if(reivweslist!=null)
                {
                    for(int i=0;i<reivweslist.size();i++)
                    {
                        if(reivweslist.get(i).getRatings_count().length()==0 || reivweslist.get(i).getRatings_count().equalsIgnoreCase("0.0"))
                        {
                            isRatingEmpty=true;
                        }

                        System.out.println("btnclick2------------------");
                    }

                    if(!isRatingEmpty)
                    {
                        if (isInternetPresent) {

                            System.out.println("btnclick3------------------");
                            System.out.println("------------ride_id-------------" + Str_rideid);

                            Map<String, String> jsonParams = new HashMap<String, String>();
                            jsonParams.put("ratingsFor", "rider");
                            jsonParams.put("ride_id", Str_rideid);

                            System.out.println("ride_id------------------"+Str_rideid);

                            System.out.println("ratingsFor------------------"+"rider");

                            for (int i=0;i<reivweslist.size();i++)
                            {
                                jsonParams.put("ratings["+i+"][option_id]",reivweslist.get(i).getOptions_id());
                                jsonParams.put("ratings["+i+"][option_title]",reivweslist.get(i).getOptions_title());
                                jsonParams.put("ratings["+i+"][rating]",reivweslist.get(i).getRatings_count());

                                System.out.println("option_id------------------" + reivweslist.get(i).getOptions_id());
                                System.out.println("option_title------------------" + reivweslist.get(i).getOptions_title());
                                System.out.println("rating------------------"+reivweslist.get(i).getRatings_count());
                            }
                            Post_RequestReviwes(ServiceConstant.submit_reviwes_url,jsonParams);

                        }else {
                            Alert(getResources().getString(R.string.alert_sorry_label_title), getResources().getString(R.string.alert_nointernet));
                        }
                    }
                    else
                    {
                        Alert(getResources().getString(R.string.lbel_notification), getResources().getString(R.string.lbel_notification_selectrating));
                    }

                }


            }
        });

    }


    private void initialize() {
        session = new SessionManager(RatingsPage.this);
        // get user data from session
        HashMap<String, String> user = session.getUserDetails();
        driver_id = user.get(SessionManager.KEY_DRIVERID);
        reivweslist = new ArrayList<Reviwes_Pojo>();

        listview = (ListView)findViewById(R.id.listView_rating);
        empty_Tv = (TextView)findViewById(R.id.reviwes_no_textview);
        Bt_rate_rider = (Button)findViewById(R.id.btn_submit_reviwes);
        Rl_layout_rating = (RelativeLayout)findViewById(R.id.layout_reviwesubmit_btn);


        Intent i = getIntent();
        Str_rideid =  i.getStringExtra("rideid");


          Tv_skip = (TextView)findViewById(R.id.review_skip);

        cd = new ConnectionDetector(RatingsPage.this);
        isInternetPresent = cd.isConnectingToInternet();

        if (isInternetPresent){
            PostRequest(ServiceConstant.reviwes_options_list_url);
            System.out.println("raatingslist------------------" +ServiceConstant.reviwes_options_list_url);
        }else {
            Alert(getResources().getString(R.string.alert_sorry_label_title), getResources().getString(R.string.alert_nointernet));

        }
    }

    //--------------Alert Method-----------
    private void Alert(String title, String message) {
        final PkDialog mDialog = new PkDialog(RatingsPage.this);
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


    //-----------------------Code for reviwes options list post request-----------------
    private void PostRequest(String Url) {
        dialog = new Dialog(RatingsPage.this);
        dialog.getWindow();
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.custom_loading);
        dialog.setCanceledOnTouchOutside(false);
        dialog.show();

        TextView dialog_title=(TextView)dialog.findViewById(R.id.custom_loading_textview);
        dialog_title.setText(getResources().getString(R.string.action_loading));


        postrequest = new StringRequest(Request.Method.POST, Url,
                new Response.Listener<String>() {

                    @Override
                    public void onResponse(String response) {
                        Log.e("reviwes", response);

                        String status = "",Str_total="",Str_Rating="";

                        try {
                            JSONObject object = new JSONObject(response);
                            status = object.getString("status");
                            Str_total = object.getString("total");

                            System.out.println("status---------"+object.getString("status"));

                            JSONArray jarry = object.getJSONArray("review_options");

                            if (jarry.length()>0){

                                for (int i =0;jarry.length()>0;i++){

                                    JSONObject jobject =jarry.getJSONObject(i);

                                    Reviwes_Pojo item = new Reviwes_Pojo();

                                    item.setOptions_title(jobject.getString("option_title"));
                                    item.setRatings_count("");
                                    item.setOptions_id(jobject.getString("option_id"));

                                    reivweslist.add(item);

                                }
                                show_progress_status=true;

                            }else{
                                show_progress_status=false;
                            }
                        }catch (Exception e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }

                        if (status.equalsIgnoreCase("1")){
                            adapter = new Reviwes_adapter(RatingsPage.this,reivweslist);
                            listview.setAdapter(adapter);
                            dialog.dismiss();
                        }else{
                            Toast.makeText(getApplicationContext(),getResources().getString(R.string.fetchdatatoast), Toast.LENGTH_SHORT).show();
                        }
                        if(show_progress_status)
                        {
                            empty_Tv.setVisibility(View.GONE);
                        }
                        else
                        {
                            empty_Tv.setVisibility(View.VISIBLE);
                            listview.setEmptyView(empty_Tv);
                        }

                    }
                }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                VolleyErrorResponse.VolleyError(RatingsPage.this,error);
            }

        }) {

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<String, String>();
                headers.put("User-agent", ServiceConstant.useragent);
                return headers;
            }

            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> jsonParams = new HashMap<String, String>();
                jsonParams.put("optionsFor", "rider");

                return jsonParams;
            }
        };
        AppController.getInstance().addToRequestQueue(postrequest);
    }

    //-----------------------Code for reviwes post request-----------------
    private void Post_RequestReviwes(String Url, final Map<String, String>jsonParams) {
        dialog = new Dialog(RatingsPage.this);
        dialog.getWindow();
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.rating_loading_layout);
        dialog.setCanceledOnTouchOutside(false);
        dialog.show();

        TextView dialog_title=(TextView)dialog.findViewById(R.id.custom_loading_textview_rating);
        dialog_title.setText(getResources().getString(R.string.action_loading_rating));


        postrequest = new StringRequest(Request.Method.POST, Url,
                new Response.Listener<String>() {

                    @Override
                    public void onResponse(String response) {
                        Log.e("reviwes", response);

                        System.out.println("reviwes------------" + response);

                        String status = "",Str_response="";

                      try {
                            JSONObject object = new JSONObject(response);
                            status = object.getString("status");
                            Str_response = object.getString("response");

                          System.out.println("status------"+status);

                        }catch (Exception e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }

                        if (status.equalsIgnoreCase("1")) {
                            final MaterialDialog dialog = new MaterialDialog(RatingsPage.this);
                            dialog.setTitle(getResources().getString(R.string.action_loading_sucess))
                                    .setMessage(Str_response)
                                    .setPositiveButton(
                                            "OK", new View.OnClickListener() {
                                                @Override
                                                public void onClick(View v) {
                                                    dialog.dismiss();

                                                    Intent broadcastIntent_tripdetail = new Intent();
                                                    broadcastIntent_tripdetail.setAction("com.finish.endtripenterdetail");
                                                    sendBroadcast(broadcastIntent_tripdetail);

                                                    Intent broadcastIntent_drivermap = new Intent();
                                                    broadcastIntent_drivermap.setAction("com.finish.canceltrip.DriverMapActivity");
                                                    sendBroadcast(broadcastIntent_drivermap);

                                                    finish();
                                                }
                                            }
                                    )
                                    .show();

                        } else {
                            Alert(getResources().getString(R.string.alert_sorry_label_title), Str_response);
                        }


                        dialog.dismiss();

                    }
                }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                VolleyErrorResponse.VolleyError(RatingsPage.this,error);
                dialog.dismiss();
            }

        }) {

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<String, String>();
                headers.put("User-agent", ServiceConstant.useragent);
                return headers;
            }

            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                return jsonParams;
            }
        };
        AppController.getInstance().addToRequestQueue(postrequest);
    }


    //-----------------Move Back on  phone pressed  back button------------------
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if ((keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0)) {
            // nothing
            return true;
        }
        return false;
    }




}
