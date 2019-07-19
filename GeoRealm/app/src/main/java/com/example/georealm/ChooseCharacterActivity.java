package com.example.georealm;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Resources;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.georealm.adapters.CharacterCardDataAdapter;
import com.example.georealm.data.CharacterCardData;
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
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class ChooseCharacterActivity extends FragmentActivity implements OnMapReadyCallback {

    private static final int CREATE_CHARACTER_REQUEST = 1;

    // MAPS
    private GoogleMap mMap;

    // CHARACTER LIST
    private RecyclerView character_list;
    private RecyclerView.Adapter character_card_adapter;
    private RecyclerView.LayoutManager layout_manager;

    // FIRESTORE
    FirebaseFirestore database;

    private String user_id_token;

    // UI
    private ImageButton button_back;
    private ImageButton button_create;
    private TextView no_character_info;
    private ImageButton button_play;
    private RelativeLayout layout_play;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choose_character);

        user_id_token = getIntent().getStringExtra("user_id_token");

        // MAPS
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        // CHARACTER LIST
        character_list = findViewById(R.id.character_list);

        character_list.setHasFixedSize(true);

        layout_manager = new LinearLayoutManager(ChooseCharacterActivity.this);
        character_list.setLayoutManager(layout_manager);

        layout_play = findViewById(R.id.layout_play);
        character_card_adapter = new CharacterCardDataAdapter(this, layout_play);
        character_list.setAdapter(character_card_adapter);

        // FIRESTORE
        database = FirebaseFirestore.getInstance();

        no_character_info = findViewById(R.id.no_characters_info);
        CollectionReference characters_ref = database.collection("users")
                .document(user_id_token).collection("characters");
        characters_ref.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {

                if (task.isSuccessful()) {

                    for (QueryDocumentSnapshot document : task.getResult()) {

                        CharacterCardData character_data = document.toObject(CharacterCardData.class);
                        ((CharacterCardDataAdapter)character_card_adapter).addCharacterToList(character_card_adapter.getItemCount(), character_data);
                    }

                    if (character_card_adapter.getItemCount() == 0) {

                        no_character_info.setVisibility(View.VISIBLE);
                    }

                    // CHARACTER LIST
                    character_card_adapter.notifyDataSetChanged();
                }
                else {

                    Toast.makeText(ChooseCharacterActivity.this,
                            "Failed to get characters", Toast.LENGTH_SHORT).show();
                }
            }
        });

        // UI
        button_back = findViewById(R.id.button_back);
        button_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                finish();
            }
        });

        button_create = findViewById(R.id.button_create);
        button_create.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(ChooseCharacterActivity.this, CreateCharacterActivity.class);
                startActivityForResult(intent, CREATE_CHARACTER_REQUEST);
            }
        });

        button_play = findViewById(R.id.button_play);
        button_play.setEnabled(false);
        button_play.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(ChooseCharacterActivity.this, GameActivity.class);
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

        LatLng sydney = new LatLng(-34, 151);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(sydney, 15));
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {

        if (requestCode == CREATE_CHARACTER_REQUEST) {

            if (resultCode == Activity.RESULT_OK) {

                final String character_name = data.getStringExtra("character_name");
                int subclass = data.getIntExtra("character_class", -1);
                String character_class = "";
                String character_subclass = "";
                int character_level = 1;

                switch (subclass) {

                    case Constants.BERSERKER:
                        character_class = "swordsman";
                        character_subclass = "berserker";
                        break;
                    case Constants.PALADIN:
                        character_class = "swordsman";
                        character_subclass = "paladin";
                        break;
                    case Constants.PYROMANCER:
                        character_class = "sorcerer";
                        character_subclass = "pyromancer";
                        break;
                    case Constants.ICEBOUND:
                        character_class = "sorcerer";
                        character_subclass = "icebound";
                        break;
                    case Constants.ASSASSIN:
                        character_class = "rogue";
                        character_subclass = "assassin";
                        break;
                    case Constants.SHADOW:
                        character_class = "rogue";
                        character_subclass = "shadow";
                        break;
                }

                final CharacterCardData character_data = new CharacterCardData(
                        character_name, character_class, character_subclass, character_level);

                database.collection("users").document(user_id_token)
                        .collection("characters")
                        .document(character_name).set(character_data)
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {

                                Toast.makeText(ChooseCharacterActivity.this,
                                        "Character " + character_name + " created successfully",
                                        Toast.LENGTH_SHORT).show();

                                ((CharacterCardDataAdapter)character_card_adapter).addCharacterToList(0, character_data);
                                character_card_adapter.notifyDataSetChanged();
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {

                                Toast.makeText(ChooseCharacterActivity.this,
                                        "Failed to create character",
                                        Toast.LENGTH_SHORT).show();
                            }
                        });
            }
        }
    }
}
