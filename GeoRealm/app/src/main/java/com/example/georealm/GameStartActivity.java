package com.example.georealm;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.location.Location;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.IdpResponse;
import com.google.android.gms.common.Scopes;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.Arrays;

public class GameStartActivity extends FragmentActivity implements OnMapReadyCallback {

    // MAPS
    private GoogleMap map;

    private FusedLocationProviderClient fused_location_provider_client;

    private final LatLng default_location = new LatLng(-33.8523341, 151.2106085);
    private static final int DEFAULT_ZOOM = 15;
    private static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;
    private boolean location_permission_granted;

    private Location last_known_location;

    private static final String KEY_LOCATION = "location";

    // UI
    private AlertDialog.Builder dialog_builder;
    private ImageButton button_quit;
    private DialogInterface.OnClickListener quit_dialog_click_listener;
    private RelativeLayout layout_main_menu;
    private RelativeLayout layout_choose_character;
    private RelativeLayout layout_select_character;
    private RelativeLayout layout_my_account;
    private ImageButton button_play;
    private ImageButton button_back;
    private ImageButton button_create;
    private ImageButton button_select_character;
    private ImageButton button_my_account;
    private ImageButton button_highscore;

    // LOGIN
    private static final int RC_SIGN_IN = 123;

    private RelativeLayout layout_login;
    private RelativeLayout layout_logout;
    private ImageButton button_login;
    private ImageButton button_logout;
    FirebaseUser user;

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

        // UI
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

        layout_main_menu = findViewById(R.id.layout_main_menu);
        layout_choose_character = findViewById(R.id.layout_choose_character);
        layout_select_character = findViewById(R.id.layout_select_character);
        layout_my_account = findViewById(R.id.layout_my_account);

        button_back = findViewById(R.id.button_back);
        button_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                layout_choose_character.setVisibility(View.GONE);
                layout_main_menu.setVisibility(View.VISIBLE);
            }
        });

        button_play = findViewById(R.id.button_play);
        button_play.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // Toast.makeText(getApplicationContext(), "play", Toast.LENGTH_LONG).show();
                Intent intent = new Intent(GameStartActivity.this,
                        GameActivity.class);

                startActivity(intent);
            }
        });

        button_create = findViewById(R.id.button_create);
        button_create.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // Toast.makeText(getApplicationContext(), "create", Toast.LENGTH_LONG).show();

                Intent intent = new Intent(GameStartActivity.this, CreateCharacterActivity.class);
                startActivity(intent);
            }
        });

        button_select_character = findViewById(R.id.button_select_character);
        button_select_character.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                chooseCharacter();
            }
        });

        button_my_account = findViewById(R.id.button_my_account);
        button_my_account.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // Toast.makeText(getApplicationContext(), "my account", Toast.LENGTH_LONG).show();

                Intent intent = new Intent(GameStartActivity.this, AccountActivity.class);
                startActivity(intent);
            }
        });

        button_highscore = findViewById(R.id.button_highscore);
        button_highscore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // Toast.makeText(getApplicationContext(), "highscore", Toast.LENGTH_LONG).show();

                Intent intent = new Intent(GameStartActivity.this, HighscoreActivity.class);
                startActivity(intent);
            }
        });

        // LOGIN
        layout_login = findViewById(R.id.layout_login);
        button_login = findViewById(R.id.button_login);
        button_login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                AuthUI.IdpConfig googleIdp = new AuthUI.IdpConfig.GoogleBuilder()
                        .setScopes(Arrays.asList(Scopes.PROFILE))
                        .build();

                startActivityForResult(
                        AuthUI.getInstance()
                                .createSignInIntentBuilder()
                                .setAvailableProviders(Arrays.asList(googleIdp))
                                .build(), RC_SIGN_IN);
            }
        });

        button_logout = findViewById(R.id.button_logout);
        layout_logout = findViewById(R.id.layout_logout);
        button_logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Toast.makeText(getApplicationContext(), " logging out",
                        Toast.LENGTH_LONG).show();

                final String user_email = user.getEmail();

                AuthUI.getInstance()
                        .signOut(getApplicationContext())
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {

                                Toast.makeText(getApplicationContext(),
                                        user_email + " logged out",
                                        Toast.LENGTH_LONG).show();

                                updateUI(null);
                            }
                        });
            }
        });

        // UI
        user = FirebaseAuth.getInstance().getCurrentUser();
        updateUI(user);
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
                        } else {

                            map.moveCamera(CameraUpdateFactory
                                    .newLatLngZoom(default_location, DEFAULT_ZOOM));
                            map.getUiSettings().setMyLocationButtonEnabled(false);
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

        if (user == null) {

            layout_main_menu.setVisibility(View.VISIBLE);
            layout_choose_character.setVisibility(View.GONE);
            layout_my_account.setVisibility(View.GONE);
            layout_logout.setVisibility(View.GONE);
            layout_select_character.setVisibility(View.GONE);
            layout_login.setVisibility(View.VISIBLE);
        }
        else {

            layout_main_menu.setVisibility(View.VISIBLE);
            layout_choose_character.setVisibility(View.GONE);
            layout_login.setVisibility(View.GONE);
            layout_select_character.setVisibility(View.VISIBLE);
            layout_my_account.setVisibility(View.VISIBLE);
            layout_logout.setVisibility(View.VISIBLE);

            Toast.makeText(getApplicationContext(), "welcome " + user.getEmail(),
                    Toast.LENGTH_LONG).show();
        }
    }

    // LOGIN
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {

        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {

            IdpResponse response = IdpResponse.fromResultIntent(data);

            if (resultCode == RESULT_OK) {

                user = FirebaseAuth.getInstance().getCurrentUser();

                // Toast.makeText(getApplicationContext(), "welcome " + user.getEmail(),
                //         Toast.LENGTH_LONG).show();

                updateUI(user);

                chooseCharacter();
            }
            else {

                Toast.makeText(getApplicationContext(), "login failed",
                        Toast.LENGTH_LONG).show();
            }
        }
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

    private void chooseCharacter() {

        // get characters from firestore
        // if no characters => message

        layout_main_menu.setVisibility(View.GONE);
        layout_choose_character.setVisibility(View.VISIBLE);
    }
}
