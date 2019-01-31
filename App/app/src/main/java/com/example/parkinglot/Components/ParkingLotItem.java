package com.example.parkinglot.Components;

public class ParkingLotItem {

    public String name;

    public int availableParkingSpaces;

    public int totalParkingSpaces;

    public String distance;

    public ParkingLotItem(String name, int availableParkingSpaces, int totalParkingSpaces, String distance) {
        this.name = name;
        this.availableParkingSpaces = availableParkingSpaces;
        this.totalParkingSpaces = totalParkingSpaces;
        this.distance = distance;
    }
}
