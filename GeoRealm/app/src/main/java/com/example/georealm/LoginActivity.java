package com.example.georealm;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Resources;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
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
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

import static com.example.georealm.helper.Constants.DEFAULT_ZOOM;

public class LoginActivity extends FragmentActivity implements OnMapReadyCallback {

    // MAPS
    private GoogleMap map;
    private double latitude;
    private double longitude;

    // UI
    private Button button_login;
    private Button button_register;
    private Button button_back;
    private EditText edit_email;
    private EditText edit_password;
    private ProgressBar progress_bar;

    // LOGIN
    private static final int REGISTER_REQUEST = 2;

    // FIREBASE AUTH
    private FirebaseAuth authentication;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);


        // FIREBASE AUTH
        authentication = FirebaseAuth.getInstance();


        // MAPS
        latitude = getIntent().getDoubleExtra("latitude", -33.8523341);
        longitude = getIntent().getDoubleExtra("longitude", 151.2106085);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);


        // UI
        edit_email = findViewById(R.id.edit_email);
        edit_password = findViewById(R.id.edit_password);
        progress_bar = findViewById(R.id.progress_bar);

        button_login = findViewById(R.id.button_login);
        button_login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String email = edit_email.getText().toString();
                String password = edit_password.getText().toString();

                if (!email.equals("") && !password.equals("")) {

                    progress_bar.setVisibility(View.VISIBLE);
                    enableCommands(false);

                    authentication.signInWithEmailAndPassword(email, password)
                            .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {

                                    if (task.isSuccessful()) {

                                        Intent return_intent = new Intent();
                                        setResult(RESULT_OK, return_intent);
                                        finish();
                                    }
                                    else {

                                        Toast.makeText(LoginActivity.this,
                                                "Wrong credentials", Toast.LENGTH_SHORT).show();
                                    }

                                    progress_bar.setVisibility(View.GONE);
                                    enableCommands(true);
                                }
                            });
                }
                else {

                    Toast.makeText(LoginActivity.this,
                            "Wrong credentials", Toast.LENGTH_SHORT).show();
                }
            }
        });

        button_register = findViewById(R.id.button_click_here);
        button_register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
                intent.putExtra("latitude", latitude);
                intent.putExtra("longitude", longitude);
                startActivityForResult(intent, REGISTER_REQUEST);
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {

        if (requestCode == REGISTER_REQUEST) {

            if (resultCode == Activity.RESULT_OK) {

                Intent return_intent = new Intent();
                setResult(RESULT_OK, return_intent);
                finish();
            }
        }
    }

    private void enableCommands(boolean enable) {

        button_register.setEnabled(enable);
        button_login.setEnabled(enable);
        // button_back.setEnabled(enable);
        edit_email.setEnabled(enable);
        edit_password.setEnabled(enable);
    }
}
