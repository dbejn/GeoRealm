package com.example.georealm.bluetooth;

import android.bluetooth.BluetoothSocket;

public class BluetoothSockets {

    private static BluetoothSockets instance = null;

    public BluetoothSocket add_friend_socket;

    protected BluetoothSockets() { }

    public static synchronized BluetoothSockets Instance() {

        if (instance == null) {

            instance = new BluetoothSockets();
        }

        return instance;
    }
}
