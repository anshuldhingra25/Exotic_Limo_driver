package drawable.cabilydriver.Utils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

import com.cabily.cabilydriver.HomePage;


import java.util.HashMap;

public class SessionManager {
    // Shared Preferences
    SharedPreferences pref;
    // Editor for Shared preferences
    Editor editor;
    // Context
    Context _context;
    // Shared pref mode
    int PRIVATE_MODE = 0;
    // Sharedpref file name
    private static final String PREF_NAME = "AndroidHivePref";
    // All Shared Preferences Keys
    private static final String IS_LOGIN = "IsLoggedIn";
    // Email address (make variable public to access from outside)
    public static final String KEY_EMAIL = "email";
    public static final String KEY = "key";
    public static final String KEY_DRIVER_NAME = "drivername";
    public static final String KEY_DRIVERID = "driverid";
    public static final String KEY_DRIVER_IMAGE = "driverimage";
    public static final String KEY_VEHICLENO = "vechicleno";
    public static final String KEY_VEHICLE_MODEL = "vehiclemodel";
    public static final String KEY_SEC_KEY = "vehiclemodel";
    public static final String KEY_VEHICLE_NAME = "VEHICLE_NAME";
    public static final String KEY_ONLINE = "online";

    // Constructor
    @SuppressLint("CommitPrefEdits")
    public SessionManager(Context context) {
        this._context = context;
        pref = _context.getSharedPreferences(PREF_NAME, PRIVATE_MODE);
        editor = pref.edit();
    }

    /**
     * Create login session
     */
    public void createLoginSession(String image, String driverid, String name, String email, String vehicleno, String vechilemodel, String key, String sec_key) {
        editor.putBoolean(IS_LOGIN, true);
        editor.putString(KEY_DRIVER_IMAGE, image);
        editor.putString(KEY_DRIVERID, driverid);
        editor.putString(KEY_DRIVER_NAME, name);
        editor.putString(KEY_EMAIL, email);
        editor.putString(KEY_VEHICLENO, vehicleno);
        editor.putString(KEY_VEHICLE_MODEL, vechilemodel);
        editor.putString(KEY_SEC_KEY, sec_key);
        editor.putString(KEY, key);
        editor.commit();
    }

    public void createSessionOnline(String online){
        editor.putString(KEY_ONLINE, online);
        editor.commit();
    }

    /**
     * */
    public HashMap<String, String> getUserDetails() {
        HashMap<String, String> user = new HashMap<String, String>();
        user.put(KEY_DRIVER_IMAGE, pref.getString(KEY_DRIVER_IMAGE, ""));
        user.put(KEY_DRIVERID, pref.getString(KEY_DRIVERID, ""));
        user.put(KEY_DRIVER_NAME, pref.getString(KEY_DRIVER_NAME, ""));
        user.put(KEY_EMAIL, pref.getString(KEY_EMAIL, ""));
        user.put(KEY_VEHICLENO, pref.getString(KEY_VEHICLENO, ""));
        user.put(KEY_VEHICLE_MODEL, pref.getString(KEY_VEHICLE_MODEL, ""));
        user.put(KEY, pref.getString(KEY, ""));
        return user;
    }

    public HashMap<String, String> getOnlineDetails() {
        HashMap<String, String> online = new HashMap<String, String>();
        online.put(KEY_ONLINE, pref.getString(KEY_ONLINE, ""));
        return online;
    }




    public void setUserVehicle(String name){
        editor.putString(KEY_VEHICLE_NAME, name);
        editor.commit();
    }

    public String getUserVehicle(){
        return pref.getString(KEY_VEHICLE_NAME, "");
    }

    /**
     * Clear session details
     */
    public void logoutUser() {
        editor.clear();
        editor.commit();
        Intent i = new Intent(_context, HomePage.class);
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        _context.startActivity(i);
    }

    /**
     * Quick check for login
     * *
     */
    // Get Login State
    public boolean isLoggedIn() {
        return pref.getBoolean(IS_LOGIN, false);
    }
}