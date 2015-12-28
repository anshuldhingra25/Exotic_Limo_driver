package com.cabily.cabilydriver;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.app.service.ServiceConstant;
import com.cabily.cabilydriver.Utils.AppController;
import com.cabily.cabilydriver.Utils.ConnectionDetector;
import com.cabily.cabilydriver.Utils.SessionManager;
import com.cabily.cabilydriver.Utils.VolleyErrorResponse;
import com.cabily.cabilydriver.adapter.PlaceSearchAdapter;
import com.cabily.cabilydriver.widgets.PkDialog;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

import de.measite.minidns.record.SRV;
import me.drakeet.materialdialog.MaterialDialog;

/**
 * Created by user88 on 11/18/2015.
 */
public class GooglePlaceSearch extends Activity {

    ArrayList<String> itemList_location=new ArrayList<String>();
    ArrayList<String> itemList_placeId=new ArrayList<String>();
    PlaceSearchAdapter adapter;
    private boolean isdataAvailable=false;

    private RelativeLayout alert_layout;
    private TextView alert_textview;
    private TextView tv_emptyText;
    private RelativeLayout back;
    private  EditText Et_search;

    private String driver_id = "";
    private Boolean isInternetPresent = false;
    private ConnectionDetector cd;
    private SessionManager session;
    Dialog dialog;

    Dialog mdialog;

    private ProgressBar progresswheel;
    private ListView list_view;

    private String Slatitude="",Slongitude="",Sdrop_location="";

    private StringRequest postrequest;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.drop_location);
        initialize();


        list_view.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
               Sdrop_location = itemList_location.get(position);

                cd = new ConnectionDetector(GooglePlaceSearch.this);
                isInternetPresent = cd.isConnectingToInternet();
                if (isInternetPresent) {
                    LatLongRequest(ServiceConstant.GetAddressFrom_LatLong_url + itemList_placeId.get(position));
                } else {
                    alert_layout.setVisibility(View.VISIBLE);
                    alert_textview.setText(getResources().getString(R.string.alert_nointernet));
                }

            }
        });



        Et_search.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {

                cd = new ConnectionDetector(GooglePlaceSearch.this);
                isInternetPresent = cd.isConnectingToInternet();

                if (isInternetPresent) {
                    if (postrequest != null) {
                        postrequest.cancel();
                    }
                    CitySearchRequest(ServiceConstant.place_search_url + Et_search.getText().toString().toLowerCase());
                } else {
                    alert_layout.setVisibility(View.VISIBLE);
                    alert_textview.setText(getResources().getString(R.string.alert_nointernet));
                }

            }
        });

        Et_search.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (event != null && (event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) {
                    CloseKeyboard(Et_search);
                }
                return false;
            }
        });

    }

    private void initialize() {
        session = new SessionManager(GooglePlaceSearch.this);
        // get user data from session
        HashMap<String, String> user = session.getUserDetails();
        driver_id = user.get(SessionManager.KEY_DRIVERID);

        progresswheel = (ProgressBar) findViewById(R.id.droplocation_progressBar);
        alert_layout = (RelativeLayout) findViewById(R.id.droplocation_alert_layout);
        alert_textview = (TextView) findViewById(R.id.droplocation_alert_textView);
        back = (RelativeLayout) findViewById(R.id.droplocation_back_layout);
        tv_emptyText = (TextView) findViewById(R.id.estimate_price_empty_textview);
        list_view = (ListView)findViewById(R.id.droplocation_listView);
        Et_search = (EditText)findViewById(R.id.droplocation_editText);

    }



    //--------------Alert Method-----------
    private void Alert(String title, String message) {
        final PkDialog mDialog = new PkDialog(GooglePlaceSearch.this);
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



    //-------------------Search Place Request----------------
    private void CitySearchRequest(String Url) {

        progresswheel.setVisibility(View.VISIBLE);
        System.out.println("--------------Search city url-------------------" + Url);
        postrequest = new StringRequest(Request.Method.GET, Url, new Response.Listener<String>() {

            @Override
            public void onResponse(String response) {

                System.out.println("--------------Search city  reponse-------------------" + response);
                String status="";
                try {
                    JSONObject object = new JSONObject(response);
                    if (object.length() > 0) {

                        status=object.getString("status");
                        JSONArray place_array = object.getJSONArray("predictions");
                        if(status.equalsIgnoreCase("OK"))
                        {
                            if(place_array.length()>0)
                            {
                                itemList_location.clear();
                                itemList_placeId.clear();
                                for (int i = 0; i < place_array.length(); i++)
                                {
                                    JSONObject place_object = place_array.getJSONObject(i);
                                    itemList_location.add(place_object.getString("description"));
                                    itemList_placeId.add(place_object.getString("place_id"));
                                }
                                isdataAvailable=true;
                            }
                            else
                            {
                                itemList_location.clear();
                                itemList_placeId.clear();
                                isdataAvailable=false;
                            }
                        }
                        else
                        {
                            itemList_location.clear();
                            itemList_placeId.clear();
                            isdataAvailable=false;
                        }
                    }
                } catch (JSONException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }

                progresswheel.setVisibility(View.INVISIBLE);
                alert_layout.setVisibility(View.GONE);
                if(isdataAvailable)
                {
                    tv_emptyText.setVisibility(View.GONE);
                }
                else
                {
                    tv_emptyText.setVisibility(View.VISIBLE);
                }
                adapter=new PlaceSearchAdapter(GooglePlaceSearch.this,itemList_location);
                list_view.setAdapter(adapter);
                adapter.notifyDataSetChanged();

            }
        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                progresswheel.setVisibility(View.INVISIBLE);
                alert_layout.setVisibility(View.GONE);

                // close keyboard
                CloseKeyboard(Et_search);
                VolleyErrorResponse.VolleyError(GooglePlaceSearch.this, error);
            }
        });
        postrequest.setRetryPolicy(new DefaultRetryPolicy(20000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        postrequest.setShouldCache(false);
        AppController.getInstance().addToRequestQueue(postrequest);
    }

    private void CloseKeyboard(EditText edittext) {
        InputMethodManager in = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        in.hideSoftInputFromWindow(edittext.getApplicationWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
    }


    //-------------------Get Latitude and Longitude from Address(Place ID) Request----------------
    private void LatLongRequest(String Url) {

        mdialog = new Dialog(GooglePlaceSearch.this);
        mdialog.getWindow();
        mdialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        mdialog.setContentView(R.layout.custom_loading);
        mdialog.setCanceledOnTouchOutside(false);
        mdialog.show();

        TextView dialog_title=(TextView)mdialog.findViewById(R.id.custom_loading_textview);
        dialog_title.setText(getResources().getString(R.string.action_loading));

        System.out.println("--------------LatLong url-------------------" + Url);
        postrequest = new StringRequest(Request.Method.GET, Url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {

                System.out.println("--------------LatLong  reponse-------------------" + response);
                String status = "";
                try {
                    JSONObject object = new JSONObject(response);
                    if (object.length() > 0) {

                        status = object.getString("status");
                        JSONObject place_object = object.getJSONObject("result");
                        if (status.equalsIgnoreCase("OK")) {
                            if (place_object.length() > 0) {
                                JSONObject geometry_object = place_object.getJSONObject("geometry");
                                if (geometry_object.length() > 0) {
                                    JSONObject location_object = geometry_object.getJSONObject("location");
                                    if (location_object.length() > 0) {
                                        Slatitude = location_object.getString("lat");
                                        Slongitude = location_object.getString("lng");
                                        isdataAvailable = true;
                                    } else {
                                        isdataAvailable = false;
                                    }
                                } else {
                                    isdataAvailable = false;
                                }
                            } else {
                                isdataAvailable = false;
                            }
                        } else {
                            isdataAvailable = false;
                        }
                    }
                } catch (JSONException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                if (isdataAvailable) {
                    Intent intent=new Intent();
                    intent.putExtra("Lattitude",Slatitude);
                    intent.putExtra("Longitude",Slongitude);
                    intent.putExtra("address",Sdrop_location);

                    System.out.println("msg-------------" + Sdrop_location);
                    System.out.println("Lattitude-------------" + Slatitude);
                    System.out.println("Longitude-------------" + Slongitude);

                    setResult(RESULT_OK, intent);
                    finish();


                } else {
                    mdialog.dismiss();
                    Alert(getResources().getString(R.string.alert_sorry_label_title), status);
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                mdialog.dismiss();
                VolleyErrorResponse.VolleyError(GooglePlaceSearch.this, error);
            }
        });
        postrequest.setRetryPolicy(new DefaultRetryPolicy(20000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        postrequest.setShouldCache(false);
        AppController.getInstance().addToRequestQueue(postrequest);
    }








}
