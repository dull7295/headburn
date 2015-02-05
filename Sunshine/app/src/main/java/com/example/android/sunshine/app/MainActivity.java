package com.example.android.sunshine.app;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;


public class MainActivity extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, new ForecastFragment())
                    .commit();
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            Intent i=new Intent(this,SettingsActivity.class);
             startActivity(i);
            return true;
        } else if(id ==R.id.action_locateme) {
            locateme();
        } else if (id==R.id.action_refresh) {
            Log.e("mainactivity handling","action refresh******");
        }

        return super.onOptionsItemSelected(item);
    }
   public void locateme() {
       SharedPreferences sp= PreferenceManager.getDefaultSharedPreferences(this);
       String location=sp.getString(getString(R.string.pref_location_key),getString(R.string.pref_location_default));
       Uri geoLocation=Uri.parse("geo:0,0?").buildUpon().appendQueryParameter("q",location).build();
       Intent openMap=new Intent(Intent.ACTION_VIEW);
       openMap.setData(geoLocation);
       if(openMap.resolveActivity(getPackageManager()) !=null)
       {
           startActivity(openMap);
       }else {
           Log.e("hello","not yet started google map ohhhh ");
       }
   }

}
