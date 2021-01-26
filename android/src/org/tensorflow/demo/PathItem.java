package org.tensorflow.demo;

import com.skt.Tmap.TMapPoint;

public class PathItem {
    private static final int NON_TURNTYPE = -1;

    private boolean nodeType;
    private int turnType;
    private TMapPoint tMapPoint;

    public PathItem(boolean nodeType, TMapPoint tMapPoint){
        this.nodeType  = nodeType;
        this.tMapPoint = tMapPoint;
        this.turnType = NON_TURNTYPE;
    }

    public PathItem(boolean nodeType, int turnType, TMapPoint tMapPoint){
        this.nodeType = nodeType;
        this.turnType = turnType;
        this.tMapPoint = tMapPoint;
    }

    public boolean isNodeType() {
        return nodeType;
    }

    public void setNodeType(boolean nodeType) {
        this.nodeType = nodeType;
    }

    public int getTurnType() {
        return turnType;
    }

    public void setTurnType(int turnType) {
        this.turnType = turnType;
    }

    public TMapPoint gettMapPoint() {
        return tMapPoint;
    }

    public void settMapPoint(TMapPoint tMapPoint) {
        this.tMapPoint = tMapPoint;
    }

    public String toString(){
        if(this.nodeType == true){
            return "nodeType : POINT \nLon: "+tMapPoint.getLongitude()+"\nLat: "+tMapPoint.getLatitude()+"\n";
        }
        else{
            return "nodeType : LINESTRING\nLon: "+tMapPoint.getLongitude()+"\nLat: "+tMapPoint.getLatitude()+"\n";
        }
    }
}
