package com.example.georealm.bluetooth;

import android.bluetooth.BluetoothSocket;
import android.os.Bundle;
import android.os.Message;
import android.util.Log;

import com.example.georealm.InteractActivity;
import com.example.georealm.helper.Constants;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.logging.Handler;

public class AddFriendConnectionThread extends Thread {

    private InteractActivity.AddFriendConnectionHandler handler;
    private final BluetoothSocket socket;
    private final InputStream input_stream;
    private final OutputStream output_stream;
    private byte[] buffer;
    private boolean running = true;

    public AddFriendConnectionThread(BluetoothSocket soc, InteractActivity.AddFriendConnectionHandler han) {

        socket = soc;
        handler = han;

        InputStream temp_in = null;
        OutputStream temp_out = null;
        try {

            temp_in = socket.getInputStream();
        }
        catch (IOException exception) {

            Log.e("BLUETOOTH_EXCEPTION", exception.getMessage());
        }
        try {

            temp_out = socket.getOutputStream();
        }
        catch (IOException exception) {

            Log.e("BLUETOOTH_EXCEPTION", exception.getMessage());
        }

        input_stream = temp_in;
        output_stream = temp_out;
    }

    @Override
    public void run() {

        int num_of_bytes;

        while (running) {

            try {

                buffer = new byte[1024];

                num_of_bytes = input_stream.read(buffer);
                byte[] temp_buffer = buffer.clone();

                if (num_of_bytes > 0) {

                    Message message = handler.obtainMessage(
                            Constants.CONFIRM_PASSWORD, num_of_bytes, -1, temp_buffer);
                    message.sendToTarget();

                    break;
                }
            }
            catch (IOException exception) {

                Log.e("BLUETOOTH_EXCEPTION", exception.getMessage());
                handler.sendEmptyMessage(Constants.CONFIRM_PASSWORD_ERROR);
                break;
            }
        }

        // closeSocket();
    }

    public void write(byte[] buffer) {

        try{

            output_stream.write(buffer);
            handler.sendEmptyMessage(10);
        }
        catch (IOException exception) {

            Log.e("BLUETOOTH_EXCEPTION", exception.getMessage());

            closeSocket();
            handler.sendEmptyMessage(Constants.CONFIRM_PASSWORD_ERROR);
            cancel();
        }
    }

    public void cancel() {

        running = false;
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
