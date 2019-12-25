package org.tensorflow.demo;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.res.AssetManager;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.mapbox.android.core.location.LocationEngine;
import com.mapbox.android.core.location.LocationEngineProvider;
import com.mapbox.android.core.location.LocationEngineRequest;
import com.mapbox.android.core.permissions.PermissionsListener;
import com.mapbox.android.core.permissions.PermissionsManager;
import com.mapbox.geojson.Feature;
import com.mapbox.geojson.FeatureCollection;
import org.postgis.Geometry;
import org.postgis.Point;

import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.location.LocationComponent;
import com.mapbox.mapboxsdk.location.LocationComponentActivationOptions;
import com.mapbox.mapboxsdk.location.modes.CameraMode;
import com.mapbox.mapboxsdk.location.modes.RenderMode;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.mapboxsdk.maps.Style;
import com.mapbox.mapboxsdk.plugins.annotation.OnSymbolClickListener;
import com.mapbox.mapboxsdk.plugins.annotation.Symbol;
import com.mapbox.mapboxsdk.plugins.annotation.SymbolManager;
import com.mapbox.mapboxsdk.plugins.annotation.SymbolOptions;
import com.mapbox.mapboxsdk.style.layers.SymbolLayer;
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.sql.SQLException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconAllowOverlap;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconIgnorePlacement;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconImage;

/**
 * Use the LocationComponent to easily add a device location "puck" to a Mapbox map.
 */
public class MapActivity extends AppCompatActivity implements
        OnMapReadyCallback, PermissionsListener {

    private static final String MARKER = "marker-icon";
    private Symbol tempSymbol = null;

    private ArrayList<Sign> signList = new ArrayList<Sign>();
    private HashMap<String, Drawable> signNameToSignDrawable = new HashMap<>();
    private ImageView[] signPlaceHolders  = new ImageView[8];

    private PermissionsManager permissionsManager;
    public MapboxMap mapboxMap;
    private MapView mapView;
    private Button buttonToCamera;

    private RequestQueue requestQueue;
    private String getURL = "http://193.219.91.103:9560/atvaizdavimas";

    // Variables needed to add the location engine
    private LocationEngine locationEngine;
    private long DEFAULT_INTERVAL_IN_MILLISECONDS = 1000L;
    private long DEFAULT_MAX_WAIT_TIME = DEFAULT_INTERVAL_IN_MILLISECONDS * 5;
    // Variables needed to listen to location updates
    private MapActivityLocationCallback callback = new MapActivityLocationCallback(this);
    SymbolManager sm;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

// Mapbox access token is configured here. This needs to be called either in your application
// object or in the same activity which contains the mapview.
        Mapbox.getInstance(this, getString(R.string.mapbox_access_token));

// This contains the MapView in XML and needs to be called after the access token is configured.
        setContentView(R.layout.activity_map);

        signPlaceHolders[0] = findViewById(R.id.image1);
        signPlaceHolders[1] = findViewById(R.id.image2);
        signPlaceHolders[2] = findViewById(R.id.image3);
        signPlaceHolders[3] = findViewById(R.id.image4);
        signPlaceHolders[4] = findViewById(R.id.image5);
        signPlaceHolders[5] = findViewById(R.id.image6);
        signPlaceHolders[6] = findViewById(R.id.image7);
        signPlaceHolders[7] = findViewById(R.id.image8);

        buttonToCamera = findViewById(R.id.cameraButton);
        buttonToCamera.setOnClickListener(onToCameraPressed);
        mapView = findViewById(R.id.mapView);
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(this);
    }

    @Override
    public void onBackPressed()
    {
        Intent intent = new Intent(getApplicationContext(), DetectorActivity.class);
        startActivity(intent);
    }

    Button.OnClickListener onToCameraPressed = new Button.OnClickListener() {
        @Override
        public void onClick(View v) {
            Intent intent = new Intent(getApplicationContext(), DetectorActivity.class);
            startActivity(intent);
        }
    };

    @Override
    public void onMapReady(@NonNull final MapboxMap mapboxMap) {
        MapActivity.this.mapboxMap = mapboxMap;

        mapboxMap.setStyle(new Style.Builder().fromUri("mapbox://styles/mapbox/streets-v10"),
                new Style.OnStyleLoaded() {
                    @Override
                    public void onStyleLoaded(@NonNull Style style) {
                        enableLocationComponent(style);
                        /* Image: An image is loaded. */
                        style.addImage(MARKER, BitmapFactory.decodeResource(
                                MapActivity.this.getResources(), R.drawable.mapbox_marker_icon_default));
                        sm = new SymbolManager(mapView, mapboxMap, style);
                        sm.setIconAllowOverlap(true);
                        sm.setIconIgnorePlacement(true);
                        FillDrawableHashMap();
                        GetDataFromRest(style);
                    }
                });
    }

    private void FillDrawableHashMap(){
        AssetManager am = getAssets();
        String fileName;
        String[] files = new String[signList.size()];

        try{
            files = am.list("icon_images");
        }catch(IOException e){
            Log.println(Log.ERROR, "FINDFILEERROR", e.toString());
        }

        for (String file : files) {
            fileName = file.substring(0, file.indexOf('.'));
            try{
                Drawable d = new BitmapDrawable(MapActivity.this.getResources(), BitmapFactory.decodeStream(am.open("icon_images/" + file)));
                signNameToSignDrawable.put(fileName, d);
            }catch(IOException e) {
                Log.println(Log.ERROR, "FILLDRAWABLEERROR", e.toString());
            }
        }
    }

    private void addMarkers() {
        String data = "";
        String name = signList.get(0).getSign_name();

        for (Sign sign : signList) {
            if (name.equals(sign.getSign_name())) {
                data += sign.getSign_name() + ";";
            } else {
                sm.create(new SymbolOptions()
                        .withLatLng(new LatLng(sign.getPoint().getY(), sign.getPoint().getX()))
                        .withIconImage(MARKER)
                        .withIconSize(1f)
                        .withTextAnchor(data));

                Log.println(Log.ERROR, "DATA", data);

                data = sign.getSign_name() + ";";
                name = sign.getSign_name();
            }
        }

        sm.create(new SymbolOptions()
                .withLatLng(new LatLng(signList.get(signList.size() - 1).getPoint().getY(), signList.get(signList.size() - 1).getPoint().getX()))
                .withIconImage(MARKER)
                .withIconSize(1f)
                .withTextAnchor(data));

        sm.addClickListener(new OnSymbolClickListener() {
            @Override
            public void onAnnotationClick(Symbol symbol) {
                if(tempSymbol == symbol){
                    for (ImageView view : signPlaceHolders) {
                        view.setImageDrawable(null);
                    }
                    tempSymbol = null;
                }else{
                    tempSymbol = symbol;
                    String[] signNames = symbol.getTextAnchor().split(";");
                    int index = 0;

                    for (ImageView view : signPlaceHolders) {
                        if(index < signNames.length){
                            view.setImageDrawable(signNameToSignDrawable.get(signNames[index]));
                            index++;
                        }else{
                            view.setImageDrawable(null);
                        }
                    }
                }
            }
        });
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

    private void GetDataFromRest(Style s){
        JsonArrayRequest arrayRequest = new JsonArrayRequest(
                Request.Method.GET,
                getURL,
                null,
                new Response.Listener<JSONArray>(){
                    @Override
                    public void onResponse(JSONArray response){
                        Log.e("Rest  worked", response.toString());

                        JSONObject object;

                        try{
                            Point point = new Point();
                            for(int i = 0; i < response.length(); i++){
                                object = response.getJSONObject(i);

                                try{
                                    point = new Point(object.getString("st_astext"));
                                }catch(SQLException e){
                                    Log.e("POINTAS", e.toString());
                                }

                                Sign sign = new Sign(object.getInt("kat_id"),
                                        object.getInt("zen_id"),
                                        object.getString("zen_pav"),
                                        point);

                                signList.add(sign);
                            }

                            addMarkers();

                            cancelAllRequests("getRequest");

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

        addToRequestQueue(arrayRequest, "getRequest");
    }

    public RequestQueue getRequestQueue() {
        if (requestQueue == null)
            requestQueue = Volley.newRequestQueue(getApplicationContext());
        return requestQueue;
    }

    public void addToRequestQueue(Request request, String tag) {
        request.setTag(tag);
        getRequestQueue().add(request);
    }

    public void cancelAllRequests(String tag) {
        getRequestQueue().cancelAll(tag);
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