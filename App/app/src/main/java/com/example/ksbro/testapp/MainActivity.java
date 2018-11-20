package com.example.ksbro.testapp;

import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;


import com.android.volley.RequestQueue;
import com.android.volley.Response.Listener;
import com.android.volley.VolleyError;
import com.android.volley.Response;
import com.android.volley.Request;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONException;

import com.android.volley.toolbox.BasicNetwork;
import com.android.volley.toolbox.DiskBasedCache;
import com.android.volley.toolbox.HurlStack;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import java.net.URL;

import static android.os.SystemClock.sleep;
import static com.android.volley.Request.*;
import static com.android.volley.Request.Method.GET;
import static com.android.volley.Response.*;


public class MainActivity extends AppCompatActivity {


    RequestQueue rq;
    String surl = "http://10.0.2.2:8000/Parking";

    Button btInfo;

    // Used to load the 'native-lib' library on application startup.
    static {
        System.loadLibrary("native-lib");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        final Button button = findViewById(R.id.button2);
        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                ImageView img = (ImageView) findViewById(R.id.imageView11);
                img.setImageResource(R.drawable.full);
            }
        });

        this.btInfo = (Button) findViewById(R.id.buttoninfo);
        //this.tvRepoList = (TextView) findViewById(R.id.tv_repo_list);  // Link our repository list text output box.
        //this.tvRepoList.setMovementMethod(new ScrollingMovementMethod());
        rq = Volley.newRequestQueue(this);



    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }



    private void callNodeJS() {
        // First, we insert the username into the repo url.
        // The repo url is defined in GitHubs API docs (https://developer.github.com/v3/repos/).


        // Next, we create a new JsonArrayRequest. This will use Volley to make a HTTP request
        // that expects a JSON Array Response.
        // To fully understand this, I'd recommend reading the office docs: https://developer.android.com/training/volley/index.html

            JsonRequest arrReq = new JsonObjectRequest(GET, surl, null,
                    new Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                            // Check the length of our response (to see if the user has any repos)
                            if (response.length() > 0) {
                                // The user does have repos, so let's loop through them all.
                                for (int i = 0; i < response.length(); i++) {
                                    try {
                                        // For each repo, add a new line to our repo list.
                                        JSONArray jsonArr = response.getJSONArray("parkingSpaces");
                                        //through array
                                        for (int j = 0; j < jsonArr.length(); j++) {
                                            JSONObject jsonobject = jsonArr.getJSONObject(j);
                                            JSONObject loc = jsonobject.getJSONObject("location");
                                            String x = loc.getString("xPos");
                                            String y = loc.getString("yPos");
                                            Integer avail = jsonobject.getInt("spotStatus");
                                            System.out.println(x + ", " + y + ": " + avail);
                                            int drawMode = (avail != 1) ? R.drawable.full : R.drawable.open;
                                            if (x.equals("0") & y.equals("0")) {
                                                ImageView img = (ImageView) findViewById(R.id.imageView11);
                                                img.setImageResource(drawMode);
                                                System.out.println("check1");
                                            }
                                            else if (x.equals("0") & y.equals("1")) {
                                                ImageView img = (ImageView) findViewById(R.id.imageView12);
                                                img.setImageResource(drawMode);
                                                System.out.println("check3");
                                            } else if (x.equals("0") & y.equals("2")) {
                                                ImageView img = (ImageView) findViewById(R.id.imageView21);
                                                img.setImageResource(drawMode);
                                                System.out.println("check5");
                                            }
                                        }

                                    } catch (JSONException e) {
                                        // If there is an error then output this to the logs.
                                        Log.e("Volley", "Invalid JSON Object.");
                                        Log.e("Volley", e.toString());
                                    }

                                }
                            }

                        }
                    },

                    new ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            // If there a HTTP error then add a note to our repo list.
                            //setRepoListText(error.toString());

                            Log.e("Volley", error.toString());
                        }
                    }
            );
            // Add the request we just defined to our request queue.
            // The request queue will automatically handle the request as soon as it can.
            rq.add(arrReq);

    }

    public void getInfoClicked(View v) {
        // Clear the repo list (so we have a fresh screen to add to)
        //clearRepoList();
        // Call our getRepoList() function that is defined above and pass in the
        // text which has been entered into the etGitHubUser text input field.
        callNodeJS();
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

    /**
     * A native method that is implemented by the 'native-lib' native library,
     * which is packaged with this application.
     */
    public native String stringFromJNI();


    //String url = "http://localhost:8000/Parking";

    //final TextView mTextView = (TextView) findViewById(R.id.text);

    // Request a string response from the provided URL.
    /*StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
            new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
                    // Display the first 500 characters of the response string.
                    mTextView.setText("Response is: "+ response.substring(0,500));
                }
            }, new Response.ErrorListener() {
        @Override
        public void onErrorResponse(VolleyError error) {
            mTextView.setText("That didn't work!");
        }
    });


    // Get a RequestQueue
    RequestQueue queue = MySingleton.getInstance(this.getApplicationContext()).
            getRequestQueue();


    JsonObjectRequest jsonObjectRequest = new JsonObjectRequest
            (Request.Method.GET, url, null, new Response.Listener<JSONObject>() {

                @Override
                public void onResponse(JSONObject response) {
                    mTextView.setText("Response: " + response.toString());
                }
            }, new Response.ErrorListener() {

                @Override
                public void onErrorResponse(VolleyError error) {
                    // TODO: Handle error

                }
            });

        // Access the RequestQueue through your singleton class.
        //MySingleton.getInstance(this).addToRequestQueue(jsonObjectRequest);*/

}
