package org.tensorflow.demo;

import android.graphics.Color;
import android.location.Location;
import android.os.AsyncTask;
import android.speech.tts.TextToSpeech;
import android.widget.Toast;

import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.skt.Tmap.TMapData;
import com.skt.Tmap.TMapPoint;
import com.skt.Tmap.util.HttpConnect;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

public class FindPathData {
    private TMapData tMapData;
    private ArrayList<PathItem> pathItems;
    private TMapPoint startpoint;
    private TMapPoint endpoint;
    private Location location;
    public PolylineOptions polylineOptions;
    private String totalDistance;
    private String totalTime;
    private String destination;
    private CalculateTL calculateTL;
    private List<TrafficLightInfo> trafficLightInfoList;
    private FBLatLon fbLatLon;
    private StringBuilder pathPoint;
    private MarkerOptions markerOptions;

    int idx = 0;
    private Element root;

    public FindPathData(TMapData tMapData) {
        this.tMapData = tMapData;
        pathItems = new ArrayList<PathItem>();
        calculateTL = new CalculateTL();
        trafficLightInfoList = new ArrayList<>();
        fbLatLon = new FBLatLon();
        pathPoint = new StringBuilder();
        polylineOptions = new PolylineOptions();
        markerOptions = new MarkerOptions();
    }

    public void Find(String destination, Location lastlocation, TMapPoint endpoint) {
        fbLatLon.RemoveAll();
        idx = 0;

        this.endpoint = endpoint;
        this.destination = destination;

        Document document = null;

        startpoint = new TMapPoint(lastlocation.getLatitude(), lastlocation.getLongitude());

        try {
            document = new TMapData().findPathDataAllType(TMapData.TMapPathType.PEDESTRIAN_PATH, startpoint, endpoint);
            //root = document.getDocumentElement();
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

                LatLng startLatlng = new LatLng(lastlocation.getLatitude(), lastlocation.getLongitude());

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
                                calculateTL.ReadDataSig(trafficLightInfoList, tMapPoint, Turntype);
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


                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }

                    LatLng position = new LatLng(endpoint.getLatitude(), endpoint.getLongitude());
                    String name = destination;
                    markerOptions.title(name);
//                    markerOptions.snippet("이동거리: " + totalDistance + " 소요시간: " + totalTime);
//                    markerOptions.position(position);
                    markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE));

                    ((MapActivity)MapActivity.mContext).FindPOI();
                }
            }
        }
    }




    public ArrayList<PathItem> getPathItems() {
        return pathItems;
    }

    public PolylineOptions getPolylineOptions() {
        return polylineOptions;
    }

    public MarkerOptions getMarkerOptions() {
        return markerOptions;
    }
}
