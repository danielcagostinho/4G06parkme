package com.example.parkinglot;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.PointF;
import android.location.Location;
import android.support.annotation.NonNull;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import com.android.volley.*;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import com.example.parkinglot.Components.ParkingLotItem;
import com.example.parkinglot.Components.ParkingLotListView;

import com.mapbox.android.core.permissions.PermissionsListener;
import com.mapbox.android.core.permissions.PermissionsManager;
import com.mapbox.api.directions.v5.models.DirectionsResponse;
import com.mapbox.api.directions.v5.models.DirectionsRoute;
import com.mapbox.api.geocoding.v5.models.CarmenFeature;
import com.mapbox.geojson.Feature;
import com.mapbox.geojson.FeatureCollection;
import com.mapbox.geojson.Point;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.camera.CameraPosition;
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.location.LocationComponent;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.mapboxsdk.maps.Style;
import com.mapbox.mapboxsdk.plugins.places.autocomplete.PlaceAutocomplete;
import com.mapbox.mapboxsdk.plugins.places.autocomplete.model.PlaceOptions;
import com.mapbox.mapboxsdk.style.layers.PropertyFactory;
import com.mapbox.mapboxsdk.style.layers.SymbolLayer;
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource;
import com.mapbox.services.android.navigation.ui.v5.NavigationLauncher;
import com.mapbox.services.android.navigation.ui.v5.NavigationLauncherOptions;
import com.mapbox.services.android.navigation.ui.v5.route.NavigationMapRoute;
import com.mapbox.services.android.navigation.v5.navigation.NavigationRoute;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;


public class MapsActivity extends AppCompatActivity implements
        OnMapReadyCallback,
        MapboxMap.OnCameraIdleListener,
        MapboxMap.OnCameraMoveStartedListener,
        MapboxMap.OnMapClickListener,
        PermissionsListener {


    final private float MAX_ZOOM_OUT = 12f;
    final private int SEARCH_RESPONSE = 1;
    private boolean movingMap = false;
    private boolean initialized = false;

    private ParkingLotListView adapter;

    private ArrayList<ParkingLotItem> parkingLots;  // parking lots with in area
    private ListView listView;                      // list view of parking lots

    public static String TAG = "Parking";           // logging tag
    private MapView mapView;
    private MapboxMap mapBoxMap;

    // variables for adding location layer
    private PermissionsManager permissionsManager;
    private LocationComponent locationComponent;

    private Requestor requestor;

//     variables for calculating and drawing a route
    private DirectionsRoute currentRoute;
    private NavigationMapRoute navigationMapRoute;

    // variables needed to initialize navigation
    private FloatingActionButton navigationButton;

    // marker symbol vars
    private String destinationLayerID = "destination-layer";

    private String parkingLayerID = "parking-layer";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        PlaceAutocomplete.clearRecentHistory(this);
        Mapbox.getInstance(this, getString(R.string.mapbox_access_token));
        setContentView(R.layout.activity_maps);
        setUpParkingLotList();
        mapView = findViewById(R.id.mapView);
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(this);
        requestor = new Requestor(getApplicationContext());
    }


    @Override
    public void onMapReady(@NonNull final MapboxMap mapBoxMap) {
        this.mapBoxMap = mapBoxMap;
        mapBoxMap.addOnCameraIdleListener(this);
        mapBoxMap.addOnCameraMoveStartedListener(this);
        mapBoxMap.getUiSettings().setRotateGesturesEnabled(false);

        // SET UP STYLE: NEED
        mapBoxMap.setStyle(Style.MAPBOX_STREETS, new Style.OnStyleLoaded() {

            // do everything once map is loaded
            @Override
            public void onStyleLoaded(@NonNull Style style) {

                setSymbolLayers(style);

                enableLocationComponent(style);

                initSearchFab();

                mapBoxMap.addOnMapClickListener(MapsActivity.this);
                navigationButton = findViewById(R.id.navigationBtn);
                navigationButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (currentRoute == null)
                            return;
                        NavigationLauncherOptions options = NavigationLauncherOptions.builder()
                                .directionsRoute(currentRoute)
                                .build();
                        // Call this method with Context from within an Activity
                        NavigationLauncher.startNavigation(MapsActivity.this, options);
                    }
                });
            }});


    }

    private void setSymbolLayers(@NonNull  Style style) {

        style.addImage(("destination"), BitmapFactory.decodeResource(
                getResources(), R.drawable.map_default_map_marker));


        style.addLayer(new SymbolLayer("destination-layer", destinationLayerID)
                .withProperties(
                        PropertyFactory.iconImage("destination"),
                        PropertyFactory.iconIgnorePlacement(true),
                        PropertyFactory.iconAllowOverlap(true)
                ));

        style.addImage(("full"), BitmapFactory.decodeResource(
                getResources(), R.drawable.parking_lot_marker_full));

        style.addImage(("free"), BitmapFactory.decodeResource(
                getResources(), R.drawable.parking_lot_marker_free));

        style.addImage(("busy2"), BitmapFactory.decodeResource(
                getResources(), R.drawable.parking_lot_marker_busy2));

        style.addLayer(new SymbolLayer("parking-layer", parkingLayerID)
                .withProperties(
                        PropertyFactory.iconImage("{icon}"),
                        PropertyFactory.iconIgnorePlacement(true),
                        PropertyFactory.iconAllowOverlap(true)
                ));
    }

    @SuppressWarnings( {"MissingPermission"})
    private void initSearchFab() {
        findViewById(R.id.searchBtn).setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {

                // get user country for better search
                Location userLocation = locationComponent.getLastKnownLocation();
                String userCountry  = getApplication().getResources().getConfiguration().locale.getCountry();
                Intent intent = new PlaceAutocomplete.IntentBuilder()
                        .accessToken(Mapbox.getAccessToken())
                        .placeOptions(PlaceOptions.builder()
                                .backgroundColor(Color.parseColor("#EEEEEE"))
                                .limit(10)
                                .proximity(Point.fromLngLat(userLocation.getLongitude(), userLocation.getLatitude()))
                                .country(userCountry)
                                .build(PlaceOptions.MODE_FULLSCREEN))
                        .build(MapsActivity.this);
                startActivityForResult(intent, SEARCH_RESPONSE);
            }
        });
    }

    @SuppressWarnings( {"MissingPermission"})
    @Override
    public boolean onMapClick(@NonNull LatLng point) {
        PointF screenPoint = mapBoxMap.getProjection().toScreenLocation(point);
        List<Feature> parkingIcons = mapBoxMap.queryRenderedFeatures(screenPoint, parkingLayerID);
        List<Feature> destinationIcons = mapBoxMap.queryRenderedFeatures(screenPoint, destinationLayerID);
        if (!parkingIcons.isEmpty()) {
            Feature selectedFeature = parkingIcons.get(0);
            int index = selectedFeature.getNumberProperty("index").intValue();
            ParkingLotItem  lot = parkingLots.get(index);

            // get the route
            getAndDrawRoute(Point.fromLngLat(lot.latlong.getLongitude(), lot.latlong.getLatitude()));
        } else if (!destinationIcons.isEmpty()) {

            // if we clicked on destination, remove marker
            removeDestinationMarker();
            navigationButton.setEnabled(false);
            currentRoute = null;
            navigationMapRoute.removeRoute();
            Log.d(TAG, "Removed markers!");
        } else {

            // add marker at user clicker
            Point destinationPoint = Point.fromLngLat(point.getLongitude(), point.getLatitude());

            addDestinationMarker(destinationPoint);

            getAndDrawRoute(destinationPoint);
            navigationButton.setEnabled(true);
}

        return true;
    }

    // transition from search to main activity
    @SuppressWarnings( {"MissingPermission"})
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK && requestCode == SEARCH_RESPONSE) {

            CarmenFeature selectedCarmenFeature = PlaceAutocomplete.getPlace(data);
            Point location = (Point) selectedCarmenFeature.geometry();
            addDestinationMarker(location);
            getAndDrawRoute(location);
            moveCamera(location);
        }
    }

    // update nearby location upon move
    @Override
    public void onCameraIdle() {

        // only try if we actually moved
        if (movingMap && initialized) {
            // update current location
            getLocationAndNearbyLots(false);
            movingMap = false;
        }
    }

    @Override
    public void onCameraMoveStarted(int reason) {
        movingMap = true;
    }

    // sets up parking lot list
    @SuppressWarnings( {"MissingPermission"})
    private void setUpParkingLotList() {
        listView= findViewById(R.id.list);
        parkingLots = new ArrayList<>();

        adapter= new ParkingLotListView(parkingLots,getApplicationContext());
        listView.setAdapter(adapter);

        // set empty view
        TextView empty= findViewById(R.id.empty);
        listView.setEmptyView(empty);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // move to parking lot
                ParkingLotItem parkingLot = parkingLots.get(position);
                LatLng parkingLotPosition = parkingLot.latlong;
                moveCamera(parkingLot.latlong);

                currentRoute = parkingLot.getRoute();
                drawRoute(currentRoute);
            }
        });
    }

    @SuppressWarnings( {"MissingPermission"})
    private void enableLocationComponent(@NonNull Style loadedMapStyle) {
        FloatingActionButton userGPSButton = findViewById(R.id.userLocationBtn);
        // Check if permissions are enabled and if not request
        if (PermissionsManager.areLocationPermissionsGranted(this)) {

            // Activate the MapboxMap LocationComponent to show user location
            // Adding in LocationComponentOptions is also an optional parameter
            locationComponent = mapBoxMap.getLocationComponent();
            locationComponent.activateLocationComponent(this, loadedMapStyle);
            locationComponent.setLocationComponentEnabled(true);

            // Set the component's camera mode
//            locationComponent.setCameraMode(CameraMode.TRACKING);
            Location userLocation = locationComponent.getLastKnownLocation();
            userGPSButton.show();

            // move to user location on click
            userGPSButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Location userLocation = locationComponent.getLastKnownLocation();
                    moveCamera(userLocation);
                }
            });
            moveCamera(userLocation);
            initialized = true;
        } else {
            permissionsManager = new PermissionsManager(this);
            permissionsManager.requestLocationPermissions(this);
            userGPSButton.setEnabled(true);
            userGPSButton.hide();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        permissionsManager.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    public void onExplanationNeeded(List<String> permissionsToExplain) {
        // do nothing
    }

    @Override
    public void onPermissionResult(boolean granted) {
        if (granted) {
            enableLocationComponent(mapBoxMap.getStyle());
        } else {
            // do nothing
        }
    }

    private void getLocationAndNearbyLots(final boolean moveCamera) {
//        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

            Location location = locationComponent.getLastKnownLocation();
            // set position
            LatLng userPosition = new LatLng(location.getLatitude(), location.getLongitude());

            if (moveCamera)
                moveCamera(userPosition);

            double zoomLevel = mapBoxMap.getCameraPosition().zoom;

            // dont bother if we are so far zoomed out
            if (zoomLevel < MAX_ZOOM_OUT) {
                removeAllParkingLotMarkers();
                return;
            }

            // calculate distance between end points of screen for search radius
            LatLng left = mapBoxMap.getProjection().getVisibleRegion().farLeft;
            LatLng right = mapBoxMap.getProjection().getVisibleRegion().farRight;
            float radius = getDistance(left, right);


            // get parking lots relative to maps position
            requestor.GetParkingLots(userPosition, radius, new com.android.volley.Response.Listener<String>() {

                @Override
                public void onResponse(String res) {
                    try {
                        JSONObject response = new JSONObject(res);
                        JSONArray parkingLotsArray = response.getJSONArray("parking_lots");
                        ArrayList<ParkingLotItem> parkingLotItems = new ArrayList<>();
                        // add a marker
                        for (int i = 0; i < parkingLotsArray.length(); i++) {
                            JSONObject lot = (JSONObject) parkingLotsArray.get(i);
                            JSONObject location = lot.getJSONObject("location");
                            String addressName = lot.getJSONObject("address").get("street").toString();
                            double latitude = (double) location.get("lat");
                            double longitude = (double) location.get("long");
                            LatLng latLng = new LatLng(latitude, longitude);


                            // get parking space data
                            JSONObject parkingSpaces = lot.getJSONObject("parking_spaces");

                            // get percentage of available spots left
                            float percentage = 1f - (parkingSpaces.getInt("available") * 1.0f) / (parkingSpaces.getInt("total") * 1.0f);

                            // add parking lots to list
                            ParkingLotItem parkingLot = new ParkingLotItem(
                                    addressName,
                                    parkingSpaces.getInt("available"), latLng, percentage);

                            parkingLotItems.add(parkingLot);

                            // add to global
                            parkingLots.add(parkingLot);


                            getRoute(Point.fromLngLat(longitude, latitude),
                                    (DirectionsRoute route) -> {

                                        parkingLot.setRoute(route);
                                        // update list
                                        adapter = new ParkingLotListView(parkingLotItems, getApplicationContext());
                                        listView.setAdapter(adapter);
                                    },     // set parking route
                                    () -> {parkingLot.setRoute(null);});                          // set to nothing
                        }
                        addParkingLotMarkers(parkingLotItems);

                    } catch (JSONException e) {
                        Log.d(TAG, "onResponse JSON Error: " + e);
                    } catch (Exception e) {
                        Log.d(TAG, "onResponse Exception: " + e);
                    }

                }
            }, new com.android.volley.Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    // do nothing
                    Log.d(TAG, "onErrorResponse: " + error.toString());
                }
            });
        } else {
            Log.e(TAG, "Permission denied");
        }
    }

    // moves the camera to a latlng
    private void moveCamera(LatLng latLng) {
        // move and zoom
        float zoomLevel = 14.0f; // goes up to 21

        CameraPosition position = new CameraPosition.Builder()
                .target(latLng) // Sets the new camera position
                .zoom(zoomLevel) // Sets the zoom
                .build(); // Creates a CameraPosition from the builder

        mapBoxMap.animateCamera(CameraUpdateFactory
                .newCameraPosition(position), 2000);
    }

    private void moveCamera(Point point) {
        moveCamera(new LatLng(point.latitude(), point.longitude()));
    }

    private void moveCamera(Location location) {
        moveCamera(new LatLng(location.getLatitude(), location.getLongitude()));
    }

    // returns the distance betwen two lat/long points
    private float getDistance(LatLng a, LatLng b) {

        double result = a.distanceTo(b);

        // convert to km
        return (float) (result / 1000f);
    }

    // gets the icon of the parking lot marker on maps
    private String getParkingLotIcon(float percentage) {
        if (percentage >= 0.9f)
            return "full";
        else if (percentage >= 0.50f)
            return "busy2";
        return "free";
    }

    @SuppressWarnings({"MissingPermission"})
    private void getRoute(Point destination, Consumer<DirectionsRoute> onRouteObtain, Runnable onError) {

        // user is always origin
        Location userLocation =locationComponent.getLastKnownLocation();
        Point origin = Point.fromLngLat(userLocation.getLongitude(),userLocation.getLatitude());

        NavigationRoute.builder(this)
            .accessToken(Mapbox.getAccessToken())
            .origin(origin)
            .destination(destination)
            .build()
            .getRoute(new Callback<DirectionsResponse>() {

                @Override
                public void onResponse(Call<DirectionsResponse> call, Response<DirectionsResponse> response) {
                    // You can get the generic HTTP info about the response
                    if (response.body() == null) {
                        onError.run();
                        return;
                    } else if (response.body().routes().size() < 1) {
                        onError.run();
                        return;
                    }

                    // call consumer
                    onRouteObtain.accept(response.body().routes().get(0));
                }

                @Override
                public void onFailure(Call<DirectionsResponse> call, Throwable throwable) {
                    onError.run();
                }
            });

    }

    // draws the route and enabled nav button
    private void drawRoute(DirectionsRoute route) {
        if (route == null) {
            Log.d(TAG, "No route available");
            return;
        }
        if (navigationMapRoute != null) {
            navigationMapRoute.removeRoute();
        } else {
            navigationMapRoute = new NavigationMapRoute(null, mapView, mapBoxMap, R.style.NavigationMapRoute);
        }
        navigationMapRoute.addRoute(route);
        navigationButton.setEnabled(true);
    }

    // helper function to get and draw a route
    private void getAndDrawRoute(Point point) {
        getRoute(point, (DirectionsRoute route) -> {
            currentRoute = route;
            drawRoute(route);
        }, () -> {
            navigationButton.setEnabled(false);
        });
    }
    // adds user input/searched destination marker
    private void addDestinationMarker(Point destination) {
        Style style = mapBoxMap.getStyle();
        Feature lot = Feature.fromGeometry(Point.fromLngLat(destination.longitude(), destination.latitude()));
        GeoJsonSource destinationGeoJsonSource = style.getSourceAs(destinationLayerID);
        if (destinationGeoJsonSource == null) {
            destinationGeoJsonSource = new GeoJsonSource(destinationLayerID, lot);
            style.addSource(destinationGeoJsonSource);

        } else {
            // update data
            destinationGeoJsonSource.setGeoJson(lot);
        }
    }

    // removes all destination markers
    private void removeDestinationMarker() {
        Style style = mapBoxMap.getStyle();
        GeoJsonSource destinationGeoJsonSource = style.getSourceAs(destinationLayerID);
        if (destinationGeoJsonSource != null) {
            destinationGeoJsonSource.setGeoJson(FeatureCollection.fromFeatures(new ArrayList<>()));
        }
    }

    // adds parking lot markers to view
    private void addParkingLotMarkers(ArrayList<ParkingLotItem> parkingLots) {

        Style style = mapBoxMap.getStyle();
        List<Feature> features = new ArrayList<>();

        // add each parking lot feature with percentage and position
        for (ParkingLotItem lot : parkingLots) {
            Point latlng = Point.fromLngLat(lot.latlong.getLongitude(), lot.latlong.getLatitude());
            Feature feat = Feature.fromGeometry(latlng);
            feat.addStringProperty("icon", getParkingLotIcon(lot.percentage));      // add percentage
            feat.addNumberProperty("index", parkingLots.indexOf(lot));              // add index
            features.add(feat);
        }

        GeoJsonSource lotsGeoJsonSource = style.getSourceAs(parkingLayerID);

        // add features on map
        if (lotsGeoJsonSource == null) {
            lotsGeoJsonSource = new GeoJsonSource(parkingLayerID, FeatureCollection.fromFeatures(features));
            style.addSource(lotsGeoJsonSource);

        } else {
            // update data
            lotsGeoJsonSource.setGeoJson(FeatureCollection.fromFeatures(features));
        }
    }

    // removes all parking lot markers
    private void removeAllParkingLotMarkers() {
        Style style = mapBoxMap.getStyle();
        GeoJsonSource parking = style.getSourceAs(parkingLayerID);
        if (parking != null) {
            parking.setGeoJson(FeatureCollection.fromFeatures(new ArrayList<>()));
        }
    }

}
