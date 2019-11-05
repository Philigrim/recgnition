package org.tensorflow.demo;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.mapbox.android.core.location.LocationEngine;
import com.mapbox.android.core.location.LocationEngineProvider;
import com.mapbox.android.core.location.LocationEngineRequest;
import com.mapbox.android.core.permissions.PermissionsListener;
import com.mapbox.android.core.permissions.PermissionsManager;
import com.mapbox.geojson.Feature;
import com.mapbox.geojson.FeatureCollection;
import com.mapbox.geojson.Point;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.location.LocationComponent;
import com.mapbox.mapboxsdk.location.LocationComponentActivationOptions;
import com.mapbox.mapboxsdk.location.modes.CameraMode;
import com.mapbox.mapboxsdk.location.modes.RenderMode;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.mapboxsdk.maps.Style;
import com.mapbox.mapboxsdk.style.layers.SymbolLayer;
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static com.mapbox.mapboxsdk.style.expressions.Expression.literal;
import static com.mapbox.mapboxsdk.style.expressions.Expression.step;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconAllowOverlap;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconIgnorePlacement;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconImage;

/**
 * Use the LocationComponent to easily add a device location "puck" to a Mapbox map.
 */
public class MapActivity extends AppCompatActivity implements
        OnMapReadyCallback, PermissionsListener {

    private static final String MARKER_SOURCE = "markers-source";
    private static final String MARKER_STYLE_LAYER = "markers-style-layer";
    private static final String MARKER_IMAGE = "custom-marker";

    private PermissionsManager permissionsManager;
    public MapboxMap mapboxMap;
    private MapView mapView;
    private Button buttonToCamera;
    private Button buttonToAddSign;
    private Location userLocation;

    private Random rand = new Random();
    private int index = 0;

    private Style myStyle;

    // Variables needed to add the location engine
    private LocationEngine locationEngine;
    private long DEFAULT_INTERVAL_IN_MILLISECONDS = 1000L;
    private long DEFAULT_MAX_WAIT_TIME = DEFAULT_INTERVAL_IN_MILLISECONDS * 5;
    // Variables needed to listen to location updates
    private MapActivityLocationCallback callback = new MapActivityLocationCallback(this);


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

// Mapbox access token is configured here. This needs to be called either in your application
// object or in the same activity which contains the mapview.
        Mapbox.getInstance(this, getString(R.string.mapbox_access_token));

// This contains the MapView in XML and needs to be called after the access token is configured.
        setContentView(R.layout.activity_map);
        buttonToCamera = findViewById(R.id.cameraButton);
        buttonToAddSign = findViewById(R.id.addSign);
        mapView = findViewById(R.id.mapView);
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(this);

        buttonToCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openCamera();
            }
        });

        buttonToAddSign.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                userLocation = callback.getLocation();
                String direction = directionFacing(userLocation.getBearing());

                float latMultiplier = setLatMultiplier(direction);
                float longMultiplier = setLongMultiplier(direction);

                float userLat = (float)userLocation.getLatitude();
                float userLong = (float)userLocation.getLongitude();

                addMarkers(myStyle, userLong * longMultiplier, userLat * latMultiplier);
            }
        });
    }

    private String directionFacing(float bearing)
    {
        if(bearing > 315 || bearing > 0 && bearing < 45){
            return "North";
        }else if(bearing >= 45 && bearing < 135){
            return "East";
        }else if(bearing >= 135f && bearing < 225){
            return "West";
        }else{
            return "South";
        }
    }

    private float setLatMultiplier(String direction){
        if(direction == "North"){
            return 1.00001f;
        }else if(direction == "East"){
            return 0.999999f;
        }else if(direction == "South"){
            return 0.99999f;
        }else{
            return 1.000001f;
        }
    }

    private float setLongMultiplier(String direction){
        if(direction == "North"){
            return 1.000001f;
        }else if(direction == "East"){
            return 1.00001f;
        }else if(direction == "South"){
            return 0.999999f;
        }else{
            return 0.99999f;
        }
    }

    private void openCamera(){
        Intent intent = new Intent(this, DetectorActivity.class);
        startActivity(intent);
    }

    @Override
    public void onMapReady(@NonNull final MapboxMap mapboxMap) {
        MapActivity.this.mapboxMap = mapboxMap;

        mapboxMap.setStyle(new Style.Builder().fromUri("mapbox://styles/mapbox/streets-v10"),
                new Style.OnStyleLoaded() {
                    @Override
                    public void onStyleLoaded(@NonNull Style style) {
                        enableLocationComponent(style);
                        /* Image: An image is loaded. */
                        style.addImage(MARKER_IMAGE, BitmapFactory.decodeResource(
                                MapActivity.this.getResources(), R.drawable.ivaziuoti_draudziama));
                        myStyle = style;
                    }
                });
    }

    private void addMarkers(@NonNull Style loadedMapStyle, float longitude, float latitude) {
        List<Feature> features = new ArrayList<>();
        features.add(Feature.fromGeometry(Point.fromLngLat(longitude, latitude)));

        /* Source: A data source specifies the geographic coordinate where the image marker gets placed. */

        loadedMapStyle.addSource(new GeoJsonSource((MARKER_SOURCE + index), FeatureCollection.fromFeatures(features)));

        /* Style layer: A style layer ties together the source and image and specifies how they are displayed on the map. */
        SymbolLayer singleLayer = new SymbolLayer(MARKER_STYLE_LAYER + index, MARKER_SOURCE + index);
        singleLayer.setProperties(
            iconImage(MARKER_IMAGE),
                iconIgnorePlacement(true),
                iconAllowOverlap(true));


        loadedMapStyle.addLayer(singleLayer);

//        new SymbolLayer((MARKER_STYLE_LAYER + index), (MARKER_SOURCE + index))
//                .withProperties(
//                        PropertyFactory.iconAllowOverlap(true),
//                        PropertyFactory.iconIgnorePlacement(true),
//                        PropertyFactory.iconImage(MARKER_IMAGE),
//// Adjust the second number of the Float array based on the height of your marker image.
//// This is because the bottom of the marker should be anchored to the coordinate point, rather
//// than the middle of the marker being the anchor point on the map.
//                        PropertyFactory.iconOffset(new Float[] {0f, 0f})

        index++;
    }

    public void addMarkersFromCamera(){
        float latMultiplier = 1.01f;
        float longMultiplier = 1.001f;
        float userLat = (float)userLocation.getLatitude();
        float userLong = (float)userLocation.getLongitude();

        addMarkers(myStyle, userLong * longMultiplier, userLat * latMultiplier);
    }

    /**
     * Initialize the Maps SDK's LocationComponent
     */
    @SuppressWarnings( {"MissingPermission"})
    private void enableLocationComponent(@NonNull Style loadedMapStyle) {
// Check if permissions are enabled and if not request
        if (PermissionsManager.areLocationPermissionsGranted(this)) {

// Get an instance of the component
            LocationComponent locationComponent = mapboxMap.getLocationComponent();

// Set the LocationComponent activation options
            LocationComponentActivationOptions locationComponentActivationOptions =
                    LocationComponentActivationOptions.builder(this, loadedMapStyle)
                            .useDefaultLocationEngine(false)
                            .build();

// Activate with the LocationComponentActivationOptions object
            locationComponent.activateLocationComponent(locationComponentActivationOptions);

// Enable to make component visible
            locationComponent.setLocationComponentEnabled(true);

// Set the component's camera mode
            locationComponent.setCameraMode(CameraMode.TRACKING);

// Set the component's render mode
            locationComponent.setRenderMode(RenderMode.COMPASS);

            initLocationEngine();
        } else {
            permissionsManager = new PermissionsManager(this);
            permissionsManager.requestLocationPermissions(this);
        }
    }

    /**
     * Set up the LocationEngine and the parameters for querying the device's location
     */
    @SuppressLint("MissingPermission")
    private void initLocationEngine() {
        locationEngine = LocationEngineProvider.getBestLocationEngine(this);

        LocationEngineRequest request = new LocationEngineRequest.Builder(DEFAULT_INTERVAL_IN_MILLISECONDS)
                .setPriority(LocationEngineRequest.PRIORITY_HIGH_ACCURACY)
                .setMaxWaitTime(DEFAULT_MAX_WAIT_TIME).build();

        locationEngine.requestLocationUpdates(request, callback, getMainLooper());
        locationEngine.getLastLocation(callback);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        permissionsManager.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    public void onExplanationNeeded(List<String> permissionsToExplain) {
        Toast.makeText(this, R.string.user_location_permission_explanation, Toast.LENGTH_LONG).show();
    }

    @Override
    public void onPermissionResult(boolean granted) {
        if (granted) {
            if (mapboxMap.getStyle() != null) {
                enableLocationComponent(mapboxMap.getStyle());
            }
        } else {
            Toast.makeText(this, R.string.user_location_permission_not_granted, Toast.LENGTH_LONG).show();
            finish();
        }
    }

    @Override
    @SuppressWarnings( {"MissingPermission"})
    protected void onStart() {
        super.onStart();
        mapView.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mapView.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mapView.onStop();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mapView.onSaveInstanceState(outState);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Prevent leaks
        if (locationEngine != null) {
            locationEngine.removeLocationUpdates(callback);
        }
        mapView.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }
}