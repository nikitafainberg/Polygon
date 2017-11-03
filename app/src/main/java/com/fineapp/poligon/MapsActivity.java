package com.fineapp.poligon;

import android.location.Location;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.kml.KmlContainer;
import com.google.maps.android.kml.KmlLayer;
import com.google.maps.android.kml.KmlPlacemark;
import com.google.maps.android.kml.KmlPolygon;

import java.util.ArrayList;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap map;
    private ArrayList<LatLng> outers;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        map = googleMap;

        LatLng israel = new LatLng(32.070779943443,34.79991805485883);
        map.moveCamera(CameraUpdateFactory.newLatLng(israel));
        CameraPosition cameraPosition = new CameraPosition.Builder().target(israel).zoom(14).build();
        map.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));

        final KmlLayer layer;

        try {
            layer = new KmlLayer(map, R.raw.allowedarea, MapsActivity.this);
            layer.addLayerToMap();

            for (KmlContainer container : layer.getContainers()) {
                Iterable<KmlPlacemark> placemarks = container.getPlacemarks();
                if (placemarks != null){
                    for (KmlPlacemark placemark : placemarks){
                        outers = ((KmlPolygon) placemark.getGeometry()).getOuterBoundaryCoordinates();
                    }
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        map.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                if (isPointInPolygon(latLng)){
                    Toast.makeText(MapsActivity.this, "True", Toast.LENGTH_SHORT).show();
                }else {
                    Toast.makeText(MapsActivity.this, "you need " + getClosest(latLng) / 1000 + " km to rich the goal", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private double getClosest(LatLng latLng){

        Location locationClick = new Location("locationClick");
        locationClick.setLatitude(latLng.latitude);
        locationClick.setLongitude(latLng.longitude);
        double length = 0;


        for (int i = 0; i < outers.size(); i++){

            Location location = new Location("");
            location.setLatitude(outers.get(i).latitude);
            location.setLongitude(outers.get(i).longitude);
            if (length == 0){
                length = locationClick.distanceTo(location);
            }else {
                double newLength = locationClick.distanceTo(location);
                if (newLength < length){
                    length = newLength;
                }
            }
        }

        return length;
    }

    private boolean isPointInPolygon(LatLng latLng) {
        int count = 0;
        for (int i = 0; i < outers.size() - 1; i++) {
            if (check(latLng, outers.get(i), outers.get(i + 1))) {
                count++;
            }
        }
        return ((count % 2) == 1);
    }

    private boolean check(LatLng click, LatLng poinA, LatLng pointB) {

        double x1 = poinA.longitude;
        double y1 = poinA.latitude;
        double x2 = pointB.longitude;
        double y2 = pointB.latitude;
        double xClick = click.longitude;
        double yClick = click.latitude;

        if ((y1 > yClick && y2 > yClick) || (y1 < yClick && y2 < yClick) || (x1 < xClick && x2 < xClick)) {
            return false;
        }

        double scale = (y1 - y2) / (x1 - x2);
        double a = (-x1) * scale + y1;
        double x = (yClick - a) / scale;

        return x > xClick;
    }
}