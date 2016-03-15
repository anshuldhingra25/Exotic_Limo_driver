package com.cabily.cabilydriver;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.Hockeyapp.ActionBarActivityHockeyApp;
import com.cabily.cabilydriver.widgets.PkDialog;
import com.squareup.picasso.MemoryPolicy;
import com.squareup.picasso.Picasso;

/**
 * Created by user88 on 3/8/2016.
 */
public class NewTripAlert extends ActionBarActivityHockeyApp {

    private ImageView riderlater_userimg, ridelater_call_userimg;
    private TextView Tv_ridelater_username, Tv_message, Tv_ridelater_userphoneno, Tv_ridelater_user_address, Tv_ridelater_time;
    private String SuserName = "", Suser_Mobileno = "", SUser_Message = "", SAction = "", SUser_Rating = "", SUser_image = "", SUser_pickup_location = "", Suser_Pickup_time = "", SRide_id = "";
    private RatingBar UserRating;
    private RelativeLayout layout_ok;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ridelater_alert);
        initialize();

        layout_ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
                Intent intent = new Intent(NewTripAlert.this, TripSummaryDetail.class);
                intent.putExtra("ride_id", SRide_id);
                startActivity(intent);
                overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
            }
        });


        ridelater_call_userimg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Suser_Mobileno != null) {
                    Intent callIntent = new Intent(Intent.ACTION_CALL);
                    callIntent.setData(Uri.parse("tel:" + Suser_Mobileno));
                    startActivity(callIntent);
                } else {
                    Alert(NewTripAlert.this.getResources().getString(R.string.alert_sorry_label_title), NewTripAlert.this.getResources().getString(R.string.arrived_alert_content1));
                }

            }
        });

    }


    private void initialize() {

        riderlater_userimg = (ImageView) findViewById(R.id.ridelater_userimage);
        ridelater_call_userimg = (ImageView) findViewById(R.id.call_userimg);
        Tv_ridelater_username = (TextView) findViewById(R.id.ridelater_username);
        Tv_ridelater_user_address = (TextView) findViewById(R.id.ridelater_useraddress);
        Tv_ridelater_time = (TextView) findViewById(R.id.ridelater_user_pickptime);
        UserRating = (RatingBar) findViewById(R.id.ridelater_user_ratings);
        Tv_message = (TextView) findViewById(R.id.newtrip_header_message);
        layout_ok = (RelativeLayout) findViewById(R.id.layout_ridelater_alert_ok);


        Intent i = getIntent();
        SAction = i.getStringExtra("Action");
        SUser_Message = i.getStringExtra("Message");
        SuserName = i.getStringExtra("Username");
        Suser_Mobileno = i.getStringExtra("Mobilenumber");
        SUser_Rating = i.getStringExtra("UserRating");
        SUser_image = i.getStringExtra("UserImage");
        SUser_pickup_location = i.getStringExtra("UserPickuplocation");
        Suser_Pickup_time = i.getStringExtra("UserPickupTime");
        SRide_id = i.getStringExtra("RideId");

        System.out.println("SRide_id-----------------" + SRide_id);

        Tv_message.setText(SUser_Message);
        Tv_ridelater_username.setText(SuserName);
        Tv_ridelater_user_address.setText(SUser_pickup_location);
        Tv_ridelater_time.setText(Suser_Pickup_time);
        UserRating.setRating(Float.parseFloat(SUser_Rating));


        Picasso.with(NewTripAlert.this).load(String.valueOf(SUser_image)).placeholder(R.drawable.nouserimg).memoryPolicy(MemoryPolicy.NO_CACHE).into(riderlater_userimg);

    }

    //--------------Alert Method-----------
    private void Alert(String title, String message) {
        final PkDialog mDialog = new PkDialog(NewTripAlert.this);
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
}
