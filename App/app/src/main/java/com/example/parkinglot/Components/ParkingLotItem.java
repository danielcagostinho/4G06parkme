package com.example.parkinglot.Components;

import com.mapbox.api.directions.v5.models.DirectionsRoute;
import com.mapbox.mapboxsdk.geometry.LatLng;

public class ParkingLotItem {

    public String name;

    public int availableParkingSpaces;

    public int totalParkingSpaces;


    public float percentage;    // how full a parking lot is

    public LatLng latlong;

    DirectionsRoute route;

    public ParkingLotItem(String name, int availableParkingSpaces, LatLng latlong, float percentage) {
        this.name = name;
        this.availableParkingSpaces = availableParkingSpaces;
        this.latlong = latlong;
        this.percentage = percentage;
    }


    public void setRoute(DirectionsRoute route) {
        this.route = route;
    }

    // gets the route
    public DirectionsRoute getRoute() {
        return route;
    }

    // gets the distance in the route
    protected String getDistance() {

        // calculate distance
        if (route == null)
            return "";

        double distance = route.distance();
        String distanceString;

        // km or m
        if (distance < 1000) {
            distanceString = Integer.toString((int) (distance)) + "m";
        } else {
            distanceString = String.format("%.2f",distance/1000) + "km";
        }
        distanceString = "(" + distanceString + ")";
        return distanceString;
    }

    // gets the time
    protected String getTime() {
        if (route == null)
            return "";
        double d =  route.duration();
        int duration = (int) d;
        int minutes = duration / 60;
        int hours = minutes / 60;
        minutes -= (hours* 60);

        String result ="";
        if (hours > 0)
            result += (Integer.toString(hours) +" hr ");
        if (minutes > 0)
            result += (Integer.toString(minutes) + " min");
        if (hours == 0 && minutes ==0) {
            return "<1 min";
        }
        return result;
    }
}
