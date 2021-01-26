package org.tensorflow.demo;

import com.skt.Tmap.TMapPoint;

public class TrafficLightInfo {
    public int id;
    public TMapPoint point;
    public int turnType;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public TMapPoint getPoint() {
        return point;
    }

    public void setPoint(TMapPoint point) {
        this.point = point;
    }

    public int getTurnType() {
        return turnType;
    }

    public void setTurnType(int turnType) {
        this.turnType = turnType;
    }


}
