package org.tensorflow.demo;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Looper;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;


import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQuery;
import com.firebase.geofire.GeoQueryEventListener;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.PermissionListener;
import com.skt.Tmap.TMapData;
import com.skt.Tmap.TMapPOIItem;
import com.skt.Tmap.TMapPoint;
import com.skt.Tmap.TMapTapi;
import com.skt.Tmap.util.HttpConnect;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Random;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;


public class MapActivity extends AppCompatActivity implements OnMapReadyCallback,
      IOnLoadLocationListener, GeoQueryEventListener{

    TTSSTT ttsstt;
    TextView srchText;
    String destination_text;

    //Map
    private GoogleMap googleMap;
    private Marker currentMarker = null;

    private IOnLoadLocationListener iOnLoadLocationListener;


    //Tmap 연동
    private Element root;
    private TMapTapi tMapTapi;
    private TMapData tMapData;
    PolylineOptions polylineOptions;

    TMapPoint endpoint, startpoint;

    private Button findPath_btn;

    //경로 저장
    public ArrayList<PathItem> pathItems = null;
    private StringBuilder pathPoint = new StringBuilder();
    String totalDistance, totalTime;

    //경로 turn point fb 저장
    FBLatLon fbLatLon = new FBLatLon();
    int idx = 1;
    int count_info = 0;

    //경로 내 신호등 찾기 변수
    List<TrafficLightInfo> trafficLightInfo = new ArrayList<>();
    DatabaseReference TrafficL;

    CalculateTL calculateTL = new CalculateTL();

    //지오펜싱 변수
    //realtime change
    private DatabaseReference myCity;
    private Location lastLocation;

    private GeoQuery geoQuery;

    private DatabaseReference myLocationRef;
    private GeoFire geoFire;
    public List<LatLng> dangerousArea;
    private InfoGeofence infoGeofence = new InfoGeofence();

    Integer index_pathItems = 0;
    Integer index_info = 6;
    LinearLayout linear_menu_tutorial;
    LinearLayout linear_menu_camera;

    private LocationRequest locationRequest;
    private LocationCallback locationCallback;
    private FusedLocationProviderClient fusedLocationProviderClient;


    public static Context mContext;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main_map);

        mContext = this;

        PopupActivity popupActivity = new PopupActivity(this);
        popupActivity.callFunction();

        ttsstt = new TTSSTT(mContext);

        linear_menu_tutorial = (LinearLayout) findViewById(R.id.linear_menu_tutorial);
        linear_menu_camera = (LinearLayout) findViewById(R.id.linear_menu_camera);

        // 메뉴 - 튜토리얼 클릭
        linear_menu_tutorial.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ttsstt.TimeToClick("도움말", HelpActivity.class);
            }
        });

        // 메뉴 - 카메라 클릭
        linear_menu_camera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ttsstt.TimeToClick("카메라", DetectorActivity.class);
            }
        });



        Dexter.withActivity(this)
                .withPermission(Manifest.permission.ACCESS_FINE_LOCATION)
                .withListener(new PermissionListener() {
                    @Override
                    public void onPermissionGranted(PermissionGrantedResponse response) {
                        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
                        Log.d("findPath_btn", "start : findPath_btn");
                        //MAP


                        buildLocationRequest();
                        buildLocationCallback();

                        srchText = (TextView) findViewById(R.id.srch);
                        srchText.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                ttsstt.speechToText(srchText);
                            }
                        });

                        findPath_btn = (Button)findViewById(R.id.srch_btn);

                        findPath_btn.setOnClickListener(new Button.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                ttsstt.ClickFind(findPath_btn.getText().toString());
                            }
                        });

                        tMapData = new TMapData();

                        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(MapActivity.this);

                        //Log.d(TAG, "initArea() Start");
                        initArea();
                       // Log.d(TAG, "initArea() end");
                        settingGeoFire();
                    }

                    @Override
                    public void onPermissionDenied(PermissionDeniedResponse response) {
                        Toast.makeText(MapActivity.this, "You must enable permission", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(PermissionRequest permission, PermissionToken token) {

                    }
                }).check();
    }

        // 위험지역 넣는 곳
    private void initArea() {
        myCity = FirebaseDatabase.getInstance()
                .getReference("RiskFactor");
        iOnLoadLocationListener = MapActivity.this;
        // Load from Firebase
        myCity.addValueEventListener(new ValueEventListener() {

            // use realtime database --> 실시간으로 움직임임
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                // Update dangerousArea list
                List<MyLatLng> latLngList = new ArrayList<>();
                for (DataSnapshot locationSnapShot : dataSnapshot.getChildren()) {
                    MyLatLng latLng = locationSnapShot.getValue(MyLatLng.class);
                    latLngList.add(latLng);
                }

                iOnLoadLocationListener.onLoadLocationSuccess(latLngList);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void addUserMarker() {
        geoFire.setLocation("You", new GeoLocation(lastLocation.getLatitude(),
                lastLocation.getLongitude()), new GeoFire.CompletionListener() {
            @Override
            public void onComplete(String key, DatabaseError error) {
                if (currentMarker != null) currentMarker.remove();
                currentMarker = googleMap.addMarker(new MarkerOptions()
                        .position(new LatLng(lastLocation.getLatitude(),
                                lastLocation.getLongitude()))
                        .title("You"));
                //After add marker, move camera
                googleMap.animateCamera(CameraUpdateFactory
                        .newLatLngZoom(currentMarker.getPosition(), 18.0f));
            }
        });

    }

        // setting geofire on my reference
    private void settingGeoFire() {
        myLocationRef = FirebaseDatabase.getInstance().getReference("MyLocation");
        geoFire = new GeoFire(myLocationRef);
    }

    private void buildLocationCallback() {
        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(final LocationResult locationResult) {
                if (googleMap != null) {

                    lastLocation = locationResult.getLastLocation();

                    // geofire에 내 위치 저장
                    addUserMarker();
                }
            }
        };
    }

    private void buildLocationRequest() {
        locationRequest = new LocationRequest();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(5000);
        locationRequest.setFastestInterval(3000);
        locationRequest.setSmallestDisplacement(10f);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode){
            case 1:
                if(resultCode == RESULT_OK)
                {
                    String result = data.getStringExtra("result");
                    srchText.setText(result);
                }
                else if(resultCode == RESULT_CANCELED) {
                    srchText.setText("목적지 입력");
                }
                break;
        }
    }

    //MAP
    //1. GOOGLE MAP
    @Override
    public void onMapReady(GoogleMap googleMap) {
        this.googleMap = googleMap;
        LatLng DEFAULT_LOCATION = new LatLng(37.451076, 126.656574);

        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(DEFAULT_LOCATION,18);
        googleMap.moveCamera(cameraUpdate);

        googleMap.getUiSettings().setZoomControlsEnabled(true);

        tMapTapi = new TMapTapi(this);
        tMapTapi.setSKTMapAuthentication("l7xx1e068a7450414185a033d514672de76f");
        tMapTapi.setOnAuthenticationListener(new TMapTapi.OnAuthenticationListenerCallback() {
            @Override
            public void SKTMapApikeySucceed() {
                Log.d("TMAP", "SKTMAPAPIKEYSUCCEED");
            }

            @Override
            public void SKTMapApikeyFailed(String s) {
                Log.d("TMAP", "SKTMAPAPIKEYFAILED" + s);
            }
        });

        if (fusedLocationProviderClient != null)
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    return;
                }
            }
        fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, Looper.myLooper());

        // Add Circle for dangerous
        addCircleArea();
    }

    public void addCircleArea() {
        if(geoQuery != null)
        {
            geoQuery.removeGeoQueryEventListener(this);
            geoQuery.removeAllListeners();
        }

        for(final LatLng latLng : dangerousArea)
        {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    googleMap.addCircle(new CircleOptions().center(latLng)
                            .radius(12) // 12m
                            .strokeColor(Color.BLUE)
                            .fillColor(0x220000FF)     // 22 is transparent code
                            .strokeWidth(5.0f)
                    );
                }
            });


            // Create GeoQuery when user in dangerous location

            geoQuery = geoFire.queryAtLocation(new GeoLocation(latLng.latitude, latLng.longitude), 0.012f); // 500m
            // onkeyentered 이런거 부르는 애
            geoQuery.addGeoQueryEventListener(MapActivity.this);
        }
    }


    @Override
    protected void onStop() {
       fusedLocationProviderClient.removeLocationUpdates(locationCallback);
        super.onStop();
    }

    // 위험 요소 접근시 말함
    @Override
    public void onKeyEntered(String key, GeoLocation location) {

        double lat = location.latitude;
        double lon = location.longitude;
        Location now_location = new Location("");
        now_location.setLatitude(lat);
        now_location.setLongitude(lon);
        String content;

        Integer count = 0;
        for(LatLng dan : dangerousArea)
        {
            count++;
            double d_lat = dan.latitude;
            double d_lon = dan.longitude;
            Location d_location = new Location("");
            d_location.setLatitude(d_lat);
            d_location.setLongitude(d_lon);
            if(d_location.distanceTo(now_location) <= 12)
            {
                break;
            }

        }
        if(index_info >= 6)
        {
            System.out.println("Enter distance");
            if(pathItems.get(index_pathItems).getTurnType() == 200)
            {
                content = "경로를 안내합니다. 직진해주세요.";
                Toast.makeText(MapActivity.this, content, Toast.LENGTH_SHORT).show();
                ttsstt.tts.speak(content, TextToSpeech.QUEUE_FLUSH, null,null);
            }
            else if(pathItems.get(index_pathItems).getTurnType() == 12)
            {
                content = "좌회전하세요. ";
                Toast.makeText(MapActivity.this, content, Toast.LENGTH_SHORT).show();
                ttsstt.tts.speak(content, TextToSpeech.QUEUE_FLUSH, null,null);
            }
            else if(pathItems.get(index_pathItems).getTurnType() == 13)
            {
                content = "우회전하세요. ";
                Toast.makeText(MapActivity.this, content, Toast.LENGTH_SHORT).show();
                ttsstt.tts.speak(content, TextToSpeech.QUEUE_FLUSH, null,null);
            }
            else if(pathItems.get(index_pathItems).getTurnType() == 211)
            {
                content = "전방에 횡단보도가 있습니다. 카메라를 들어주세요. ";
                Toast.makeText(MapActivity.this, content, Toast.LENGTH_SHORT).show();
                ttsstt.tts.speak(content, TextToSpeech.QUEUE_FLUSH, null,null);
            }
            else if(pathItems.get(index_pathItems).getTurnType() == 212)
            {
                content = "좌측에 횡단보도가 있습니다. 카메라를 들어주세요. ";
                Toast.makeText(MapActivity.this, content, Toast.LENGTH_SHORT).show();
                ttsstt.tts.speak(content, TextToSpeech.QUEUE_FLUSH, null,null);
            }
            else if(pathItems.get(index_pathItems).getTurnType() == 213)
            {
                content = "우측에 횡단보도가 있습니다. 카메라를 들어주세요. ";
                Toast.makeText(MapActivity.this, content, Toast.LENGTH_SHORT).show();
                ttsstt.tts.speak(content, TextToSpeech.QUEUE_FLUSH, null,null);
            }
            else if(pathItems.get(index_pathItems).getTurnType() == 201)
            {
                content = "도착했습니다.";
                Toast.makeText(MapActivity.this, content, Toast.LENGTH_SHORT).show();
                ttsstt.tts.speak(content, TextToSpeech.QUEUE_FLUSH, null,null);
            }
            System.out.println("index_info : " + index_info + " index_pathItems : "+index_pathItems);
            count_info = 0;
        }




        if(count == 1 || count == 2)
            ttsstt.tts.speak("근처에 계단이 있습니다. 주의해주세요.", TextToSpeech.QUEUE_FLUSH, null, null);
        else if(count == 3 || count == 4)
            ttsstt.tts.speak("근처에 경사진 길이 있습니다. 주의해주세요.", TextToSpeech.QUEUE_FLUSH, null, null);
        else if(count == 5)
            ttsstt.tts.speak("차도와 인도의 구분이 모호합니다. 주의해주세요.", TextToSpeech.QUEUE_FLUSH, null, null);
    }

    @Override
    public void onKeyExited(String key) {
        //sendNotification("EDMTDev", String.format("%s leave the dangerous area", key));

        System.out.println("befor add count : " + count_info);
        if(count_info == 0)
        {
            System.out.println("Key exited");
            index_pathItems++;
            index_info++;
            count_info++;
            System.out.println("after add count : " + count_info);

        }

    }


    @Override
    public void onKeyMoved(String key, GeoLocation location) {
        //sendNotification("EDMTDev", String.format("%s move within the dangerous area", key));
    }

    @Override
    public void onGeoQueryReady() {

    }

    @Override
    public void onGeoQueryError(DatabaseError error) {
        Toast.makeText(this, "" + error.getMessage(), Toast.LENGTH_SHORT).show();
    }

    private void sendNotification(String title, String content) {

        Toast.makeText(this, "" + content, Toast.LENGTH_SHORT).show();
        String NOTIFICATION_CHANNEL_ID = "emdt_multiple_location";
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
        {
            NotificationChannel notificationChannel = new NotificationChannel(NOTIFICATION_CHANNEL_ID, "My Notification",
                    NotificationManager.IMPORTANCE_DEFAULT);

            // Config
            notificationChannel.setDescription("Channel description");
            notificationChannel.enableLights(true);
            notificationChannel.setLightColor(Color.RED);
            notificationChannel.setVibrationPattern(new long[]{0,1000,500,1000});
            notificationChannel.enableVibration(true);
            notificationManager.createNotificationChannel(notificationChannel);
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID);
        builder.setContentTitle(title)
                .setContentText(content)
                .setAutoCancel(false)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher));

        Notification notification = builder.build();
        notificationManager.notify(new Random().nextInt(), notification);

    }

    @Override
    public void onLoadLocationSuccess(List<MyLatLng> latLngs) {
        dangerousArea = new ArrayList<>();

        for(MyLatLng myLatLng : latLngs)
        {
            LatLng convert = new LatLng(myLatLng.getLatitude(), myLatLng.getLongitude());
            dangerousArea.add(convert);

        }
        Log.d("dangerous : ", dangerousArea.toString());
        // New, after dangerous Area is have data, we will call MAp display
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.Map);
        mapFragment.getMapAsync(MapActivity.this);

        // clear map and add again
        if(googleMap != null)
        {
            googleMap.clear();;
            // Add user Marker
            addUserMarker();

            // Add Circle of dangerous area
            addCircleArea();
        }
    }

    @Override
    public void onLoadLocationFailed(String message) {
        Toast.makeText(this, ""+message,Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onDestroy() {
        if (ttsstt.tts != null) {
            if(ttsstt.tts.isSpeaking()) {
                ttsstt.tts.stop();
            }
            ttsstt.tts.shutdown();
        }
        super.onDestroy();
    }


    //2.T-map
    public void FindPOI() {
        fbLatLon.RemoveAll();

        pathItems = new ArrayList<PathItem>();

        //입력받은 목적지의 공백 제거
        destination_text = ttsstt.getDestination();
        final String destination_R = destination_text.replace(" ","");

        tMapData.findAllPOI(destination_R, new TMapData.FindAllPOIListenerCallback() {
            @Override
            public void onFindAllPOI(ArrayList<TMapPOIItem> arrayList) {
                for(int i = 0; i < arrayList.size(); i++){
                    TMapPOIItem item = (TMapPOIItem) arrayList.get(i);

                    String tmp = item.getPOIName();

                    //찾은 명칭에서 공백제거
                    tmp = tmp.replace(" ","");

                    if(tmp.equals(destination_R)){
                        endpoint = new TMapPoint(item.getPOIPoint().getLatitude(), item.getPOIPoint().getLongitude());
                        FindPathData();
                        return;
                    }
                }

                ttsstt.tts.speak("목적지를 찾지 못했습니다. 정확한 명칭을 입력해주세요.", TextToSpeech.QUEUE_FLUSH, null,null);
            }
        });
    }

    public void FindPathData() {
        fbLatLon.RemoveAll();

        runOnUiThread(new Runnable() {
                          @Override
                          public void run() {
                              googleMap.clear();
                              addUserMarker();
                              idx = 0;
                              addCircleArea();
                          }
                      });

        Document document = null;

        startpoint = new TMapPoint(lastLocation.getLatitude(), lastLocation.getLongitude());

        try {
            document = new TMapData().findPathDataAllType(TMapData.TMapPathType.PEDESTRIAN_PATH, startpoint, endpoint);
            root = document.getDocumentElement();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        } catch (SAXException e) {
            e.printStackTrace();
        }

        if (document != null) {
            XPathFactory xPathFactory = XPathFactory.newInstance();
            XPath xPath = xPathFactory.newXPath();

            try {
                XPathExpression expression = xPath.compile("//Placemark");
                NodeList nodeList = (NodeList) expression.evaluate(document, XPathConstants.NODESET);

                LatLng startLatlng = new LatLng(lastLocation.getLatitude(), lastLocation.getLongitude());

                polylineOptions = new PolylineOptions();
                polylineOptions.width(30).color(Color.RED).add(startLatlng);

                NodeList Dis = root.getElementsByTagName("tmap:totalDistance");

                //총 거리
                int iTotalDistance = Integer.parseInt(Dis.item(0).getChildNodes().item(0).getNodeValue());
                if (iTotalDistance > 1000) {
                    int km = iTotalDistance / 1000;
                    totalDistance = km + "km";
                } else {
                    totalDistance = iTotalDistance + "m";
                }

                NodeList time = root.getElementsByTagName("tmap:totalTime");

                //총 시간
                int iTotalTime = Integer.parseInt(time.item(0).getChildNodes().item(0).getNodeValue());
                int h = iTotalTime / 3000;
                int m = iTotalTime % 3000 / 60;

                if (h <= 0)
                    totalTime = m + "분";
                else
                    totalTime = h + "시간" + m + "분";

                for (int i = 0; i < nodeList.getLength(); i++) {
                    NodeList child = nodeList.item(i).getChildNodes();

                    int Turntype = -1;
                    TMapPoint tMapPoint;

                    for (int j = 0; j < child.getLength(); j++) {
                        Node node = child.item(j);
                        if (node.getNodeName().equals("tmap:turnType"))
                            Turntype = Integer.parseInt(node.getTextContent());

                        if (node.getNodeName().equals("Point")) {
                            String[] str = node.getTextContent().split(",");
                            tMapPoint = new TMapPoint(Double.parseDouble(str[1]), Double.parseDouble(str[0]));
                            PathItem pathItem = new PathItem(true, Turntype, tMapPoint);
                            pathItems.add(pathItem);

                            if (Turntype == 211 || Turntype == 212 || Turntype == 213 || Turntype == 214 || Turntype == 215 || Turntype == 216 || Turntype == 217) {
                                calculateTL.ReadDataSig(trafficLightInfo, tMapPoint, Turntype);
                            }

                            fbLatLon.writeNewLatLon(pathItem, idx++);
                        }
                    }
                }
            } catch (XPathExpressionException e) {
                e.printStackTrace();
            }

            for (int i = 0; i < pathItems.size(); i++)
                pathPoint.append(pathItems.get(i).toString() + "\n");

            NodeList list = document.getElementsByTagName("LineString");
            for (int i = 0; i < list.getLength(); i++) {
                Element element = (Element) list.item(i);
                String str = HttpConnect.getContentFromNode(element, "coordinates");

                if (str != null) {
                    String[] str2 = str.split(" ");

                    for (int j = 0; j < str2.length; j++) {
                        try {
                            String[] str3 = str2[j].split(",");
                            TMapPoint point = new TMapPoint(Double.parseDouble(str3[1]), Double.parseDouble(str3[0]));
                            LatLng pt = new LatLng(point.getLatitude(), point.getLongitude());
                            polylineOptions.add(pt);

                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            googleMap.addPolyline(polylineOptions);
                            LatLng position = new LatLng(endpoint.getLatitude(), endpoint.getLongitude());
                            String name = destination_text;
                            MarkerOptions markerOptions = new MarkerOptions();
                            markerOptions.title(name);
                            markerOptions.snippet("이동거리: " + totalDistance + "  소요 시간: " + totalTime);
                            markerOptions.position(position);
                            markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE));
                            googleMap.addMarker(markerOptions).showInfoWindow();
                        }
                    });
                }
            }
        }
        infoGeofence.initInfoArea(trafficLightInfo);
    }

}
