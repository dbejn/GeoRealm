package com.example.georealm;

import android.content.Intent;
import android.content.res.Resources;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

public class GameActivity extends FragmentActivity implements OnMapReadyCallback {

    // MAPS
    private GoogleMap mMap;

    // UI
    private ImageButton button_menu;
    private TextView text_menu;
    private LinearLayout layout_menu;

    private RelativeLayout layout_info;
    private ImageButton button_quit;
    private TextView text_quit;

    private ImageButton button_character;
    private ImageButton button_instructions;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);

        // MAPS
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        // UI
        layout_info = findViewById(R.id.layout_info);
        button_quit = findViewById(R.id.button_quit);
        button_quit.setVisibility(View.INVISIBLE);
        text_quit = findViewById(R.id.text_quit);
        text_quit.setVisibility(View.INVISIBLE);

        text_menu = findViewById(R.id.text_menu);
        layout_menu = findViewById(R.id.layout_menu);
        layout_menu.setVisibility(View.INVISIBLE);
        button_menu = findViewById(R.id.button_menu);
        button_menu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (text_menu.getText().toString().compareTo("menu") == 0) {

                    button_quit.setVisibility(View.VISIBLE);
                    text_quit.setVisibility(View.VISIBLE);

                    text_menu.setText(R.string.back);
                    button_menu.setImageResource(R.drawable.ic_arrow_up);
                    layout_info.setBackgroundResource(R.drawable.bot_left_rounded_rec_trans);
                    layout_menu.setVisibility(View.VISIBLE);
                }
                else {

                    button_quit.setVisibility(View.INVISIBLE);
                    text_quit.setVisibility(View.INVISIBLE);

                    layout_menu.setVisibility(View.INVISIBLE);
                    layout_info.setBackgroundResource(R.drawable.bot_rounded_rec_trans);
                    text_menu.setText(R.string.menu);
                    button_menu.setImageResource(R.drawable.ic_menu);
                }
            }
        });

        button_character = findViewById(R.id.button_character);
        button_character.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(GameActivity.this, CharacterActivity.class);
                startActivity(intent);
            }
        });

        button_instructions = findViewById(R.id.button_instructions);
        button_instructions.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(GameActivity.this, BattleActivity.class);
                startActivity(intent);
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

        LatLng my_marker_loc = new LatLng(-33.87365, 151.20689);
        LatLng character_marker_loc = new LatLng(-33.88365, 151.21689);
        LatLng gem_marker_loc = new LatLng(-33.895, 151.21);
        LatLng monster_marker_loc = new LatLng(-33.87, 151.22);
        LatLng shop_marker_loc = new LatLng(-33.884, 151.20);

        Marker my_marker = mMap.addMarker(new MarkerOptions()
                .position(my_marker_loc)
                .title("player")
                .snippet("berserker")
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.my_marker)));

        Marker character_marker = mMap.addMarker(new MarkerOptions()
                .position(character_marker_loc)
                .title("character")
                .snippet("paladin")
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.character_marker)));

        Marker gem_marker = mMap.addMarker(new MarkerOptions()
                .position(gem_marker_loc)
                .title("gem")
                .snippet("collect")
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.gem_marker)));

        Marker monster_marker = mMap.addMarker(new MarkerOptions()
                .position(monster_marker_loc)
                .title("monster")
                .snippet("slay")
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.monster_marker)));

        Marker shop_marker = mMap.addMarker(new MarkerOptions()
                .position(shop_marker_loc)
                .title("shop")
                .snippet("buy")
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.shop_marker)));

        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(my_marker_loc, 15));
    }
}
