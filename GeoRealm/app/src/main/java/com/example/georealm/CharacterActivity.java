package com.example.georealm;

import android.content.res.Resources;
import android.graphics.Typeface;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.MarkerOptions;

public class CharacterActivity extends FragmentActivity implements OnMapReadyCallback {

    // MAPS
    private GoogleMap mMap;

    //TABS
    private Button button_info;
    private Button button_skills;
    private Button button_inventory;
    private Button current_button;

    private View marker_info;
    private View marker_skills;
    private View marker_inventory;
    private View current_marker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_character);

        // MAPS
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        // TABS
        button_info = findViewById(R.id.tab_button_info);
        button_skills = findViewById(R.id.tab_button_skills);
        button_inventory = findViewById(R.id.tab_button_inventory);

        marker_info = findViewById(R.id.tab_marker_info);
        marker_skills = findViewById(R.id.tab_marker_skills);
        marker_inventory = findViewById(R.id.tab_marker_inventory);

        current_button = button_info;
        current_marker = marker_info;

        button_info.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                current_button.setTypeface(Typeface.DEFAULT, Typeface.ITALIC);
                current_marker.setVisibility(View.INVISIBLE);

                current_button = button_info;
                current_marker = marker_info;

                current_button.setTypeface(Typeface.DEFAULT, Typeface.BOLD_ITALIC);
                current_marker.setVisibility(View.VISIBLE);
            }
        });

        button_skills.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                current_button.setTypeface(Typeface.DEFAULT, Typeface.ITALIC);
                current_marker.setVisibility(View.INVISIBLE);

                current_button = button_skills;
                current_marker = marker_skills;

                current_button.setTypeface(Typeface.DEFAULT, Typeface.BOLD_ITALIC);
                current_marker.setVisibility(View.VISIBLE);
            }
        });

        button_inventory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                current_button.setTypeface(Typeface.DEFAULT, Typeface.ITALIC);
                current_marker.setVisibility(View.INVISIBLE);

                current_button = button_inventory;
                current_marker = marker_inventory;

                current_button.setTypeface(Typeface.DEFAULT, Typeface.BOLD_ITALIC);
                current_marker.setVisibility(View.VISIBLE);
            }
        });
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {

        mMap = googleMap;

        try {

            mMap.setMapStyle(MapStyleOptions
                    .loadRawResourceStyle(this, R.raw.google_maps_style));
        }
        catch (Resources.NotFoundException e) {

            // exception
        }

        LatLng sydney = new LatLng(-34, 151);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(sydney, 15));
    }
}
