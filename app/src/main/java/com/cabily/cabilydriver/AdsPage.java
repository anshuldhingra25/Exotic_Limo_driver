package com.cabily.cabilydriver;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

/**
 */
public class AdsPage extends Activity {
    private ImageView Iv_close, Iv_banner;
    private TextView Tv_title, Tv_message;
    private View Vi_space;
    private String sTitle = "", sMessage = "", mBanner = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ads_page);
        initialize();

        Iv_close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
                overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
            }
        });
    }

    private void initialize() {
        Iv_close = (ImageView) findViewById(R.id.ads_page_close_imageView);
        Iv_banner = (ImageView) findViewById(R.id.ads_page_banner_image);
        Tv_title = (TextView) findViewById(R.id.ads_page_title);
        Tv_message = (TextView) findViewById(R.id.ads_page_message);
        Vi_space = (View) findViewById(R.id.ads_page_view);

        Intent intent = getIntent();

        if (intent.hasExtra("AdsTitle")) {
            sTitle = intent.getStringExtra("AdsTitle");
        }

        if (intent.hasExtra("AdsMessage")) {
            sMessage = intent.getStringExtra("AdsMessage");
        }

        if (intent.hasExtra("AdsBanner")) {
            mBanner = intent.getStringExtra("AdsBanner");
        }

        if (mBanner.length() > 0) {
            Iv_banner.setVisibility(View.VISIBLE);
            Vi_space.setVisibility(View.GONE);

            Picasso.with(AdsPage.this).load(String.valueOf(mBanner)).into(Iv_banner);

        } else {
            Iv_banner.setVisibility(View.GONE);
            Vi_space.setVisibility(View.VISIBLE);
        }

        Tv_title.setText(sTitle);
        Tv_message.setText(sMessage);

    }


    //-----------------Move Back on pressed phone back button------------------
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if ((keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0)) {
            finish();
            overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
            return true;
        }
        return false;
    }
}