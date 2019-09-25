package com.example.georealm;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.location.Location;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;
import android.os.Bundle;
import androidx.core.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static com.example.georealm.helper.Constants.DEFAULT_ZOOM;

public class GameStartActivity extends FragmentActivity implements OnMapReadyCallback {

    // MAPS
    private GoogleMap map;
    private double latitude;
    private double longitude;

    private FusedLocationProviderClient fused_location_provider_client;

    private final LatLng default_location = new LatLng(-33.8523341, 151.2106085);
    private static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;
    private boolean location_permission_granted;

    private Location last_known_location;

    private static final String KEY_LOCATION = "location";

    // UI
    private AlertDialog.Builder dialog_builder;
    private Button button_quit;
    private DialogInterface.OnClickListener quit_dialog_click_listener;
    private Button button_select_character;
    private Button button_my_account;
    private Button button_highscore;
    private Button button_instructions;
    private ProgressBar progress_bar;

    // LOGIN
    private static final int LOGIN_REQUEST = 1;

    private Button button_login;
    private Button button_logout;
    private FirebaseAuth authentication;
    private FirebaseUser user;

    // FIRESTORE
    FirebaseFirestore database;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (savedInstanceState != null) {

            last_known_location = savedInstanceState.getParcelable(KEY_LOCATION);
        }

        setContentView(R.layout.activity_game_start);


        // MAPS
        fused_location_provider_client = LocationServices
                .getFusedLocationProviderClient(this);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);


        // FIREBASE AUTH
        authentication = FirebaseAuth.getInstance();


        // FIRESTORE
        database = FirebaseFirestore.getInstance();


        // UI
        progress_bar = findViewById(R.id.progress_bar);

        dialog_builder = new AlertDialog.Builder(GameStartActivity.this);
        quit_dialog_click_listener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                switch (which) {

                    case DialogInterface.BUTTON_POSITIVE:

                        finish();
                        break;

                    case DialogInterface.BUTTON_NEGATIVE:

                        dialog.dismiss();
                        break;
                }
            }
        };

        button_quit = findViewById(R.id.button_quit);
        button_quit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                dialog_builder.setTitle("Quit GeoRealm");
                dialog_builder.setMessage("Are you sure?");
                dialog_builder.setPositiveButton("Yes", quit_dialog_click_listener);
                dialog_builder.setNegativeButton("No", quit_dialog_click_listener);
                dialog_builder.show();
            }
        });

        button_select_character = findViewById(R.id.button_select_character);
        button_select_character.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(GameStartActivity.this, ChooseCharacterActivity.class);
                intent.putExtra("latitude", latitude);
                intent.putExtra("longitude", longitude);
                intent.putExtra("username", user.getDisplayName());
                startActivity(intent);
            }
        });

        button_my_account = findViewById(R.id.button_my_account);
        button_my_account.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(GameStartActivity.this, AccountActivity.class);
                intent.putExtra("latitude", latitude);
                intent.putExtra("longitude", longitude);
                intent.putExtra("username", user.getDisplayName());
                intent.putExtra("type", "user");
                startActivity(intent);
            }
        });

        button_highscore = findViewById(R.id.button_highscore);
        button_highscore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(GameStartActivity.this, HighscoreActivity.class);
                intent.putExtra("latitude", latitude);
                intent.putExtra("longitude", longitude);
                if (user != null)
                    intent.putExtra("username", user.getDisplayName());
                else
                    intent.putExtra("username", "");
                startActivity(intent);
            }
        });


        // LOGIN
        button_login = findViewById(R.id.button_login);
        button_login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(GameStartActivity.this, LoginActivity.class);
                intent.putExtra("latitude", latitude);
                intent.putExtra("longitude", longitude);
                startActivityForResult(intent, LOGIN_REQUEST);
            }
        });

        button_logout = findViewById(R.id.button_logout);
        button_logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                progress_bar.setVisibility(View.VISIBLE);
                enableCommands(false);

                final String username = user.getDisplayName();

                AuthUI.getInstance()
                        .signOut(getApplicationContext())
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {

                                Toast.makeText(getApplicationContext(),
                                        username + " logged out",
                                        Toast.LENGTH_LONG).show();

                                user = null;
                                updateUI(null);

                                progress_bar.setVisibility(View.INVISIBLE);
                                enableCommands(true);
                            }
                        });
            }
        });

        button_instructions = findViewById(R.id.button_instructions);


        // UI
        setupUser();
    }

    // MAPS
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

    private void getDeviceLocation() {

        try {

            if (location_permission_granted) {

                Task<Location> location_result = fused_location_provider_client.getLastLocation();
                location_result.addOnCompleteListener(this, new OnCompleteListener<Location>() {
                    @Override
                    public void onComplete(@NonNull Task<Location> task) {
                        if (task.isSuccessful()) {

                            last_known_location = task.getResult();
                            map.moveCamera(CameraUpdateFactory.newLatLngZoom(
                                    new LatLng(last_known_location.getLatitude(),
                                            last_known_location.getLongitude()), DEFAULT_ZOOM));

                            latitude = last_known_location.getLatitude();
                            longitude = last_known_location.getLongitude();
                        }
                        else {

                            map.moveCamera(CameraUpdateFactory
                                    .newLatLngZoom(default_location, DEFAULT_ZOOM));
                            map.getUiSettings().setMyLocationButtonEnabled(false);

                            latitude = default_location.latitude;
                            longitude = default_location.longitude;
                        }
                    }
                });
            }
        } catch (SecurityException e) {

            Log.e("Exception: %s", e.getMessage());
        }
    }

    private void updateLocationUI() {

        if (map == null) {

            return;
        }
        else {

            map.getUiSettings().setAllGesturesEnabled(false);
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

    // UI
    @Override
    public void onBackPressed() {

        dialog_builder.setTitle("Quit GeoRealm");
        dialog_builder.setMessage("Are you sure?");
        dialog_builder.setPositiveButton("Yes", quit_dialog_click_listener);
        dialog_builder.setNegativeButton("No", quit_dialog_click_listener);
        dialog_builder.show();

        // super.onBackPressed();
    }

    private void updateUI(FirebaseUser user) {

        if (user == null || user.getDisplayName() == null) {

            button_my_account.setVisibility(View.GONE);
            button_logout.setVisibility(View.GONE);
            button_select_character.setVisibility(View.GONE);
            button_login.setVisibility(View.VISIBLE);
        }
        else {

            button_login.setVisibility(View.GONE);
            button_select_character.setVisibility(View.VISIBLE);
            button_my_account.setVisibility(View.VISIBLE);
            button_logout.setVisibility(View.VISIBLE);
        }
    }

    // LOGIN
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {

        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == LOGIN_REQUEST) {

            if (resultCode == RESULT_OK) {

                setupUser();
            }
        }
    }

    private void setupUser() {

        progress_bar.setVisibility(View.VISIBLE);
        enableCommands(false);

        user = authentication.getCurrentUser();

        if (user != null) {

            if (user.getDisplayName() != null) {

                DocumentReference user_ref = database.collection("users").document(user.getDisplayName());
                user_ref.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {

                        if (task.isSuccessful()) {

                            DocumentSnapshot document = task.getResult();
                            if (document.exists()) {

                                Toast.makeText(GameStartActivity.this, "Welcome " + user.getDisplayName(), Toast.LENGTH_SHORT).show();

                                updateUI(user);

                                progress_bar.setVisibility(View.GONE);
                                enableCommands(true);
                            }
                            else {

                                SimpleDateFormat date_format = new SimpleDateFormat("dd.MM.yyyy HH:mm");
                                Date date = Calendar.getInstance().getTime();
                                String string_date = date_format.format(date);

                                Map<String, Object> user_document = new HashMap<>();
                                user_document.put("score", 0);
                                user_document.put("latitude", 0);
                                user_document.put("longitude", 0);
                                user_document.put("status", "offline");
                                user_document.put("last_online", string_date);

                                database.collection("users").document(user.getDisplayName()).set(user_document)
                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {

                                                Toast.makeText(GameStartActivity.this,
                                                        "Account successfully created. Welcome " + user.getDisplayName(),
                                                        Toast.LENGTH_SHORT).show();

                                                updateUI(user);

                                                progress_bar.setVisibility(View.GONE);
                                                enableCommands(true);
                                            }
                                        })
                                        .addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {

                                                Toast.makeText(GameStartActivity.this, "Failed to create user document", Toast.LENGTH_SHORT).show();

                                                updateUI(user);

                                                progress_bar.setVisibility(View.GONE);
                                                enableCommands(true);
                                            }
                                        });
                            }
                        }
                        else {

                            Toast.makeText(GameStartActivity.this, "Failed to get user document", Toast.LENGTH_SHORT).show();

                            updateUI(user);

                            progress_bar.setVisibility(View.GONE);
                            enableCommands(true);
                        }
                    }
                });
            }
            else {

                Toast.makeText(GameStartActivity.this,
                        "No username", Toast.LENGTH_SHORT).show();

                progress_bar.setVisibility(View.GONE);
                enableCommands(true);

                // otvori activity za promenu username-a
            }
        }
        else {

            updateUI(null);

            progress_bar.setVisibility(View.GONE);
            enableCommands(true);
        }
    }

    private void enableCommands(boolean enable) {

        button_login.setEnabled(enable);
        button_logout.setEnabled(enable);
        button_my_account.setEnabled(enable);
        button_select_character.setEnabled(enable);
        button_highscore.setEnabled(enable);
        button_quit.setEnabled(enable);
        button_instructions.setEnabled(enable);
    }

    // LIFE CYCLE
    @Override
    protected void onRestart() {

        super.onRestart();
        // Toast.makeText(getApplicationContext(), "on restart", Toast.LENGTH_LONG).show();
    }

    @Override
    protected void onStart() {

        super.onStart();
        // Toast.makeText(getApplicationContext(), "on start", Toast.LENGTH_LONG).show();
    }

    @Override
    protected void onResume() {

        super.onResume();
        // Toast.makeText(getApplicationContext(), "on resume", Toast.LENGTH_LONG).show();
    }

    @Override
    protected void onPause() {

        // Toast.makeText(getApplicationContext(), "on pause", Toast.LENGTH_LONG).show();
        super.onPause();
    }

    @Override
    protected void onStop() {

        // Toast.makeText(getApplicationContext(), "on stop", Toast.LENGTH_LONG).show();
        super.onStop();
    }

    @Override
    protected void onDestroy() {

        // Toast.makeText(getApplicationContext(), "on destroy", Toast.LENGTH_LONG).show();
        super.onDestroy();
    }
}
