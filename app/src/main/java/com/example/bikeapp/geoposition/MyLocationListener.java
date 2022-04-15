package com.example.bikeapp.geoposition;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;

public class MyLocationListener implements LocationListener {

    public static Location imHere;

    public static void SetUpLocationListener(Context context) // это нужно запустить в самом начале работы программы
    {
        LocationManager locationManager = (LocationManager)
                context.getSystemService(Context.LOCATION_SERVICE);

        LocationListener locationListener = new MyLocationListener();

        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)//доработать вывод сообщения
                != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        locationManager.requestLocationUpdates(
                LocationManager.GPS_PROVIDER, 5000, 10, locationListener); // здесь можно указать другие более подходящие вам параметры

        imHere = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
    }

    @Override
    public void onLocationChanged(@NonNull Location location) {
        imHere = location;
        Log.d("bikeApp", "my location lat " + location.getLatitude() +
                " lon " + location.getLongitude());
    }
}
