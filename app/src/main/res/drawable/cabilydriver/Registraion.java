package drawable.cabilydriver;

import android.app.Activity;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import com.Hockeyapp.ActionBarActivityHockeyApp;
import com.cabily.cabilydriver.R;


public class Registraion extends ActionBarActivityHockeyApp {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.accept_ride);
    }
}
