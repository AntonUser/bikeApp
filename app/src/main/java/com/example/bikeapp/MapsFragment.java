package com.example.bikeapp;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import com.example.bikeapp.database.HelperFactory;
import com.example.bikeapp.database.entities.GeoPoint;
import com.example.bikeapp.geoposition.MyLocationListener;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.PolylineOptions;

import java.sql.SQLException;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class MapsFragment extends Fragment implements OnMapReadyCallback {
    private PolylineOptions line;
    private GoogleMap googleMap;
    private boolean isStarted = true;

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        isStarted = true;
        View view = inflater.inflate(R.layout.fragment_maps, container, false);
        SupportMapFragment mapFragment =
                (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        this.googleMap = googleMap;
        googleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        googleMap.setOnMyLocationChangeListener(onMyLocationChangeListener);
        line = new PolylineOptions();
        List<GeoPoint> geoPoints = null;
        try {
            geoPoints = HelperFactory.getHelper().getGeoPointDAO().getPointsByDate();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        assert geoPoints != null;
        geoPoints.forEach(element -> line.add(new LatLng(element.getLatitude(),
                element.getLongitude())));
        if (ActivityCompat.checkSelfPermission(getActivity(),
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.checkSelfPermission(getActivity(),
                    Manifest.permission.ACCESS_COARSE_LOCATION);
        }

        line.width(16f).color(R.color.design_default_color_primary_dark);
        googleMap.setMyLocationEnabled(true);
        //Добавляем линию на карту
        googleMap.addPolyline(line);
    }

    GoogleMap.OnMyLocationChangeListener onMyLocationChangeListener =//обработчик перемещений гугл
            // использует также личный  класс MyLocationListener, местоположение берётся из не
            location ->
            {
                double latitude = -91;
                double longitude = -181;
                float[] results = new float[1];
                if (isStarted)
                    googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(
                            new LatLng(location.getLatitude(), location.getLongitude()), 18));
                Location.distanceBetween(MyLocationListener.imHere.getLatitude(), MyLocationListener.imHere.getLongitude(), latitude, longitude, results);
                if (results[0] > (MyLocationListener.imHere.getAccuracy() )) {
                    if (MyLocationListener.imHere != null) {
                        Log.d("TAG", "GPS is on");
                        latitude = location.getLatitude();
                        longitude = location.getLongitude();
                    } else {
                        MyLocationListener.setUpLocationListener(this.getActivity());
                    }

                    Log.d("bikeApp", "new location lat " + location.getLatitude() +
                            " lon " + location.getLongitude());

                    Date date = Calendar.getInstance().getTime();//текущая дата

                    String dateStr = date.getDate() + "."
                            + date.getMonth() + "." + (date.getYear() + 1900);//дата "dd.mm.yyyy"


                    try {
                        HelperFactory.getHelper().getGeoPointDAO().create(
                                new GeoPoint(dateStr, latitude, longitude)
                        );
                    } catch (SQLException throwables) {
                        Log.e("bikeApp", "error create geopoint. " + throwables.getMessage());
                        throwables.printStackTrace();
                    }

                    if (latitude > -91 || longitude > -181) {
                        line.add(new com.google.android.gms.maps.model.LatLng(latitude, longitude));
                    }

                    googleMap.addPolyline(line);
                }
                isStarted = false;// делается чтобы камера не обнавлялась в дальнейшем, а лишь при первом запуске активити
            };
}
