package com.example.georealm;

import android.content.res.Resources;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.view.TextureView;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.georealm.adapters.HighscoreCardDataAdapter;
import com.example.georealm.data.HighscoreStruct;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.example.georealm.helper.Constants.DEFAULT_ZOOM;

public class HighscoreActivity extends FragmentActivity implements OnMapReadyCallback {

    // MAPS
    private GoogleMap map;

    // UI
    private Button button_back;

    // MY SCORE
    private TextView text_position;
    private TextView text_character_name;
    private TextView text_account_name;
    private TextView text_score;

    // HIGHSCORE
    private RecyclerView highscore_list;
    private RecyclerView.Adapter highscore_card_adapter;
    private RecyclerView.LayoutManager layout_manager;

    // FIRESTORE
    private FirebaseFirestore database;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_highscore);


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


        // MY SCORE
        text_position = findViewById(R.id.position);
        text_character_name = findViewById(R.id.character_name);
        text_account_name = findViewById(R.id.account_name);
        text_score = findViewById(R.id.score);

        // HIGHSCORE
        layout_manager = new LinearLayoutManager(HighscoreActivity.this);
        highscore_list = findViewById(R.id.highscore_list);
        highscore_list.setHasFixedSize(true);
        highscore_list.setLayoutManager(layout_manager);

        final List<HighscoreStruct> highscores = new ArrayList<>();
        database.collection("users")
                .orderBy("score", Query.Direction.DESCENDING)
                .get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {

                if (task.isSuccessful()) {

                    int position = 1;

                    for (QueryDocumentSnapshot document : task.getResult()) {

                        Map<String, Object> document_map = document.getData();

                        if (position <= 100) {

                            HighscoreStruct highscore = new HighscoreStruct();
                            highscore.position = position;
                            highscore.character_name = document.getId();
                            highscore.score = Integer.parseInt(document_map.get("score").toString());
                            highscore.account_name = "";

                            highscores.add(highscore);
                        }

                        if (document.getId().equals(getIntent().getStringExtra("username"))) {

                            text_position.setText(getString(R.string.position, position));
                            text_character_name.setText(document.getId());
                            text_score.setText(document_map.get("score").toString());
                        }

                        position++;
                    }

                    if (getIntent().getStringExtra("username").equals("")) {

                        RelativeLayout my_score = findViewById(R.id.my_score);
                        my_score.setVisibility(View.GONE);
                    }

                    highscore_card_adapter = new HighscoreCardDataAdapter(HighscoreActivity.this, highscores);
                    highscore_list.setAdapter(highscore_card_adapter);
                }
                else {

                    Toast.makeText(HighscoreActivity.this,
                            "Unable to retrieve highscores. Please, try again.",
                            Toast.LENGTH_SHORT).show();
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
}
