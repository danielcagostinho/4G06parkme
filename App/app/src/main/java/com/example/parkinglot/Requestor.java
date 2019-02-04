package com.example.parkinglot;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.parkinglot.Components.RequestorConstants;
import com.mapbox.mapboxsdk.geometry.LatLng;



public class Requestor {

    RequestQueue queue;

    public Requestor(Context context) {
        queue = Volley.newRequestQueue(context);
    }

    // make a basic get request
    private void makeGETRequest(String url, Response.Listener<String> onResponse, @Nullable Response.ErrorListener OnErrorResponse) {
        url = RequestorConstants.REQUEST_BASE + url;
        // Request a string response from the provided URL.
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url, onResponse, OnErrorResponse);

        // Add the request to the RequestQueue.
        queue.add(stringRequest);
    }

    // Gets all parking lots in an area
    public void GetParkingLots(LatLng currentPosition, double radius, Response.Listener<String> success, Response.ErrorListener failure) {
        makeGETRequest(String.format("parkinglots/radius?latitude=%s&longitude=%s&radius=%s",currentPosition.getLatitude(),currentPosition.getLongitude(),radius), success, failure);
    }
    public void getClosestSpot(Context context, SharedPreferences sharedPreferences, Response.Listener<String> success, Response.ErrorListener failure){
        SharedPreferences mSharedPreference= PreferenceManager.getDefaultSharedPreferences(context);
        String access= Boolean.toString(mSharedPreference.getBoolean("access_switch", false));
        String prox = mSharedPreference.getString("spot_pref", "0");
        makeGETRequest(String.format("parkinglots/best?accessible=%s&preference=%s", access, prox ),success, failure);
    }
    public void getLotInfo(Context context, String lot, Response.Listener<String> success, Response.ErrorListener failure){
        makeGETRequest(String.format("parkinglots/best?accessible=%s&preference=%s", lot),success, failure);
    }
}
