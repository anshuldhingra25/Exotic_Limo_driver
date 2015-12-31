package drawable.cabilydriver;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.Hockeyapp.FragmentHockeyApp;
import com.android.volley.AuthFailureError;
import com.android.volley.NetworkError;
import com.android.volley.NoConnectionError;
import com.android.volley.ParseError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.ServerError;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.cabily.cabilydriver.R;
import com.cabily.cabilydriver.Utils.AppController;
import com.cabily.cabilydriver.Utils.SessionManager;
import com.cabily.cabilydriver.widgets.PkDialog;
import com.special.ResideMenu.ResideMenu;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by user14 on 9/22/2015.
 */
public class BankAccount extends FragmentHockeyApp implements View.OnClickListener, TextWatcher {

    private StringRequest postrequest;
    private SessionManager session;
    private String driver_id = "";
    private View parentView;
    private ResideMenu resideMenu;
    private EditText holder_name, holder_address, account_no, bankname, branchname, branchaddress, ifsccode, routingno;
    private Button save_btn;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        parentView = inflater.inflate(R.layout.bank_account, container, false);
        try {
            String trip_summary = getActivity().getResources().getString(R.string.bank_account);
            getActivity().setTitle("" + trip_summary);
        } catch (Exception e) {
        }
        parentView.findViewById(R.id.ham_home).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(resideMenu != null ){
                    resideMenu.openMenu(ResideMenu.DIRECTION_LEFT);
                }
            }
        });
        initialize(parentView);
        return parentView;
    }

    private void initialize(View rootview) {
        holder_name = (EditText) rootview.findViewById(R.id.bank_ac_holder_name);
        holder_name.addTextChangedListener(this);
        holder_address = (EditText) rootview.findViewById(R.id.bank_ac_holder_address);
        account_no = (EditText) rootview.findViewById(R.id.bank_ac_account_number);
        bankname = (EditText) rootview.findViewById(R.id.bank_ac_bank_name);
        branchname = (EditText) rootview.findViewById(R.id.bank_ac_branch_name);
        branchaddress = (EditText) rootview.findViewById(R.id.bank_ac_branch_address);
        ifsccode = (EditText) rootview.findViewById(R.id.bank_ac_ifsc_code);
        routingno = (EditText) rootview.findViewById(R.id.bank_ac_routing_number);
        save_btn = (Button) rootview.findViewById(R.id.bank_ac_save_button);
        session = new SessionManager(getActivity());

        HashMap<String, String> user = session.getUserDetails();
        driver_id = user.get(SessionManager.KEY_DRIVERID);
        postRequest("/provider/get-banking-info");
        save_btn.setOnClickListener(this);
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



    @Override
    public void afterTextChanged(Editable s) {
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        Character c;
        c = s.charAt(0);
        if (holder_name.getText().toString().length() == 0) {
            holder_name.setError("Name is required!");
        }
    }


    @Override
    public void onClick(View v) {
        if (!isValidName(holder_name.getText().toString())) {
            holder_name.setError(getResources().getString(R.string.action_alert_bank_Username));
        } else if (!isValid(holder_address.getText().toString())) {
            holder_address.setError(getResources().getString(R.string.action_alert_bank_address));
        } else if (!isValid(account_no.getText().toString())) {
            account_no.setError(getResources().getString(R.string.action_alert_bank_accountno));
        } else if (!isValid(bankname.getText().toString())) {
            bankname.setError(getResources().getString(R.string.action_alert_bank_name));
        } else if (!isValid(branchname.getText().toString())) {
            branchname.setError(getResources().getString(R.string.action_alert_branch_name));
        } else if (!isValid(branchaddress.getText().toString())) {
            branchaddress.setError(getResources().getString(R.string.action_alert_branch_address));
        } else if (!isValid(ifsccode.getText().toString())) {
            ifsccode.setError(getResources().getString(R.string.action_alert_bank_ifs_code));
        } else if (!isValid(routingno.getText().toString())) {
            routingno.setError(getResources().getString(R.string.action_alert_bank_routingno));
        } else {
            save("/provider/save-banking-info");
        }

    }

    // validating holder name
    private boolean isValidName(String name) {
        String NAME_PATTERN = "[A-Z][a-zA-Z]*";
        Pattern pattern = Pattern.compile(NAME_PATTERN);
        Matcher matcher = pattern.matcher(name);
        return matcher.matches();
    }

    // validating password with retype password
    public boolean isValid(String pass) {
        if ((pass.startsWith(" ")) || (pass.length() == 0)) {
            return false;
        }
        return true;
    }


    private void postRequest(String Url) {
        postrequest = new StringRequest(Request.Method.POST, Url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                String status = "", response1 = "", detail = "", acc_holder_name = "", acc_holder_address = "", acc_number = "", bank_name = "", branch_name = "", branch_address = "", swift_code = "", routing_number = "";
                try {
                    JSONObject object = new JSONObject(response);
                    status = object.getString("status");
                    response1 = object.getString("response");
                    JSONObject object1 = new JSONObject(response1);
                    detail = object1.getString("banking");
                    JSONObject object2 = new JSONObject(detail);
                    acc_holder_name = object2.getString("acc_holder_name");
                    acc_holder_address = object2.getString("acc_holder_address");
                    acc_number = object2.getString("acc_number");
                    bank_name = object2.getString("bank_name");
                    branch_name = object2.getString("branch_name");
                    branch_address = object2.getString("branch_address");
                    swift_code = object2.getString("swift_code");
                    routing_number = object2.getString("routing_number");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                if (status.equalsIgnoreCase("1")) {
                    holder_name.setText(acc_holder_name);
                    holder_address.setText(acc_holder_address);
                    account_no.setText(acc_number);
                    bankname.setText(bank_name);
                    branchname.setText(branch_name);
                    branchaddress.setText(branch_address);
                    ifsccode.setText(swift_code);
                    routingno.setText(routing_number);
                } else {
                    Alert(getResources().getString(R.string.alert_sorry_label_title), getResources().getString(R.string.action_alert_bankinfo_invails));
                }
            }
        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {

                if (error instanceof TimeoutError || error instanceof NoConnectionError) {
                    Toast.makeText(getActivity(), "Unable to fetch data from server", Toast.LENGTH_LONG).show();
                } else if (error instanceof AuthFailureError) {
                    Toast.makeText(getActivity(), "AuthFailureError", Toast.LENGTH_LONG).show();
                } else if (error instanceof ServerError) {
                    Toast.makeText(getActivity(), "ServerError", Toast.LENGTH_LONG).show();
                } else if (error instanceof NetworkError) {
                    Toast.makeText(getActivity(), "NetworkError", Toast.LENGTH_LONG).show();
                } else if (error instanceof ParseError) {
                    Toast.makeText(getActivity(), "ParseError", Toast.LENGTH_LONG).show();
                }
            }
        }) {

            @Override
            protected java.util.Map<String, String> getParams() throws AuthFailureError {
                java.util.Map<String, String> jsonParams = new HashMap<String, String>();
                jsonParams.put("driver_id", driver_id);
                return jsonParams;
            }

        };
        AppController.getInstance().addToRequestQueue(postrequest);
    }


    //-------------------------------save data--------------------------------------------------//

    private void save(String Url) {

        postrequest = new StringRequest(Request.Method.POST, Url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                System.out.println("--------------Bank Details reponse-------------------" + response);
                String status = "", response1 = "", detail = "", acc_holder_name = "", acc_holder_address = "", acc_number = "", bank_name = "", branch_name = "", branch_address = "", swift_code = "", routing_number = "";

                try {

                    JSONObject object = new JSONObject(response);
                    status = object.getString("status");
                    response1 = object.getString("response");

                } catch (JSONException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                if (status.equalsIgnoreCase("1")) {
                    Toast.makeText(getActivity(), getResources().getString(R.string.alertsaved_label_title), Toast.LENGTH_LONG).show();
                } else {
                    //work
                    Alert(getResources().getString(R.string.alert_sorry_label_title),response1);
                }
            }
        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {

                if (error instanceof TimeoutError || error instanceof NoConnectionError) {
                    Toast.makeText(getActivity(), "Unable to fetch data from server", Toast.LENGTH_LONG).show();
                } else if (error instanceof AuthFailureError) {
                    Toast.makeText(getActivity(), "AuthFailureError", Toast.LENGTH_LONG).show();
                } else if (error instanceof ServerError) {
                    Toast.makeText(getActivity(), "ServerError", Toast.LENGTH_LONG).show();
                } else if (error instanceof NetworkError) {
                    Toast.makeText(getActivity(), "NetworkError", Toast.LENGTH_LONG).show();
                } else if (error instanceof ParseError) {
                    Toast.makeText(getActivity(), "ParseError", Toast.LENGTH_LONG).show();
                }
            }
        }) {

            @Override
            protected java.util.Map<String, String> getParams() throws AuthFailureError {
                java.util.Map<String, String> jsonParams = new HashMap<String, String>();
                jsonParams.put("driver_id", driver_id);
                jsonParams.put("acc_holder_name", holder_name.getText().toString());
                jsonParams.put("acc_holder_address", holder_address.getText().toString());
                jsonParams.put("acc_number", account_no.getText().toString());
                jsonParams.put("bank_name", bankname.getText().toString());
                jsonParams.put("branch_name", branchname.getText().toString());
                jsonParams.put("branch_address", branchaddress.getText().toString());
                jsonParams.put("swift_code", ifsccode.getText().toString());
                jsonParams.put("routing_number", routingno.getText().toString());


                return jsonParams;
            }

        };
        AppController.getInstance().addToRequestQueue(postrequest);
    }


}
