package com.cabily.cabilydriver;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBar;
import android.view.MotionEvent;
import android.view.View;

import com.android.volley.Request;
import com.app.service.ServiceConstant;
import com.app.service.ServiceManager;
import com.cabily.cabilydriver.Utils.SessionManager;
import com.special.ResideMenu.ResideMenu;
import com.special.ResideMenu.ResideMenuItem;

import java.util.HashMap;

/**
 */
public class NavigationDrawer extends BaseActivity implements View.OnClickListener {

    private ResideMenu resideMenu;
    private ResideMenuItem itemHome;
    private ResideMenuItem itemTripsummary;
    private ResideMenuItem itemBankaccount;
    private ResideMenuItem itemPaymentStatement;
    private ResideMenuItem itemChangepassword;
    private ResideMenuItem itemLogout;
    private SessionManager session;
    private ActionBar actionBar;

    /**
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        actionBar = getSupportActionBar();
        actionBar.setDisplayShowTitleEnabled(false);
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setHomeButtonEnabled(true);
        actionBar.setIcon(R.drawable.drawer_icon);
        session = new SessionManager(NavigationDrawer.this);
        setUpMenu();

        if (savedInstanceState == null)
            changeFragment(new DashBoardDriver());
    }


    public ActionBar getActionBarSupport() {
        return actionBar;
    }

    private void setUpMenu() {
        // attach to current activity;
        resideMenu = new ResideMenu(this);
        resideMenu.setBackground(R.drawable.menu_background);
        resideMenu.attachToActivity(this);
        resideMenu.setScaleValue(0.6f);
        resideMenu.setSwipeDirectionDisable(1);
        // create menu items;
        String trip_summary = getResources().getString(R.string.trip_summary);
        itemHome = new ResideMenuItem(this, R.drawable.icon_home, "Home");
        itemTripsummary = new ResideMenuItem(this, R.drawable.icon_profile, "" + trip_summary);
        itemBankaccount = new ResideMenuItem(this, R.drawable.icon_calendar, "Bank Account");
        itemPaymentStatement = new ResideMenuItem(this, R.drawable.icon_settings, "Payment Details");
        itemChangepassword = new ResideMenuItem(this,R.drawable.password_new,"Change Password");
        itemLogout = new ResideMenuItem(this, R.drawable.icon_settings, "Logout");
        itemHome.setOnClickListener(this);
        itemTripsummary.setOnClickListener(this);
        itemBankaccount.setOnClickListener(this);
        itemPaymentStatement.setOnClickListener(this);
        itemChangepassword.setOnClickListener(this);

        itemLogout.setOnClickListener(this);
        resideMenu.addMenuItem(itemHome, ResideMenu.DIRECTION_LEFT);
        resideMenu.addMenuItem(itemTripsummary, ResideMenu.DIRECTION_LEFT);
        resideMenu.addMenuItem(itemBankaccount, ResideMenu.DIRECTION_LEFT);
        resideMenu.addMenuItem(itemPaymentStatement, ResideMenu.DIRECTION_LEFT);
        resideMenu.addMenuItem(itemChangepassword,ResideMenu.DIRECTION_LEFT);
        resideMenu.addMenuItem(itemLogout, ResideMenu.DIRECTION_LEFT);
    }

    @Override
    public void onBackPressed() {
        // showBackPressedDialog(false);
    }

    private void showBackPressedDialog(final boolean isLogout) {
        System.gc();
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.dialog_app_exit)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        if (isLogout) {
                            logout();
                        }
                        finish();
                    }
                })
                .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.dismiss();
                    }
                });
        Dialog dialog = builder.create();
        dialog.show();
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        return resideMenu.dispatchTouchEvent(ev);
    }

    @Override
    public void onClick(View view) {
        if (R.id.ic_menu == view.getId()) {
            resideMenu.openMenu(ResideMenu.DIRECTION_LEFT);
        } else if (view == itemHome) {
            changeFragment(new DashBoardDriver());
        } else if (view == itemTripsummary) {
            changeFragment(new TripSummeryList());
        } else if (view == itemBankaccount) {
            changeFragment(new BankDetails());
        } else if (view == itemPaymentStatement) {
            changeFragment(new PaymentDetails());
        }else if(view == itemChangepassword){
            changeFragment(new ChangePassWord());
        }  else if (view == itemLogout) {
            showBackPressedDialog(true);
        }
        resideMenu.closeMenu();

    }

    private void logout() {
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                session.logoutUser();
            }
        });

            thread.start();
       // session.logoutUser();
        showDialog("Logout");
        HashMap<String, String> jsonParams = new HashMap<String, String>();
        HashMap<String, String> userDetails = session.getUserDetails();
        String driverId = userDetails.get("driverid");
        jsonParams.put("driver_id", "" + driverId);
        jsonParams.put("device", "" + "ANDROID");
        ServiceManager manager = new ServiceManager(this, updateAvailablityServiceListener);
        manager.makeServiceRequest(ServiceConstant.LOGOUT_REQUEST, Request.Method.POST, jsonParams);
    }

    private ServiceManager.ServiceListener updateAvailablityServiceListener = new ServiceManager.ServiceListener() {
        @Override
        public void onCompleteListener(Object object) {
            dismissDialog();
        }

        @Override
        public void onErrorListener(Object error) {
            dismissDialog();
        }
    };

    private void changeFragment(Fragment targetFragment) {
        resideMenu.clearIgnoredViewList();
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.main_fragment, targetFragment, "fragment")
                .setTransitionStyle(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
                .commit();
    }

    // What good method is to access resideMenu？
    public ResideMenu getResideMenu() {
        return resideMenu;
    }
}


