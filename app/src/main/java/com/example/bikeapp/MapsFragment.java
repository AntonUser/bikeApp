package com.example.bikeapp;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
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
import com.google.android.gms.maps.model.PolylineOptions;

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
            /**
             * mouse wheel zooming ftw
             * http://stackoverflow.com/questions/11024809/how-can-my-view-respond-to-a-mousewheel
             *
             * @param v
             * @param event
             * @return
             */
            @Override
            public boolean onGenericMotion(View v, MotionEvent event) {
                if (0 != (event.getSource() & InputDevice.SOURCE_CLASS_POINTER)) {
                    switch (event.getAction()) {
                        case MotionEvent.ACTION_SCROLL:
                            if (event.getAxisValue(MotionEvent.AXIS_VSCROLL) < 0.0f)
                                mMapView.getController().zoomOut();
                            else {
                                //this part just centers the map on the current mouse location before the zoom action occurs
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

//    private PolylineOptions line;
//    private GoogleMap googleMap;
//    private boolean isStarted = true;
//
//    @RequiresApi(api = Build.VERSION_CODES.N)
//    @Nullable
//    @Override
//    public View onCreateView(@NonNull LayoutInflater inflater,
//                             @Nullable ViewGroup container,
//                             @Nullable Bundle savedInstanceState) {
//        isStarted = true;
//        View view = inflater.inflate(R.layout.fragment_maps, container, false);
//        SupportMapFragment mapFragment =
//                (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
//        if (mapFragment != null) {
//            mapFragment.getMapAsync(this);
//        }
//        return view;
//    }
//
//    @Override
//    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
//        super.onViewCreated(view, savedInstanceState);
//    }
//
//    @RequiresApi(api = Build.VERSION_CODES.N)
//    @Override
//    public void onMapReady(@NonNull GoogleMap googleMap) {
//        this.googleMap = googleMap;
//        googleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
//        googleMap.setOnMyLocationChangeListener(onMyLocationChangeListener);
//        line = new PolylineOptions();
//        List<GeoPoint> geoPoints = null;
//        try {
//            geoPoints = HelperFactory.getHelper().getGeoPointDAO().getPointsByDate();
//        } catch (SQLException throwables) {
//            throwables.printStackTrace();
//        }
//        assert geoPoints != null;
//        geoPoints.forEach(element -> line.add(new LatLng(element.getLatitude(),
//                element.getLongitude())));
//        if (ActivityCompat.checkSelfPermission(getActivity(),
//                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
//            ActivityCompat.checkSelfPermission(getActivity(),
//                    Manifest.permission.ACCESS_COARSE_LOCATION);
//        }
//
//        line.width(16f).color(R.color.design_default_color_primary_dark);
//        googleMap.setMyLocationEnabled(true);
//        //Добавляем линию на карту
//        googleMap.addPolyline(line);
}

//    GoogleMap.OnMyLocationChangeListener onMyLocationChangeListener =//обработчик перемещений гугл
//            // использует также личный  класс MyLocationListener, местоположение берётся из не
//            location ->
//            {
//                double latitude = -91;
//                double longitude = -181;
//                float[] results = new float[1];
//                if (isStarted)
//                    googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(
//                            new LatLng(location.getLatitude(), location.getLongitude()), 18));
//                Location.distanceBetween(MyLocationListener.imHere.getLatitude(), MyLocationListener.imHere.getLongitude(), latitude, longitude, results);
//                if (results[0] > (MyLocationListener.imHere.getAccuracy())) {
//                    if (MyLocationListener.imHere != null) {
//                        Log.d("TAG", "GPS is on");
//                        latitude = location.getLatitude();
//                        longitude = location.getLongitude();
//                    } else {
//                        MyLocationListener.setUpLocationListener(this.getActivity());
//                    }
//
//                    Log.d("bikeApp", "new location lat " + location.getLatitude() +
//                            " lon " + location.getLongitude());
//
//                    Date date = Calendar.getInstance().getTime();//текущая дата
//
//                    String dateStr = date.getDate() + "."
//                            + date.getMonth() + "." + (date.getYear() + 1900);//дата "dd.mm.yyyy"
//
//
//                    try {
//                        HelperFactory.getHelper().getGeoPointDAO().create(
//                                new GeoPoint(dateStr, latitude, longitude)
//                        );
//                    } catch (SQLException throwables) {
//                        Log.e("bikeApp", "error create geopoint. " + throwables.getMessage());
//                        throwables.printStackTrace();
//                    }
//
//                    if (latitude > -91 || longitude > -181) {
//                        line.add(new com.google.android.gms.maps.model.LatLng(latitude, longitude));
//                    }
//
//                    googleMap.addPolyline(line);
//                }
//                isStarted = false;// делается чтобы камера не обнавлялась в дальнейшем, а лишь при первом запуске активити
//            };
//}
