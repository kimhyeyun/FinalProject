package org.tensorflow.demo;

import android.content.Context;
import android.icu.text.IDNA;

import androidx.annotation.NonNull;

import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class InfoGeofence {
    List<LatLng> InfoArea = new ArrayList<>();
    Context mcontext;
    List<MyLatLng> latLngList1 = new ArrayList<>();

    public ArrayList<PathItem> pathItems = null;

    public void initInfoArea(final List<TrafficLightInfo> trafficLightInfo)
    {
        this.pathItems =  ((MapActivity)MapActivity.mContext).pathItems;
        // pathItem 시작지 제외하고 infoarea에 저장
        for(int i = 0;i<pathItems.size();i++)
        {
           // if(pathItems.get(i).getTurnType() != 200)
         //   {
                InfoArea.add(new LatLng(pathItems.get(i).gettMapPoint().getLatitude(), pathItems.get(i).gettMapPoint().getLongitude()));
       //    }
        }
        for(int i = 0;i<InfoArea.size();i++)
        {
            ((MapActivity)MapActivity.mContext).dangerousArea.add(InfoArea.get(i));
            ((MapActivity)MapActivity.mContext).addCircleArea();
        }

    }
}
