package org.tensorflow.demo.record;

public class Location extends android.location.Location {

    public Location(android.location.Location location) {
        super(location);
    }

    @Override
    public float getSpeed() { return super.getSpeed() * 3.6f; }
}
