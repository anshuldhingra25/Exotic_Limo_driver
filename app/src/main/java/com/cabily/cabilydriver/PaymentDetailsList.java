package com.cabily.cabilydriver;

import android.app.Activity;
import android.app.Dialog;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ListView;
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
import com.cabily.cabilydriver.Pojo.PaymentDetailsListPojo;
import com.cabily.cabilydriver.Utils.AppController;
import com.cabily.cabilydriver.Utils.ConnectionDetector;
import com.cabily.cabilydriver.Utils.SessionManager;
import com.cabily.cabilydriver.Utils.VolleyErrorResponse;
import com.cabily.cabilydriver.adapter.PaymentDetailsListAdapter;
import com.cabily.cabilydriver.widgets.PkDialog;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Currency;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import me.drakeet.materialdialog.MaterialDialog;

/**
 *
 */
public class PaymentDetailsList extends ActivityHockeyApp {

    TextView payment, amount, recv_date;
    ListView list;
    StringRequest postrequest;
    private SessionManager session;
    String driver_id = "";
    private ArrayList<PaymentDetailsListPojo> paymentdetailList;
    private PaymentDetailsListAdapter adapter;
    private String payid = "";
    private RelativeLayout layout_back;
    private Dialog dialog;
    private TextView Emty_Text;

    private Boolean isInternetPresent = false;
    private ConnectionDetector cd;

    private boolean show_progress_status = false;
    private String Str_currency_code = "";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.payment_details_list);
        initialize();
        layout_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
                overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
            }
        });

    }


    private void initialize() {
        paymentdetailList = new ArrayList<PaymentDetailsListPojo>();
        session = new SessionManager(PaymentDetailsList.this);
        payment = (TextView) findViewById(R.id.pmt_detail_payment);
        amount = (TextView) findViewById(R.id.pmt_detail_amount);
        recv_date = (TextView) findViewById(R.id.pmt_detail_receiveddate);
        list = (ListView) findViewById(R.id.pmt_detail_listview);
        layout_back = (RelativeLayout) findViewById(R.id.layout_payment_list_back);
        Emty_Text = (TextView) findViewById(R.id.payment_lists_no_textview);


        HashMap<String, String> user = session.getUserDetails();
        driver_id = user.get(SessionManager.KEY_DRIVERID);

        payid = getIntent().getStringExtra("Payid");

        cd = new ConnectionDetector(PaymentDetailsList.this);
        isInternetPresent = cd.isConnectingToInternet();

        if (isInternetPresent) {
            paymentRequest(ServiceConstant.paymentdetails_lis_url);
            System.out.println("paymentlists------------------" + ServiceConstant.paymentdetails_lis_url);
        } else {
            Alert(getResources().getString(R.string.alert_sorry_label_title), getResources().getString(R.string.alert_nointernet));

        }
    }

    //--------------Alert Method-----------
    private void Alert(String title, String message) {
        final PkDialog mDialog = new PkDialog(PaymentDetailsList.this);
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


    private void paymentRequest(String Url) {
        dialog = new Dialog(PaymentDetailsList.this);
        dialog.getWindow();
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.custom_loading);
        dialog.setCanceledOnTouchOutside(false);
        dialog.show();

        TextView dialog_title = (TextView) dialog.findViewById(R.id.custom_loading_textview);
        dialog_title.setText(getResources().getString(R.string.action_loading));

        postrequest = new StringRequest(Request.Method.POST, Url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                System.out.println("--------------Payment Details reponse-------------------" + response);

                String status = "", response1 = "", pay_id = "", pay_duration_from = "", pay_duration_to = "", amount1 = "", pay_date = "";
                try {
                    JSONObject object = new JSONObject(response);
                    status = object.getString("status");

                    JSONObject jobject = object.getJSONObject("response");

                    Str_currency_code = jobject.getString("currency");
                    System.out.println("curr-----------" + Str_currency_code);
                    Currency currencycode = Currency.getInstance(getLocale(Str_currency_code));

                    JSONArray ride_array = jobject.getJSONArray("payments");
                    paymentdetailList.clear();

                    if (ride_array.length() > 0) {
                        for (int k = 0; k < ride_array.length(); k++) {
                            JSONObject product_object = ride_array.getJSONObject(k);
                            PaymentDetailsListPojo items1 = new PaymentDetailsListPojo();
                            pay_id = product_object.getString("pay_id");
                            pay_duration_from = product_object.getString("pay_duration_from");
                            pay_duration_to = product_object.getString("pay_duration_to");
                            amount1 = currencycode.getSymbol() + product_object.getString("amount");
                            pay_date = product_object.getString("pay_date");
                        }
                    }
                    JSONArray ride_array1 = jobject.getJSONArray("listsArr");
                    if (ride_array.length() > 0) {
                        for (int k = 0; k < ride_array1.length(); k++) {
                            JSONObject product_object = ride_array1.getJSONObject(k);
                            PaymentDetailsListPojo items1 = new PaymentDetailsListPojo();
                            items1.setride_id(product_object.getString("ride_id"));
                            items1.setamount(currencycode.getSymbol() + product_object.getString("amount"));
                            items1.setride_date(product_object.getString("ride_date"));
                            paymentdetailList.add(items1);
                        }
                        show_progress_status = true;

                    } else {
                        paymentdetailList.clear();
                        show_progress_status = false;
                    }

                } catch (JSONException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                payment.setText(pay_duration_from + " to " + pay_duration_to);
                amount.setText(amount1);
                recv_date.setText(pay_date);

                System.out.println("pay-----------------" + paymentdetailList);

                adapter = new PaymentDetailsListAdapter(PaymentDetailsList.this, paymentdetailList);
                list.setAdapter(adapter);
                dialog.dismiss();

                if (show_progress_status) {
                    Emty_Text.setVisibility(View.GONE);
                } else {
                    Emty_Text.setVisibility(View.VISIBLE);
                    list.setEmptyView(Emty_Text);
                }

            }
        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                VolleyErrorResponse.VolleyError(PaymentDetailsList.this, error);
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
            protected java.util.Map<String, String> getParams() throws AuthFailureError {
                java.util.Map<String, String> jsonParams = new HashMap<String, String>();
                jsonParams.put("driver_id", driver_id);
                jsonParams.put("pay_id", payid);
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
