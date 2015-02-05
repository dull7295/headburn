package com.example.android.sunshine.app;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.support.v7.widget.ShareActionProvider;

public class DetailActivity extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, new PlaceholderFragment())
                    .commit();
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_detail, menu);
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
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment {
        public String mforecastStr;
        final String FORECAST_SHARE_HASHTAG=" #sunshine";
        public PlaceholderFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_detail, container, false);
            //getting data from intent and displaying it on textview
            Intent i=getActivity().getIntent();
            mforecastStr=i.getStringExtra("SENDING");
            TextView textView =(TextView)rootView.findViewById(R.id.textview_detail);
            textView.setText(mforecastStr);
            return rootView;
        }
        @Override
        public void onCreate(Bundle bundle) {
            super.onCreate(bundle);
            setHasOptionsMenu(true);
        }

        @Override
        public boolean onOptionsItemSelected(MenuItem item) {
            return super.onOptionsItemSelected(item);
         }


        @Override
        public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
            super.onCreateOptionsMenu(menu, inflater);
            inflater.inflate(R.menu.detailfragment,menu);

            MenuItem item=menu.findItem(R.id.share_weather);
           ShareActionProvider sp=(ShareActionProvider) MenuItemCompat.getActionProvider(item);
           if(sp !=null)
           {
               sp.setShareIntent(getShareForecastIntent());
           } else {
               Log.d("Not Available","Shred Action Provider is not available");
           }

        }
        private Intent getShareForecastIntent(){
            Intent shareWeatherIntent=new Intent(Intent.ACTION_SEND);
            shareWeatherIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
            shareWeatherIntent.setType("text/plain");
            shareWeatherIntent.putExtra(Intent.EXTRA_TEXT, mforecastStr + FORECAST_SHARE_HASHTAG);
            return shareWeatherIntent;

        }
    }
}
