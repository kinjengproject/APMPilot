package org.kinjeng.apmpilot.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.maps.MapFragment;

import org.kinjeng.apmpilot.R;

/**
 * Created by sblaksono on 20/11/2016.
 */

public class FlightMapFragment extends MapFragment {

    @Override
    public View onCreateView(LayoutInflater layoutInflater, ViewGroup viewGroup, Bundle bundle) {
        return layoutInflater.inflate(R.layout.fragment_flight_map, viewGroup, false);
    }

}
