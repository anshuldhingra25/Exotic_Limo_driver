package drawable.cabilydriver;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.cabily.cabilydriver.*;
import com.cabily.cabilydriver.BaseActivity;

import java.text.NumberFormat;
import java.util.Currency;
import java.util.Locale;

/**
 * Created by user88 on 11/6/2015.
 */
public class PushNotificationAlert extends BaseActivity {
    private TextView Message_Tv,Textview_Ok,Textview_alert_header;
    private  String message="",action="",amount="",rideid="",currency_code="",str_amount="";
    private RelativeLayout Rl_layout_alert_ok;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.pushnotification);
        initialize();

        Rl_layout_alert_ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                System.out.println("action-----------" + action);

                if (action.equalsIgnoreCase("ride_cancelled")) {
                    finish();
                } else if (action.equalsIgnoreCase("receive_cash")) {

                    System.out.println("inside receive_cash-----------" + str_amount);

                    Intent intent = new Intent(com.cabily.cabilydriver.PushNotificationAlert.this, PaymentPage.class);
                    intent.putExtra("amount", str_amount);
                    intent.putExtra("rideid", rideid);
                    startActivity(intent);
                    overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
                    finish();

                } else if (action.equalsIgnoreCase("payment_paid")) {
                    Intent intent = new Intent(com.cabily.cabilydriver.PushNotificationAlert.this, RatingsPage.class);
                    startActivity(intent);
                    overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
                    finish();
                }
            }
        });
    }


    private void initialize() {
        Intent i = getIntent();
        message = i.getStringExtra("Message");
        action = i.getStringExtra("Action");
        amount = i.getStringExtra("amount");
        rideid = i.getStringExtra("RideId");
        currency_code = i.getStringExtra("Currencycode");

        Textview_Ok = (TextView)findViewById(R.id.pushnotification_alert_ok_textview);
        Message_Tv = (TextView)findViewById(R.id.pushnotification_alert_messge_textview);
        Textview_alert_header = (TextView)findViewById(R.id.pushnotification_alert_messge_label);
        Rl_layout_alert_ok = (RelativeLayout)findViewById(R.id.pushnotification_alert_ok);
        Message_Tv.setText(message);

          if (action.equalsIgnoreCase("ride_cancelled")){
              Textview_alert_header.setText(getResources().getString(R.string.label_pushnotification_canceled));
          }else if(action.equalsIgnoreCase("receive_cash")){
              Textview_alert_header.setText(getResources().getString(R.string.label_pushnotification_cashreceived));
          }else if(action.equalsIgnoreCase("payment_paid")){
              Textview_alert_header.setText(getResources().getString(R.string.label_pushnotification_ride_completed));
          }

        if(currency_code!=null)
        {
            Currency currencycode= Currency.getInstance(getLocale(currency_code));
            str_amount=currencycode.getSymbol()+amount;
        }
    }




    //method to convert currency code to currency symbol
    private static Locale getLocale(String strCode) {

        for (Locale locale : NumberFormat.getAvailableLocales()) {
            String code = NumberFormat.getCurrencyInstance(locale).getCurrency().getCurrencyCode();
            if (strCode!=null&&strCode.equals(code)) {
                return locale;
            }
        }
        return null;
    }

}
