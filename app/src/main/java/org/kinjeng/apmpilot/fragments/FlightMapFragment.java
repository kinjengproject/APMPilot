package org.kinjeng.apmpilot.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.o3dr.services.android.lib.coordinate.LatLong;

import org.kinjeng.apmpilot.classes.Settings;

/**
 * Created by sblaksono on 20/11/2016.
 */

public class FlightMapFragment extends SupportMapFragment implements OnMapReadyCallback {

    protected GoogleMap map;

    public void init() {
        getMapAsync(this);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        map = googleMap;
        map.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
        map.getUiSettings().setAllGesturesEnabled(false);
        try {
            map.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(-6.175387, 106.827131), Settings.getInt("pref_map_zoom", 18)));
        }
        catch (Exception e) {

        }
    }

    public void moveCamera(LatLong latLong) {
        map.moveCamera(CameraUpdateFactory.newLatLng(new LatLng(latLong.getLatitude(), latLong.getLongitude())));
    }

}
