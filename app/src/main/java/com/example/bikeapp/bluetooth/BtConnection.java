package com.example.bikeapp.bluetooth;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import android.widget.Toast;

import com.example.bikeapp.adapter.Constants;

public class BtConnection {
    private Context context;
    private SharedPreferences sharedPreferences;
    private BluetoothAdapter bluetoothAdapter;
    private BluetoothDevice device;
    private ConnectThread connectThread;
    private static BtConnection btConnection;

    private BtConnection(Context context) {
        this.context = context;
        this.sharedPreferences = context.getSharedPreferences(Constants.MY_PREFERENCE,
                Context.MODE_PRIVATE);
        this.bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    }

    public static BtConnection createBtConnection(Context context) {
        if (btConnection == null) {
            btConnection = new BtConnection(context);
        }
        return btConnection;
    }

    public void connect() {
        String mac = sharedPreferences.getString(Constants.MAC_KEY, "");
        Log.d("bikeApp", "mac " + mac);
        if (!bluetoothAdapter.isEnabled() || mac.isEmpty()) {
            Toast.makeText(context, "Не включен bluetooth или не выбрано устройство для подключения", Toast.LENGTH_SHORT).show();
            return;
        }
        device = bluetoothAdapter.getRemoteDevice(mac);
        if (device == null) {
            Toast.makeText(context, "Устройство не активировано", Toast.LENGTH_SHORT).show();
            return;
        }
        connectThread = new ConnectThread(bluetoothAdapter, device);
        connectThread.start();
    }

    public void sendData(String data) {
        connectThread.getRThread().sendData(data.getBytes());
    }

}
