package edu.cmu.EasyOrder_Android;

import android.location.Location;
import android.location.LocationManager;

/**
 * Created by yunpengx on 7/27/17.
 */

public class PickupLocation {
    private String location;
    private double latitude;
    private double longitude;
    private double ETA;

    public String getLocaiton() {
        return this.location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public double getLatitude() {
        return this.latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return this.longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public double getETA() {
        return this.ETA;
    }

    public void setETA(double ETA) {
        this.ETA = ETA;
    }

    public Location getLatLngLocation () {
        Location loc = new Location(LocationManager.GPS_PROVIDER);
        loc.setLatitude(this.latitude);
        loc.setLongitude(this.longitude);
        return loc;
    }

    @Override
    public String toString() {
        return "Location: " + getLocaiton() + "\tETA: " + getETA();
    }
}
