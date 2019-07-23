package com.example.georealm;

import android.content.Intent;
import android.content.res.Resources;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import static com.example.georealm.helper.Constants.DEFAULT_ZOOM;

public class RegisterActivity extends FragmentActivity implements OnMapReadyCallback {

    // MAPS
    private GoogleMap map;

    // UI
    private Button button_register;
    private Button button_back;
    private EditText edit_email;
    private EditText edit_username;
    private EditText edit_password;
    private ProgressBar progress_bar;

    // FIREBASE AUTH
    private FirebaseAuth authentication;
    private FirebaseUser user;

    // FIRESTORE
    private FirebaseFirestore database;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);


        // FIREBASE AUTH
        authentication = FirebaseAuth.getInstance();


        // FIRESTORE
        database = FirebaseFirestore.getInstance();


        // MAPS
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);


        // UI
        edit_email = findViewById(R.id.edit_email);
        edit_username = findViewById(R.id.edit_username);
        edit_password = findViewById(R.id.edit_password);
        progress_bar = findViewById(R.id.progress_bar);

        button_register = findViewById(R.id.button_register);
        button_register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (edit_email.getText().toString().equals("")) {

                    Toast.makeText(RegisterActivity.this,
                            "Invalid email",
                            Toast.LENGTH_SHORT).show();
                }
                else if (edit_username.getText().toString().length() < 4) {

                    Toast.makeText(RegisterActivity.this,
                            "Username must be at least 4 characters long",
                            Toast.LENGTH_SHORT).show();
                }
                else if (edit_password.getText().toString().length() < 6) {

                    Toast.makeText(RegisterActivity.this,
                            "Password must be at least 6 characters long",
                            Toast.LENGTH_SHORT).show();
                }
                else {

                    progress_bar.setVisibility(View.VISIBLE);
                    enableCommands(false);

                    final String email = edit_email.getText().toString();
                    final String username = edit_username.getText().toString();
                    final String password = edit_password.getText().toString();

                    DocumentReference user_ref = database.collection("users")
                            .document(username);
                    user_ref.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {

                            if (task.isSuccessful()) {

                                DocumentSnapshot document = task.getResult();
                                if (document.exists()) {

                                    Toast.makeText(RegisterActivity.this,
                                            "User " + username + " already exists",
                                            Toast.LENGTH_SHORT).show();

                                    progress_bar.setVisibility(View.INVISIBLE);
                                    enableCommands(true);
                                }
                                else {

                                    authentication.createUserWithEmailAndPassword(email, password)
                                            .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                                                @Override
                                                public void onComplete(@NonNull Task<AuthResult> task) {

                                                    if (task.isSuccessful()) {

                                                        user = authentication.getCurrentUser();

                                                        UserProfileChangeRequest profile_update = new UserProfileChangeRequest.Builder()
                                                                .setDisplayName(username).build();

                                                        user.updateProfile(profile_update)
                                                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                    @Override
                                                                    public void onComplete(@NonNull Task<Void> task) {

                                                                        /*if (task.isSuccessful()) {

                                                                            Toast.makeText(RegisterActivity.this,
                                                                                    "Account " + username + " created successfully",
                                                                                    Toast.LENGTH_SHORT).show();
                                                                        }
                                                                        else {

                                                                            Toast.makeText(RegisterActivity.this,
                                                                                    "Account created successfully",
                                                                                    Toast.LENGTH_SHORT).show();
                                                                        }*/

                                                                        Intent return_intent = new Intent();
                                                                        setResult(RESULT_OK, return_intent);
                                                                        finish();
                                                                    }
                                                                });
                                                    }
                                                    else {

                                                        Toast.makeText(RegisterActivity.this,
                                                                "Failed to create an account",
                                                                Toast.LENGTH_SHORT).show();

                                                        progress_bar.setVisibility(View.GONE);
                                                        enableCommands(true);
                                                    }
                                                }
                                            });
                                }
                            }
                            else {

                                Toast.makeText(RegisterActivity.this,
                                        "Failed to get user document. Account creation failed",
                                        Toast.LENGTH_SHORT).show();

                                progress_bar.setVisibility(View.INVISIBLE);
                                enableCommands(true);
                            }
                        }
                    });
                }
            }
        });

        button_back = findViewById(R.id.button_back);
        button_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent return_intent = new Intent();
                setResult(RESULT_CANCELED, return_intent);
                finish();
            }
        });
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {

        map = googleMap;
        map.getUiSettings().setAllGesturesEnabled(false);

        try {

            map.setMapStyle(MapStyleOptions
                    .loadRawResourceStyle(this, R.raw.google_maps_style));
        }
        catch (Resources.NotFoundException e) {

            // exception
        }

        double latitude = getIntent().getDoubleExtra("latitude", -33.8523341);
        double longitude = getIntent().getDoubleExtra("longitude", 151.2106085);
        LatLng location = new LatLng(latitude, longitude);

        map.moveCamera(CameraUpdateFactory
                .newLatLngZoom(location, DEFAULT_ZOOM));
        map.getUiSettings().setMyLocationButtonEnabled(false);
    }

    private void enableCommands(boolean enable) {

        button_register.setEnabled(enable);
        // button_back.setEnabled(enable);
        edit_email.setEnabled(enable);
        edit_username.setEnabled(enable);
        edit_password.setEnabled(enable);
    }
}
