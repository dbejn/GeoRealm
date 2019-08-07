package com.example.georealm.adapters;

import android.app.Activity;
import android.view.View;

import com.example.georealm.R;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.Marker;

public class MarkerInfoWindowAdapter implements GoogleMap.InfoWindowAdapter {

    private Activity context;

    public MarkerInfoWindowAdapter(Activity ctx) {

        context = ctx;
    }

    @Override
    public View getInfoWindow(Marker marker) {

        if (marker.getTitle().equals("gem")) {

            return context.getLayoutInflater().inflate(R.layout.layout_marker_gem, null);
        }

        return null;
    }

    @Override
    public View getInfoContents(Marker marker) {

        return null;
    }
}
