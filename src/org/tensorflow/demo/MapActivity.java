package org.tensorflow.demo;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.res.AssetManager;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;
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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
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

    private ArrayList<Sign> signList = new ArrayList<Sign>();

    private ArrayList<String> icons = new ArrayList<String>();

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

//        buttonToAddSign.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                userLocation = callback.getLocation();
//
//                float userLat = (float)userLocation.getLatitude();
//                float userLong = (float)userLocation.getLongitude();
//            }
//        });
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
                        myStyle = style;
                        GetDataFromRest();
                    }
                });
    }

    private void AddIconsToStyle(@NonNull Style style){
        AssetManager am = getAssets();
        int index = 0;
        String fileName;
        String[] files = new String[signList.size()];

        ArrayList<String> signNames = new ArrayList<String>();

        for(Sign sign : signList){
            signNames.add(sign.getSign_name());
        }

        try{
            files = am.list("icon_images");
        }catch(IOException e){
            Log.println(Log.ERROR, "ERROR", e.toString());
        }

        for (String file : files) {
            fileName = file.substring(0, file.indexOf('.'));
            if(signNames.contains(fileName)){
                try{
                    Log.println(Log.INFO, "INFO", fileName);
                    Drawable d = Drawable.createFromStream(am.open("icon_images/" + file), null);
                    style.addImage(signList.get(index).getSign_name(), BitmapFactory.decodeStream(am.open("icon_images/" + file)));
                    index++;
                }catch(IOException e) {
                    Log.println(Log.ERROR, "ERROR", e.toString());
                }
            }
        }
    }

    private void addMarkers(@NonNull Style loadedMapStyle) {
        List<Feature> features = new ArrayList<>();

        for(Sign sign : signList){
            features.add(Feature.fromGeometry(sign.getPoint()));

            /* Source: A data source specifies the geographic coordinate where the image marker gets placed. */
            loadedMapStyle.addSource(new GeoJsonSource(sign.getUnique_sign_id(), FeatureCollection.fromFeatures(features)));

            /* Style layer: A style layer ties together the source and image and specifies how they are displayed on the map. */
            SymbolLayer singleLayer = new SymbolLayer(sign.getUnique_sign_layer_id(), sign.getUnique_sign_id());
            singleLayer.setProperties(
                    iconImage(sign.getSign_name()),
                    iconIgnorePlacement(true),
                    iconAllowOverlap(true));

            loadedMapStyle.addLayer(singleLayer);
        }
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
    
    private void GetDataFromRest(){
        String restURL = "http://193.219.91.103:9560/aslohas";

        RequestQueue requestQueue = Volley.newRequestQueue(this);

        JsonArrayRequest arrayRequest = new JsonArrayRequest(
                Request.Method.GET,
                restURL,
                null,
                new Response.Listener<JSONArray>(){
                    @Override
                    public void onResponse(JSONArray response){
                        Log.e("Rest  worked", response.toString());

                        JSONObject object;
                        int uniqueId = 0;
                        int uniqueLayerId = 0;

                        try{
                            for(int i = 0; i < response.length(); i++){
                                object = response.getJSONObject(i);

                                Sign sign = new Sign(object.getInt("kat_id"),
                                        object.getInt("zen_id"),
                                        object.getString("zen_pav"),
                                        Point.fromJson(object.get("st_asgeojson").toString()),
                                        uniqueId,
                                        uniqueLayerId);

                                signList.add(sign);

                                uniqueId++;
                                uniqueLayerId++;

                                Log.e("BUGABUGA", MessageFormat.format("Kategorijos id: {0}\n Zenklo id: {1}\n Zenklo pavadinimas: {2}\n Zenklo latitude: {3}\n Zenklo longitude: {4}\n", signList.get(i).getCategory_id(),
                                signList.get(i).getSign_id(), signList.get(i).getSign_name(), signList.get(i).getPoint().latitude(), signList.get(i).getPoint().longitude()));
                            }

                            AddIconsToStyle(myStyle);
                            addMarkers(myStyle);

                        }catch(JSONException e){
                            Log.e("erroriukas toks", e.toString());
                        }
                    }
                },

                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e("Rest Response", error.toString());
                    }
                });

        requestQueue.add(arrayRequest);
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