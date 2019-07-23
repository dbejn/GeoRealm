package com.example.georealm;

import android.content.Intent;
import android.content.res.Resources;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.example.georealm.helper.Constants;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.api.Distribution;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

import static com.example.georealm.helper.Constants.DEFAULT_ZOOM;

public class CreateCharacterActivity extends FragmentActivity implements OnMapReadyCallback {

    // MAPS
    private GoogleMap map;

    // UI
    private Button button_back;
    private Button button_create;
    private ImageButton button_more_swordsman;
    private ImageButton button_more_sorcerer;
    private ImageButton button_more_rogue;

    private EditText edit_character_name;

    private LinearLayout swordsman_subclass_layout;
    private RelativeLayout select_berserker_layout;
    private RelativeLayout select_paladin_layout;

    private LinearLayout sorcerer_subclass_layout;
    private RelativeLayout select_pyromancer_layout;
    private RelativeLayout select_icebound_layout;

    private LinearLayout rogue_subclass_layout;
    private RelativeLayout select_assassin_layout;
    private RelativeLayout select_shadow_layout;

    private ProgressBar progress_bar;

    private int selected_class_num = -1;

    // FIRESTORE
    private FirebaseFirestore database;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_character);


        // MAPS
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);


        // FIRESTORE
        database = FirebaseFirestore.getInstance();


        // UI
        progress_bar = findViewById(R.id.progress_bar);

        swordsman_subclass_layout = findViewById(R.id.swordsman_subclass_layout);
        sorcerer_subclass_layout = findViewById(R.id.sorcerer_subclass_layout);
        rogue_subclass_layout = findViewById(R.id.rogue_subclass_layout);

        edit_character_name = findViewById(R.id.edit_character_name);

        button_back = findViewById(R.id.button_back);
        button_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent return_intent = new Intent();
                setResult(RESULT_CANCELED, return_intent);
                finish();
            }
        });

        button_create = findViewById(R.id.button_create);
        button_create.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (edit_character_name.getText().toString().length() < 3) {

                    Toast.makeText(CreateCharacterActivity.this,
                            "Name must be at least 3 characters long",
                            Toast.LENGTH_SHORT).show();
                }
                else {

                    progress_bar.setVisibility(View.VISIBLE);
                    enableCommands(false);

                    DocumentReference character_ref = database.collection("characters")
                            .document(edit_character_name.getText().toString());
                    character_ref.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {

                            if (task.isSuccessful()) {

                                DocumentSnapshot document = task.getResult();
                                if (document.exists()) {

                                    Toast.makeText(CreateCharacterActivity.this,
                                            "Character " + edit_character_name.getText().toString() + " already exists",
                                            Toast.LENGTH_SHORT).show();
                                }
                                else {

                                    Intent return_intent = new Intent();
                                    return_intent.putExtra("character_name", edit_character_name.getText().toString());
                                    return_intent.putExtra("character_class", selected_class_num);
                                    setResult(RESULT_OK, return_intent);
                                    finish();
                                }
                            }
                            else {

                                Toast.makeText(CreateCharacterActivity.this,
                                        "Failed to get character document. Character creation failed",
                                        Toast.LENGTH_SHORT).show();
                            }

                            progress_bar.setVisibility(View.INVISIBLE);
                            enableCommands(true);
                        }
                    });
                }
            }
        });


        // SWORDSMAN
        button_more_swordsman = findViewById(R.id.button_more_swordsman_class);
        button_more_swordsman.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (swordsman_subclass_layout.getVisibility() == View.VISIBLE) {

                    swordsman_subclass_layout.setVisibility(View.GONE);
                    if (selected_class_num == Constants.BERSERKER || selected_class_num == Constants.PALADIN) {

                        deselectCurrent();
                    }
                    button_more_swordsman.setImageResource(R.drawable.ic_arrow_down);
                }
                else {

                    swordsman_subclass_layout.setVisibility(View.VISIBLE);
                    button_more_swordsman.setImageResource(R.drawable.ic_arrow_up);
                }
            }
        });

        select_berserker_layout = findViewById(R.id.select_berserker);
        select_berserker_layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                selectSubclass(Constants.BERSERKER, select_berserker_layout);
            }
        });

        select_paladin_layout = findViewById(R.id.select_paladin);
        select_paladin_layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                selectSubclass(Constants.PALADIN, select_paladin_layout);
            }
        });


        // SORCERER
        button_more_sorcerer = findViewById(R.id.button_more_sorcerer_class);
        button_more_sorcerer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (sorcerer_subclass_layout.getVisibility() == View.VISIBLE) {

                    sorcerer_subclass_layout.setVisibility(View.GONE);
                    if (selected_class_num == Constants.PYROMANCER || selected_class_num == Constants.ICEBOUND) {

                        deselectCurrent();
                    }
                    button_more_sorcerer.setImageResource(R.drawable.ic_arrow_down);
                }
                else {

                    sorcerer_subclass_layout.setVisibility(View.VISIBLE);
                    button_more_sorcerer.setImageResource(R.drawable.ic_arrow_up);
                }
            }
        });

        select_pyromancer_layout = findViewById(R.id.select_pyromancer);
        select_pyromancer_layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                selectSubclass(Constants.PYROMANCER, select_pyromancer_layout);
            }
        });

        select_icebound_layout = findViewById(R.id.select_icebound);
        select_icebound_layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                selectSubclass(Constants.ICEBOUND, select_icebound_layout);
            }
        });


        // ROGUE
        button_more_rogue = findViewById(R.id.button_more_rogue_class);
        button_more_rogue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (rogue_subclass_layout.getVisibility() == View.VISIBLE) {

                    rogue_subclass_layout.setVisibility(View.GONE);
                    if (selected_class_num == Constants.ASSASSIN || selected_class_num == Constants.SHADOW) {

                        deselectCurrent();
                    }
                    button_more_rogue.setImageResource(R.drawable.ic_arrow_down);
                }
                else {

                    rogue_subclass_layout.setVisibility(View.VISIBLE);
                    button_more_rogue.setImageResource(R.drawable.ic_arrow_up);
                }
            }
        });

        select_assassin_layout = findViewById(R.id.select_assassin);
        select_assassin_layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                selectSubclass(Constants.ASSASSIN, select_assassin_layout);
            }
        });

        select_shadow_layout = findViewById(R.id.select_shadow);
        select_shadow_layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                selectSubclass(Constants.SHADOW, select_shadow_layout);
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

    private void deselectCurrentLayout() {

        switch (selected_class_num) {

            case Constants.BERSERKER:
                select_berserker_layout.setForeground(null);
                break;
            case Constants.PALADIN:
                select_paladin_layout.setForeground(null);
                break;
            case Constants.PYROMANCER:
                select_pyromancer_layout.setForeground(null);
                break;
            case Constants.ICEBOUND:
                select_icebound_layout.setForeground(null);
                break;
            case Constants.ASSASSIN:
                select_assassin_layout.setForeground(null);
                break;
            case Constants.SHADOW:
                select_shadow_layout.setForeground(null);
                break;
        }
    }

    private void deselectCurrent() {

        button_create.setBackgroundResource(R.drawable.left_rounded_rec_disabled);
        button_create.setEnabled(false);
        deselectCurrentLayout();
        selected_class_num = -1;
    }

    private void selectSubclass(int subclass, RelativeLayout select_layout) {

        if (selected_class_num != subclass) {

            deselectCurrentLayout();
            select_layout.setForeground(getDrawable(R.drawable.select));
            selected_class_num = subclass;
            button_create.setBackgroundResource(R.drawable.left_rounded_rec_base);
            button_create.setEnabled(true);
        }
    }

    private void enableCommands(boolean enable) {

        button_create.setEnabled(enable);
        edit_character_name.setEnabled(enable);
        swordsman_subclass_layout.setEnabled(enable);
        sorcerer_subclass_layout.setEnabled(enable);
        rogue_subclass_layout.setEnabled(enable);
        button_back.setEnabled(enable);
    }
}
