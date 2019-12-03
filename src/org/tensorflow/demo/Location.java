package org.tensorflow.demo;

public class Location extends android.location.Location {

    public Location(android.location.Location location) {
        super(location);
    }

    @Override
    public float getSpeed() {
        float nSpeed = super.getSpeed() * 3.6f;

        return nSpeed;
    }
}
