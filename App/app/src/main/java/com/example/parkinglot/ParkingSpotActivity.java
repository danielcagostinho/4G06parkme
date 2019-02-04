package com.example.parkinglot;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.support.v4.app.NavUtils;
import android.support.v7.app.ActionBar;

import android.content.res.Resources;
import android.graphics.Color;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.android.volley.VolleyError;
import com.example.parkinglot.Components.ParkingLotItem;
import com.example.parkinglot.Components.ParkingLotListView;
import com.example.parkinglot.Components.SettingsActivity;
import com.mapbox.api.directions.v5.models.DirectionsRoute;
import com.mapbox.geojson.Point;
import com.mapbox.mapboxsdk.geometry.LatLng;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.example.parkinglot.Components.ParkingSpot;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

public class ParkingSpotActivity extends AppCompatActivity {

    private static final String LOG_TAG = MapsActivity.class.getSimpleName();
    private Requestor requestor;

    private ArrayList <ParkingSpot> parkingSpots;

    private String recommended;
    private String savedSpot;

    private Timer mTimer1;
    private TimerTask mTt1;
    private Handler mTimerHandler = new Handler();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_parking_spot);
        setTitle("Parking Spots");
        setupActionBar();
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        SharedPreferences mSettings = getApplicationContext().getSharedPreferences("Settings", Context.MODE_PRIVATE);

        requestor = new Requestor(getApplicationContext());

        //poll
        startTimer();

        runOnUiThread(new Runnable(){
            @Override
            public void run(){
                parseInfo(getApplicationContext(),"5c49470d78dea5feb9d02a2c" );
            }
        });

    }

    private void stopTimer(){
        if (mTimer1 != null) {

            mTimer1.cancel();
            mTimer1.purge();
        }
    }
    private void startTimer(){
        mTimer1 = new Timer();
        mTt1 = new TimerTask(){
            public void run(){
                mTimerHandler.post(new Runnable(){
                    public void run(){
                        parseInfo(getApplicationContext(),"5c49470d78dea5feb9d02a2c" );
                    }
                });
            }

       };
        mTimer1.schedule(mTt1, 1, 5000);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.parkme_menu, menu);
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
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void setupActionBar() {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            // Show the Up button in the action bar.
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }


    public void launch_settings(MenuItem item) {
        Log.d(LOG_TAG, "Settings clicked!");
        Intent intent = new Intent(this, SettingsActivity.class);
        startActivity(intent);
    }

    private void updateView(Context context, SharedPreferences mPref){
            //parse request
        String lot_name = "5c49470d78dea5feb9d02a2c";

        for (int i = 0; i < parkingSpots.size(); i++){
            if (parkingSpots.get(i).occupancy.equals("false")){
                if (parkingSpots.get(i).accessibility.equals("true")){
                    updateColor("s"+parkingSpots.get(i).id, android.R.color.holo_blue_dark);
                }
                else{
                    updateColor("s"+parkingSpots.get(i).id, android.R.color.holo_green_dark);
                }

            }else{
                updateColor("s"+parkingSpots.get(i).id, android.R.color.darker_gray);
            }


        }
        updateColor(recommended, android.R.color.holo_orange_light);
    }

    private void parseInfo(Context context, String lot_name){
        SharedPreferences mSettings = getApplicationContext().getSharedPreferences("Settings", Context.MODE_PRIVATE);
        parkingSpots = new ArrayList<>();

        requestor.getLotInfo(lot_name ,(String res) -> {
            try {
                // get res
                JSONObject response = new JSONObject(res);
                JSONArray parkingSpotsArray = response.getJSONArray("parkingspaces");

                // loop through each lot
                for (int i = 0; i < parkingSpotsArray.length(); i++) {

                    JSONObject spot = (JSONObject) parkingSpotsArray.get(i);
                    String id = spot.getString("id");
                    String accessible = (spot.has("accessible") ? spot.getString("accessible") : "false");
                    String occupancy = spot.getString("occupancy");

                    ParkingSpot parkingSpot = new ParkingSpot(
                            id,
                            accessible,
                            occupancy);
                    parkingSpots.add(parkingSpot);
                }
                showBestSpot(context, mSettings, "5c49470d78dea5feb9d02a2c");

            } catch (JSONException e) {
                Log.d(LOG_TAG, "onResponse JSON Error: " + e);
            } catch (Exception e) {
                Log.d(LOG_TAG, "onResponse Exception: " + e);
            }
        }, (VolleyError error) -> {
            // do nothing
            Log.d(LOG_TAG, "Volley Error: " + error.toString());
        });

    }

    public void showBestSpot(Context context, SharedPreferences mPref, String lotname){

        requestor.getBestSpot(context, mPref, lotname ,(String res) -> {
            try {
                // get res
                JSONObject response = new JSONObject(res);
                String spot_rec = response.getString("id");

                recommended = "s"+ spot_rec;
                updateView(context, mPref);
                Log.d(LOG_TAG, "Recommended: " + recommended);
            } catch (JSONException e) {
                Log.d(LOG_TAG, "onResponse JSON Error: " + e);
            } catch (Exception e) {
                Log.d(LOG_TAG, "onResponse Exception: " + e);
            }

        }, (VolleyError error) -> {
            // do nothing
            Log.d(LOG_TAG, "Volley Error: " + error.toString());
        });

    }

    public void showDialog(View view){
        AlertDialog.Builder alert = new AlertDialog.Builder(this);
        Button b = (Button) view;
        String spotNumber = b.getText().toString();
        alert.setTitle("Parking Spot #" + spotNumber);
        alert.setMessage("Would you like to save this spot?");
        alert.setPositiveButton("Save Spot", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                ViewCompat.setBackgroundTintList(view, ContextCompat.getColorStateList(ParkingSpotActivity.this, android.R.color.holo_orange_light));
                //updateColor("s5c4946c9ac8f790000f001cf", android.R.color.holo_purple);

                //ViewCompat.setBackgroundTintList(view, ContextCompat.getColorStateList(ParkingSpotActivity.this, android.R.color.holo_blue_light));

                //Toast.makeText(ParkingSpotActivity.this, "Spot Saved", Toast.LENGTH_SHORT).show();
                savedSpot = Integer.toString(view.getId());
            }
        });
        alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Toast.makeText(ParkingSpotActivity.this, "Cancelled", Toast.LENGTH_SHORT).show();
            }
        });
        alert.create().show();
    }

    public void updateColor(String id, int colour){
        int mID = getResources().getIdentifier(id, "id", getBaseContext().getPackageName());
        Button mButton = findViewById(mID);
        ViewCompat.setBackgroundTintList(mButton, ContextCompat.getColorStateList(this, colour));
    }
}
