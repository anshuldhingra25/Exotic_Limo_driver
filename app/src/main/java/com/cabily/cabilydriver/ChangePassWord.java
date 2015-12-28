package com.cabily.cabilydriver;

import android.app.Dialog;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.android.volley.AuthFailureError;
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
import com.cabily.cabilydriver.widgets.PkDialog;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by user88 on 12/21/2015.
 */
public class ChangePassWord extends Fragment {

    private EditText Et_currrentpassword, Et_new_password, Et_new_confirm_password;
    private Button Bt_done;
    private Dialog dialog;
    private StringRequest postrequest;
    private String driver_id = "";
    private SessionManager session;
    private Boolean isInternetPresent = false;
    private ConnectionDetector cd;

    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootview = inflater.inflate(R.layout.change_password, container, false);
        init(rootview);
        Bt_done.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cd = new ConnectionDetector(getActivity());
                isInternetPresent = cd.isConnectingToInternet();
                if (isInternetPresent) {
                    changepassword_PostRequest(ServiceConstant.changepassword);
                    System.out.println("changepwd-----------" + ServiceConstant.changepassword);
                } else {
                    Alert(getResources().getString(R.string.alert_sorry_label_title), getResources().getString(R.string.alert_nointernet));
                }
            }
        });

        return rootview;

    }

    private void init(View rootview) {

        session = new SessionManager(getActivity());

        Et_currrentpassword = (EditText) rootview.findViewById(R.id.loginpage_currentPwd_edittext_label);
        Et_new_password = (EditText) rootview.findViewById(R.id.loginpage_new_Pwd_edittext_label);
        Et_new_confirm_password = (EditText) rootview.findViewById(R.id.loginpage_confirm_new_Pwd_edittext_label);
        Bt_done = (Button) rootview.findViewById(R.id.signin_main_button);

        HashMap<String, String> user = session.getUserDetails();
        driver_id = user.get(SessionManager.KEY_DRIVERID);

    }

    private void Alert(String title, String message) {
        final PkDialog mDialog = new PkDialog(getActivity());
        mDialog.setDialogTitle(title);
        mDialog.setDialogMessage(message);
        mDialog.setPositiveButton(getResources().getString(R.string.alert_label_ok), new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mDialog.dismiss();
                getActivity().finish();


            }
        });
        mDialog.show();
    }


    //--------------------------code for post forgot password-----------------------
    private void changepassword_PostRequest(String Url) {
        dialog = new Dialog(getActivity());
        dialog.getWindow();
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.custom_loading);
        dialog.setCanceledOnTouchOutside(false);
        dialog.show();

        System.out.println("loadin-----------");
        TextView dialog_title = (TextView) dialog.findViewById(R.id.custom_loading_textview);
        dialog_title.setText(getResources().getString(R.string.action_loading));


        postrequest = new StringRequest(Request.Method.POST, Url,
                new Response.Listener<String>() {

                    @Override
                    public void onResponse(String response) {
                        Log.e("changepwd", response);

                        System.out.println("changepwdresponse---------" + response);

                        String Str_status = "", Str_response = "";

                        try {
                            JSONObject object = new JSONObject(response);
                            Str_status = object.getString("status");
                            Str_response = object.getString("response");
                        } catch (Exception e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }

                        if (Str_status.equalsIgnoreCase("1")) {
                            Alert(getResources().getString(R.string.label_pushnotification_cashreceived), Str_response);
                        } else {
                            Alert(getResources().getString(R.string.alert_sorry_label_title), Str_response);
                        }

                        dialog.dismiss();


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
                headers.put("isapplication", ServiceConstant.isapplication);
                headers.put("applanguage", ServiceConstant.applanguage);

                return headers;
            }

            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> jsonParams = new HashMap<String, String>();
                jsonParams.put("driver_id", driver_id);
                jsonParams.put("password", Et_currrentpassword.getText().toString());
                jsonParams.put("new_password", Et_new_password.getText().toString());

                System.out.println("--------------driver_id-------------------" + driver_id);
                System.out.println("--------------password-------------------" + Et_currrentpassword.getText().toString());
                System.out.println("--------------new_password-------------------" + Et_currrentpassword.getText().toString());

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
