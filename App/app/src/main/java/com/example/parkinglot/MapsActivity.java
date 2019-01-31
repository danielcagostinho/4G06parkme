package com.example.parkinglot;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.example.parkinglot.Components.ParkingLotItem;
import com.example.parkinglot.Components.ParkingLotListView;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocompleteFragment;
import com.google.android.gms.location.places.ui.PlaceSelectionListener;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnSuccessListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class MapsActivity extends FragmentActivity implements
        OnMapReadyCallback, GoogleMap.OnCameraIdleListener, GoogleMap.OnCameraMoveStartedListener {

    private GoogleMap mMap;
    private boolean movingMap =false;

    final private float MAX_ZOOM_OUT = 12f;
    private PlaceAutocompleteFragment autocompleteFragment;
    private FusedLocationProviderClient locationClient;

    public static String TAG = "Parking";

    private List<Marker> markers;

    Requestor requestor;


    ArrayList<ParkingLotItem> parkingLots;
    ListView listView;
    private static ParkingLotListView adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        markers = new ArrayList<Marker>();

        requestor = new Requestor(this);


        locationClient = LocationServices.getFusedLocationProviderClient(this);


        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        autoCompleteListener();
        setUpParkingLotList();
    }

    private void setUpParkingLotList() {
        listView=(ListView)findViewById(R.id.list);

        parkingLots = new ArrayList<>();
//        parkingLots.add(new ParkingLotItem("test", 0, 100,"114m"));

        adapter= new ParkingLotListView(parkingLots,getApplicationContext());
        listView.setAdapter(adapter);

        // set empty view
        TextView empty= findViewById(R.id.empty);
        listView.setEmptyView(empty);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                ParkingLotItem parkingLot = parkingLots.get(position);
                Log.d(TAG, "Clicked on a parking lot: " + parkingLot.name);
            }

        });
    }

    // auto complete listener set up
    private void autoCompleteListener() {

        autocompleteFragment = (PlaceAutocompleteFragment)
                getFragmentManager().findFragmentById(R.id.place_autocomplete_fragment);

        autocompleteFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(Place place) {

                // create marker at that positon
                mMap.addMarker(new MarkerOptions().position(place.getLatLng()).title(place.getName().toString()));

                moveCamera(place.getLatLng());
            }

            @Override
            public void onError(Status status) {
                // TODO: Handle the error.
            }
        });
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setOnCameraIdleListener(this);
        mMap.setOnCameraMoveStartedListener(this);
        mMap.getUiSettings().setZoomControlsEnabled(true);
        mMap.getUiSettings().setCompassEnabled(true);
        mMap.getUiSettings().setMyLocationButtonEnabled(true);
        mMap.getUiSettings().setAllGesturesEnabled(true);
        Log.d(TAG, "onMapReady...");
        getLocationAndNearbyLots(true);

    }

    private void getLocationAndNearbyLots(final boolean moveCamera) {
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            locationClient.getLastLocation().addOnSuccessListener(this, new OnSuccessListener<Location>() {
                @Override
                public void onSuccess(Location location) {

                    final LatLng currentLatLng = new LatLng(location.getLatitude(), location.getLongitude());

                    if (moveCamera)
                        moveCamera(currentLatLng);

                    // set bias for search
                    autocompleteFragment.setBoundsBias(new LatLngBounds(currentLatLng, currentLatLng));

                    float zoomLevel = mMap.getCameraPosition().zoom;

                    // dont bother if we are so far zoomed out
                    if (zoomLevel < MAX_ZOOM_OUT) {
                        purgeMarkers();
                        return;
                    }

                    // calculate distance between end points of screen for search radius
                    LatLng left = mMap.getProjection().getVisibleRegion().farLeft;
                    LatLng right = mMap.getProjection().getVisibleRegion().farRight;
                    float radius = getDistance(left, right);

                    // get parking lots relative to maps position
                    requestor.GetParkingLots(mMap.getCameraPosition().target, radius, new Response.Listener<String>() {

                        @Override
                        public void onResponse(String res) {

                            // remove all existing markers first
                            purgeMarkers();
                            try {
                                JSONObject response = new JSONObject(res);
                                JSONArray parkingLots = response.getJSONArray("parking_lots");
                                ArrayList<ParkingLotItem> parkingLotItems = new ArrayList<>();
                                // add a marker
                                for(int i =0; i< parkingLots.length();i++) {
                                    JSONObject lot = (JSONObject) parkingLots.get(i);
                                    JSONObject location = lot.getJSONObject("location");
                                    String addressName = lot.getJSONObject("address").get("street").toString();
                                    double latitude =  (double) location.get("lat");
                                    double longitude =  (double) location.get("long");
                                    LatLng latLng = new LatLng(latitude,longitude);



                                    // get parking space data
                                    JSONObject parkingSpaces = lot.getJSONObject("parking_spaces");

                                    // calculate distance
                                    float distance = getDistance(currentLatLng, latLng);
                                    String distanceString= "";

                                    // km or m
                                    if (distance < 1) {
                                        distanceString = Integer.toString((int) (distance*1000)) + "m";
                                    } else {
                                        distanceString = String.format("%0.2f",distance) + "km";
                                    }
                                    distanceString = "(" + distanceString + ")";

                                    // add parking lots to list
                                    parkingLotItems.add(new ParkingLotItem(addressName, parkingSpaces.getInt("available"), parkingSpaces.getInt("total"), distanceString ));
                                    Log.d(TAG, "Added Parking lots");


                                    // change color of marker based on availability
                                    float color = BitmapDescriptorFactory.HUE_RED;
                                    float percentage = (parkingSpaces.getInt("available") * 1.0f) / (parkingSpaces.getInt("total") * 1.0f);

                                    if (percentage >= 0.75f) {
                                        color = BitmapDescriptorFactory.HUE_RED;
                                    } else if (percentage >=0.5f) {
                                        color = BitmapDescriptorFactory.HUE_ORANGE;
                                    } else {
                                        color = BitmapDescriptorFactory.HUE_GREEN;
                                    }
                                    Marker marker = mMap.addMarker(new MarkerOptions().position(latLng).title(addressName).icon(BitmapDescriptorFactory
                                            .defaultMarker(color)));
                                    markers.add(marker);

                                    // update list
                                    adapter= new ParkingLotListView(parkingLotItems,getApplicationContext());
                                    listView.setAdapter(adapter);
                                }

                            } catch (JSONException e) {
                                Log.d(TAG, "onResponse: " + e);
                            }
                        }
                    }, new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            Log.d(TAG, "onErrorResponse: " + error);
                        }
                    });
                }
            });
            mMap.setMyLocationEnabled(true);
        } else {
            Log.e(TAG, "Permission denied");
        }
    }

    // moves the camera to a latlng
    private void moveCamera(LatLng latLng) {
        // move and zoom
        float zoomLevel = 16.0f; // goes up to 21

        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, zoomLevel));
    }

    // removes all markers
    private void purgeMarkers() {
        for(Marker marker: markers)
          marker.remove();
        markers.clear();
    }


    @Override
    public void onCameraIdle() {
        // only try if we actually moved
        if (movingMap) {
            Log.d(TAG, "onCameraIdle...");

            // update current location
            getLocationAndNearbyLots(false);
            movingMap = false;
        }

    }

    @Override
    public void onCameraMoveStarted(int reason) {
        if (reason == GoogleMap.OnCameraMoveStartedListener.REASON_GESTURE || reason == GoogleMap.OnCameraMoveStartedListener.REASON_DEVELOPER_ANIMATION) {
            movingMap = true;
            Log.d(TAG, "onCameraMoveStarted: Moving not tapping");
        }
    }

    // returns the distance betwen two lat/long points
    private float getDistance(LatLng a, LatLng b) {

        float[] results = new float[1];
        Location.distanceBetween(a.latitude, a.longitude, b.latitude, b.longitude, results);

        // convert to km
        return results[0] / 1000f;
    }
}
