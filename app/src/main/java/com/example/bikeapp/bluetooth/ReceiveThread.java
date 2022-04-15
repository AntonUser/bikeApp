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
        while (true) {
            try {
//                inputStream.readAllBytes
                int length = inputStream.read(buffer);
                String message = new String(buffer, 0, length);
                Log.d(TAG, "message: " + message);
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
