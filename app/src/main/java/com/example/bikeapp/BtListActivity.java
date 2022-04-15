package com.example.bikeapp;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.ClipData;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.bikeapp.adapter.BtAdapter;
import com.example.bikeapp.adapter.Constants;
import com.example.bikeapp.adapter.ListItem;
import com.example.bikeapp.bluetooth.BtConnection;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class BtListActivity extends AppCompatActivity {
    private static final int BT_REQUEST_PERMISSION_CODE = 136;

    private ListView listView;
    private BtAdapter adapter;
    private BluetoothAdapter mBluetoothAdapter;
    private List<ListItem> list;
    private ActionBar actionBar;
    private MenuItem item;
    private BtConnection btConnection;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        btConnection = BtConnection.createBtConnection(this);
        actionBar = getSupportActionBar();
        getBtPermission();
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        list = new ArrayList<>();
        setContentView(R.layout.activity_bt_list);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null)
            actionBar.setDisplayHomeAsUpEnabled(true);
        listView = findViewById(R.id.listView);
        adapter = new BtAdapter(this, R.layout.bt_list_item, list);
        getPairedDevices();//заполнили лист устройствами
        listView.setAdapter(adapter);
        onItemClickListener();
    }

    @Override
    protected void onResume() {
        super.onResume();
        IntentFilter intentFilter1 = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        IntentFilter intentFilter2 = new IntentFilter(BluetoothDevice.ACTION_BOND_STATE_CHANGED);
        IntentFilter intentFilter3 = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        registerReceiver(broadcastReceiver, intentFilter1);
        registerReceiver(broadcastReceiver, intentFilter2);
        registerReceiver(broadcastReceiver, intentFilter3);
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(broadcastReceiver);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.search, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                break;
            case R.id.search:
                this.item = item;
                if (mBluetoothAdapter.isDiscovering()) {
                    item.setIcon(R.drawable.ic_baseline_bluetooth_searching_24);
                    mBluetoothAdapter.cancelDiscovery();
                    getPairedDevices();
                    return true;
                }
                item.setIcon(R.drawable.ic_baseline_bluetooth_24);
                actionBar.setTitle(R.string.discovery_status);
                list.clear();
                ListItem itemTitle = new ListItem();
                itemTitle.setItemType(BtAdapter.ITEM_TITLE);
                list.add(itemTitle);
                adapter.notifyDataSetChanged();
                mBluetoothAdapter.startDiscovery();
                break;
            case R.id.connect:
                btConnection.connect();
                break;
        }
        return true;
    }

    private void getPairedDevices() {
        Set<BluetoothDevice> bluetoothDevices = mBluetoothAdapter.getBondedDevices();
        if (bluetoothDevices.size() > 0) {
            list.clear();
            for (BluetoothDevice bluetoothDevice : bluetoothDevices) {
                list.add(new ListItem(bluetoothDevice));
            }
            adapter.notifyDataSetChanged();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == BT_REQUEST_PERMISSION_CODE) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            } else {
                Toast.makeText(this, "Нет разрешения на поиск устройства!", Toast.LENGTH_SHORT).show();
            }
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    private void onItemClickListener() {
        listView.setOnItemClickListener((parent, view, position, id) -> {
            ListItem item = (ListItem) parent.getItemAtPosition(position);
            if (item.getItemType().equals(BtAdapter.ITEM_SEARCHED)) {
                SharedPreferences.Editor edit = this.getSharedPreferences(Constants.MY_PREFERENCE, Context.MODE_PRIVATE).edit();//Сохраняем элемент в память, чекбокс отметится возле него
                edit.putString(Constants.MAC_KEY, item.getBluetoothDevice().getAddress());
                edit.apply();
                item.getBluetoothDevice().createBond();
            }
        });
    }

    private void getBtPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, BT_REQUEST_PERMISSION_CODE);
        } else {
        }
    }

    private final BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (BluetoothDevice.ACTION_FOUND.equals(intent.getAction())) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                Log.d("bikeApp", "discovering : " + device.getName() + " mac:" + device.getAddress());
                if (device.getName() != null) {
                    ListItem item = new ListItem(device);
                    item.setItemType(BtAdapter.ITEM_SEARCHED);
                    list.add(item);
                }
                adapter.notifyDataSetChanged();
            } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(intent.getAction())) {
                actionBar.setTitle(R.string.app_name);
                item.setIcon(R.drawable.ic_baseline_bluetooth_searching_24);
                getPairedDevices();
            } else if (BluetoothDevice.ACTION_BOND_STATE_CHANGED.equals(intent.getAction())) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                if (device.getBondState() == BluetoothDevice.BOND_BONDED) {
                    getPairedDevices();
                }
            }
        }
    };
}