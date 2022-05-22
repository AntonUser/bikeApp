package com.example.bikeapp;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.NavigationUI;

import com.example.bikeapp.adapter.Constants;
import com.example.bikeapp.database.HelperFactory;
import com.example.bikeapp.database.entities.GeoPoint;
import com.example.bikeapp.geoposition.LocationLiveData;
import com.example.bikeapp.geoposition.MyLocationListener;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.sql.SQLException;
import java.util.Calendar;
import java.util.Date;

public class MainActivity extends AppCompatActivity {
    private final String TAG = "bikeApp";
    private BluetoothAdapter bluetoothAdapter;
    private SharedPreferences preference;
    private ActivityResultLauncher<Intent> mStartForResult = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
            result -> {
            });
    private SharedPreferences.Editor edit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EventBus.getDefault().register(this);
        edit = this.getSharedPreferences("tokens", Context.MODE_PRIVATE).edit();
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
        if (ActivityCompat.checkSelfPermission(this,//запрашиваем разрешение на геолокацию
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this,
                        Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION}, 23);
        }
        setContentView(R.layout.activity_main);
        setUpNavigation();
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        preference = getSharedPreferences(Constants.MY_PREFERENCE, Context.MODE_PRIVATE);
        Log.d(TAG, "Mac : " + preference.getString(Constants.MAC_KEY, "no bt selected"));
        HelperFactory.setHelper(getApplicationContext());
        if (bluetoothAdapter == null) {
            Log.d(TAG, "Устройство не поддерживает bluetooth");
        }

        final Context mainContext = this;
        MyLocationListener.setUpLocationListener(mainContext);//.start();//запускаем обработку местоположений
    }

    @Override
    protected void onResume() {
        super.onResume();
        MyLocationListener.setUpLocationListener(this);
        new LocationLiveData(this).observe(this, location -> {
            Date date = Calendar.getInstance().getTime();//текущая дата
            String dateStr = date.getDate() + "."
                    + date.getMonth() + "." + (date.getYear() + 1900);//дата "dd.mm.yyyy"
            try {
                HelperFactory.getHelper().getGeoPointDAO().create(
                        new GeoPoint(dateStr,
                                MyLocationListener.imHere.getLatitude(),
                                MyLocationListener.imHere.getLongitude())
                );
            } catch (SQLException throwables) {
                Log.e("bikeApp", "error create geopoint. " + throwables.getMessage());
                throwables.printStackTrace();
            }
        });
    }

    @Override
    protected void onDestroy() {
        HelperFactory.releaseHelper();
        EventBus.getDefault().unregister(this);
        super.onDestroy();
    }

    @Override
    protected void onStart() {
        if (!bluetoothAdapter.isEnabled()) {
            Log.d(TAG, "Запрос включения bluetooth");
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            mStartForResult.launch(enableBtIntent);
        }
        super.onStart();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.bt_menu_button) {
            if (bluetoothAdapter.isEnabled()) {
                Intent i = new Intent(MainActivity.this, BtListActivity.class);
                startActivity(i);
            } else {
                Toast.makeText(MainActivity.this, "Пожалуйста включите блютуз для перехода в список устройств", Toast.LENGTH_SHORT).show();
            }
        }
        return super.onOptionsItemSelected(item);
    }

    //навигация внизу активити
    private void setUpNavigation() {
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigation);
        bottomNavigationView.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), android.R.color.transparent));
        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager()
                .findFragmentById(R.id.fragment);
        if (navHostFragment == null) return;
        NavigationUI.setupWithNavController(bottomNavigationView,
                navHostFragment.getNavController());
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}

