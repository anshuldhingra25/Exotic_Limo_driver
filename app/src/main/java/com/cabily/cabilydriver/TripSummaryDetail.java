package com.cabily.cabilydriver;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.app.service.ServiceConstant;
import com.cabily.cabilydriver.Pojo.CancelReasonPojo;
import com.cabily.cabilydriver.Utils.AppController;
import com.cabily.cabilydriver.Utils.ConnectionDetector;
import com.cabily.cabilydriver.Utils.GPSTracker;
import com.cabily.cabilydriver.Utils.SessionManager;
import com.cabily.cabilydriver.Utils.VolleyErrorResponse;
import com.cabily.cabilydriver.adapter.CancelReasonAdapter;
import com.cabily.cabilydriver.subclass.SubclassActivity;
import com.cabily.cabilydriver.widgets.PkDialog;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Currency;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import me.drakeet.materialdialog.MaterialDialog;

/**
 * Created by user14 on 9/22/2015.
 */
public class TripSummaryDetail extends SubclassActivity {

    private RelativeLayout Rl_total_bills_details,Rl_layout_Tripststus,Rl_layout_amount_status,Rl_layout_pickup_details,Rl_layout_drop_details;
    private LinearLayout Ll_layouit_tripsummerydetail_timings;

    private TextView Tv_tripdetail_rideId,Tv_tripdetail_address,Tv_tripdetail_pickup,Tv_tripdetail_drop,Tv_tripdetail_ride_distance,
            Tv_tripdetail_timetaken,Tv_tripdetail_waitingtime,Tv_tripdetail_total_paid,Tv_tripdetail_total_amount_paid,Tv_trip_status,Tv_trip_paid_status,Tv_wallet_uage,Tv_coupon_discount;

    Dialog dialog;
    Dialog ridecancel_dialog;

    private  String   Str_continue_ridedetail="";
    private String Str_rideId = "";
    private String driver_id = "";
    private Boolean isInternetPresent = false;
    private ConnectionDetector cd;
    private Context context;
    private SessionManager session;

    private StringRequest postrequest;
    private RelativeLayout Rl_layout_tripdetail_back_img,layout_completed_details,layout_address_and_loction_details;

    private Button Bt_Cancel_ride,Bt_Continue_Ride;
    private GoogleMap googleMap;
    private boolean show_progress_status=false;
    GPSTracker gps;

    private double MyCurrent_lat = 0.0, MyCurrent_long = 0.0;

    private String Str_lattitude= "",Str_logitude="";
    private  String Str_droplattitude="",Str_droplongitude="";
    private double strlat,strlon;
    private String Str_loctionaddress="",Str_drop="";
    private  String Str_Username="",Str_useremail="",Str_pickup_date="",Str_phoneno="",Str_pickup_time="",Str_userimg="",Str_userrating="",Str_rideid="",Str_pickuplocation="",Str_pickup_lat="",Str_pickup_long="";

    private RelativeLayout alert_layout;
    private TextView alert_textview;
    private ListView cancel_listview;
    private ArrayAdapter<String>listAdapter ;
    private ArrayList<CancelReasonPojo>Cancelreason_arraylist;
    private CancelReasonAdapter adapter;
    private StringRequest canceltrip_postrequest;

    private String Str_currency="";

    private TextView Tv_driverTip;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.tripsummer_list_detail);
        initialize();
        initilizeMap();
        //--------Disabling the map functionality---------
        googleMap.getUiSettings().setAllGesturesEnabled(false);

        Rl_layout_tripdetail_back_img.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
                overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
            }
        });

        Bt_Cancel_ride.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(TripSummaryDetail.this,CancelTrip.class);
                intent.putExtra("RideId",Str_rideId);
                startActivity(intent);
                overridePendingTransition(R.anim.fade_in,R.anim.fade_out);
            }
        });


        Bt_Continue_Ride.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (Str_continue_ridedetail.equalsIgnoreCase("arrived")){
                    Intent intent = new Intent(TripSummaryDetail.this,ArrivedTrip.class);
                    intent.putExtra("address",Str_loctionaddress);
                    intent.putExtra("rideId",Str_rideId);
                    intent.putExtra("pickuplat",Str_pickup_lat);
                    intent.putExtra("pickup_long",Str_pickup_long);
                    intent.putExtra("username",Str_Username);
                    intent.putExtra("userrating",Str_userrating);
                    intent.putExtra("phoneno",Str_phoneno);
                    intent.putExtra("userimg",Str_userimg);
                    startActivity(intent);
                    overridePendingTransition(R.anim.fade_in,R.anim.fade_out);
                }else if (Str_continue_ridedetail.equalsIgnoreCase("begin")){
                    Intent intent = new Intent(TripSummaryDetail.this,BeginTrip.class);
                    intent.putExtra("user_name", Str_Username);
                    intent.putExtra("user_phoneno", Str_phoneno);
                    intent.putExtra("user_image",Str_userimg);
                    intent.putExtra("rideid",Str_rideId);
                    startActivity(intent);
                    overridePendingTransition(R.anim.fade_in,R.anim.fade_out);
                }else if (Str_continue_ridedetail.equalsIgnoreCase("end")){
                    Intent intent = new Intent(TripSummaryDetail.this,EndTrip_EnterDetails.class);
                    intent.putExtra("user_name", Str_Username);
                    intent.putExtra("user_phoneno", Str_phoneno);
                    intent.putExtra("user_image",Str_userimg);
                    intent.putExtra("rideid",Str_rideId);
                    intent.putExtra("pickuptime",Str_pickup_date);
                    startActivity(intent);
                    overridePendingTransition(R.anim.fade_in,R.anim.fade_out);
                }
                else {
                    Intent intent = new Intent(TripSummaryDetail.this,EndTrip.class);
                    intent.putExtra("name",Str_Username);
                    intent.putExtra("rideid",Str_rideId);
                    intent.putExtra("mobilno",Str_phoneno);
                    startActivity(intent);
                    overridePendingTransition(R.anim.fade_in,R.anim.fade_out);
                }

            }
        });

    }

    private void initialize(){
        session = new SessionManager(TripSummaryDetail.this);
        // get user data from session
        HashMap<String, String> user = session.getUserDetails();
        driver_id = user.get(SessionManager.KEY_DRIVERID);

        Cancelreason_arraylist = new ArrayList<CancelReasonPojo>();
        Intent i = getIntent();
        Str_rideId = i.getStringExtra("ride_id");
        System.out.println("rideid----------------" + Str_rideId);
        Tv_trip_paid_status = (TextView)findViewById(R.id.payment_paid_Textview_tripdetail);
        Tv_trip_status = (TextView)findViewById(R.id.tripdetail_status);
        Tv_tripdetail_total_amount_paid = (TextView)findViewById(R.id.tripdetail_view_total_paid_amount);
        Tv_tripdetail_total_paid = (TextView)findViewById(R.id.trip_detail_view_total_amount);

        Tv_tripdetail_ride_distance = (TextView)findViewById(R.id.trip_detail_distancekm);
        Tv_tripdetail_timetaken =(TextView)findViewById(R.id.tripdetail_timetaken_value);
        Tv_tripdetail_waitingtime =(TextView)findViewById(R.id.tripdetail_wait_time_value);

        Tv_coupon_discount = (TextView)findViewById(R.id.coupon_discount);
        Tv_wallet_uage = (TextView)findViewById(R.id.wallet_usage);
        Bt_Continue_Ride =(Button)findViewById(R.id.trip_summerydetail_continue_ride_button);
        Bt_Cancel_ride = (Button)findViewById(R.id.trip_summerydetail_cancelride_button);
      //  Tv_tripdetail_drop = (TextView)findViewById(R.id.tripsummery_detail_view_drop_date);
        Tv_tripdetail_pickup = (TextView)findViewById(R.id.trip_view_pickupdates);
        Tv_tripdetail_address =(TextView)findViewById(R.id.Tv_tripsummery_view_address);
        Tv_tripdetail_rideId = (TextView)findViewById(R.id.  tripsummry_rideidTv);
        Ll_layouit_tripsummerydetail_timings = (LinearLayout)findViewById(R.id.trip_details_view_details_time);
        Rl_total_bills_details = (RelativeLayout)findViewById(R.id.trip_detail_bill_details);
        Rl_layout_Tripststus = (RelativeLayout)findViewById(R.id.layout_trip_summery_details_status);
        Rl_layout_amount_status = (RelativeLayout)findViewById(R.id.layout_tripdetail_payment_status);
        Rl_layout_tripdetail_back_img = (RelativeLayout)findViewById(R.id.tripsummry_layouts);
        layout_completed_details = (RelativeLayout)findViewById(R.id.layoutsummery_and_bill_details);
        layout_address_and_loction_details = (RelativeLayout)findViewById(R.id.layout_rideaddress_and_locarions_details);
        Rl_layout_pickup_details = (RelativeLayout)findViewById(R.id.layout_tripsummery_pickup);
       // Rl_layout_drop_details = (RelativeLayout)findViewById(R.id.trip_summery_layout_drop_details);
        Tv_driverTip=(TextView )findViewById(R.id.driver_tip_tv);


        cd = new ConnectionDetector(TripSummaryDetail.this);
        isInternetPresent = cd.isConnectingToInternet();

        if (isInternetPresent) {
            postRequest_tripdetail(ServiceConstant.tripsummery_view_url);
        } else {
            Alert(getResources().getString(R.string.alert_sorry_label_title), getResources().getString(R.string.alert_nointernet));
        }
    }


    private void initilizeMap() {
        if (googleMap == null) {
            googleMap = ((MapFragment)TripSummaryDetail.this.getFragmentManager().findFragmentById(R.id.tripsummery_view_map)).getMap();
            // check if map is created successfully or not
            if (googleMap == null) {
                Toast.makeText(TripSummaryDetail.this, "Sorry! unable to create maps", Toast.LENGTH_SHORT).show();
            }
        }
        // Changing map type
        googleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        // Showing / hiding your current location
        googleMap.setMyLocationEnabled(false);
        // Enable / Disable zooming controls
        googleMap.getUiSettings().setZoomControlsEnabled(false);
        // Enable / Disable my location button
        googleMap.getUiSettings().setMyLocationButtonEnabled(false);
        // Enable / Disable Compass icon
        googleMap.getUiSettings().setCompassEnabled(false);
        // Enable / Disable Rotate gesture
        googleMap.getUiSettings().setRotateGesturesEnabled(true);
        // Enable / Disable zooming functionality
        googleMap.getUiSettings().setZoomGesturesEnabled(true);
        googleMap.setMyLocationEnabled(false);

    }

    //--------------Alert Method-----------
    private void Alert(String title, String message) {
        final PkDialog mDialog = new PkDialog(TripSummaryDetail.this);
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


    //-----------------------Change Post Request-----------------
    private void postRequest_tripdetail(String Url)
    {
        System.out.println("post---------------------");

        dialog = new Dialog(TripSummaryDetail.this);
        dialog.getWindow();
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.custom_loading);
        dialog.setCanceledOnTouchOutside(false);
        dialog.show();

        TextView dialog_title=(TextView)dialog.findViewById(R.id.custom_loading_textview);
        dialog_title.setText(getResources().getString(R.string.action_loading));

        System.out.println("-------------tripdetail----------------" + Url);

        postrequest = new StringRequest(Request.Method.POST, Url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        System.out.println("-------------tripdetail----------------" + response);
                        String Sstatus = "",Scurrency_code="", Str_ridestatus="",Str_rideid="",Smessage = "",Str_totalbill="",Str_totalpaid="",Str_coupon_code="",Str_cancel="",Str_wallet_usage="",Str_cabtype="",Str_drop_date="",
                                trip_paid_status="",Str_continu_ride="",Str_ride_distance="",Str_time_taken="",Str_wait_time="";
                        Currency currencycode = null;
                        String Str_tipStatus="",Str_tipAmount="";

                        try {
                            JSONObject object = new JSONObject(response);

                            Sstatus = object.getString("status");
                            System.out.println("status-----------"+Sstatus);

                            JSONObject  jobject = object.getJSONObject("response");
                            if (jobject.length()>0)
                            {
                                JSONObject jsonObject = jobject.getJSONObject("details");

                                if (jsonObject.length()>0)
                                {
                                    Str_currency = jsonObject.getString("currency");
                                    currencycode = Currency.getInstance(getLocale(Str_currency));

                                    Str_ridestatus = jsonObject.getString("ride_status");
                                    Str_rideid = jsonObject.getString("ride_id");
                                    trip_paid_status = jsonObject.getString("pay_status");
                                    Str_cancel = jsonObject.getString("do_cancel_action");
                                    Str_continue_ridedetail = jsonObject.getString("continue_ride");

                                }

                                JSONObject jobject2 = jsonObject.getJSONObject("pickup");

                                if (jobject2.length()>0)
                                {
                                    Str_loctionaddress = jobject2.getString("location");

                                    JSONObject jobject3 = jobject2.getJSONObject("latlong");
                                    Str_lattitude  = jobject3.getString("lat");
                                    Str_logitude = jobject3.getString("lon");

                                    System.out.println("lat---------"+Str_lattitude);
                                    System.out.println("lon---------"+Str_logitude);

                                    strlat = Double.parseDouble(Str_lattitude);
                                    strlon = Double.parseDouble(Str_logitude);

                                    //-------------------code for set marker-------------------------
                                    googleMap.addMarker(new MarkerOptions()
                                            .position(new LatLng((strlat),(strlon)))
                                            .icon(BitmapDescriptorFactory.fromResource(R.drawable.marker)));
                                    // Move the camera to last position with a zoom level
                                    CameraPosition cameraPosition = new CameraPosition.Builder().target(new LatLng((strlat), (strlon))).zoom(12).build();
                                    googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));

                                }


                               /* JSONObject jobject_drop = jsonObject.getJSONObject("drop");

                                if (jobject_drop.length()>0){

                                    Str_drop =jobject_drop.getString("location");

                                    JSONObject jobject_drop1 = jobject2.getJSONObject("latlong");
                                    Str_droplattitude = jobject_drop1.getString("lon");
                                    Str_droplongitude = jobject_drop1.getString("lat");

                                    System.out.println("drop-------------"+Str_drop);

                                }*/

                                Str_pickup_date = jsonObject.getString("pickup_date");

                                if (Str_ridestatus.equalsIgnoreCase("Completed")||Str_ridestatus.equalsIgnoreCase("Finished"))
                                {

                                    JSONObject jobject4 = jsonObject.getJSONObject("summary");
                                    if (jobject4.length()>0)
                                    {
                                        Str_ride_distance = jobject4.getString("ride_distance");
                                        Str_time_taken = jobject4.getString("ride_duration");
                                        Str_wait_time = jobject4.getString("waiting_duration");
                                    }

                                    JSONObject jobject5 = jsonObject.getJSONObject("fare");
                                    if (jobject5.length()>0)
                                    {
                                        Str_totalbill = currencycode.getSymbol()+jobject5.getString("grand_bill");
                                        Str_totalpaid =  currencycode.getSymbol()+jobject5.getString("total_paid");
                                        Str_coupon_code = jobject5.getString("coupon_discount");
                                        Str_wallet_usage = jobject5.getString("wallet_usage");
                                    }


                                    JSONObject tips_object = jsonObject.getJSONObject("tips");
                                    if (tips_object.length()>0)
                                    {
                                        Str_tipStatus = currencycode.getSymbol()+tips_object.getString("tips_status");
                                        Str_tipAmount =  currencycode.getSymbol()+tips_object.getString("tips_amount");
                                    }


                                }

                                String  data = jobject.getString("user_profile");
                                Object json = new JSONTokener(data).nextValue();
                                if (json instanceof JSONObject){
                                    JSONObject jobject_profile = new JSONObject(data);
                                    Str_useremail =  jobject_profile.getString("user_email");
                                    Str_Username = jobject_profile.getString("user_name");
                                    Str_phoneno = jobject_profile.getString("phone_number");
                                    Str_userimg = jobject_profile.getString("user_image");
                                    Str_userrating = jobject_profile.getString("user_review");
                                    Str_rideid = jobject_profile.getString("ride_id");
                                    Str_pickuplocation = jobject_profile.getString("pickup_location");
                                    Str_pickup_lat = jobject_profile.getString("pickup_lat");
                                    Str_pickup_long = jobject_profile.getString("pickup_lon");
                                    Str_pickup_time = jobject_profile.getString("pickup_time");
                                    System.out.println("phone-----------------"+Str_phoneno);
                                    System.out.println("pickup_long-----------------"+Str_pickup_long);
                                    System.out.println("pickup_lat-----------------"+Str_pickup_lat);
                                    System.out.println("username-----------------"+Str_Username);

                                } else if(json instanceof JSONArray){
                                }
                                Str_continu_ride  =jsonObject.getString("continue_ride");
                            }

                            if (Sstatus.equalsIgnoreCase("1"))
                            {
                                Tv_tripdetail_address.setText(Str_loctionaddress);
                                Tv_tripdetail_pickup.setText(Str_pickup_date);
                               //  Tv_tripdetail_drop.setText(Str_drop);
                                Tv_tripdetail_total_paid.setText(Str_totalbill);
                                Tv_tripdetail_total_amount_paid.setText(Str_totalpaid);
                                Tv_tripdetail_rideId.setText(Str_rideid);
                                Tv_trip_status.setText(getResources().getString(R.string.tripsummery_add_Ride_label)+" "+Str_ridestatus);
                                Tv_trip_paid_status.setText(getResources().getString(R.string.tripsummery_add_Payment_label)+" "+trip_paid_status);

                                Tv_tripdetail_ride_distance.setText(Str_ride_distance+" "+getResources().getString(R.string.tripsummery_add_km_label));
                                Tv_tripdetail_timetaken.setText(Str_time_taken+" "+getResources().getString(R.string.tripsummery_add_mins_label));
                                Tv_tripdetail_waitingtime.setText(Str_wait_time+" "+getResources().getString(R.string.tripsummery_add_mins_label));

                                //----------------------code for ride details---------------------
                                if (Str_ridestatus.equalsIgnoreCase("Completed")||Str_ridestatus.equalsIgnoreCase("Finished"))
                                {
                                    layout_address_and_loction_details.setVisibility(View.VISIBLE);
                                    layout_completed_details.setVisibility(View.VISIBLE);
                                    Rl_layout_amount_status.setVisibility(View.VISIBLE);
                                    Rl_layout_Tripststus.setVisibility(View.VISIBLE);
                                    Rl_layout_pickup_details.setVisibility(View.VISIBLE);
                                  //  Rl_layout_drop_details.setVisibility(View.VISIBLE);

                                    //--------------code for discount and wallet usage------------
                                    if (Str_wallet_usage.length()>0){
                                        Tv_wallet_uage.setVisibility(View.VISIBLE);
                                        Tv_wallet_uage.setVisibility(View.GONE);
                                        Tv_wallet_uage.setText("Wallet used"+Str_wallet_usage);
                                    }else{
                                        Tv_wallet_uage.setVisibility(View.GONE);
                                    }

                                    if (Str_coupon_code.length()>0){
                                        Tv_coupon_discount.setVisibility(View.VISIBLE);
                                        Tv_coupon_discount.setVisibility(View.GONE);
                                        Tv_coupon_discount.setText("Coupon discount used" + Str_coupon_code);
                                    }

                                    if(Str_tipStatus.equalsIgnoreCase("0"))
                                    {
                                        Tv_driverTip.setVisibility(View.GONE);
                                    }
                                    else
                                    {
                                        Tv_driverTip.setVisibility(View.VISIBLE);
                                        Tv_driverTip.setText(getResources().getString(R.string.cabily_driver_tip_amount)+" "+Str_tipAmount);
                                    }

                                }else{
                                    Rl_layout_amount_status.setVisibility(View.GONE);
                                   // Rl_layout_drop_details.setVisibility(View.GONE);
                                    Rl_layout_Tripststus.setVisibility(View.VISIBLE);
                                    layout_address_and_loction_details.setVisibility(View.VISIBLE);
                                    Rl_layout_pickup_details.setVisibility(View.VISIBLE);
                                }

                                //------------code for continue ride---------------
                                if (Str_continue_ridedetail.equalsIgnoreCase("arrived")){
                                    Bt_Continue_Ride.setVisibility(View.VISIBLE);
                                    Bt_Cancel_ride.setVisibility(View.VISIBLE);
                                }else if(Str_continue_ridedetail.equalsIgnoreCase("begin")){
                                    Bt_Continue_Ride.setVisibility(View.VISIBLE);
                                    Bt_Cancel_ride.setVisibility(View.VISIBLE);
                                }else if(Str_continue_ridedetail.equalsIgnoreCase("end")){
                                    Bt_Continue_Ride.setVisibility(View.VISIBLE);
                                    Bt_Cancel_ride.setVisibility(View.GONE);
                                }else{
                                    Bt_Continue_Ride.setVisibility(View.GONE);
                                   // Rl_layout_drop_details.setVisibility(View.GONE);
                                }

                                //---------code for cancel ride----------
                                if (Str_cancel.equalsIgnoreCase("1")){
                                    Bt_Cancel_ride.setVisibility(View.VISIBLE);
                                }else {
                                    Bt_Cancel_ride.setVisibility(View.GONE);
                                }

                            }
                            else{
                                Alert(getResources().getString(R.string.alert_sorry_label_title),getResources().getString(R.string.fetchdatatoast));
                               // Toast.makeText(TripSummaryDetail.this,getResources().getString(R.string.fetchdatatoast),Toast.LENGTH_SHORT).show();
                            }

                        } catch (JSONException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }
                        dialog.dismiss();

                    }
                }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                dialog.dismiss();
                VolleyErrorResponse.VolleyError(TripSummaryDetail.this, error);
            }
        }) {

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<String, String>();
                headers.put("User-agent",ServiceConstant.useragent);
                return headers;
            }
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> jsonParams = new HashMap<String, String>();

                jsonParams.put("driver_id",driver_id);
                jsonParams.put("ride_id",Str_rideId);

                System.out.println("driver_id--------------" + driver_id);
                System.out.println("ride_id--------------" +Str_rideId);

                return jsonParams;
            }
        };
        AppController.getInstance().addToRequestQueue(postrequest);
    }

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
