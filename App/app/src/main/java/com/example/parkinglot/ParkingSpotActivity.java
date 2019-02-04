package com.example.parkinglot;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
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

import com.example.parkinglot.Components.SettingsActivity;

public class ParkingSpotActivity extends AppCompatActivity {

    private static final String LOG_TAG = MapsActivity.class.getSimpleName();
    private Requestor requestor;

    Handler handler = new Handler();
    // Define the code block to be executed
    private Runnable runnableCode = new Runnable() {
        @Override
        public void run() {
            // Do something here on the main thread
            Log.d("Handlers", "Called on main thread");
            //upateView();
            // Repeat this the same runnable code block again another 2 seconds
            handler.postDelayed(runnableCode, 2000);
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_parking_spot);
        setTitle("Parking Spots");
        //Toolbar toolbar = findViewById(R.id.toolbar);
        //setSupportActionBar(toolbar);
        setupActionBar();
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // Start the initial runnable task by posting through the handler
        handler.post(runnableCode);
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

    /*public boolean onMenuItemSelected(int featureId, MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            if (!super.onMenuItemSelected(featureId, item)) {
                NavUtils.navigateUpFromSameTask(this);
            }
            return true;
        }
        return super.onMenuItemSelected(featureId, item);
    }*/

    public void launch_settings(MenuItem item) {
        Log.d(LOG_TAG, "Settings clicked!");
        Intent intent = new Intent(this, SettingsActivity.class);
        startActivity(intent);
    }

    private void updateView(Context context){

    }

    public void showDialog(View view){
        AlertDialog.Builder alert = new AlertDialog.Builder(this);
        alert.setTitle("Parking Spot #");
        alert.setMessage("show some information here");
        alert.setPositiveButton("Save Spot", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                ViewCompat.setBackgroundTintList(view, ContextCompat.getColorStateList(ParkingSpotActivity.this, android.R.color.holo_blue_light));
                Toast.makeText(ParkingSpotActivity.this, "Spot Saved", Toast.LENGTH_SHORT).show();
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

    public void updateColor(View view){
        int mID = getResources().getIdentifier("s5c57245fd347320000f51d0a", "id", getBaseContext().getPackageName());
        Button mButton = findViewById(mID);
        ViewCompat.setBackgroundTintList(mButton, ContextCompat.getColorStateList(this, android.R.color.holo_purple));
    }
}
