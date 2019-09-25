package com.example.georealm;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.georealm.adapters.ItemCardDataAdapter;
import com.example.georealm.data.CharacterData;
import com.example.georealm.data.Icebound;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;

import static com.example.georealm.helper.Constants.DEFAULT_ZOOM;
import static com.example.georealm.helper.Constants.ICEBOUND;
import static com.example.georealm.helper.Constants.PYROMANCER;

public class ShopActivity extends FragmentActivity implements OnMapReadyCallback {

    // MAPS
    private GoogleMap map;

    // CHARACTER
    private CharacterData character;

    // TABS
    private Button button_tab_buy;
    private Button button_tab_sell;
    private Button current_button;

    private View marker_buy;
    private View marker_sell;
    private View current_marker;

    private ConstraintLayout layout_shop_buy;
    private ConstraintLayout layout_shop_sell;
    private ConstraintLayout current_view;

    // BUY
    private Button button_buy;
    private SeekBar buy_amount_slider;
    private TextView buy_amount_text;
    private RecyclerView buy_item_list;
    private RecyclerView.Adapter item_buy_card_adapter;
    private RecyclerView.LayoutManager buy_layout_manager;

    // SELL
    private Button button_sell;
    private SeekBar sell_amount_slider;
    private TextView sell_amount_text;
    private RecyclerView sell_item_list;
    private RecyclerView.Adapter item_sell_card_adapter;
    private RecyclerView.LayoutManager sell_layout_manager;

    // UI
    private ProgressBar progress_bar;
    private Button button_back;

    // FIRESTORE
    private FirebaseFirestore database;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shop);


        // MAPS
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);


        // CHARACTER
        int character_subclass = getIntent().getIntExtra("subclass", 1);
        switch (character_subclass) {

            case PYROMANCER:
                // character = (Icebound) getIntent().getParcelableExtra("character");
                break;
            case ICEBOUND:
                character = (Icebound) getIntent().getParcelableExtra("character");
                break;
        }


        // UI
        progress_bar = findViewById(R.id.progress_bar);
        button_back = findViewById(R.id.button_back);
        button_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent return_intent = new Intent();
                return_intent.putExtra("character", character);
                setResult(RESULT_OK, return_intent);
                finish();
            }
        });

        // FIRESTORE
        database = FirebaseFirestore.getInstance();


        // TABS
        layout_shop_buy = findViewById(R.id.layout_shop_buy);
        layout_shop_sell = findViewById(R.id.layout_shop_sell);

        button_tab_buy = findViewById(R.id.tab_button_buy);
        button_tab_sell = findViewById(R.id.tab_button_sell);

        marker_buy = findViewById(R.id.tab_marker_buy);
        marker_sell = findViewById(R.id.tab_marker_sell);

        current_view = layout_shop_buy;
        current_button = button_tab_buy;
        current_marker = marker_buy;

        button_tab_buy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                changeTab(layout_shop_buy, marker_buy, button_tab_buy);
            }
        });

        button_tab_sell.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                changeTab(layout_shop_sell, marker_sell, button_tab_sell);
            }
        });

        setBuyTab();
        setSellTab();
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

    private void changeTab(ConstraintLayout tab, View marker, Button button) {

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

    private void setBuyTab() {

        buy_amount_slider = findViewById(R.id.buy_amount_slider);
        buy_amount_slider.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

                buy_amount_text.setText(getString(R.string.integer, progress));
                button_buy.setText(getString(R.string.buy_cost, progress * 5));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        buy_amount_text = findViewById(R.id.num_to_buy);
        button_buy = findViewById(R.id.button_buy);
        button_buy.setText(getString(R.string.buy_cost, 0));
        button_buy.setEnabled(false);
        button_buy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                final int amount = buy_amount_slider.getProgress();
                if (amount != 0) {

                    progress_bar.setVisibility(View.VISIBLE);

                    final int item = Integer.parseInt(button_buy.getTag().toString());
                    makeSale(item, amount, 5, true);

                    DocumentReference character_ref = database.collection("characters").document(character.getCharacter_name());
                    character_ref.set(character).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {

                            if (task.isSuccessful()) {

                                Toast.makeText(ShopActivity.this,
                                        "Item bought successfully", Toast.LENGTH_SHORT).show();

                                updateAdapters();
                            }
                            else {

                                makeSale(item, -amount, 5, true);
                                Toast.makeText(ShopActivity.this,
                                        "Failed to buy item. Please, try again later", Toast.LENGTH_SHORT).show();
                            }

                            progress_bar.setVisibility(View.GONE);
                        }
                    });
                }
            }
        });

        buy_layout_manager = new LinearLayoutManager(ShopActivity.this);
        buy_item_list = findViewById(R.id.buy_item_list);
        buy_item_list.setHasFixedSize(true);
        buy_item_list.setLayoutManager(buy_layout_manager);

        item_buy_card_adapter = new ItemCardDataAdapter(character, buy_amount_slider, buy_amount_text, this, button_buy, true);
        buy_item_list.setAdapter(item_buy_card_adapter);
    }

    private void setSellTab() {

        sell_amount_slider = findViewById(R.id.sell_amount_slider);
        sell_amount_slider.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

                sell_amount_text.setText(getString(R.string.integer, progress));
                button_sell.setText(getString(R.string.sell_cost, progress * 5));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        sell_amount_text = findViewById(R.id.num_to_sell);
        button_sell = findViewById(R.id.button_sell);
        button_sell.setText(getString(R.string.sell_cost, 0));
        button_sell.setEnabled(false);
        button_sell.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                final int amount = sell_amount_slider.getProgress();
                if (amount != 0) {

                    progress_bar.setVisibility(View.VISIBLE);

                    final int item = Integer.parseInt(button_sell.getTag().toString());
                    makeSale(item, -amount, 2, false);

                    DocumentReference character_ref = database.collection("characters").document(character.getCharacter_name());
                    character_ref.set(character).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {

                            if (task.isSuccessful()) {

                                Toast.makeText(ShopActivity.this,
                                        "Item sold successfully", Toast.LENGTH_SHORT).show();

                                updateAdapters();
                            }
                            else {

                                makeSale(item, amount, 2, false);
                                Toast.makeText(ShopActivity.this,
                                        "Failed to sell item. Please, try again later", Toast.LENGTH_SHORT).show();
                            }

                            progress_bar.setVisibility(View.GONE);
                        }
                    });
                }
            }
        });

        sell_layout_manager = new LinearLayoutManager(ShopActivity.this);
        sell_item_list = findViewById(R.id.sell_item_list);
        sell_item_list.setHasFixedSize(true);
        sell_item_list.setLayoutManager(sell_layout_manager);

        item_sell_card_adapter = new ItemCardDataAdapter(character, sell_amount_slider, sell_amount_text, this, button_sell, false);
        sell_item_list.setAdapter(item_sell_card_adapter);
    }

    private void updateAdapters() {

        // BUY
        buy_amount_slider.setProgress(0);
        buy_amount_slider.setMax(0);
        buy_amount_text.setText(getString(R.string.integer, 0));
        button_buy.setBackgroundResource(R.drawable.top_rounded_rec_disabled);
        button_buy.setText(getString(R.string.buy_cost, 0));
        button_buy.setEnabled(false);

        ((ItemCardDataAdapter)item_buy_card_adapter).updateCharacter(character);
        ((ItemCardDataAdapter)item_buy_card_adapter).deselectCurrentLayout();
        buy_item_list.setAdapter(item_buy_card_adapter);


        // SELL
        sell_amount_slider.setProgress(0);
        sell_amount_slider.setMax(0);
        sell_amount_text.setText(getString(R.string.integer, 0));
        button_sell.setBackgroundResource(R.drawable.top_rounded_rec_disabled);
        button_sell.setText(getString(R.string.sell_cost, 0));
        button_sell.setEnabled(false);

        ((ItemCardDataAdapter)item_sell_card_adapter).updateCharacter(character);
        ((ItemCardDataAdapter)item_sell_card_adapter).deselectCurrentLayout();
        sell_item_list.setAdapter(item_sell_card_adapter);
    }

    private void makeSale(int item, int amount, int price, boolean is_buy) {

        character.setGems_collected(character.getGems_collected() - amount * price);
        if (is_buy)
            character.setGems_spent(character.getGems_spent() + amount * price);
        List<Integer> items = character.getItems();
        items.set(item, items.get(item) + amount);
        character.setItems(items);
    }

    @Override
    public void onBackPressed() {

        Intent return_intent = new Intent();
        return_intent.putExtra("character", character);
        setResult(RESULT_OK, return_intent);
        finish();
    }
}
