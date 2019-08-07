package com.example.georealm;

import android.content.res.Resources;
import android.graphics.Typeface;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.georealm.adapters.SkillCardDataAdapter;
import com.example.georealm.data.CharacterData;
import com.example.georealm.data.Icebound;
import com.example.georealm.helper.Constants;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import static com.example.georealm.helper.Constants.ASSASSIN;
import static com.example.georealm.helper.Constants.BERSERKER;
import static com.example.georealm.helper.Constants.DEFAULT_ZOOM;
import static com.example.georealm.helper.Constants.ICEBOUND;
import static com.example.georealm.helper.Constants.ICEBOUND_SKILL_DESCRIPTIONS;
import static com.example.georealm.helper.Constants.ICEBOUND_SKILL_NAMES;
import static com.example.georealm.helper.Constants.ICEBOUND_SKILL_STATS;
import static com.example.georealm.helper.Constants.PALADIN;
import static com.example.georealm.helper.Constants.PYROMANCER;
import static com.example.georealm.helper.Constants.ROGUE;
import static com.example.georealm.helper.Constants.SHADOW;
import static com.example.georealm.helper.Constants.SORCERER;
import static com.example.georealm.helper.Constants.SWORDSMAN;

public class CharacterActivity extends FragmentActivity implements OnMapReadyCallback {

    // MAPS
    private GoogleMap map;

    //TABS
    private Button button_info;
    private Button button_skills;
    private Button button_inventory;
    private Button current_button;

    private View marker_info;
    private View marker_skills;
    private View marker_inventory;
    private View current_marker;

    private ScrollView layout_character_info;
    private ConstraintLayout layout_character_skills;
    private ConstraintLayout layout_character_inventory;
    private View current_view;

    // UI
    private CharacterData character;
    private ProgressBar progress_bar;
    private Button button_back;
    private int work_finished = 0;

    // INFO
    private ImageView info_icon;
    private TextView info_subclass;
    private TextView info_level;
    private TextView info_experience;
    private TextView info_health;
    private TextView info_resource;
    private TextView info_attack;
    private TextView info_defence;
    private TextView info_magic;
    private TextView info_magic_res;
    private TextView info_gems_collected;
    private TextView info_gems_spent;
    private TextView info_monsters_slain;
    private TextView info_duels_won;
    private TextView info_score;
    private TextView info_resource_type;

    // SKILLS
    private Button button_learn_skill;
    private RecyclerView skill_list;
    private RecyclerView.Adapter skill_card_adapter;
    private RecyclerView.LayoutManager layout_manager;

    // INVENTORY
    private TextView health_pots;
    private TextView mana_pots;
    private TextView stamina_pots;

    // FIRESTORE
    private FirebaseFirestore database;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_character);


        // MAPS
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);


        // FIRESTORE
        database = FirebaseFirestore.getInstance();


        // TABS
        layout_character_info = findViewById(R.id.layout_character_info);
        layout_character_skills = findViewById(R.id.layout_character_skills);
        layout_character_inventory = findViewById(R.id.layout_character_inventory);

        current_view = layout_character_info;

        button_info = findViewById(R.id.tab_button_info);
        button_skills = findViewById(R.id.tab_button_skills);
        button_inventory = findViewById(R.id.tab_button_inventory);

        marker_info = findViewById(R.id.tab_marker_info);
        marker_skills = findViewById(R.id.tab_marker_skills);
        marker_inventory = findViewById(R.id.tab_marker_inventory);

        current_button = button_info;
        current_marker = marker_info;

        button_info.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                changeTab(layout_character_info, marker_info, button_info);
            }
        });

        button_skills.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                changeTab(layout_character_skills, marker_skills, button_skills);
            }
        });

        button_inventory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                changeTab(layout_character_inventory, marker_inventory, button_inventory);
            }
        });


        // UI
        progress_bar = findViewById(R.id.progress_bar);
        progress_bar.setVisibility(View.VISIBLE);

        button_back = findViewById(R.id.button_back);
        button_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                finish();
            }
        });

        int character_subclass = getIntent().getIntExtra("subclass", 1);
        switch (character_subclass) {

            case PYROMANCER:
                character = (Icebound) getIntent().getParcelableExtra("character");
                break;
            case ICEBOUND:
                character = (Icebound) getIntent().getParcelableExtra("character");
                break;
        }

        setCharacterInfoTab();
        setCharacterSkillsTab();
        setCharacterInventoryTab();
        progress_bar.setVisibility(View.GONE);
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

    private void setCharacterInfoTab() {

        info_icon = findViewById(R.id.character_info_icon);
        info_subclass = findViewById(R.id.character_info_subclass);
        info_level = findViewById(R.id.character_info_level);
        info_experience = findViewById(R.id.character_info_experience);
        info_health = findViewById(R.id.character_info_health);
        info_resource = findViewById(R.id.character_info_resource);
        info_attack = findViewById(R.id.character_info_attack);
        info_defence = findViewById(R.id.character_info_defence);
        info_magic = findViewById(R.id.character_info_magic);
        info_magic_res = findViewById(R.id.character_info_resistance);
        info_gems_collected = findViewById(R.id.character_info_gems);
        info_gems_spent = findViewById(R.id.character_info_gems_spent);
        info_monsters_slain = findViewById(R.id.character_info_monsters_slain);
        info_duels_won = findViewById(R.id.character_info_duels_won);
        info_score = findViewById(R.id.character_info_score);
        info_resource_type = findViewById(R.id.text_resource);

        int level = character.getCharacter_level();
        info_level.setText(level);
        info_experience.setText(getString(R.string.info_experience,
                character.getExperience(), (int)(1000 * Math.pow(2.5, level - 1))));
        info_health.setText(character.getHealth());
        info_resource.setText(character.getResource());
        info_attack.setText(character.getAttack_damage());
        info_defence.setText(character.getDefence());
        info_magic.setText(character.getMagic_damage());
        info_magic_res.setText(character.getMagic_resistance());
        info_gems_collected.setText(character.getGems_collected());
        info_gems_spent.setText(character.getGems_spent());
        info_monsters_slain.setText(character.getMonsters_slain());
        info_duels_won.setText(getString(R.string.info_duels,
                character.getDuels_won(), character.getDuels_fought()));
        info_score.setText(character.getScore());

        switch (character.getCharacter_subclass()) {

            case BERSERKER:
                info_icon.setImageResource(R.drawable.ic_sword);
                info_resource_type.setText(getString(R.string.stamina));
                info_subclass.setText(getString(R.string.berserker));
                break;
            case PALADIN:
                info_icon.setImageResource(R.drawable.ic_sword);
                info_resource_type.setText(getString(R.string.stamina));
                info_subclass.setText(getString(R.string.paladin));
                break;
            case PYROMANCER:
                info_icon.setImageResource(R.drawable.ic_hat);
                info_resource_type.setText(getString(R.string.mana));
                info_subclass.setText(getString(R.string.pyromancer));
                break;
            case ICEBOUND:
                info_icon.setImageResource(R.drawable.ic_hat);
                info_resource_type.setText(getString(R.string.mana));
                info_subclass.setText(getString(R.string.icebound));
                break;
            case ASSASSIN:
                info_icon.setImageResource(R.drawable.ic_dagger);
                info_resource_type.setText(getString(R.string.stamina));
                info_subclass.setText(getString(R.string.assassin));
                break;
            case SHADOW:
                info_icon.setImageResource(R.drawable.ic_dagger);
                info_resource_type.setText(getString(R.string.mana));
                info_subclass.setText(getString(R.string.shadow));
                break;
        }
    }

    private void setCharacterSkillsTab() {

        layout_manager = new LinearLayoutManager(CharacterActivity.this);
        skill_list.setLayoutManager(layout_manager);
        String[] skill_names = {};
        String[] skill_descriptions = {};
        String[] skill_stats = {};
        button_learn_skill = findViewById(R.id.button_learn_skill);
        button_learn_skill.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                progress_bar.setVisibility(View.VISIBLE);

                DocumentReference character_doc = database.collection("characters").document(character.getCharacter_name());
                character_doc.update("skills", FieldValue.arrayUnion((int)button_learn_skill.getTag()))
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {

                                if (task.isSuccessful()) {

                                    Toast.makeText(CharacterActivity.this, "Skill learned", Toast.LENGTH_SHORT).show();
                                    character.getSkills().add((int)button_learn_skill.getTag());
                                }
                                else {

                                    Toast.makeText(CharacterActivity.this, "Failed to learn skill, please try again", Toast.LENGTH_SHORT).show();
                                }

                                progress_bar.setVisibility(View.GONE);
                            }
                        });
            }
        });

        switch (character.getCharacter_subclass()) {

            case Constants.BERSERKER:

                break;
            case Constants.PALADIN:

                break;
            case Constants.PYROMANCER:

                break;
            case Constants.ICEBOUND:
                skill_names = ICEBOUND_SKILL_NAMES;
                skill_descriptions = ICEBOUND_SKILL_DESCRIPTIONS;
                skill_stats = ICEBOUND_SKILL_STATS;
                break;
            case Constants.ASSASSIN:

                break;
            case Constants.SHADOW:

                break;
        }

        skill_card_adapter = new SkillCardDataAdapter(this, button_learn_skill,
                skill_names, skill_descriptions, skill_stats, character);
        skill_list.setAdapter(skill_card_adapter);
    }

    private void setCharacterInventoryTab() {

        health_pots = findViewById(R.id.health_pots_num);
        mana_pots = findViewById(R.id.mana_pots_num);
        stamina_pots = findViewById(R.id.stamina_pots_num);

        health_pots.setText(getString(R.string.info_duels, character.getItems().get(0), 3));
        mana_pots.setText(getString(R.string.info_duels, character.getItems().get(1), 3));
        stamina_pots.setText(getString(R.string.info_duels, character.getItems().get(2), 3));
    }
}
