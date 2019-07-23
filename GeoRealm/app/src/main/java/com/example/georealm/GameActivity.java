package com.example.georealm;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.location.Location;
import android.provider.DocumentsContract;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.ImageViewCompat;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.georealm.data.CharacterData;
import com.example.georealm.helper.Constants;
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
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import static com.example.georealm.helper.Constants.DEFAULT_ZOOM;
import static com.example.georealm.helper.Constants.MIN_ZOOM;

public class GameActivity extends FragmentActivity implements OnMapReadyCallback {

    // MAPS
    private GoogleMap map;

    private FusedLocationProviderClient fused_location_provider_client;
    private LocationRequest location_request;
    private LocationCallback location_callback;

    private static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;
    private boolean location_permission_granted;

    private Location last_known_location;

    private static final String KEY_LOCATION = "location";

    private Marker my_marker;

    // UI
    private Button button_menu;
    private LinearLayout layout_menu;

    private ConstraintLayout layout_info;
    private Button button_quit;

    private Button button_view_character;
    private Button button_change_character;
    private Button button_highscore;
    private Button button_instructions;

    private ImageView class_image;
    private TextView text_character;
    private String character_name;
    private String character_class;
    private String character_subclass;
    private int character_level;
    private String username;

    private ProgressBar progress_bar;

    // FIRESTORE
    private FirebaseFirestore database;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (savedInstanceState != null) {

            last_known_location = savedInstanceState.getParcelable(KEY_LOCATION);
        }

        setContentView(R.layout.activity_game);

        username = getIntent().getStringExtra("username");
        character_name = getIntent().getStringExtra("character_name");
        character_class = getIntent().getStringExtra("character_class");
        character_subclass = getIntent().getStringExtra("character_subclass");
        character_level = getIntent().getIntExtra("character_level", 1);

        // MAPS
        fused_location_provider_client = LocationServices
                .getFusedLocationProviderClient(this);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        location_callback = new LocationCallback() {

            @Override
            public void onLocationResult(LocationResult locationResult) {

                if (locationResult == null) {

                    return;
                }

                Location location = locationResult.getLastLocation();
                if (location != null) {

                    last_known_location = location;

                    if (my_marker != null) {

                        my_marker.setPosition(new LatLng(last_known_location.getLatitude(), last_known_location.getLongitude()));
                    }

                    if (map != null) {

                        map.animateCamera(CameraUpdateFactory.newLatLngZoom(
                                new LatLng(last_known_location.getLatitude(),
                                        last_known_location.getLongitude()), map.getCameraPosition().zoom));
                    }
                }
            }
        };

        location_request = new LocationRequest().setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY).setInterval(10000);

        // UI
        layout_info = findViewById(R.id.layout_info);
        button_quit = findViewById(R.id.button_quit);
        button_quit.setVisibility(View.INVISIBLE);

        layout_menu = findViewById(R.id.layout_menu);
        layout_menu.setVisibility(View.INVISIBLE);
        button_menu = findViewById(R.id.button_menu);
        button_menu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (button_menu.getText().toString().compareTo("menu") == 0) {

                    button_quit.setVisibility(View.VISIBLE);

                    button_menu.setText(R.string.back);
                    button_menu.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_arrow_up,  0);
                    layout_info.setBackgroundResource(R.drawable.bot_left_rounded_rec_trans);
                    layout_menu.setVisibility(View.VISIBLE);
                }
                else {

                    button_quit.setVisibility(View.INVISIBLE);

                    layout_menu.setVisibility(View.GONE);
                    layout_info.setBackgroundResource(R.drawable.bot_rounded_rec_trans);
                    button_menu.setText(R.string.menu);
                    button_menu.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_menu,  0);
                }
            }
        });

        button_view_character = findViewById(R.id.button_view_character);
        button_view_character.setOnClickListener(new View.OnClickListener() {
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

        button_change_character = findViewById(R.id.button_change_character);

        button_highscore = findViewById(R.id.button_highscore);

        text_character = findViewById(R.id.text_character);
        class_image = findViewById(R.id.class_image);

        progress_bar = findViewById(R.id.progress_bar);


        // FIRESTORE
        progress_bar.setVisibility(View.VISIBLE);

        database = FirebaseFirestore.getInstance();
        DocumentReference character_doc = database.collection("characters").document("character_name");
        character_doc.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {

                if (task.isSuccessful()) {

                    DocumentSnapshot document = task.getResult();
                    if (!document.exists()) {

                        CharacterData character_data = new CharacterData(
                                character_name, character_class, character_subclass, character_level);

                        DocumentReference character_create_doc = database
                                .collection("characters").document("character_name");
                        character_create_doc.set(character_data).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {

                                if (task.isSuccessful()) {

                                    setClassImage(character_class);
                                    text_character.setText(getString(R.string.name_level, character_name, character_level));

                                    progress_bar.setVisibility(View.GONE);

                                    Toast.makeText(GameActivity.this,
                                            "Welcome to the GeoRealm", Toast.LENGTH_SHORT).show();
                                }
                                else {

                                    Toast.makeText(GameActivity.this,
                                            "Could not start a game, please try again", Toast.LENGTH_SHORT).show();

                                    finish();
                                }
                            }
                        });
                    }
                    else {

                        setClassImage(character_class);
                        text_character.setText(getString(R.string.name_level, character_name, character_level));

                        progress_bar.setVisibility(View.GONE);

                        Toast.makeText(GameActivity.this,
                                "Welcome to the GeoRealm", Toast.LENGTH_SHORT).show();
                    }
                }
                else {

                    Toast.makeText(GameActivity.this,
                            "Could not start a game, please try again", Toast.LENGTH_SHORT).show();

                    finish();
                }
            }
        });
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {

        if (map != null) {

            outState.putParcelable(KEY_LOCATION, last_known_location);
            super.onSaveInstanceState(outState);
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {

        map = googleMap;

        try {

            map.setMapStyle(MapStyleOptions
                    .loadRawResourceStyle(this, R.raw.google_maps_style));
        }
        catch (Resources.NotFoundException e) {

            // exception
        }

        getLocationPermission();

        updateLocationUI();

        getDeviceLocation();
    }

    private void getLocationPermission() {

        if (ContextCompat.checkSelfPermission(this.getApplicationContext(),
                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

            location_permission_granted = true;
        }
        else {

            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
        }
    }

    private void updateLocationUI() {

        if (map == null) {

            return;
        }
        else {

            map.getUiSettings().setAllGesturesEnabled(false);
            map.getUiSettings().setZoomGesturesEnabled(true);
            map.getUiSettings().setRotateGesturesEnabled(true);
            map.getUiSettings().setCompassEnabled(false);
            map.setMinZoomPreference(MIN_ZOOM);
            map.setOnCameraIdleListener(new GoogleMap.OnCameraIdleListener() {
                @Override
                public void onCameraIdle() {

                    CameraUpdate animation = CameraUpdateFactory
                            .newLatLngZoom(new LatLng(last_known_location.getLatitude(),
                                    last_known_location.getLongitude()),
                                    map.getCameraPosition().zoom);
                    map.animateCamera(animation);
                }
            });
        }

        try {

            if (location_permission_granted) {

                // map.setMyLocationEnabled(true);
                // map.getUiSettings().setMyLocationButtonEnabled(true);
            }
            else {

                // map.setMyLocationEnabled(false);
                // map.getUiSettings().setMyLocationButtonEnabled(false);
                last_known_location = null;
                getLocationPermission();
            }
        }
        catch (SecurityException e) {

            Log.e("Exception: %s", e.getMessage());
        }
    }

    private void getDeviceLocation() {

        try {

            if (location_permission_granted) {

                Task<Location> location_result = fused_location_provider_client.getLastLocation();
                location_result.addOnCompleteListener(this, new OnCompleteListener<Location>() {
                    @Override
                    public void onComplete(@NonNull Task<Location> task) {
                        if (task.isSuccessful()) {

                            last_known_location = task.getResult();
                            if (last_known_location != null) {

                                map.animateCamera(CameraUpdateFactory.newLatLngZoom(
                                        new LatLng(last_known_location.getLatitude(),
                                                last_known_location.getLongitude()), DEFAULT_ZOOM));

                                my_marker = map.addMarker(new MarkerOptions()
                                        .position(new LatLng(last_known_location.getLatitude(), last_known_location.getLongitude()))
                                        .title(username)
                                        .snippet(character_name)
                                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.my_marker)));
                            }
                            else {

                                Toast.makeText(GameActivity.this, "Error fetching location", Toast.LENGTH_SHORT).show();
                                finish();
                            }
                        }
                        else {

                            Toast.makeText(GameActivity.this, "Error fetching location", Toast.LENGTH_SHORT).show();
                            finish();
                        }
                    }
                });
            }
        } catch (SecurityException e) {

            Log.e("Exception: %s", e.getMessage());
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {

        location_permission_granted = false;
        switch (requestCode) {

            case PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION: {

                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    location_permission_granted = true;
                }
            }
        }

        updateLocationUI();
    }

    private void setClassImage(String classs) {

        switch (classs) {

            case "swordsman":
                class_image.setImageResource(R.drawable.ic_sword);
                break;
            case "sorcerer":
                class_image.setImageResource(R.drawable.ic_hat);
                break;
            case "rogue":
                class_image.setImageResource(R.drawable.ic_dagger);
                break;
        }
    }

    private void startLocationUpdates() {

        if (ContextCompat.checkSelfPermission(this.getApplicationContext(),
                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

            fused_location_provider_client.requestLocationUpdates(location_request, location_callback, null);
        }
        else {

            Toast.makeText(GameActivity.this, "Error fetching location", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void stopLocationUpdates() {

        fused_location_provider_client.removeLocationUpdates(location_callback);
    }

    @Override
    protected void onResume() {

        super.onResume();

        startLocationUpdates();
    }

    @Override
    protected void onPause() {

        super.onPause();

        stopLocationUpdates();
    }
}
