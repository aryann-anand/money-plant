package com.christo.moneyplant.models.bin;

public class Bin {

    private String uid;
    private double latitude;
    private double longitude;

    public void setUid(String uid) {
        this.uid = uid;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    private String address;

    public String getUid() {
        return uid;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public String getAddress() {
        return address;
    }
}
