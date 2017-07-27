package edu.cmu.EasyOrder_Android;

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

    @Override
    public String toString() {
        return "Location: " + getLocaiton() + "\tETA: " + getETA();
    }
}
