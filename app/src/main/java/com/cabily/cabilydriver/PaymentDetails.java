package com.cabily.cabilydriver;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import com.Hockeyapp.FragmentHockeyApp;
import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.app.service.ServiceConstant;
import com.cabily.cabilydriver.Pojo.PaymentdetailsPojo;
import com.cabily.cabilydriver.Utils.AppController;
import com.cabily.cabilydriver.Utils.ConnectionDetector;
import com.cabily.cabilydriver.Utils.SessionManager;
import com.cabily.cabilydriver.Utils.VolleyErrorResponse;
import com.cabily.cabilydriver.adapter.PaymentDetailsAdapter;
import com.cabily.cabilydriver.widgets.PkDialog;
import com.special.ResideMenu.ResideMenu;


import org.json.JSONArray;
import org.json.JSONObject;

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
public class PaymentDetails extends FragmentHockeyApp {

    private View parentView;
    private ResideMenu resideMenu;
    ListView payment_list;
    StringRequest postrequest;
    private SessionManager session;
    String driver_id = "", payid;
    private ArrayList<PaymentdetailsPojo> paymentstatementList;
    private PaymentDetailsAdapter adapter;
    private Dialog dialog;
    private Boolean isInternetPresent = false;
    private ConnectionDetector cd;
    private ActionBar actionBar;
    private TextView empty_Tv;
    private boolean show_progress_status = false;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        parentView = inflater.inflate(R.layout.payment_statement_main, container, false);
        ActionBarActivity actionBarActivity = (ActionBarActivity) getActivity();
        actionBar = actionBarActivity.getSupportActionBar();
        actionBar.setDisplayShowTitleEnabled(false);
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setHomeButtonEnabled(true);
        actionBar.hide();
        initialize(parentView);
        parentView.findViewById(R.id.ham_home).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(resideMenu != null ){
                    resideMenu.openMenu(ResideMenu.DIRECTION_LEFT);
                }
            }
        });
        payment_list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(getActivity(), PaymentDetailsList.class);
                startActivity(intent);
                getActivity().overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
            }
        });
        setUpViews();
        return parentView;
    }

    private void setUpViews() {
        NavigationDrawer parentActivity = (NavigationDrawer) getActivity();
        resideMenu = parentActivity.getResideMenu();
    }


    private void initialize(View rootview) {
        paymentstatementList = new ArrayList<PaymentdetailsPojo>();
        session = new SessionManager(getActivity());
        payment_list = (ListView) rootview.findViewById(R.id.listView_paymentdetails);
        empty_Tv = (TextView) rootview.findViewById(R.id.payment_no_textview);

        HashMap<String, String> user = session.getUserDetails();
        driver_id = user.get(SessionManager.KEY_DRIVERID);


        cd = new ConnectionDetector(getActivity());
        isInternetPresent = cd.isConnectingToInternet();

        if (isInternetPresent) {
            PostRequest(ServiceConstant.paymentdetails_url);
            System.out.println("payment------------------" + ServiceConstant.paymentdetails_url);
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


    //-----------------------Code for payment details post request-----------------
    private void PostRequest(String Url) {
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
                        Log.e("paymrnt", response);

                        String status = "", Str_currency_code = "",Str_response="";


                        try {
                            JSONObject object = new JSONObject(response);
                            status = object.getString("status");

                            if (status.equalsIgnoreCase("1")){

                                JSONObject jsonObject = object.getJSONObject("response");

                                Str_currency_code = jsonObject.getString("currency");

                                System.out.println("currency--------------" + Str_currency_code);

                                Currency currencycode = Currency.getInstance(getLocale(Str_currency_code));

                                JSONArray jarry = jsonObject.getJSONArray("payments");

                                if (jarry.length() > 0) {

                                    for (int i = 0; jarry.length() > 0; i++) {

                                        JSONObject jobject = jarry.getJSONObject(i);

                                        PaymentdetailsPojo item = new PaymentdetailsPojo();

                                        item.setamount(currencycode.getSymbol() + jobject.getString("amount"));
                                        item.setpay_date(jobject.getString("pay_date"));
                                        item.setpay_duration_from(jobject.getString("pay_duration_from"));
                                        item.setpay_duration_to(jobject.getString("pay_duration_to"));
                                        item.setpay_id(jobject.getString("pay_id"));

                                        System.out.println("pay_id--------" + jobject.getString("pay_id"));

                                        paymentstatementList.add(item);
                                    }
                                    show_progress_status = true;

                                } else {
                                    show_progress_status = false;
                                }

                            }else{
                                Str_response = object.getString("response");
                            }

                        } catch (Exception e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }

                        if (status.equalsIgnoreCase("1")){
                            adapter = new PaymentDetailsAdapter(getActivity(), paymentstatementList);
                            payment_list.setAdapter(adapter);
                            dialog.dismiss();

                            if (show_progress_status) {
                                empty_Tv.setVisibility(View.GONE);
                            } else {
                                empty_Tv.setVisibility(View.VISIBLE);
                                payment_list.setEmptyView(empty_Tv);
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
                return headers;
            }

            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> jsonParams = new HashMap<String, String>();
                jsonParams.put("driver_id", driver_id);
                System.out
                        .println("--------------driver_id-------------------"
                                + driver_id);
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


