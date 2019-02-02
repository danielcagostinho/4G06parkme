package com.example.parkinglot.Components;

import com.google.android.gms.maps.model.LatLng;

public class ParkingLotItem {

    public String name;

    public int availableParkingSpaces;

    public int totalParkingSpaces;

    public String distance;

    public LatLng latlong;
    public ParkingLotItem(String name, int availableParkingSpaces, int totalParkingSpaces, String distance, LatLng latlong) {
        this.name = name;
        this.availableParkingSpaces = availableParkingSpaces;
        this.totalParkingSpaces = totalParkingSpaces;
        this.distance = distance;
        this.latlong = latlong;
    }
}
