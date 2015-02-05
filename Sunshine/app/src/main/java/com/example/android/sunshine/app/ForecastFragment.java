package com.example.android.sunshine.app;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by murali on 1/20/15.
 */

/**
 * A placeholder fragment containing a simple view.
 */
public class ForecastFragment extends Fragment {
    private ArrayAdapter<String> mForeCastAdapter;
    public ForecastFragment() {
    }
    @Override
    public void onCreate(Bundle savedInstance) {
       super.onCreate(savedInstance);
        setHasOptionsMenu(true);
    }
    @Override
    public void onCreateOptionsMenu(Menu menu,MenuInflater inflater) {

        inflater.inflate(R.menu.forecastfragment,menu);
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
      int id =item.getItemId();
      if( id == R.id.action_refresh) {
          updateWeather();
          return true;
      }
        return super.onOptionsItemSelected(item);
    }
    @Override
    public void onStart() {
        updateWeather();
        super.onStart();
    }
    public void updateWeather() {
        SharedPreferences sp= PreferenceManager.getDefaultSharedPreferences(getActivity());
        String str=sp.getString(getString(R.string.pref_location_key),getString(R.string.pref_location_default));
        new FetchWeatherTask().execute(str);
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);

        mForeCastAdapter =new ArrayAdapter<String>(getActivity(),R.layout.list_item_forecast,R.id.list_item_forecast_textview);
        ListView listView=(ListView) rootView.findViewById(R.id.listview_forecast);
        listView.setAdapter(mForeCastAdapter);
         listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
             @Override
             public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                 String str=mForeCastAdapter.getItem(position).toString();
                 //Toast.makeText(getActivity(),str,Toast.LENGTH_SHORT).show();
                 Intent startDetailActivityIntent=new Intent(getActivity().getApplicationContext(),DetailActivity.class).
                         putExtra("SENDING",str);
                 startActivity(startDetailActivityIntent);

             }
         });

        return rootView;
    }
    private String[] getWeatherDataFromJson(String forecastJsonStr, int numDays)
            throws JSONException {

        // These are the names of the JSON objects that need to be extracted.
        final String OWM_LIST = "list";
        final String OWM_WEATHER = "weather";
        final String OWM_TEMPERATURE = "temp";
        final String OWM_MAX = "max";
        final String OWM_MIN = "min";
        final String OWM_DATETIME = "dt";
        final String OWM_DESCRIPTION = "main";

        JSONObject forecastJson = new JSONObject(forecastJsonStr);
        JSONArray weatherArray = forecastJson.getJSONArray(OWM_LIST);

        String[] resultStrs = new String[numDays];
        for(int i = 0; i < weatherArray.length(); i++) {
            // For now, using the format "Day, description, hi/low"
            String day;
            String description;
            String highAndLow;

            // Get the JSON object representing the day
            JSONObject dayForecast = weatherArray.getJSONObject(i);

            // The date/time is returned as a long.  We need to convert that
            // into something human-readable, since most people won't read "1400356800" as
            // "this saturday".
            long dateTime = dayForecast.getLong(OWM_DATETIME);
            day = getReadableDateString(dateTime);

            // description is in a child array called "weather", which is 1 element long.
            JSONObject weatherObject = dayForecast.getJSONArray(OWM_WEATHER).getJSONObject(0);
            description = weatherObject.getString(OWM_DESCRIPTION);

            // Temperatures are in a child object called "temp".  Try not to name variables
            // "temp" when working with temperature.  It confuses everybody.
            JSONObject temperatureObject = dayForecast.getJSONObject(OWM_TEMPERATURE);
            double high = temperatureObject.getDouble(OWM_MAX);
            double low = temperatureObject.getDouble(OWM_MIN);

            highAndLow = formatHighLows(high, low);
            resultStrs[i] = day + " - " + description + " - " + highAndLow;
        }

        return resultStrs;
    }
    private String getReadableDateString(long time){
        // Because the API returns a unix timestamp (measured in seconds),
        // it must be converted to milliseconds in order to be converted to valid date.
        Date date = new Date(time * 1000);
        SimpleDateFormat format = new SimpleDateFormat("E, MMM d");
        return format.format(date).toString();
    }

    /**
     * Prepare the weather high/lows for presentation.
     */
    private String formatHighLows(double high, double low) {


       SharedPreferences sp=PreferenceManager.getDefaultSharedPreferences(getActivity());
        String unitType=sp.getString(getString(R.string.pref_units_key),getString(R.string.pref_units_metric));
        String metricType=getString(R.string.pref_units_metric);
        String impType=getString(R.string.pref_units_imperial);

        if(unitType.equals(impType))
        {
            high=(high*1.8)+32;
            low=(low*1.8)+32;
        }else if(!unitType.equals(metricType)) {
            Log.d("UNIT TYPE","unit type not found");
            //Toast.makeText(getActivity(, "selected imperial", Toast.LENGTH_SHORT).show();
        }
        // For presentation, assume the user doesn't care about tenths of a degree.
        long roundedHigh = Math.round(high);
        long roundedLow = Math.round(low);

        String highLowStr = roundedHigh + "/" + roundedLow;
        return highLowStr;
    }

    public class FetchWeatherTask extends AsyncTask<String,Void,String[]> {
        private final String LOG_TAG = FetchWeatherTask.class.getSimpleName();

        @Override
        protected String [] doInBackground(String... params) {

            HttpURLConnection urlConnection =null;
            BufferedReader reader=null;
            // Will contain the raw JSON response as a string.
            String forecastJsonStr = null;

            try {

               final String FORECAST_STRING="http://api.openweathermap.org/data/2.5/forecast/daily?";
               final String QUERY_PARAM="q";
               final String FORMAT_PARAM="mode";
               final String UNITS_PARAM="metric";
               final String DAYS_PARAM="cnt";

               Uri builtUri=Uri.parse(FORECAST_STRING).buildUpon().
                       appendQueryParameter(QUERY_PARAM,params[0]).
                       appendQueryParameter(FORMAT_PARAM,"json").
                       appendQueryParameter(UNITS_PARAM,"metric").
                       appendQueryParameter(DAYS_PARAM,"7").build();
                Log.d(LOG_TAG,builtUri.toString());
                URL url=new URL(builtUri.toString());
                urlConnection=(HttpURLConnection)url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();
                InputStream inputStream=urlConnection.getInputStream();
                StringBuffer buffer=new StringBuffer();
                if (inputStream == null) {
                    // Nothing to do.
                    return null;
                }
                reader = new BufferedReader(new InputStreamReader(inputStream));

                String line;
                while ((line = reader.readLine()) != null) {
                    // Since it's JSON, adding a newline isn't necessary (it won't affect parsing)
                    // But it does make debugging a *lot* easier if you print out the completed
                    // buffer for debugging.
                    buffer.append(line + "\n");
                }

                if (buffer.length() == 0) {
                    // Stream was empty.  No point in parsing.
                    return null;
                }
                forecastJsonStr = buffer.toString();
                Log.d(LOG_TAG,forecastJsonStr);

            } catch (IOException e) {
                Log.e(LOG_TAG, "Error ", e);
                // If the code didn't successfully get the weather data, there's no point in attemping
                // to parse it.
                return null;
            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (final IOException e) {
                        Log.e(LOG_TAG, "Error closing stream", e);
                    }
                }
            }
            try {
                int numDays=7;
                 return getWeatherDataFromJson(forecastJsonStr, numDays);
            }catch(JSONException je)
            {
                Log.e(LOG_TAG,je.getMessage());
            }
            return null;
        }

        @Override
        protected void onPostExecute(String[] result) {
            if(result != null) {
                mForeCastAdapter.clear();
                for(String str: result) {
                    mForeCastAdapter.add(str);
                }
            }
        }


    }

}