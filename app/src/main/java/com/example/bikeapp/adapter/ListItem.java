package com.example.bikeapp.adapter;

import android.bluetooth.BluetoothDevice;

import java.util.Objects;

public class ListItem {
    private BluetoothDevice bluetoothDevice;
    private String itemType = BtAdapter.ITEM_SAVED; // saved/searched/title

    public ListItem() {
    }

    public ListItem(BluetoothDevice bluetoothDevice) {
        this.bluetoothDevice = bluetoothDevice;
    }

    public BluetoothDevice getBluetoothDevice() {
        return bluetoothDevice;
    }

    public void setBluetoothDevice(BluetoothDevice bluetoothDevice) {
        this.bluetoothDevice = bluetoothDevice;
    }

    public String getItemType() {
        return itemType;
    }

    public void setItemType(String itemType) {
        this.itemType = itemType;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ListItem listItem = (ListItem) o;
        return Objects.equals(bluetoothDevice, listItem.bluetoothDevice) &&
                Objects.equals(itemType, listItem.itemType);
    }

    @Override
    public int hashCode() {
        return Objects.hash(bluetoothDevice, itemType);
    }
}
