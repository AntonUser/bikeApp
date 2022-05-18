 package com.example.bikeapp.bluetooth;

import android.bluetooth.BluetoothSocket;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class ReceiveThread extends Thread {
    private BluetoothSocket bluetoothSocket;
    private InputStream inputStream;
    private OutputStream outputStream;
    private byte[] buffer;
    private final String TAG = "bikeApp";

    public ReceiveThread(BluetoothSocket bluetoothSocket) {
        this.bluetoothSocket = bluetoothSocket;
        try {
            inputStream = bluetoothSocket.getInputStream();
        } catch (IOException e) {

        }
        try {
            outputStream = bluetoothSocket.getOutputStream();
        } catch (IOException e) {

        }
    }

    @Override
    public void run() {
        buffer = new byte[1024];
        String inString = "";
        while (true) {
            try {

                int length = inputStream.read(buffer);
                String message = new String(buffer, 0, length);
//                Log.d(TAG, message);
                //клеим строки пока не встретим конец строки
                inString += message;

                if (message.charAt(message.length() - 1) == '\n') {
                    Log.d(TAG, inString);
                    inString = "";
                }

            } catch (IOException e) {
                break;
            }
        }
    }

    public void sendData(byte[] bytes) {
        try {
            outputStream.write(bytes);
        } catch (IOException e) {
            Log.d(TAG, "Ошибка передачи данных");
        }
    }
}
