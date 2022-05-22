package com.example.bikeapp.bluetooth;

import android.bluetooth.BluetoothSocket;
import android.util.Log;

import com.example.bikeapp.models.InDataModel;

import org.greenrobot.eventbus.EventBus;

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
        buffer = new byte[6];
        while (true) {
            try {
                int length = inputStream.read(buffer);
                String message = new String(buffer, 0, length);

                if (message != null && !message.isEmpty()) {
                    if (message.charAt(0) == '.') {
                        try {
                            int pos = message.indexOf((char) 13);
                            if (pos > 0) {
                                int num = Integer.parseInt(message.substring(1, pos));
                                EventBus.getDefault().post(new InDataModel(num));
                                //  Log.d("bikeApp", "Парсинг прошёл успешно");
                            }
                        } catch (NumberFormatException ex) {
                            Log.d(TAG, "Пришло не число");
                        }
                    }
                }
            } catch (IOException e) {
                EventBus.getDefault().post(new InDataModel(0));
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
