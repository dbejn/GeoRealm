package com.example.georealm;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.location.Location;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;
import android.os.Bundle;
import androidx.core.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.georealm.adapters.MarkerInfoWindowAdapter;
import com.example.georealm.data.CharacterData;
import com.example.georealm.data.Icebound;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.example.georealm.helper.Constants.CREATE_MARKERS_URL;
import static com.example.georealm.helper.Constants.DEFAULT_ZOOM;
import static com.example.georealm.helper.Constants.ICEBOUND;
import static com.example.georealm.helper.Constants.PYROMANCER;
import static com.example.georealm.helper.Constants.ROGUE;
import static com.example.georealm.helper.Constants.SORCERER;
import static com.example.georealm.helper.Constants.SWORDSMAN;
import static com.example.georealm.helper.Functions.arrayContains;

public class GameActivity extends FragmentActivity
        implements GoogleMap.OnInfoWindowLongClickListener, GoogleMap.OnInfoWindowClickListener,
        GoogleMap.OnMarkerClickListener, OnMapReadyCallback {

    private static final int SHOP_REQUEST = 1;

    // MAPS
    private GoogleMap map;

    private FusedLocationProviderClient fused_location_provider_client;
    private LocationRequest location_request;
    private LocationCallback location_callback;

    private static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;
    private boolean location_permission_granted;

    private Location last_known_location;

    private static final String KEY_LOCATION = "location";

    private Marker my_marker;
    private List<Marker> player_markers;
    private Marker last_opened_marker;

    // UI
    private Button button_menu;
    private LinearLayout layout_menu;

    private ConstraintLayout layout_info;
    private Button button_quit;

    private Button button_view_character;
    private Button button_change_character;
    private Button button_highscore;
    private Button button_instructions;

    private ImageView class_image;
    private TextView text_character;
    private String character_name;
    private int character_class;
    private int character_subclass;
    private int character_level;
    private String username;

    private ProgressBar progress_bar;

    private CharacterData character;

    // FIRESTORE
    private FirebaseFirestore database;

    // CLOUD FUNCTIONS
    private RequestQueue request_queue;
    private StringRequest gem_markers_string_request;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (savedInstanceState != null) {

            last_known_location = savedInstanceState.getParcelable(KEY_LOCATION);
        }

        setContentView(R.layout.activity_game);

        username = getIntent().getStringExtra("username");
        character_name = getIntent().getStringExtra("character_name");
        character_class = getIntent().getIntExtra("character_class", 1);
        character_subclass = getIntent().getIntExtra("character_subclass", 1);
        character_level = getIntent().getIntExtra("character_level", 1);

        // MAPS
        fused_location_provider_client = LocationServices
                .getFusedLocationProviderClient(this);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        player_markers = new ArrayList<>();

        location_callback = new LocationCallback() {

            @Override
            public void onLocationResult(LocationResult locationResult) {

                if (locationResult == null) {

                    return;
                }

                Location location = locationResult.getLastLocation();
                if (location != null) {

                    last_known_location = location;

                    if (my_marker != null) {

                        my_marker.setPosition(new LatLng(last_known_location.getLatitude(), last_known_location.getLongitude()));
                    }

                    if (map != null) {

                        /*map.animateCamera(CameraUpdateFactory.newLatLngZoom(
                                new LatLng(last_known_location.getLatitude(),
                                        last_known_location.getLongitude()), map.getCameraPosition().zoom));*/
                    }

                    DocumentReference user_doc = database.collection("users").document(username);
                    user_doc.update("latitude", last_known_location.getLatitude(),
                            "longitude", last_known_location.getLongitude());

                    for (Marker marker : player_markers) {

                        marker.remove();
                    }
                    getCharacterMarkers();
                }
            }
        };

        location_request = new LocationRequest().setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY).setInterval(10000);

        // UI
        layout_info = findViewById(R.id.layout_info);
        button_quit = findViewById(R.id.button_quit);
        button_quit.setVisibility(View.INVISIBLE);
        button_quit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                finish();
            }
        });

        layout_menu = findViewById(R.id.layout_menu);
        layout_menu.setVisibility(View.INVISIBLE);
        button_menu = findViewById(R.id.button_menu);
        button_menu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (button_menu.getText().toString().compareTo("menu") == 0) {

                    button_quit.setVisibility(View.VISIBLE);

                    button_menu.setText(R.string.back);
                    button_menu.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_arrow_up,  0);
                    layout_info.setBackgroundResource(R.drawable.bot_left_rounded_rec_trans);
                    layout_menu.setVisibility(View.VISIBLE);
                }
                else {

                    button_quit.setVisibility(View.INVISIBLE);

                    layout_menu.setVisibility(View.GONE);
                    layout_info.setBackgroundResource(R.drawable.bot_rounded_rec_trans);
                    button_menu.setText(R.string.menu);
                    button_menu.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_menu,  0);
                }
            }
        });

        button_view_character = findViewById(R.id.button_view_character);
        button_view_character.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(GameActivity.this, CharacterActivity.class);
                intent.putExtra("subclass", character_subclass);
                intent.putExtra("character", character);
                intent.putExtra("latitude", my_marker.getPosition().latitude);
                intent.putExtra("longitude", my_marker.getPosition().longitude);
                startActivity(intent);
            }
        });

        button_instructions = findViewById(R.id.button_instructions);
        button_instructions.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(GameActivity.this, BattleActivity.class);
                startActivity(intent);
            }
        });

        button_change_character = findViewById(R.id.button_change_character);

        button_highscore = findViewById(R.id.button_highscore);
        button_highscore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(GameActivity.this, HighscoreActivity.class);
                intent.putExtra("latitude", my_marker.getPosition().latitude);
                intent.putExtra("longitude", my_marker.getPosition().longitude);
                intent.putExtra("username", username);
                startActivity(intent);
            }
        });

        text_character = findViewById(R.id.text_character);
        class_image = findViewById(R.id.class_image);

        progress_bar = findViewById(R.id.progress_bar);


        // FIRESTORE
        progress_bar.setVisibility(View.VISIBLE);

        database = FirebaseFirestore.getInstance();
        DocumentReference character_doc = database.collection("characters").document(character_name);
        character_doc.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {

                if (task.isSuccessful()) {

                    DocumentSnapshot document = task.getResult();
                    if (!document.exists()) {

                        switch (character_subclass) {

                            case PYROMANCER:
                                Toast.makeText(GameActivity.this,
                                        "Welcome to the GeoRealm", Toast.LENGTH_SHORT).show();
                                break;
                            case ICEBOUND:
                                character = new Icebound(character_name);
                                break;
                        }

                        DocumentReference character_create_doc = database
                                .collection("characters").document(character_name);
                        character_create_doc.set(character).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {

                                if (task.isSuccessful()) {

                                    setClassImage(character_class);
                                    text_character.setText(getString(R.string.name_level, character_name, character_level));
                                }
                                else {

                                    Toast.makeText(GameActivity.this,
                                            "Could not start a game, please try again", Toast.LENGTH_SHORT).show();

                                    finish();
                                }
                            }
                        });
                    }
                    else {

                        switch (character_subclass) {

                            case PYROMANCER:
                                Toast.makeText(GameActivity.this,
                                        "Welcome to the GeoRealm", Toast.LENGTH_SHORT).show();
                                break;
                            case ICEBOUND:
                                character = document.toObject(Icebound.class);
                                break;
                        }

                        setClassImage(character_class);
                        text_character.setText(getString(R.string.name_level, character_name, character_level));
                    }
                }
                else {

                    Toast.makeText(GameActivity.this,
                            "Could not start a game, please try again", Toast.LENGTH_SHORT).show();

                    finish();
                }
            }
        });

        setPlaying();
        changeUserStatus(true);


        // CLOUD FUNCTIONS - GEM MARKERS
        request_queue = Volley.newRequestQueue(this);
        getGemMarkers();


    }

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

            MarkerInfoWindowAdapter marker_adapter =
                    new MarkerInfoWindowAdapter(GameActivity.this);
            map.setInfoWindowAdapter(marker_adapter);

            map.setOnInfoWindowLongClickListener(this);
            map.setOnInfoWindowClickListener(this);
            map.setOnMarkerClickListener(this);
        }
        catch (Resources.NotFoundException e) {

            // exception
        }

        getLocationPermission();

        updateLocationUI();

        getDeviceLocation();

        // SHOP MARKERS
        getShopMarkers();

        // CHARACTER MARKERS
        // getCharacterMarkers();
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

    private void updateLocationUI() {

        if (map == null) {

            return;
        }
        else {

            // map.getUiSettings().setAllGesturesEnabled(false);
            map.getUiSettings().setZoomGesturesEnabled(true);
            map.getUiSettings().setRotateGesturesEnabled(true);
            map.getUiSettings().setCompassEnabled(false);
            // map.setMinZoomPreference(MIN_ZOOM);
            map.setOnCameraIdleListener(new GoogleMap.OnCameraIdleListener() {
                @Override
                public void onCameraIdle() {

                    /*CameraUpdate animation = CameraUpdateFactory
                            .newLatLngZoom(new LatLng(last_known_location.getLatitude(),
                                    last_known_location.getLongitude()),
                                    map.getCameraPosition().zoom);
                    map.animateCamera(animation);*/
                }
            });
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

    private void getDeviceLocation() {

        try {

            if (location_permission_granted) {

                Task<Location> location_result = fused_location_provider_client.getLastLocation();
                location_result.addOnCompleteListener(this, new OnCompleteListener<Location>() {
                    @Override
                    public void onComplete(@NonNull Task<Location> task) {
                        if (task.isSuccessful()) {

                            last_known_location = task.getResult();
                            if (last_known_location != null) {

                                map.animateCamera(CameraUpdateFactory.newLatLngZoom(
                                        new LatLng(last_known_location.getLatitude(),
                                                last_known_location.getLongitude()), DEFAULT_ZOOM));

                                my_marker = map.addMarker(new MarkerOptions()
                                        .position(new LatLng(last_known_location.getLatitude(), last_known_location.getLongitude()))
                                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.my_marker)));
                                my_marker.setTitle("player");

                                DocumentReference user_doc = database.collection("users").document(username);
                                user_doc.update("latitude", last_known_location.getLatitude(),
                                        "longitude", last_known_location.getLongitude());

                                map.addMarker(new MarkerOptions()
                                        .title("test_marker")
                                        .position(new LatLng(last_known_location.getLatitude(), last_known_location.getLongitude() - 0.0001f))
                                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.my_marker)));
                            }
                            else {

                                Toast.makeText(GameActivity.this, "Error fetching location", Toast.LENGTH_SHORT).show();
                                finish();
                            }
                        }
                        else {

                            Toast.makeText(GameActivity.this, "Error fetching location", Toast.LENGTH_SHORT).show();
                            finish();
                        }
                    }
                });
            }
        } catch (SecurityException e) {

            Log.e("Exception: %s", e.getMessage());
        }
    }

    @Override
    public boolean onMarkerClick(Marker marker) {

        if (last_opened_marker != null) {

            last_opened_marker.hideInfoWindow();

            if (last_opened_marker.equals(marker)) {

                last_opened_marker = null;

                return true;
            }
        }

        marker.showInfoWindow();
        last_opened_marker = marker;

        return true;
    }

    private void setPlaying() {

        List<Object> playing = new ArrayList<>();
        playing.add(character_name);
        playing.add(character_level);
        playing.add(character_class);
        playing.add(character_subclass);

        DocumentReference user_ref = database.collection("users").document(username);
        user_ref.update("playing", playing);
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

    private void getGemMarkers(){

        gem_markers_string_request = new StringRequest(Request.Method.POST, CREATE_MARKERS_URL, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {

                final String res = response;

                DocumentReference user_doc = database.collection("users").document(username);
                user_doc.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {

                        if (task.isSuccessful()) {

                            DocumentSnapshot document = task.getResult();
                            Map<String, Object> data = document.getData();
                            ArrayList<Long> markers_nums = (ArrayList<Long>) data.get("collected_gems");

                            String last_online = (String) data.get("last_online");
                            SimpleDateFormat date_format = new SimpleDateFormat("dd.MM.yyyy");
                            try {

                                Date date = date_format.parse(last_online);
                                String string_date_now = date_format.format(Calendar.getInstance().getTime());
                                Date date_now = date_format.parse(string_date_now);
                                if (date_now.after(date)) {

                                    markers_nums = null;
                                    DocumentReference user_doc = database.collection("users").document(username);
                                    user_doc.update("collected_gems", FieldValue.delete());
                                }
                            }
                            catch (ParseException e) {

                                e.printStackTrace();

                                Toast.makeText(GameActivity.this,
                                        "Could not start a game, please try again. " + e.getMessage(),
                                        Toast.LENGTH_SHORT).show();

                                finish();
                            }

                            try {

                                JSONObject res_data = new JSONObject(res);
                                JSONArray json_array_lat = res_data.getJSONArray("latitude");
                                JSONArray json_array_lon = res_data.getJSONArray("longitude");

                                for (int i = 0; i < json_array_lat.length(); i++) {

                                    if (arrayContains(markers_nums, i)) {

                                        continue;
                                    }

                                    double latitude = json_array_lat.getDouble(i);
                                    double longitude = json_array_lon.getDouble(i);

                                    Marker marker = map.addMarker(new MarkerOptions()
                                            .position(new LatLng(latitude, longitude))
                                            .infoWindowAnchor(0.5f, 0.75f)
                                            .icon(BitmapDescriptorFactory.fromResource(R.drawable.gem_marker)));
                                    marker.setTitle("gem");
                                    marker.setTag(i);

                                    if (i == json_array_lat.length() - 1) {

                                        map.animateCamera(CameraUpdateFactory.newLatLngZoom(
                                                new LatLng(latitude, longitude), DEFAULT_ZOOM));
                                    }
                                }

                                progress_bar.setVisibility(View.GONE);
                            }
                            catch (JSONException json_e) {

                                json_e.printStackTrace();

                                Toast.makeText(GameActivity.this,
                                        "Could not start a game, please try again. " + json_e.getMessage(),
                                        Toast.LENGTH_SHORT).show();

                                finish();
                            }
                        }
                        else {

                            Toast.makeText(GameActivity.this,
                                    "Could not start a game, please try again.",
                                    Toast.LENGTH_SHORT).show();

                            finish();
                        }
                    }
                });
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

                Toast.makeText(GameActivity.this,
                        "Could not start a game, please try again. " + error.getMessage(),
                        Toast.LENGTH_SHORT).show();

                finish();
            }
        });
        request_queue.add(gem_markers_string_request);
    }

    private void getShopMarkers() {

        // elfak
        Marker el_marker = map.addMarker(new MarkerOptions()
                .position(new LatLng(43.331323, 21.892429))
                .infoWindowAnchor(0.5f, 0.75f)
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.shop_marker)));
        el_marker.setTitle("shop");

        // cele kula
        Marker ce_marker = map.addMarker(new MarkerOptions()
                .position(new LatLng(43.3122823, 21.9239165))
                .infoWindowAnchor(0.5f, 0.75f)
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.shop_marker)));
        ce_marker.setTitle("shop");

        // cair
        Marker ca_marker = map.addMarker(new MarkerOptions()
                .position(new LatLng(43.3150536, 21.9086836))
                .infoWindowAnchor(0.5f, 0.75f)
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.shop_marker)));
        ca_marker.setTitle("shop");

        // digitalni muzej
        Marker di_marker = map.addMarker(new MarkerOptions()
                .position(new LatLng(43.323749, 21.895117))
                .infoWindowAnchor(0.5f, 0.75f)
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.shop_marker)));
        di_marker.setTitle("shop");
    }

    private void getCharacterMarkers() {

        database.collection("users")
                .whereGreaterThan("latitude", last_known_location.getLatitude() - 1.0f)
                .whereLessThan("latitude", last_known_location.getLatitude() + 1.0f)
                .whereEqualTo("status", "online")
                .get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {

                if (task.isSuccessful()) {

                    player_markers.clear();

                    for (QueryDocumentSnapshot document : task.getResult()) {

                        if (!document.getId().equals(username)) {

                            Map<String, Object> document_map = document.getData();
                            ArrayList<Object> playing = (ArrayList<Object>) document_map.get("playing");

                            HashMap<String, Object> hash_map = new HashMap<>();
                            hash_map.put("name", playing.get(0));
                            hash_map.put("subclass", playing.get(3));
                            hash_map.put("level", playing.get(1));
                            hash_map.put("username", document.getId());
                            hash_map.put("user_score", document_map.get("score"));

                            Marker marker = map.addMarker(new MarkerOptions()
                                    .position(new LatLng(Double.parseDouble(document_map.get("latitude").toString()),
                                            Double.parseDouble(document_map.get("longitude").toString())))
                                    .infoWindowAnchor(0.5f, 0.75f)
                                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.character_marker)));
                            marker.setTitle("character");
                            marker.setTag(hash_map);

                            player_markers.add(marker);
                        }
                    }
                }
                else {

                    Toast.makeText(GameActivity.this,
                            "Unable to retrieve other players. Please, try again.",
                            Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void setClassImage(int classs) {

        switch (classs) {

            case SWORDSMAN:
                class_image.setImageResource(R.drawable.ic_sword);
                break;
            case SORCERER:
                class_image.setImageResource(R.drawable.ic_hat);
                break;
            case ROGUE:
                class_image.setImageResource(R.drawable.ic_dagger);
                break;
        }
    }

    private void startLocationUpdates() {

        if (ContextCompat.checkSelfPermission(this.getApplicationContext(),
                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

            fused_location_provider_client.requestLocationUpdates(location_request, location_callback, null);
        }
        else {

            Toast.makeText(GameActivity.this, "Error fetching location", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void stopLocationUpdates() {

        fused_location_provider_client.removeLocationUpdates(location_callback);
    }

    private void changeUserStatus(boolean online) {

        DocumentReference user_ref = database.collection("users").document(username);

        if (online) {

            user_ref.update("status", "online");
        }
        else {

            SimpleDateFormat date_format = new SimpleDateFormat("dd.MM.yyyy HH:mm");
            Date date = Calendar.getInstance().getTime();
            String string_date = date_format.format(date);

            Map<String, Object> data = new HashMap<>();
            data.put("status", "offline");
            data.put("last_online", string_date);
            user_ref.update(data);
        }
    }

    private float calculateDistance(LatLng start, LatLng end) {

        Location start_location = new Location("");
        start_location.setLatitude(start.latitude);
        start_location.setLongitude(start.longitude);

        Location end_location = new Location("");
        end_location.setLatitude(end.latitude);
        end_location.setLongitude(end.longitude);

        return start_location.distanceTo(end_location); // meters
    }

    @Override
    protected void onResume() {

        super.onResume();

        startLocationUpdates();
    }

    @Override
    protected void onPause() {

        super.onPause();

        stopLocationUpdates();
    }

    @Override
    protected void onDestroy() {

        super.onDestroy();

        changeUserStatus(false);
        DocumentReference character_ref = database.collection("characters").document(character_name);
        if (character != null)
            character_ref.set(character);
    }

    @Override
    public void onInfoWindowLongClick(Marker marker) {

        if (marker.getTitle().equals("gem")){

            if (calculateDistance(my_marker.getPosition(), marker.getPosition()) <= 1000.0f) {

                progress_bar.setVisibility(View.VISIBLE);
                Toast.makeText(GameActivity.this, "Collecting gem...", Toast.LENGTH_SHORT).show();

                final Marker m = marker;

                int marker_num = Integer.parseInt(marker.getTag().toString());


                DocumentReference user_doc = database.collection("users").document(username);
                user_doc.update("collected_gems", FieldValue.arrayUnion(marker_num),
                        "score", FieldValue.increment(1))
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {

                                if (task.isSuccessful()) {

                                    m.remove();
                                    Toast.makeText(GameActivity.this, "+1 gem", Toast.LENGTH_SHORT).show();
                                    character.setGems_collected(character.getGems_collected() + 1);
                                    character.setScore(character.getScore() + 1);
                                    character.setExperience(character.getExperience() + 10);
                                    progress_bar.setVisibility(View.GONE);
                                }
                                else {

                                    Toast.makeText(GameActivity.this, "Failed to collect gem, please try again", Toast.LENGTH_SHORT).show();
                                    progress_bar.setVisibility(View.GONE);
                                }
                            }
                        });
            }
            else {

                Toast.makeText(GameActivity.this, "You need to get closer to this gem.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onInfoWindowClick(Marker marker) {

        if (marker.getTitle().equals("shop")) {

            if (calculateDistance(my_marker.getPosition(), marker.getPosition()) <= 1000.0f) {

                Intent intent = new Intent(GameActivity.this, ShopActivity.class);
                intent.putExtra("subclass", character_subclass);
                intent.putExtra("character", character);
                intent.putExtra("latitude", my_marker.getPosition().latitude);
                intent.putExtra("longitude", my_marker.getPosition().longitude);
                startActivityForResult(intent, SHOP_REQUEST);
            }
            else {

                Toast.makeText(GameActivity.this, "You need to get closer to this shop.", Toast.LENGTH_SHORT).show();
            }
        }
        else if (marker.getTitle().equals("character")) {

            HashMap character_map = (HashMap) marker.getTag();
            String name = character_map.get("name").toString();
            String other_username = character_map.get("username").toString();
            int user_score = Integer.parseInt(character_map.get("user_score").toString());

            Intent intent = new Intent(GameActivity.this, InteractActivity.class);
            intent.putExtra("name", name);
            intent.putExtra("username", other_username);
            intent.putExtra("user_score", user_score);
            intent.putExtra("player_name", username);
            intent.putExtra("latitude", my_marker.getPosition().latitude);
            intent.putExtra("longitude", my_marker.getPosition().longitude);
            startActivity(intent);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {

        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == SHOP_REQUEST) {

            if (resultCode == RESULT_OK) {

                switch (character_subclass) {

                    case PYROMANCER:
                        // character = (Icebound) getIntent().getParcelableExtra("character");
                        break;
                    case ICEBOUND:
                        character = (Icebound) data.getParcelableExtra("character");
                        break;
                }
            }
        }
    }
}
