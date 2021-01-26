package org.tensorflow.demo.Interface;

import org.tensorflow.demo.MyLatLng;

import java.util.List;

public interface IOnLoadLocationListener {
    void onLoadLocationSuccess(List<MyLatLng> latLngs);
    void onLoadLocationFailed(String message);
}
