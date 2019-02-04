package com.example.parkinglot.Components;

import com.mapbox.api.directions.v5.models.DirectionsRoute;
import com.mapbox.mapboxsdk.geometry.LatLng;

import org.json.JSONObject;


public class ParkingSpot {
    public String id;

    public String accessibility;

    public String occupancy;

    public ParkingSpot(String id, String accessibility, String occupancy) {
        this.id = id;
        this.accessibility = accessibility;
        this.occupancy = occupancy;
    }




}
