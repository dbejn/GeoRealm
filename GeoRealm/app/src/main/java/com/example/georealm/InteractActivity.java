package com.example.georealm;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.FragmentActivity;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.georealm.bluetooth.AcceptFriendRequestThread;
import com.example.georealm.bluetooth.AddFriendConnectionThread;
import com.example.georealm.bluetooth.BluetoothSockets;
import com.example.georealm.bluetooth.MakeFriendRequestThread;
import com.example.georealm.data.CharacterData;
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
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.lang.ref.WeakReference;

import static com.example.georealm.helper.Constants.ASSASSIN;
import static com.example.georealm.helper.Constants.BERSERKER;
import static com.example.georealm.helper.Constants.DEFAULT_ZOOM;
import static com.example.georealm.helper.Constants.ICEBOUND;
import static com.example.georealm.helper.Constants.PALADIN;
import static com.example.georealm.helper.Constants.PYROMANCER;
import static com.example.georealm.helper.Constants.SHADOW;

public class InteractActivity extends FragmentActivity implements OnMapReadyCallback {

    // BLUETOOTH
    private static final int ACTIVITY_BLUETOOTH_FRIEND_REQUEST = 1;
    private static final int ACTIVITY_BLUETOOTH_FRIEND_ACCEPT = 2;
    private static final int ACTIVITY_BLUETOOTH_DUEL_REQUEST = 3;
    private static final int ACTIVITY_BLUETOOTH_DUEL_ACCEPT = 4;
    private static final int DISCOVERABLE_TIME = 300;
    private static final int DISCOVERABLE_MAKE_FRIEND_REQUEST = 5;
    private static final int DISCOVERABLE_ACCEPT_FRIEND_REQUEST = 6;
    private String original_bt_device_name;

    private Thread active_friend_request_thread;
    public Thread active_friend_connection_thread;

    private AddFriendHandler add_friend_handler;
    private AddFriendConnectionHandler add_friend_connection_handler;

    // MAPS
    private GoogleMap map;

    // TABS
    private Button button_info;
    private Button button_add_friend;
    private Button button_duel;
    private Button current_button;

    private View marker_info;
    private View marker_add_friend;
    private View marker_duel;
    private View current_marker;

    private ScrollView layout_info;
    private ConstraintLayout layout_add_friend;
    private ConstraintLayout layout_duel;
    private View current_view;

    // UI
    private CharacterData character;
    private ProgressBar progress_bar;
    private Button button_back;

    // INFO
    private ImageView info_icon;
    private TextView info_subclass;
    private TextView info_level;
    private TextView info_gems_collected;
    private TextView info_gems_spent;
    private TextView info_monsters_slain;
    private TextView info_duels_won;
    private TextView info_score;
    private TextView info_username;
    private TextView info_user_score;

    // ADD FRIEND
    private EditText edit_make_friend_request_password;
    private EditText edit_accept_friend_request_password;
    private Button button_make_friend_request;
    private Button button_accept_friend_request;
    private TextView waiting_for_response_friend;
    public String add_friend_request_password = "";
    private Button disabled_button;

    // DUEL
    private EditText make_duel_request_password;
    private EditText accept_duel_request_password;
    private Button button_make_duel_request;
    private Button button_accept_duel_request;
    private TextView waiting_duel;

    // FIRESTORE
    private FirebaseFirestore database;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_interact);


        // BLUETOOTH
        add_friend_handler = new AddFriendHandler(this);
        add_friend_connection_handler = new AddFriendConnectionHandler(this);


        // MAPS
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);


        // FIRESTORE
        database = FirebaseFirestore.getInstance();


        // TABS
        layout_info = findViewById(R.id.layout_info);
        layout_add_friend = findViewById(R.id.layout_add_friend);
        layout_duel = findViewById(R.id.layout_duel);

        button_info = findViewById(R.id.tab_button_info);
        button_add_friend = findViewById(R.id.tab_button_add_friend);
        button_duel = findViewById(R.id.tab_button_duel);

        marker_info = findViewById(R.id.tab_marker_info);
        marker_add_friend = findViewById(R.id.tab_marker_add_friend);
        marker_duel = findViewById(R.id.tab_marker_duel);

        current_view = layout_info;
        current_button = button_info;
        current_marker = marker_info;

        button_info.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                changeTab(layout_info, marker_info, button_info);
            }
        });

        button_add_friend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                changeTab(layout_add_friend, marker_add_friend, button_add_friend);
            }
        });

        button_duel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                changeTab(layout_duel, marker_duel, button_duel);
            }
        });

        // UI
        progress_bar = findViewById(R.id.progress_bar);
        progress_bar.setVisibility(View.VISIBLE);

        button_back = findViewById(R.id.button_back);
        button_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                /*Intent return_intent = new Intent();
                setResult(RESULT_OK, return_intent);*/
                finish();
            }
        });

        button_info.setText(getIntent().getStringExtra("name"));

        setAddFriendTab();
        setDuelTab();
        setInfoTab();
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

    private final BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            String action = intent.getAction();
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {

                BluetoothDevice bt_device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);

                String device_name = bt_device.getName();
                if (device_name.equals(getIntent().getStringExtra("username"))) {

                    BluetoothAdapter.getDefaultAdapter().cancelDiscovery();

                    AcceptFriendRequestThread accept_friend_request_thread = new AcceptFriendRequestThread(
                            InteractActivity.this, bt_device, add_friend_handler);

                    accept_friend_request_thread.start();
                }
            }
            else if (BluetoothAdapter.ACTION_SCAN_MODE_CHANGED.equals(action)) {

                int state = intent.getIntExtra(BluetoothAdapter.EXTRA_SCAN_MODE, BluetoothAdapter.ERROR);
                int previous_state = intent.getIntExtra(BluetoothAdapter.EXTRA_PREVIOUS_SCAN_MODE, BluetoothAdapter.ERROR);
                if (previous_state == BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE &&
                        (state == BluetoothAdapter.SCAN_MODE_CONNECTABLE ||
                                state == BluetoothAdapter.SCAN_MODE_NONE)) {

                    cancelActivity();
                    button_make_friend_request.setText(getString(R.string.make_friend_request));
                    button_accept_friend_request.setText(getString(R.string.accept_friend_request));
                }
            }
        }
    };

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

    private void setAddFriendTab() {

        edit_make_friend_request_password = findViewById(R.id.make_request_password);
        edit_accept_friend_request_password = findViewById(R.id.accept_request_password);
        button_make_friend_request = findViewById(R.id.button_make_request);
        button_accept_friend_request = findViewById(R.id.button_accept_request);
        waiting_for_response_friend = findViewById(R.id.waiting_for_response);
        waiting_for_response_friend.setVisibility(View.GONE);

        button_make_friend_request.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (button_make_friend_request.getText().equals(getString(R.string.make_friend_request))) {

                    String password = edit_make_friend_request_password.getText().toString();
                    if (password.equals("")) {

                        Toast.makeText(InteractActivity.this,
                                "Please enter password.", Toast.LENGTH_SHORT).show();

                        return;
                    }

                    button_accept_friend_request.setEnabled(false);
                    disabled_button = button_accept_friend_request;

                    add_friend_request_password = password;
                    startBluetoothActivity(ACTIVITY_BLUETOOTH_FRIEND_REQUEST);

                    button_make_friend_request.setText(getString(R.string.cancel_friend_request));
                    waiting_for_response_friend.setVisibility(View.VISIBLE);
                }
                else {

                    cancelActivity();
                    button_make_friend_request.setText(getString(R.string.make_friend_request));
                }
            }
        });

        button_accept_friend_request.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (button_accept_friend_request.getText().equals(getString(R.string.accept_friend_request))) {

                    String password = edit_accept_friend_request_password.getText().toString();
                    if (password.equals("")) {

                        Toast.makeText(InteractActivity.this,
                                "Please enter password.", Toast.LENGTH_SHORT).show();

                        return;
                    }

                    button_make_friend_request.setEnabled(false);
                    disabled_button = button_make_friend_request;

                    add_friend_request_password = password;
                    startBluetoothActivity(ACTIVITY_BLUETOOTH_FRIEND_ACCEPT);

                    button_accept_friend_request.setText(getString(R.string.cancel_accept_friend_request));
                    waiting_for_response_friend.setVisibility(View.VISIBLE);
                }
                else {

                    cancelActivity();
                    button_accept_friend_request.setText(getString(R.string.accept_friend_request));
                }
            }
        });
    }

    private void setDuelTab() {


    }

    private void setInfoTab() {

        info_icon = findViewById(R.id.character_info_icon);
        info_subclass = findViewById(R.id.character_info_subclass);
        info_level = findViewById(R.id.character_info_level);
        info_gems_collected = findViewById(R.id.character_info_gems);
        info_gems_spent = findViewById(R.id.character_info_gems_spent);
        info_monsters_slain = findViewById(R.id.character_info_monsters_slain);
        info_duels_won = findViewById(R.id.character_info_duels_won);
        info_score = findViewById(R.id.character_info_score);
        info_username = findViewById(R.id.info_username);
        info_user_score = findViewById(R.id.info_user_score);

        String username = getIntent().getStringExtra("username");
        int user_score = getIntent().getIntExtra("user_score", 0);
        info_username.setText(username);
        info_user_score.setText(getString(R.string.integer, user_score));

        DocumentReference character_ref = database.collection("characters")
                .document(getIntent().getStringExtra("name"));
        character_ref.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {

                if (task.isSuccessful()) {

                    DocumentSnapshot document = task.getResult();
                    info_gems_collected.setText(document.get("gems_collected").toString());
                    info_gems_spent.setText(document.get("gems_spent").toString());
                    info_monsters_slain.setText(document.get("monsters_slain").toString());
                    info_duels_won.setText(getString(R.string.info_duels,
                            Integer.parseInt(document.get("duels_won").toString()),
                            Integer.parseInt(document.get("duels_fought").toString())));
                    info_score.setText(document.get("score").toString());
                    info_level.setText(document.get("character_level").toString());

                    int subclass = Integer.parseInt(document.get("character_subclass").toString());
                    switch (subclass) {

                        case BERSERKER:
                            info_icon.setImageResource(R.drawable.ic_sword);
                            info_subclass.setText(getString(R.string.berserker));
                            break;
                        case PALADIN:
                            info_icon.setImageResource(R.drawable.ic_sword);
                            info_subclass.setText(getString(R.string.paladin));
                            break;
                        case PYROMANCER:
                            info_icon.setImageResource(R.drawable.ic_hat);
                            info_subclass.setText(getString(R.string.pyromancer));
                            break;
                        case ICEBOUND:
                            info_icon.setImageResource(R.drawable.ic_hat);
                            info_subclass.setText(getString(R.string.icebound));
                            break;
                        case ASSASSIN:
                            info_icon.setImageResource(R.drawable.ic_dagger);
                            info_subclass.setText(getString(R.string.assassin));
                            break;
                        case SHADOW:
                            info_icon.setImageResource(R.drawable.ic_dagger);
                            info_subclass.setText(getString(R.string.shadow));
                            break;
                    }
                }
                else {

                    Toast.makeText(InteractActivity.this,
                            "Unable to retrieve player\'s information. Please, try again.",
                            Toast.LENGTH_SHORT).show();

                    finish();
                }

                progress_bar.setVisibility(View.GONE);
            }
        });
    }

    private void startBluetoothActivity(int activity) {

        BluetoothAdapter bt_adapter = BluetoothAdapter.getDefaultAdapter();
        if (bt_adapter != null) {

            if (!bt_adapter.isEnabled()) {

                Intent enable_bt_intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enable_bt_intent, activity);
            }
            else {

                executeBluetoothActivity(activity);
            }
        }
        else {

            Toast.makeText(InteractActivity.this,
                    "Your device does not support bluetooth.",
                    Toast.LENGTH_SHORT).show();
        }
    }

    private void makeDeviceDiscoverable() {

        Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
        discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, DISCOVERABLE_TIME); // 5 minutes
        startActivityForResult(discoverableIntent, DISCOVERABLE_TIME);
    }

    private void discoverDevices() {

        BluetoothAdapter bt_adapter = BluetoothAdapter.getDefaultAdapter();
        if (bt_adapter.isDiscovering()) {

            bt_adapter.cancelDiscovery();
        }

        if(!bt_adapter.startDiscovery()) {

            Toast.makeText(InteractActivity.this,
                    "Unable to start device discovery. Please, try again.",
                    Toast.LENGTH_SHORT).show();
        }
    }

    private void executeBluetoothActivity(int activity) {

        switch (activity) {

            case ACTIVITY_BLUETOOTH_FRIEND_REQUEST:
                makeDeviceDiscoverable();
                break;
            case ACTIVITY_BLUETOOTH_FRIEND_ACCEPT:
                // makeDeviceDiscoverable(DISCOVERABLE_ACCEPT_FRIEND_REQUEST);
                discoverDevices();
                break;
        }
    }

    public AddFriendConnectionHandler getAddFriendConnectionHandler() {

        return add_friend_connection_handler;
    }

    public void addFriend() {

        DocumentReference user_ref = database.collection("users")
                .document(getIntent().getStringExtra("player_name"));
        user_ref.update("friends", FieldValue.arrayUnion(getIntent().getStringExtra("username")))
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {

                        if (task.isSuccessful()) {

                            Toast.makeText(InteractActivity.this,
                                    "Friend added.", Toast.LENGTH_SHORT).show();
                        }
                        else {

                            Toast.makeText(InteractActivity.this,
                                    "Database error. Unable to add friend. Please, try again.",
                                    Toast.LENGTH_SHORT).show();
                        }

                        // cancelActivity();
                        BluetoothAdapter.getDefaultAdapter().cancelDiscovery();
                        BluetoothAdapter.getDefaultAdapter().disable();

                        disabled_button.setEnabled(true);
                        waiting_for_response_friend.setVisibility(View.GONE);

                        button_make_friend_request.setText(getString(R.string.make_friend_request));
                        button_accept_friend_request.setText(getString(R.string.accept_friend_request));
                    }
                });
    }

    private void cancelActivity() {

        if (active_friend_request_thread != null)
            ((MakeFriendRequestThread)active_friend_request_thread).cancel();

        if (active_friend_connection_thread != null)
            ((AddFriendConnectionThread)active_friend_connection_thread).cancel();

        BluetoothAdapter.getDefaultAdapter().cancelDiscovery();
        BluetoothAdapter.getDefaultAdapter().disable();

        disabled_button.setEnabled(true);
        waiting_for_response_friend.setVisibility(View.GONE);

        Toast.makeText(InteractActivity.this, "Bluetooth activity is finished.", Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {

        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == DISCOVERABLE_TIME) {

            active_friend_request_thread = new MakeFriendRequestThread(InteractActivity.this, add_friend_handler);

            active_friend_request_thread.start();
        }
        else if (requestCode == ACTIVITY_BLUETOOTH_FRIEND_REQUEST) {

            if (resultCode == RESULT_OK) {

                executeBluetoothActivity(ACTIVITY_BLUETOOTH_FRIEND_REQUEST);
            }
            else {

                Toast.makeText(InteractActivity.this,
                        "You must enable bluetooth in order to make friend request.",
                        Toast.LENGTH_SHORT).show();
            }
        }
        else if (requestCode == ACTIVITY_BLUETOOTH_FRIEND_ACCEPT) {

            if (resultCode == RESULT_OK) {

                executeBluetoothActivity(ACTIVITY_BLUETOOTH_FRIEND_ACCEPT);
            }
            else {

                Toast.makeText(InteractActivity.this,
                        "You must enable bluetooth in order to accept friend request.",
                        Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        BluetoothAdapter bt_adapter = BluetoothAdapter.getDefaultAdapter();
        if (bt_adapter != null)
            original_bt_device_name = bt_adapter.getName();
        bt_adapter.setName(getIntent().getStringExtra("player_name"));

        IntentFilter bt_filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        bt_filter.addAction(BluetoothAdapter.ACTION_SCAN_MODE_CHANGED);
        registerReceiver(receiver, bt_filter);
    }

    @Override
    protected void onPause() {
        super.onPause();

        unregisterReceiver(receiver);
        BluetoothAdapter.getDefaultAdapter().setName(original_bt_device_name);
        // cancelActivity();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        cancelActivity();
    }

    public static class AddFriendHandler extends Handler {

        private final WeakReference<InteractActivity> activity;

        public AddFriendHandler(InteractActivity act) {

            activity = new WeakReference<>(act);
        }

        @Override
        public void handleMessage(Message msg) {

            InteractActivity act = activity.get();
            if (act != null) {

                if (msg.what == Constants.SOCKET_ACQUIRED) {

                    act.active_friend_connection_thread = new AddFriendConnectionThread(
                            BluetoothSockets.Instance().add_friend_socket, act.getAddFriendConnectionHandler());

                    /*Toast.makeText(act.getApplicationContext(),
                            "Socket acquired", Toast.LENGTH_SHORT).show();*/

                    act.active_friend_connection_thread.start();

                    byte[] message = act.add_friend_request_password.getBytes();
                    ((AddFriendConnectionThread)act.active_friend_connection_thread).write(message);
                }
                else if (msg.what == Constants.SOCKET_ACQUIRED_ERROR) {

                    Toast.makeText(act.getApplicationContext(),
                            "Failed to connect devices. Please, try again.", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    public static class AddFriendConnectionHandler extends Handler {

        private final WeakReference<InteractActivity> activity;

        public AddFriendConnectionHandler(InteractActivity act) {

            activity = new WeakReference<>(act);
        }

        @Override
        public void handleMessage(Message msg) {

            InteractActivity act = activity.get();
            if (act != null) {

                if (msg.what == Constants.CONFIRM_PASSWORD) {

                    String password = new String((byte[])msg.obj, 0, msg.arg1);
                    if (password.equals(act.add_friend_request_password)) {

                        act.addFriend();
                    }
                    else {

                        Toast.makeText(act.getApplicationContext(),
                                "Password do not match. Please, try again.", Toast.LENGTH_SHORT).show();
                    }
                }
                else if (msg.what == Constants.CONFIRM_PASSWORD_ERROR) {

                    Toast.makeText(act.getApplicationContext(),
                            "Unable to add friend. Please, try again.", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }
}

// kada se promeni stanje (non discoverable) zaustavi thread
// disable opposite buttons
