package com.example.georealm.bluetooth;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Message;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;
import android.widget.Toast;

import com.example.georealm.InteractActivity;
import com.example.georealm.helper.Constants;

import java.io.IOException;
import java.io.Serializable;
import java.util.UUID;

public class MakeFriendRequestThread extends Thread {

    private final BluetoothServerSocket server_socket;
    private final BluetoothAdapter bt_adapter;
    private Context context;
    private boolean running = true;
    private InteractActivity.AddFriendHandler handler;

    public MakeFriendRequestThread(Context ctx, InteractActivity.AddFriendHandler han) {

        context = ctx;
        bt_adapter = BluetoothAdapter.getDefaultAdapter();
        handler = han;

        BluetoothServerSocket temp = null;
        try {

            temp = bt_adapter.listenUsingRfcommWithServiceRecord("name", UUID.fromString(Constants.APP_UUID));
        }
        catch (IOException exception) {

            Log.e("BLUETOOTH_EXCEPTION", exception.getMessage());
            // Toast.makeText(context, "Socket's listen() method failed.", Toast.LENGTH_SHORT).show();
        }

        server_socket = temp;
    }

    @Override
    public void run() {

        // Toast.makeText(context, "Waiting for connection...", Toast.LENGTH_SHORT).show();
        BluetoothSocket socket = null;

        while (running) {

            try {

                socket = server_socket.accept();
            }
            catch (IOException exception) {

                String message = exception.getMessage();
                handler.obtainMessage(Constants.SOCKET_ACQUIRED_ERROR, message).sendToTarget();
                // handler.sendEmptyMessage(Constants.SOCKET_ACQUIRED_ERROR);
                Log.e("BLUETOOTH_EXCEPTION", exception.getMessage());
                // Toast.makeText(context, "Socket's accept() method failed.", Toast.LENGTH_SHORT).show();
                break;
            }

            if (socket != null) {

                // Toast.makeText(context, "Devices are connected.", Toast.LENGTH_SHORT).show();

                BluetoothSockets.Instance().add_friend_socket = socket;
                handler.sendEmptyMessage(Constants.SOCKET_ACQUIRED);

                // closeSocket();
                break;
            }
        }

        closeSocket();
    }

    public void cancel() {

        running = false;
    }

    private void closeSocket() {

        try {

            server_socket.close();
        }
        catch (IOException exception) {

            Log.e("BLUETOOTH_EXCEPTION", exception.getMessage());
            // Toast.makeText(context, "Socket's close() method failed.", Toast.LENGTH_SHORT).show();
        }
    }
}
