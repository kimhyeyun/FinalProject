package org.tensorflow.demo;

public class MyLatLng {
    private int ID;
    private double Latitude;
    private double Longitude;
    private int Pair_ID;
    private double Distance;
    private boolean Option;

    public MyLatLng() {
    }

    public void setId(int ID){ this.ID = ID;    }

    public int getId(){ return ID; }

    public double getLatitude() {
        return Latitude;
    }

    public void setLatitude(double Latitude) {
        this.Latitude = Latitude;
    }

    public double getLongitude() {
        return Longitude;
    }

    public void setLongitude(double Longitude) {
        this.Longitude = Longitude;
    }

    public int getPair_ID() {
        return Pair_ID;
    }

    public void setPair_ID(int pair_ID) {
        Pair_ID = pair_ID;
    }

    public double getDistance() {
        return Distance;
    }

    public void setDistance(double distance) {
        Distance = distance;
    }


}
