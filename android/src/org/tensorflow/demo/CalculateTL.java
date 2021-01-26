package org.tensorflow.demo;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;

import org.tensorflow.demo.Interface.*;
import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.skt.Tmap.TMapPoint;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class CalculateTL{
    public DatabaseReference mDatabase;
    private FirebaseAuth mAuth;
    InfoGeofence infoGeofence = new InfoGeofence();

    List<MyLatLng> latLngList = new ArrayList<>();

    Context mcontext;

    double min = 100;
    int min_id;

    public int ReadDataSig(final List<TrafficLightInfo> trafficLightInfo, final TMapPoint point, final int turntype) {
        mDatabase = FirebaseDatabase.getInstance().getReference("Traffic_Light");

        mDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<MyLatLng> latLngList1 = new ArrayList<>();
                for (DataSnapshot locationSnapShot : snapshot.getChildren()) {
                    MyLatLng latLng = locationSnapShot.getValue(MyLatLng.class);
                    latLngList1.add(latLng);
                }
                latLngList = latLngList1;

                CalculateMin(trafficLightInfo, point, latLngList, turntype);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        return min_id;
    }

    private void CalculateMin(List<TrafficLightInfo> trafficLightInfo, TMapPoint point, List<MyLatLng> latLngList, int turntype) {
        TrafficLightInfo tr = new TrafficLightInfo();
        for(int i=0;i<latLngList.size();i++){
            int id = latLngList.get(i).getId();
            double tmp = Math.abs(latLngList.get(i).getLatitude()-point.getLatitude());

            if(tmp < min){
                min = tmp;
                min_id = id;
            }
        }
        tr.setId(min_id);
        tr.setPoint(new TMapPoint(latLngList.get(min_id).getLatitude(),latLngList.get(min_id).getLongitude()));
        tr.setTurnType(turntype);

        trafficLightInfo.add(tr);

        System.out.println(trafficLightInfo.get(0).getTurnType());

        // 저장한 신호등 정보 가지고 지오펜싱 구현
        //infoGeofence.initInfoArea(trafficLightInfo);
    }

}
