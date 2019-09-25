package com.example.georealm.bluetooth;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.example.georealm.InteractActivity;
import com.example.georealm.helper.Constants;

import java.io.IOException;
import java.util.UUID;

public class AcceptFriendRequestThread extends Thread {

    private final BluetoothSocket socket;
    private final BluetoothDevice bt_device;
    private final BluetoothAdapter bt_adapter;
    private Context context;
    private InteractActivity.AddFriendHandler handler;

    public AcceptFriendRequestThread(Context ctx, BluetoothDevice device, InteractActivity.AddFriendHandler han) {

        context = ctx;
        bt_device = device;
        bt_adapter = BluetoothAdapter.getDefaultAdapter();
        handler = han;

        BluetoothSocket temp = null;
        try {

            temp = bt_device.createRfcommSocketToServiceRecord(UUID.fromString(Constants.APP_UUID));
        }
        catch (IOException exception) {

            Log.e("BLUETOOTH_EXCEPTION", exception.getMessage());
            // Toast.makeText(context, "Socket's create() method failed.", Toast.LENGTH_SHORT).show();
        }

        socket = temp;
    }

    @Override
    public void run() {

        bt_adapter.cancelDiscovery();

        try {

            socket.connect();
        }
        catch (IOException exception) {

            String message = exception.getMessage();
            handler.obtainMessage(Constants.SOCKET_ACQUIRED_ERROR, message).sendToTarget();
            // handler.sendEmptyMessage(Constants.SOCKET_ACQUIRED_ERROR);
            Log.e("BLUETOOTH_EXCEPTION", exception.getMessage());
            // Toast.makeText(context, "Socket's connect() method failed.", Toast.LENGTH_SHORT).show();
            closeSocket();

            return;
        }

        // Toast.makeText(context, "Devices are connected.", Toast.LENGTH_SHORT).show();
        BluetoothSockets.Instance().add_friend_socket = socket;
        handler.sendEmptyMessage(Constants.SOCKET_ACQUIRED);
    }

    private void closeSocket() {

        try {

            socket.close();
        }
        catch (IOException exception) {

            Log.e("BLUETOOTH_EXCEPTION", exception.getMessage());
            // Toast.makeText(context, "Socket's close() method failed.", Toast.LENGTH_SHORT).show();
        }
    }
}
