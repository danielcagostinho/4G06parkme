package com.example.parkinglot;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

public class ParkingSpotActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_parking_spot);
        setTitle("Parking Spots");
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.parkme_menu,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        switch (id)
        {
            case R.id.Main:
                //Change this to go to Home
                Toast.makeText(this, "Home", Toast.LENGTH_SHORT).show();
                break;
            case R.id.Settings:
                //Change this to go to Settings
                Toast.makeText(this, "Settings", Toast.LENGTH_SHORT).show();
                break;
            case R.id.Help:
                //Change this to go to Help
                Toast.makeText(this, "Help", Toast.LENGTH_SHORT).show();
                break;
            case android.R.id.home:
                //Back Button
                this.finish();
                break;
        }
        return true;
    }

    public void showDialog(View view){
        AlertDialog.Builder alert = new AlertDialog.Builder(this);
        alert.setTitle("Parking Spot #");
        alert.setMessage("show some information here");
        alert.setPositiveButton("Save Spot", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
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
}
