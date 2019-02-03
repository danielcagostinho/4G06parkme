package com.otaliastudios.zoom.demo;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import com.otaliastudios.zoom.ZoomImageView;
import com.otaliastudios.zoom.ZoomLayout;
import com.otaliastudios.zoom.ZoomLogger;

import androidx.appcompat.app.AppCompatActivity;


public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ZoomLogger.setLogLevel(ZoomLogger.LEVEL_INFO);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setTitle("Parking Lot");
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        /*final Button buttonZoomLayout = findViewById(R.id.show_zl);
        final Button buttonZoomImage = findViewById(R.id.show_ziv);
        final ZoomLayout zoomLayout = findViewById(R.id.zoom_layout);
        final ZoomImageView zoomImage = findViewById(R.id.zoom_image);
        zoomImage.setImageDrawable(new ColorGridDrawable());

        buttonZoomLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                zoomImage.setVisibility(View.GONE);
                zoomLayout.setVisibility(View.VISIBLE);
                buttonZoomImage.setAlpha(0.65f);
                buttonZoomLayout.setAlpha(1f);
            }
        });

        buttonZoomImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                zoomLayout.setVisibility(View.GONE);
                zoomImage.setVisibility(View.VISIBLE);
                buttonZoomLayout.setAlpha(0.65f);
                buttonZoomImage.setAlpha(1f);
            }
        });

        buttonZoomLayout.performClick();*/
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
                Toast.makeText(this, "Home", Toast.LENGTH_SHORT).show();
                break;
            case R.id.Settings:
                Toast.makeText(this, "Settings", Toast.LENGTH_SHORT).show();
                break;
            case R.id.Help:
                Toast.makeText(this, "Help", Toast.LENGTH_SHORT).show();
                break;
            case android.R.id.home:
                this.finish();
                break;
        }
        return true;
    }
}
