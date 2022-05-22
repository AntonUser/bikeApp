package com.example.bikeapp;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
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

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.IOException;
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
    }

    @Override
    protected void onPause() {
        super.onPause();
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
}

