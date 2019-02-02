package com.example.parkinglot;

import android.content.Context;
import android.support.annotation.Nullable;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.mapbox.mapboxsdk.geometry.LatLng;


public class Requestor {

    final String REQUEST_BASE = "http://192.168.1.161:8000/api/";


    RequestQueue queue;

    public Requestor(Context context) {
        queue = Volley.newRequestQueue(context);


    }

    // make a basic get request
    private void makeGETRequest(String url, Response.Listener<String> onResponse, @Nullable Response.ErrorListener OnErrorResponse) {
        url =REQUEST_BASE + url ;
        // Request a string response from the provided URL.
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url, onResponse, OnErrorResponse);

        // Add the request to the RequestQueue.
        queue.add(stringRequest);
    }

    // Gets all parking lots in an area
    public void GetParkingLots(LatLng currentPosition, float radius, Response.Listener<String> success, Response.ErrorListener failure) {
        makeGETRequest(String.format("parkinglots/radius?latitude=%s&longitude=%s&radius=%s",currentPosition.getLatitude(),currentPosition.getLongitude(),radius), success, failure);
    }
}
