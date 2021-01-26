package org.tensorflow.demo;


import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.skt.Tmap.TMapPoint;

import org.tensorflow.demo.PathItem;

import java.nio.file.Path;

public class FBLatLon {
    public DatabaseReference mDatabase;
    private FirebaseAuth mAuth;
    PathItem pathItem;


    //point type 저장
    public  void writeNewLatLon(PathItem pathItem, int idx){
        mDatabase = FirebaseDatabase.getInstance().getReference("Turn");
        mAuth = FirebaseAuth.getInstance();

        int turnType = pathItem.getTurnType();
        mDatabase.child(String.valueOf(idx));
        TMapPoint point = pathItem.gettMapPoint();

        mDatabase.child(String.valueOf(idx)).child("TurnType").setValue(turnType);
        mDatabase.child(String.valueOf(idx)).child("Lon").setValue(point.getLongitude());
        mDatabase.child(String.valueOf(idx)).child("Lat").setValue(point.getLatitude());
    }


    public void RemoveAll(){
        mDatabase = FirebaseDatabase.getInstance().getReference("Turn");
        mDatabase.removeValue();
    }
}
