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
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
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

import com.example.parkinglot.Components.SettingsActivity;
import com.mapbox.android.core.permissions.PermissionsListener;
import com.mapbox.android.core.permissions.PermissionsManager;
import com.mapbox.api.directions.v5.models.DirectionsResponse;
import com.mapbox.api.directions.v5.models.DirectionsRoute;
import com.mapbox.api.geocoding.v5.models.CarmenFeature;
import com.mapbox.geojson.Feature;
import com.mapbox.geojson.FeatureCollection;
import com.mapbox.geojson.Point;
import com.mapbox.mapboxsdk.Mapbox;
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

    private static final String LOG_TAG = MapsActivity.class.getSimpleName();

    final private float MAX_ZOOM_OUT = 10f;
    final private int SEARCH_RESPONSE = 1;
    private boolean movingMap = false;
    private boolean initialized = false;

    private ParkingLotListView parkingLotListView;

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
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }
        if(id == R.id.action_savedcar){

            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        PlaceAutocomplete.clearRecentHistory(this);
        Mapbox.getInstance(this, getString(R.string.mapbox_access_token));
        setContentView(R.layout.activity_maps);
        setUpParkingLotList();
        //Toolbar toolbar = findViewById(R.id.toolbar);
        //setSupportActionBar(toolbar);
        mapView = findViewById(R.id.mapView);
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(this);
        requestor = new Requestor(getApplicationContext());

        FloatingActionButton btn = findViewById(R.id.anthonyBtn);
        btn.setOnClickListener((View view) -> {
            Intent intent = new Intent(this, ParkingSpotActivity.class);
            startActivity(intent);
        });

    }


    // Set up map and style
    @Override
    public void onMapReady(@NonNull final MapboxMap mapBoxMap) {
        this.mapBoxMap = mapBoxMap;
        mapBoxMap.addOnCameraIdleListener(this);
        mapBoxMap.addOnCameraMoveStartedListener(this);
        mapBoxMap.getUiSettings().setRotateGesturesEnabled(false);

        mapBoxMap.setStyle(Style.MAPBOX_STREETS, (Style style) -> {

            setSymbolLayers(style);
            enableLocationComponent(style);
            initSearchFab();
            mapBoxMap.addOnMapClickListener(MapsActivity.this);
            setUpNavigationButton();
            getNearbyParkingLots();
        });
    }

    // sets up graphic layer for map
    private void setSymbolLayers(@NonNull  Style style) {

        // destination markers
        style.addImage(("destination"), BitmapFactory.decodeResource(
                getResources(), R.drawable.map_default_map_marker));


        style.addLayer(new SymbolLayer("destination-layer", destinationLayerID)
                .withProperties(
                        PropertyFactory.iconImage("destination"),
                        PropertyFactory.iconIgnorePlacement(true),
                        PropertyFactory.iconAllowOverlap(true)
                ));

        // parking lot icons
        style.addImage(("full"), BitmapFactory.decodeResource(
                getResources(), R.drawable.parking_lot_marker_full));

        style.addImage(("free"), BitmapFactory.decodeResource(
                getResources(), R.drawable.parking_lot_marker_free));

        style.addImage(("busy2"), BitmapFactory.decodeResource(
                getResources(), R.drawable.parking_lot_marker_busy2));

        style.addLayer(new SymbolLayer("parking-layer", parkingLayerID)
                .withProperties(
                        PropertyFactory.iconImage("{icon}"),            // they tell which icon is going to be displayed
                        PropertyFactory.iconIgnorePlacement(true),
                        PropertyFactory.iconAllowOverlap(true)
                ));
    }

    // initializes the search widget
    @SuppressWarnings( {"MissingPermission"})
    private void initSearchFab() {
        findViewById(R.id.searchBtn).setOnClickListener((View view) -> {

            // get user country for better search
            Location userLocation = locationComponent.getLastKnownLocation();
            String userCountry  = getApplication().getResources().getConfiguration().locale.getCountry();

            // opens new activity upon clicking
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

        });
    }

    // sets up navigate button
    private void setUpNavigationButton() {
        navigationButton = findViewById(R.id.navigationBtn);
        navigationButton.setOnClickListener((View v) -> {
            if (currentRoute == null)
                return;
            NavigationLauncherOptions options = NavigationLauncherOptions.builder()
                    .directionsRoute(currentRoute)
                    .build();

            NavigationLauncher.startNavigation(MapsActivity.this, options);
        });
    }

    @SuppressWarnings( {"MissingPermission"})
    @Override
    public boolean onMapClick(@NonNull LatLng point) {

        // check for feature clicks
        PointF screenPoint = mapBoxMap.getProjection().toScreenLocation(point);
        List<Feature> parkingIcons = mapBoxMap.queryRenderedFeatures(screenPoint, parkingLayerID);
        List<Feature> destinationIcons = mapBoxMap.queryRenderedFeatures(screenPoint, destinationLayerID);

        if (!parkingIcons.isEmpty()) {

            // user clicked on a parking spot
            Feature selectedFeature = parkingIcons.get(0);
            int index = selectedFeature.getNumberProperty("index").intValue();
            ParkingLotItem  lot = parkingLots.get(index);

            // draw the route

            Point pos = Point.fromLngLat(lot.latlong.getLongitude(), lot.latlong.getLatitude());
            CameraTranslator.moveCamera(mapBoxMap, pos);
            currentRoute = lot.getRoute();
            drawRoute(currentRoute);
            listView.smoothScrollToPosition(index);
        } else if (!destinationIcons.isEmpty()) {

            // if we clicked on destination, remove marker
            removeDestinationMarker();
            navigationButton.setEnabled(false);
            currentRoute = null;
            navigationMapRoute.removeRoute();
        } else {

            // add marker at user clicker else where
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

        // on return from search
        if (resultCode == Activity.RESULT_OK && requestCode == SEARCH_RESPONSE) {

            // add destination marker and draw route
            CarmenFeature selectedCarmenFeature = PlaceAutocomplete.getPlace(data);
            Point location = (Point) selectedCarmenFeature.geometry();
            addDestinationMarker(location);
            getAndDrawRoute(location);
            CameraTranslator.moveCamera(mapBoxMap, location);
        }
    }

    // update nearby location upon move
    @Override
    public void onCameraIdle() {

        // only try if we actually moved
        if (movingMap && initialized) {
            // update current location
            getNearbyParkingLots();
            movingMap = false;
        }
    }

    // user has started moving the map
    @Override
    public void onCameraMoveStarted(int reason) {
        // only move the map if we move it
        if (reason == MapboxMap.OnCameraMoveStartedListener.REASON_API_GESTURE)
            movingMap = true;
    }


    // sets up parking lot list
    @SuppressWarnings( {"MissingPermission"})
    private void setUpParkingLotList() {
        listView= findViewById(R.id.list);
        parkingLots = new ArrayList<>();

        parkingLotListView = new ParkingLotListView(parkingLots,getApplicationContext());
        listView.setAdapter(parkingLotListView);

        // set empty view
        TextView empty= findViewById(R.id.empty);
        listView.setEmptyView(empty);

//        listView.setOnItemClickListener( (AdapterView<?> parent, View view, int position, long id) -> {
//            Log.d(TAG, "Clicking!!");
//            // move to parking lot
//            ParkingLotItem parkingLot = parkingLots.get(position);
//            LatLng parkingLotPosition = parkingLot.latlong;
//            CameraTranslator.moveCamera(mapBoxMap, parkingLot.latlong);
//        });
    }

    // enable location with map, ask for location permissions if not already
    @SuppressWarnings( {"MissingPermission"})
    private void enableLocationComponent(@NonNull Style loadedMapStyle) {
        FloatingActionButton userGPSButton = findViewById(R.id.userLocationBtn);

        // Check if permissions are enabled and if not request
        if (PermissionsManager.areLocationPermissionsGranted(this)) {

            // Activate the MapboxMap LocationComponent to show user location
            locationComponent = mapBoxMap.getLocationComponent();
            locationComponent.activateLocationComponent(this, loadedMapStyle);
            locationComponent.setLocationComponentEnabled(true);

            // Set the component's camera mode
            Location userLocation = locationComponent.getLastKnownLocation();

            // show the GPS button
            userGPSButton.show();

            // move to user location on click
            userGPSButton.setOnClickListener((View view) -> {
                    CameraTranslator.moveCamera(mapBoxMap, userLocation);
            });

            // move to user
            CameraTranslator.moveCamera(mapBoxMap, userLocation);
            initialized = true;
        } else {

            // if not enabled location
            permissionsManager = new PermissionsManager(this);
            permissionsManager.requestLocationPermissions(this);
            userGPSButton.setEnabled(true);
            userGPSButton.hide();
        }
    }

    // required for permissions
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        permissionsManager.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    // required for permissions
    @Override
    public void onExplanationNeeded(List<String> permissionsToExplain) {
        // do nothing
    }

    // required for permissions
    @Override
    public void onPermissionResult(boolean granted) {
        if (granted) {
            enableLocationComponent(mapBoxMap.getStyle());
        } else {
            // do nothing
        }
    }
    public void launch_settings(MenuItem item) {
        Log.d(LOG_TAG, "Settings clicked!");
        Intent intent = new Intent(this, SettingsActivity.class);
        startActivity(intent);
    }
    public void launch_parklot(MenuItem item) {
        Log.d(LOG_TAG, "Lot CLicked clicked!");
        Intent intent = new Intent(this, ParkingSpotActivity.class);
        startActivity(intent);

    }
    // gets the nearby parking lots
    private void getNearbyParkingLots() {
        // check for permissions first
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

            // get user location once
            Location location = locationComponent.getLastKnownLocation();
            LatLng userPosition = new LatLng(location.getLatitude(), location.getLongitude());
            double zoomLevel = mapBoxMap.getCameraPosition().zoom;

            // dont bother if we are so far zoomed out
            if (zoomLevel < MAX_ZOOM_OUT) {
                removeAllParkingLotMarkers();
                return;
            }

            // calculate distance between end points of screen for search radius
            LatLng left = mapBoxMap.getProjection().getVisibleRegion().farLeft;
            LatLng right = mapBoxMap.getProjection().getVisibleRegion().farRight;
            double radius = left.distanceTo(right);


            // get parking lots relative to maps position
            requestor.GetParkingLots(mapBoxMap.getCameraPosition().target, radius, (String res) -> {
                    try {

                        // get res
                        JSONObject response = new JSONObject(res);
                        JSONArray parkingLotsArray = response.getJSONArray("parking_lots");
                        ArrayList<ParkingLotItem> parkingLotItems = new ArrayList<>();

                        // loop through each lot
                        for (int i = 0; i < parkingLotsArray.length(); i++) {

                            JSONObject lot = (JSONObject) parkingLotsArray.get(i);
                            JSONObject parkingLotLocation = lot.getJSONObject("location");
                            JSONObject analytics = lot.getJSONObject("analytics");
                            String addressName = lot.getJSONObject("address").get("street").toString();
                            String id = lot.getString("_id");
                            double latitude = (double) parkingLotLocation.get("lat");
                            double longitude = (double) parkingLotLocation.get("long");

                            LatLng latLng = new LatLng(latitude, longitude);

                            // get parking space data
                            JSONObject parkingSpaces = lot.getJSONObject("parking_spaces");

                            // get percentage of parking spots full
                            float percentage = 1f - (parkingSpaces.getInt("available") * 1.0f) / (parkingSpaces.getInt("total") * 1.0f);

                            // add parking lots to list
                            ParkingLotItem parkingLot = new ParkingLotItem(
                                    id,
                                    addressName,
                                    parkingSpaces.getInt("available"),
                                    latLng,
                                    percentage,
                                    analytics);
                            parkingLotItems.add(parkingLot);

                            // add to global
                            parkingLots.add(parkingLot);

                            // Get the route for each parking space
                            getRoute(Point.fromLngLat(longitude, latitude),
                                    (DirectionsRoute route) -> {

                                        // add route as reference
                                        parkingLot.setRoute(route);
                                        // update list
                                        parkingLotListView = new ParkingLotListView(parkingLotItems, getApplicationContext());
                                        listView.setAdapter(parkingLotListView);
                                    },     // set parking route
                                    () -> {
                                        parkingLot.setRoute(null);
                            });                          // set to nothing
                        }
                        addParkingLotMarkers(parkingLotItems);

                    } catch (JSONException e) {
                        Log.d(TAG, "onResponse JSON Error: " + e);
                    } catch (Exception e) {
                        Log.d(TAG, "onResponse Exception: " + e);
                    }
            }, (VolleyError error) -> {
                    // do nothing
                    Log.d(TAG, "Volley Error: " + error.toString());
            });
        } else {
            Log.e(TAG, "Permission denied");
        }
    }

    // gets the icon of the parking lot marker on maps
    private String getParkingLotIcon(float percentage) {
        if (percentage >= 0.9f)
            return "full";
        else if (percentage >= 0.50f)
            return "busy2";
        return "free";
    }

    // gets the route from the user location to destination
    // Allows for custom onResponse event (onRouteObtain) and onError
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

                    // call consumer provided
                    onRouteObtain.accept(response.body().routes().get(0));
                }

                @Override
                public void onFailure(Call<DirectionsResponse> call, Throwable throwable) {

                    // call on error provided
                    onError.run();
                }
            });

    }

    // draws the route and enabled nav button
    private void drawRoute(DirectionsRoute route) {

        // if no route specified
        if (route == null) {
            Log.d(TAG, "No route available");
            return;
        }

        // remove old route (only can have one)
        if (navigationMapRoute != null) {
            navigationMapRoute.removeRoute();
        } else {
            navigationMapRoute = new NavigationMapRoute(null, mapView, mapBoxMap, R.style.NavigationMapRoute);
        }

        // enable the navigation button
        navigationMapRoute.addRoute(route);
        navigationButton.setEnabled(true);
    }

    // helper function to get and draw a route
    private void getAndDrawRoute(Point point) {
        getRoute(point, (DirectionsRoute route) -> {
            currentRoute = route;
            drawRoute(route);
        }, () -> navigationButton.setEnabled(false));
    }

    // adds user inputted/searched destination marker
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
