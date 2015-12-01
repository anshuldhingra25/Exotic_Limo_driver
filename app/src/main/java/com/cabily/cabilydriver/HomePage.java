package com.cabily.cabilydriver;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

import com.Hockeyapp.ActivityHockeyApp;
import com.app.xmpp.ChatingService;
import com.cabily.cabilydriver.Utils.SessionManager;

import me.drakeet.materialdialog.MaterialDialog;


public class HomePage extends ActivityHockeyApp {
    private Button mSignIn;
    private Button mRegister;
    SessionManager session;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.homepage);
        mSignIn = (Button) findViewById(R.id.btn_signin);
        mRegister = (Button) findViewById(R.id.btn_register);
        session = new SessionManager(HomePage.this);

        session.createSessionOnline("0");

        if (session.isLoggedIn()) {
            ChatingService.startDriverAction(HomePage.this);
            Intent i = new Intent(getApplicationContext(), NavigationDrawer.class);
            startActivity(i);
        }
        mSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(getApplicationContext(), LoginPage.class);
                startActivity(i);
                overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
            }

        });
        mRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Alert(HomePage.this.getResources().getString(R.string.lbel_alert_inform), HomePage.this.getResources().getString(R.string.lbel_alert_inform2));
            }
        });
    }

    private void Alert(String title, String alert) {
        final MaterialDialog dialog = new MaterialDialog(HomePage.this);
        dialog.setTitle(title)
                .setMessage(alert)
                .setPositiveButton(
                        "OK", new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                dialog.dismiss();
                            }
                        }
                )
                .show();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
