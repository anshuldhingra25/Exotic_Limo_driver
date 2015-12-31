package drawable.cabilydriver;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.RatingBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.cabily.cabilydriver.*;
import com.cabily.cabilydriver.CancelTrip;
import com.cabily.cabilydriver.Utils.ConnectionDetector;
import com.cabily.cabilydriver.Utils.RoundedImageView;
import com.cabily.cabilydriver.Utils.SessionManager;
import com.cabily.cabilydriver.subclass.SubclassActivity;
import com.squareup.picasso.MemoryPolicy;
import com.squareup.picasso.Picasso;

/**
 * Created by user88 on 10/30/2015.
 */
public class UserInfo extends SubclassActivity {

    private ConnectionDetector cd;
    private Boolean isInternetPresent = false;
    private SessionManager session;

    private TextView Tv_userName, Tv_usermobile_no, Tv_username_header;
    private RatingBar ratingBar;
    private RelativeLayout Rl_layout_back;
    private Button Bt_canceltrip;
    private RoundedImageView userimage;

    private String Str_username = "", Str_userrating = "", Str_usermobilno = "", Str_rideId = "";
    private String Str_user_img = "";

    public static com.cabily.cabilydriver.UserInfo userInfo_class;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.user_info);
        userInfo_class = com.cabily.cabilydriver.UserInfo.this;
        initialize();
        Rl_layout_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
                overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
            }
        });
        Bt_canceltrip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(com.cabily.cabilydriver.UserInfo.this, CancelTrip.class);
                intent.putExtra("RideId", Str_rideId);
                startActivity(intent);
                overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
            }
        });

    }

    private void initialize() {
        cd = new ConnectionDetector(com.cabily.cabilydriver.UserInfo.this);
        isInternetPresent = cd.isConnectingToInternet();
        session = new SessionManager(com.cabily.cabilydriver.UserInfo.this);
        Tv_userName = (TextView) findViewById(R.id.userinfo_usernamedetail);
        Tv_usermobile_no = (TextView) findViewById(R.id.userinfo_user_mobileno);
        ratingBar = (RatingBar) findViewById(R.id.user_ratings);
        Tv_username_header = (TextView) findViewById(R.id.user_info_user_name_head);
        Rl_layout_back = (RelativeLayout) findViewById(R.id.layout_user_info_back);
        Bt_canceltrip = (Button) findViewById(R.id.userinfo_canceltrip);
        userimage = (RoundedImageView) findViewById(R.id.userinfo_user_image);
        Intent i = getIntent();
        Str_username = i.getStringExtra("user_name");
        Str_userrating = i.getStringExtra("user_rating");
        Str_usermobilno = i.getStringExtra("user_phoneno");
        Str_user_img = i.getStringExtra("user_image");
        Str_rideId = i.getStringExtra("RideId");
        Tv_userName.setText(Str_username);
        Tv_usermobile_no.setText(Str_usermobilno);
        ratingBar.setRating(Float.parseFloat(Str_userrating));
        Tv_username_header.setText(Str_username);
        Picasso.with(com.cabily.cabilydriver.UserInfo.this).load(String.valueOf(Str_user_img)).placeholder(R.drawable.nouserimg).memoryPolicy(MemoryPolicy.NO_CACHE).into(userimage);
    }


}
