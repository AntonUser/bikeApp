package com.example.bikeapp;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.InputDevice;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import com.example.bikeapp.database.HelperFactory;

import org.osmdroid.api.IGeoPoint;
import org.osmdroid.api.IMapController;
import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.ITileSource;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Polyline;
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider;
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay;

import java.sql.SQLException;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class MapsFragment extends Fragment {
    private final int REQUEST_PERMISSIONS_REQUEST_CODE = 1;
    private MapView mMapView;
    private MyLocationNewOverlay myLocationNewOverlay;
    private LocationManager locationManager;
    private Polyline polyline;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mMapView = new MapView(inflater.getContext());
        mMapView.setDestroyMode(false);
        mMapView.setTag("mapView"); // needed for OpenStreetMapViewTest
        mMapView.setOnGenericMotionListener(new View.OnGenericMotionListener() {
            @Override
            public boolean onGenericMotion(View v, MotionEvent event) {
                if (0 != (event.getSource() & InputDevice.SOURCE_CLASS_POINTER)) {
                    switch (event.getAction()) {
                        case MotionEvent.ACTION_SCROLL:
                            if (event.getAxisValue(MotionEvent.AXIS_VSCROLL) < 0.0f)
                                mMapView.getController().zoomOut();
                            else {
                                IGeoPoint iGeoPoint = mMapView.getProjection().fromPixels((int) event.getX(), (int) event.getY());
                                mMapView.getController().animateTo(iGeoPoint);
                                mMapView.getController().zoomIn();
                            }
                            return true;
                    }
                }
                return false;
            }
        });
        final ITileSource tileSource = TileSourceFactory.MAPNIK;
        mMapView.setTileSource(tileSource);
        Configuration.getInstance().setUserAgentValue("bikeApp");
        mMapView.setMultiTouchControls(true);

        IMapController mapController = mMapView.getController();
        mapController.setZoom(12);
        GeoPoint startPoint = new GeoPoint(54.99244, 73.36859);
        mapController.setCenter(startPoint);

        this.myLocationNewOverlay = new MyLocationNewOverlay(new GpsMyLocationProvider(this.getActivity()), mMapView);
        this.myLocationNewOverlay.enableMyLocation();
        mMapView.getOverlays().add(this.myLocationNewOverlay);
        myLocationNewOverlay.onLocationChanged(myLocationNewOverlay.getLastFix(), myLocationNewOverlay.getMyLocationProvider());
        myLocationNewOverlay.getMyLocationProvider();
        locationManager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);

        polyline = new Polyline();
        List<com.example.bikeapp.database.entities.GeoPoint> geoPoints = null;
        try {
            geoPoints = HelperFactory.getHelper().getGeoPointDAO().getPointsByDate();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        geoPoints.forEach(element -> polyline.addPoint(new GeoPoint(element.getLatitude(), element.getLongitude())));
        if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getActivity(),
                Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
        }
        polyline.setWidth(16f);
        mMapView.getOverlayManager().add(polyline);

        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 4000, 15, location -> {
            mMapView.getController().setCenter(new GeoPoint(location.getLatitude(), location.getLongitude()));
            Log.d("bikeApp", "ok");
            double latitude = location.getLatitude();
            double longitude = location.getLongitude();
            Date date = Calendar.getInstance().getTime();//текущая дата

            String dateStr = date.getDate() + "."
                    + date.getMonth() + "." + (date.getYear() + 1900);//дата "dd.mm.yyyy"

            try {
                HelperFactory.getHelper().getGeoPointDAO().create(
                        new com.example.bikeapp.database.entities.GeoPoint(dateStr, latitude, longitude)
                );
            } catch (SQLException throwables) {
                Log.e("bikeApp", "error create geopoint. " + throwables.getMessage());
                throwables.printStackTrace();
            }

            polyline.addPoint(new GeoPoint(latitude, longitude));

            mMapView.invalidate();
        });

        return mMapView;
    }
}