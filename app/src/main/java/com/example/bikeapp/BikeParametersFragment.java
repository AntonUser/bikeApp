package com.example.bikeapp;

import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.LiveData;

import com.example.bikeapp.bluetooth.BtConnection;
import com.example.bikeapp.geoposition.LocationLiveData;
import com.example.bikeapp.geoposition.MyLocationListener;

public class BikeParametersFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private ProgressBar progressBar;
    private BtConnection btConnection;
    private MyLocationListener myLocationListener;
    private LiveData<Integer> liveData;
    private TextView speedView;
    private LocationLiveData locationLiveData;

    public BikeParametersFragment() {
        // Required empty public constructor
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        btConnection = BtConnection.createBtConnection(getActivity());

        locationLiveData = new LocationLiveData(getActivity());

        locationLiveData.observe(this, location -> {
            //переводим полученную скорость из м/с в км/ч
            long speedKmH = Math.round(location.getSpeed() * 3.6);

            Log.d("bikeApp", "BikeParamSpeed:" + speedKmH);

            speedView.setText(String.valueOf(speedKmH));
        });
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_bike_parameters, container, false);
        progressBar = view.findViewById(R.id.progressBar2);
        progressBar.setMax(100);
        progressBar.incrementProgressBy(1);
        progressBar.setProgress(20);
        MyLocationListener.setUpLocationListener(this.getActivity());
        myLocationListener = new MyLocationListener();
        speedView = view.findViewById(R.id.speedView);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

    }
}
