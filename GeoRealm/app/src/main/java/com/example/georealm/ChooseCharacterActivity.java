package com.example.georealm;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Resources;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentActivity;
import android.os.Bundle;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ProgressBar;
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
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import static com.example.georealm.helper.Constants.DEFAULT_ZOOM;
import static com.example.georealm.helper.Constants.ROGUE;
import static com.example.georealm.helper.Constants.SORCERER;
import static com.example.georealm.helper.Constants.SWORDSMAN;

public class ChooseCharacterActivity extends FragmentActivity implements OnMapReadyCallback {

    private static final int CREATE_CHARACTER_REQUEST = 1;

    // MAPS
    private GoogleMap map;
    private double latitude;
    private double longitude;

    // CHARACTER LIST
    private RecyclerView character_list;
    private RecyclerView.Adapter character_card_adapter;
    private RecyclerView.LayoutManager layout_manager;

    // FIRESTORE
    private FirebaseFirestore database;
    private String username;

    // UI
    private Button button_back;
    private ImageButton button_create;
    private TextView no_character_info;
    private Button button_play;
    private ProgressBar progress_bar;
    private ProgressBar progress_bar_create;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choose_character);

        username = getIntent().getStringExtra("username");


        // MAPS
        latitude = getIntent().getDoubleExtra("latitude", -33.8523341);
        longitude = getIntent().getDoubleExtra("longitude", 151.2106085);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);


        // CHARACTER LIST
        character_list = findViewById(R.id.character_list);

        character_list.setHasFixedSize(true);

        layout_manager = new LinearLayoutManager(ChooseCharacterActivity.this);
        character_list.setLayoutManager(layout_manager);

        button_play = findViewById(R.id.button_play);

        character_card_adapter = new CharacterCardDataAdapter(this, button_play);
        character_list.setAdapter(character_card_adapter);


        // UI
        progress_bar = findViewById(R.id.progress_bar);
        progress_bar_create = findViewById(R.id.progress_bar_create);

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
                intent.putExtra("latitude", latitude);
                intent.putExtra("longitude", longitude);
                startActivityForResult(intent, CREATE_CHARACTER_REQUEST);
            }
        });

        button_play.setEnabled(false);
        button_play.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(ChooseCharacterActivity.this, GameActivity.class);
                intent.putExtra("username", username);
                intent.putExtra("character_name", button_play.getTag(R.string.character_name).toString());
                intent.putExtra("character_class", Integer.parseInt(button_play.getTag(R.string.character_class).toString()));
                intent.putExtra("character_subclass", Integer.parseInt(button_play.getTag(R.string.character_subclass).toString()));
                intent.putExtra("character_level", Integer.parseInt(button_play.getTag(R.string.character_level).toString()));
                startActivity(intent);
            }
        });


        // FIRESTORE
        database = FirebaseFirestore.getInstance();

        progress_bar.setVisibility(View.VISIBLE);
        enableCommands(false);

        no_character_info = findViewById(R.id.no_characters_info);
        CollectionReference characters_ref = database.collection("users")
                .document(username).collection("characters");
        characters_ref.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {

                if (task.isSuccessful()) {

                    for (QueryDocumentSnapshot document : task.getResult()) {

                        CharacterCardData character_data = document.toObject(CharacterCardData.class);
                        ((CharacterCardDataAdapter)character_card_adapter).addCharacterToList(character_card_adapter.getItemCount(), character_data);
                    }

                    // CHARACTER LIST
                    character_card_adapter.notifyDataSetChanged();
                }
                else {

                    Toast.makeText(ChooseCharacterActivity.this,
                            "Failed to get characters", Toast.LENGTH_SHORT).show();
                }

                progress_bar.setVisibility(View.GONE);
                enableCommands(true);

                if (character_card_adapter.getItemCount() == 0) {

                    no_character_info.setVisibility(View.VISIBLE);
                }
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

        if (requestCode == CREATE_CHARACTER_REQUEST) {

            if (resultCode == Activity.RESULT_OK) {

                progress_bar_create.setVisibility(View.VISIBLE);
                enableCommands(false);

                final String character_name = data.getStringExtra("character_name");
                int character_subclass = data.getIntExtra("character_class", 1);
                int character_class = 1;

                switch (character_subclass) {

                    case Constants.BERSERKER:
                        character_class = SWORDSMAN;
                        break;
                    case Constants.PALADIN:
                        character_class = SWORDSMAN;
                        break;
                    case Constants.PYROMANCER:
                        character_class = SORCERER;
                        break;
                    case Constants.ICEBOUND:
                        character_class = SORCERER;
                        break;
                    case Constants.ASSASSIN:
                        character_class = ROGUE;
                        break;
                    case Constants.SHADOW:
                        character_class = ROGUE;
                        break;
                }

                final CharacterCardData character_data = new CharacterCardData(
                        character_name, character_class, character_subclass);

                database.collection("users").document(username)
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
                                no_character_info.setVisibility(View.GONE);

                                progress_bar_create.setVisibility(View.GONE);
                                enableCommands(true);
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {

                                Toast.makeText(ChooseCharacterActivity.this,
                                        "Failed to create character",
                                        Toast.LENGTH_SHORT).show();

                                progress_bar_create.setVisibility(View.GONE);
                                enableCommands(true);
                            }
                        });
            }
        }
    }

    private void enableCommands(boolean enable) {

        button_create.setEnabled(enable);
        button_play.setEnabled(enable);
        button_back.setEnabled(enable);
    }
}
