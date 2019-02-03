package com.example.parkinglot;

import android.location.Location;

import com.mapbox.geojson.Point;
import com.mapbox.mapboxsdk.camera.CameraPosition;
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.maps.MapboxMap;

public class CameraTranslator {

    // moves the camera to a latlng
    public static void moveCamera(MapboxMap mapBoxMap, LatLng latLng) {
        // move and zoom
        float zoomLevel = 14.0f; // goes up to 21

        CameraPosition position = new CameraPosition.Builder()
                .target(latLng) // Sets the new camera position
                .zoom(zoomLevel) // Sets the zoom
                .build(); // Creates a CameraPosition from the builder

        mapBoxMap.animateCamera(CameraUpdateFactory
                .newCameraPosition(position), 2000);
    }

    public static void moveCamera(MapboxMap mapBoxMap, Point point) {
        CameraTranslator.moveCamera(mapBoxMap, new LatLng(point.latitude(), point.longitude()));
    }

    public static void moveCamera(MapboxMap mapBoxMap, Location location) {
        moveCamera(mapBoxMap, new LatLng(location.getLatitude(), location.getLongitude()));
    }
}
