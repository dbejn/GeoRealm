package com.example.georealm;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.graphics.Typeface;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.georealm.adapters.AccountCharacterCardDataAdapter;
import com.example.georealm.adapters.CharacterCardDataAdapter;
import com.example.georealm.adapters.FriendCardDataAdapter;
import com.example.georealm.data.CharacterCardData;
import com.example.georealm.data.CharacterData;
import com.example.georealm.data.Icebound;
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
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.example.georealm.helper.Constants.DEFAULT_ZOOM;
import static com.example.georealm.helper.Constants.ICEBOUND;
import static com.example.georealm.helper.Constants.PYROMANCER;

public class AccountActivity extends FragmentActivity implements OnMapReadyCallback {

    // MAPS
    private GoogleMap map;

    //TABS
    private Button button_info;
    private Button button_characters;
    private Button button_friends;
    private Button current_button;

    private View marker_info;
    private View marker_characters;
    private View marker_friends;
    private View current_marker;

    private ScrollView layout_info;
    private ConstraintLayout layout_characters;
    private ConstraintLayout layout_friends;
    private View current_view;

    // UI
    private CharacterData character;
    private ProgressBar progress_bar;
    private Button button_back;
    private RelativeLayout friends_tab;
    private View friends_tab_separator;

    // FRIENDS
    private RecyclerView friends_list;
    private RecyclerView.Adapter friend_card_adapter;
    private RecyclerView.LayoutManager layout_manager;
    private Button button_view;
    private Button button_remove;
    private LinearLayout layout_friend_options;

    // INFO
    private TextView account_info_username;
    private TextView account_info_score;

    // CHARACTERS
    private RecyclerView characters_list;
    private RecyclerView.Adapter character_card_adapter;
    private RecyclerView.LayoutManager character_layout_manager;
    private LinearLayout character_options;
    private Button button_view_character;
    private Button button_delete_character;

    // FIRESTORE
    private FirebaseFirestore database;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account);


        // MAPS
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);


        // FIRESTORE
        database = FirebaseFirestore.getInstance();


        // UI
        button_back = findViewById(R.id.button_back);
        button_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                finish();
            }
        });


        // TABS
        layout_info = findViewById(R.id.layout_account_info);
        layout_characters = findViewById(R.id.layout_account_characters);
        layout_friends = findViewById(R.id.layout_account_friends);

        button_info = findViewById(R.id.tab_button_info);
        button_characters = findViewById(R.id.tab_button_characters);
        button_friends = findViewById(R.id.tab_button_friends);

        marker_info = findViewById(R.id.tab_marker_info);
        marker_characters = findViewById(R.id.tab_marker_characters);
        marker_friends = findViewById(R.id.tab_marker_friends);

        current_view = layout_info;
        current_button = button_info;
        current_marker = marker_info;

        button_info.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                changeTab(layout_info, marker_info, button_info);
            }
        });

        button_characters.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                changeTab(layout_characters, marker_characters, button_characters);
            }
        });

        button_friends.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                changeTab(layout_friends, marker_friends, button_friends);
            }
        });

        character_options = findViewById(R.id.layout_character_options);
        if (getIntent().getStringExtra("type").equals("friend")) {

            friends_tab = findViewById(R.id.friends_tab);
            friends_tab.setVisibility(View.GONE);

            character_options.setVisibility(View.GONE);

            friends_tab_separator = findViewById(R.id.friends_tab_separator);
            friends_tab_separator.setVisibility(View.GONE);
        }
        else {

            setFriendsTab();
        }

        setInfoTab();
        setCharactersTab();
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

    private void changeTab(View tab, View marker, Button button) {

        current_button.setTypeface(Typeface.DEFAULT, Typeface.ITALIC);
        current_marker.setVisibility(View.INVISIBLE);
        current_view.setVisibility(View.GONE);

        current_button = button;
        current_marker = marker;
        current_view = tab;

        current_button.setTypeface(Typeface.DEFAULT, Typeface.BOLD_ITALIC);
        current_marker.setVisibility(View.VISIBLE);
        current_view.setVisibility(View.VISIBLE);
    }

    private void setFriendsTab() {

        layout_manager = new LinearLayoutManager(AccountActivity.this);
        friends_list = findViewById(R.id.friends_list);
        friends_list.setHasFixedSize(true);
        friends_list.setLayoutManager(layout_manager);

        button_view = findViewById(R.id.button_view_friend);
        button_view.setEnabled(false);
        button_remove = findViewById(R.id.button_remove_friend);
        button_remove.setEnabled(false);
        layout_friend_options = findViewById(R.id.layout_friend_options);

        DocumentReference user_ref = database.collection("users")
                .document(getIntent().getStringExtra("username"));
        user_ref.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {

                if (task.isSuccessful()) {

                    DocumentSnapshot document = task.getResult();
                    Map<String, Object> document_map = document.getData();
                    ArrayList<String> friends = (ArrayList<String>) document_map.get("friends");

                    List<String> friend_names = new ArrayList<>();
                    // List<String> friend_statuses = new ArrayList<>();

                    if (friends == null)
                        friends = new ArrayList<>();

                    for (String friend_name : friends) {

                        friend_names.add(friend_name);
                    }

                    friend_card_adapter = new FriendCardDataAdapter(AccountActivity.this,
                            button_view, button_remove, friend_names, layout_friend_options);
                    friends_list.setAdapter(friend_card_adapter);

                    for (String friend_name : friends) {

                        getFriendStatus(friend_name);
                    }
                }
                else {

                    Toast.makeText(AccountActivity.this,
                            "Failed to acquire friends list. Please, try again.", Toast.LENGTH_SHORT).show();
                    finish();
                }
            }
        });

        button_view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String friend_name = button_view.getTag().toString();

                Intent intent = new Intent(AccountActivity.this, AccountActivity.class);
                intent.putExtra("latitude", getIntent().getDoubleExtra("latitude", -33.8523341));
                intent.putExtra("longitude", getIntent().getDoubleExtra("longitude", 151.2106085));
                intent.putExtra("username", friend_name);
                intent.putExtra("type", "friend");
                startActivity(intent);
            }
        });

        button_remove.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String friend_name = button_remove.getTag().toString();
                String user_name = getIntent().getStringExtra("username");

                DocumentReference user_ref = database.collection("users").document(user_name);
                user_ref.update("friends", FieldValue.arrayRemove(friend_name));

                DocumentReference friend_ref = database.collection("users").document(friend_name);
                friend_ref.update("friends", FieldValue.arrayRemove(user_name));

                layout_friend_options.setBackground(getDrawable(R.drawable.top_rounded_rec_disabled));
                button_view.setEnabled(false);
                button_remove.setEnabled(false);

                ((FriendCardDataAdapter)friend_card_adapter).removeFriend(friend_name);
                friend_card_adapter.notifyDataSetChanged();
            }
        });
    }

    private void getFriendStatus(final String friend_name) {

        DocumentReference friend_ref = database.collection("users").document(friend_name);
        friend_ref.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {

                if (task.isSuccessful()) {

                    DocumentSnapshot friend_document = task.getResult();
                    if (friend_document.exists()) {

                        Map<String, Object> document_map = friend_document.getData();
                        String status = document_map.get("status").toString();
                        if (status.equals("online")) {

                            ((FriendCardDataAdapter)friend_card_adapter).addStatus(friend_name, "online");
                        }
                        else {

                            String last_online = document_map.get("last_online").toString();
                            ((FriendCardDataAdapter)friend_card_adapter).addStatus(friend_name, last_online);
                        }
                    }

                    friend_card_adapter.notifyDataSetChanged();
                }
            }
        });
    }

    private void setInfoTab() {

        account_info_username = findViewById(R.id.account_info_username);
        account_info_score = findViewById(R.id.account_info_score);

        DocumentReference user_ref = database.collection("users")
                .document(getIntent().getStringExtra("username"));
        user_ref.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {

                if (task.isSuccessful()) {

                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {

                        account_info_username.setText(document.getId());
                        account_info_score.setText(getString(R.string.integer,
                                Integer.parseInt(document.get("score").toString())));
                    }
                }
                else {

                    Toast.makeText(AccountActivity.this,
                            "Failed to acquire info. Please, try again.", Toast.LENGTH_SHORT).show();
                    finish();
                }
            }
        });
    }

    private void setCharactersTab() {

        character_layout_manager = new LinearLayoutManager(AccountActivity.this);
        characters_list = findViewById(R.id.characters_list);
        characters_list.setHasFixedSize(true);
        characters_list.setLayoutManager(character_layout_manager);

        button_view_character = findViewById(R.id.button_view_character);
        button_delete_character = findViewById(R.id.button_delete_character);

        final CollectionReference characters_ref = database.collection("users")
                .document(getIntent().getStringExtra("username"))
                .collection("characters");
        characters_ref.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {

                if (task.isSuccessful()) {

                    List<CharacterCardData> chars = new ArrayList<>();

                    for (QueryDocumentSnapshot document : task.getResult()) {

                        CharacterCardData character_data = document.toObject(CharacterCardData.class);
                        chars.add(character_data);
                    }

                    // CHARACTER LIST
                    if (getIntent().getStringExtra("type").equals("friend")) {

                        character_card_adapter = new AccountCharacterCardDataAdapter(
                                AccountActivity.this, chars, null, null,
                                false, character_options);

                    }
                    else {

                        character_card_adapter = new AccountCharacterCardDataAdapter(
                                AccountActivity.this, chars, button_view_character,
                                button_delete_character, true, character_options);
                    }

                    characters_list.setAdapter(character_card_adapter);
                }
                else {

                    Toast.makeText(AccountActivity.this,
                            "Failed to get characters", Toast.LENGTH_SHORT).show();
                }
            }
        });

        button_view_character.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String character_name = button_view_character.getTag(R.string.character_name).toString();
                final int character_subclass = Integer.parseInt(button_view_character.getTag(R.string.character_subclass).toString());

                DocumentReference character_doc = database.collection("characters").document(character_name);
                character_doc.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {

                        if (task.isSuccessful()) {

                            DocumentSnapshot document = task.getResult();
                            if (document.exists()) {

                                CharacterData character = null;

                                switch (character_subclass) {

                                    case PYROMANCER:
                                        Toast.makeText(AccountActivity.this,
                                                "Welcome to the GeoRealm", Toast.LENGTH_SHORT).show();
                                        break;
                                    case ICEBOUND:
                                        character = document.toObject(Icebound.class);
                                        break;
                                }

                                Intent intent = new Intent(AccountActivity.this, CharacterActivity.class);
                                intent.putExtra("subclass", character_subclass);
                                intent.putExtra("character", character);
                                intent.putExtra("latitude", getIntent().getDoubleExtra("latitude", -33.8523341));
                                intent.putExtra("longitude", getIntent().getDoubleExtra("longitude", 151.2106085));
                                startActivity(intent);
                            }
                        }
                        else {

                            Toast.makeText(AccountActivity.this,
                                    "Could not get character. Please try again.", Toast.LENGTH_SHORT).show();

                            finish();
                        }
                    }
                });
            }
        });

        button_delete_character.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                final String character_name = button_delete_character.getTag(R.string.character_name).toString();
                final int position = Integer.parseInt(button_view_character.getTag(R.string.character_subclass).toString());

                AlertDialog.Builder dialog_builder = new AlertDialog.Builder(AccountActivity.this);
                dialog_builder.setTitle("Delete character " + character_name);
                dialog_builder.setMessage("Are you sure?");
                dialog_builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        database.collection("users")
                                .document(getIntent().getStringExtra("username"))
                                .collection("characters")
                                .document(character_name).delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {

                                Toast.makeText(AccountActivity.this,
                                        "Character " + character_name + " deleted successfully",
                                        Toast.LENGTH_SHORT).show();

                                ((AccountCharacterCardDataAdapter)character_card_adapter).removeCharacterFromList(position);
                                character_card_adapter.notifyDataSetChanged();

                                database.collection("characters")
                                        .document(character_name).delete();
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {

                                Toast.makeText(AccountActivity.this, "Failed to delete character", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                });
                dialog_builder.setNegativeButton("No", null);
                dialog_builder.show();
            }
        });
    }
}
