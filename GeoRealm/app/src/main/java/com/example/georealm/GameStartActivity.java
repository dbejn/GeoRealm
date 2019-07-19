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
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GetTokenResult;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

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
    private RelativeLayout layout_select_character;
    private RelativeLayout layout_my_account;
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
    String user_id_token;

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

        // FIRESTORE
        database = FirebaseFirestore.getInstance();

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

        layout_select_character = findViewById(R.id.layout_select_character);
        layout_my_account = findViewById(R.id.layout_my_account);

        button_select_character = findViewById(R.id.button_select_character);
        button_select_character.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(GameStartActivity.this, ChooseCharacterActivity.class);
                intent.putExtra("user_id_token", user_id_token);
                startActivity(intent);
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

                final String username = user.getDisplayName();

                AuthUI.getInstance()
                        .signOut(getApplicationContext())
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {

                                Toast.makeText(getApplicationContext(),
                                        username + " logged out",
                                        Toast.LENGTH_LONG).show();

                                updateUI(null);

                                user = null;
                                user_id_token = "";
                            }
                        });
            }
        });

        // UI
        user = FirebaseAuth.getInstance().getCurrentUser();
        getUserFromDatabase();
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

            layout_my_account.setVisibility(View.GONE);
            layout_logout.setVisibility(View.GONE);
            layout_select_character.setVisibility(View.GONE);
            layout_login.setVisibility(View.VISIBLE);
        }
        else {

            layout_login.setVisibility(View.GONE);
            layout_select_character.setVisibility(View.VISIBLE);
            layout_my_account.setVisibility(View.VISIBLE);
            layout_logout.setVisibility(View.VISIBLE);
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

                getUserFromDatabase();
                updateUI(user);
            }
            else {

                Toast.makeText(getApplicationContext(), "login failed",
                        Toast.LENGTH_LONG).show();
            }
        }
    }

    private void getUserFromDatabase() {

        if (user != null) {

            user.getIdToken(false).addOnSuccessListener(new OnSuccessListener<GetTokenResult>() {
                @Override
                public void onSuccess(GetTokenResult getTokenResult) {

                    user_id_token = getTokenResult.getToken();
                    user_id_token = user_id_token.substring(0, 100);

                    DocumentReference user_ref = database.collection("users").document(user_id_token);
                    user_ref.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {

                            if (task.isSuccessful()) {

                                DocumentSnapshot document = task.getResult();
                                if (document.exists()) {

                                    String username = document.getString("username");
                                    Toast.makeText(GameStartActivity.this, "Welcome " + username, Toast.LENGTH_SHORT).show();
                                }
                                else {

                                    Map<String, Object> user_document = new HashMap<>();
                                    user_document.put("email", user.getEmail());
                                    user_document.put("username", user.getDisplayName());

                                    database.collection("users").document(user_id_token).set(user_document)
                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @Override
                                                public void onSuccess(Void aVoid) {

                                                    String username = user.getDisplayName();
                                                    Toast.makeText(GameStartActivity.this, "Welcome " + username +
                                                            ". You can change username in the My Account options", Toast.LENGTH_SHORT).show();
                                                }
                                            })
                                            .addOnFailureListener(new OnFailureListener() {
                                                @Override
                                                public void onFailure(@NonNull Exception e) {

                                                    Toast.makeText(GameStartActivity.this, "Failed to create user document", Toast.LENGTH_SHORT).show();
                                                }
                                            });
                                }
                            }
                            else {

                                Toast.makeText(GameStartActivity.this, "Failed to get user document", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }
            });
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
}
