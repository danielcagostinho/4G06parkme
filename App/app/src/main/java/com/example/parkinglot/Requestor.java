package com.example.parkinglot;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.mapbox.mapboxsdk.geometry.LatLng;



public class Requestor {

    RequestQueue queue;
    Context context;

    public Requestor(Context context) {
        queue = Volley.newRequestQueue(context);
        this.context = context;
    }

    // make a basic get request
    private void makeGETRequest(String url, Response.Listener<String> onResponse, @Nullable Response.ErrorListener OnErrorResponse) {
        SharedPreferences mSettings= PreferenceManager.getDefaultSharedPreferences(context);
        url = mSettings.getString("http", "") + "/api/" + url;
        // Request a string response from the provided URL.
        Log.d(MapsActivity.TAG, "get:" + url);
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url, onResponse, OnErrorResponse);
        Log.d(MapsActivity.TAG, "get url " + url);
        // Add the request to the RequestQueue.
        queue.add(stringRequest);
    }

    // Gets all parking lots in an area
    public void GetParkingLots(LatLng currentPosition, double radius, Response.Listener<String> success, Response.ErrorListener failure) {
        makeGETRequest(String.format("parkinglots/radius?latitude=%s&longitude=%s&radius=%s",currentPosition.getLatitude(),currentPosition.getLongitude(),radius), success, failure);
    }
    public void getBestSpot(Context context, String lotname, Response.Listener<String> success, Response.ErrorListener failure){
        SharedPreferences mSharedPreferences= PreferenceManager.getDefaultSharedPreferences(context);
        String access = Boolean.toString(mSharedPreferences.getBoolean("access_switch", false));
        String prox = mSharedPreferences.getString("spot_pref", "0");
        makeGETRequest(String.format("parkinglot/%s/parkingspaces/best?accessible=%s&preference=%s", lotname, access, prox ),success, failure);
    }
    public void getLotInfo(String lot, SharedPreferences sharedPreferences,  Response.Listener<String> success, Response.ErrorListener failure){
        makeGETRequest(String.format("parkinglot/%s/parkingspaces/all", lot),success, failure);
    }
}
