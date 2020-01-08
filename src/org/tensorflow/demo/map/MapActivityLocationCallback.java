package org.tensorflow.demo.map;

import android.location.Location;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.mapbox.android.core.location.LocationEngineCallback;
import com.mapbox.android.core.location.LocationEngineResult;

import java.lang.ref.WeakReference;

public class MapActivityLocationCallback
        implements LocationEngineCallback<LocationEngineResult> {

    private final WeakReference<MapActivity> activityWeakReference;
    private Location location;

    MapActivityLocationCallback(MapActivity activity) {
        this.activityWeakReference = new WeakReference<>(activity);
    }

    /**
     * The LocationEngineCallback interface's method which fires when the device's location has changed.
     *
     * @param result the LocationEngineResult object which has the last known location within it.
     */
    @Override
    public void onSuccess(LocationEngineResult result) {
        MapActivity activity = activityWeakReference.get();

        if (activity != null) {
            location = result.getLastLocation();

            if (location == null) {
                return;
            }

            if (activity.mapboxMap != null && result.getLastLocation() != null) {
                activity.mapboxMap.getLocationComponent().forceLocationUpdate(result.getLastLocation());
            }
        }
    }

    /**
     * The LocationEngineCallback interface's method which fires when the device's location can not be captured
     *
     * @param exception the exception message
     */

    @Override
    public void onFailure(@NonNull Exception exception) {
        Log.d("LocationChangeActivity", exception.getLocalizedMessage() + "");
        MapActivity activity = activityWeakReference.get();
        if (activity != null) {
            Toast.makeText(activity, exception.getLocalizedMessage(),
                    Toast.LENGTH_SHORT).show();
        }
    }

    public Location getLocation() {
        return location;
    }
}
