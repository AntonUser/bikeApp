package com.example.bikeapp.bluetooth;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.example.bikeapp.MainActivity;
import com.example.bikeapp.bluetooth.ReceiveThread;
import com.example.bikeapp.models.InDataModel;

import org.greenrobot.eventbus.EventBus;

import java.io.IOException;

public class ConnectThread extends Thread {
    private BluetoothAdapter bluetoothAdapter;
    private BluetoothDevice device;
    private BluetoothSocket bluetoothSocket;
    private ReceiveThread rThread;
    private final String TAG = "bikeApp";
    private boolean isConnect;
    public static final String UUID = "00001101-0000-1000-8000-00805F9B34FB";

    public ConnectThread(BluetoothAdapter bluetoothAdapter, BluetoothDevice bluetoothDevice) {
        this.bluetoothAdapter = bluetoothAdapter;
        this.device = bluetoothDevice;
        try {
            bluetoothSocket = device.createInsecureRfcommSocketToServiceRecord(java.util.UUID.fromString(UUID));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        bluetoothAdapter.cancelDiscovery();
        try {
            bluetoothSocket.connect();
            rThread = new ReceiveThread(bluetoothSocket);
            rThread.start();
            EventBus.getDefault().post("Устройство подключено");
        } catch (IOException e) {
            Log.d(TAG, "Ошибка подключения");
            EventBus.getDefault().post("Ошибка подключения");
            closeConnection();
        }
    }

    public void closeConnection() {
        try {
            bluetoothSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public ReceiveThread getRThread() {
        return rThread;
    }

}
