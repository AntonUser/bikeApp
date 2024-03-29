package com.example.bikeapp;

import android.content.Context;
import android.content.SharedPreferences;
import android.location.Location;
import android.media.SoundPool;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.widget.SwitchCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.LiveData;

import com.example.bikeapp.bluetooth.BtConnection;
import com.example.bikeapp.geoposition.LocationLiveData;
import com.example.bikeapp.geoposition.MyLocationListener;
import com.example.bikeapp.models.InDataModel;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

public class BikeParametersFragment extends Fragment {
    private ProgressBar progressBar;
    private MyLocationListener myLocationListener;
    private LiveData<Integer> liveData;
    private TextView speedView;
    private TextView kilometrageView;
    private LocationLiveData locationLiveData;
    private SwitchCompat switchPower;
    private SharedPreferences sharedPreferences;
    private SharedPreferences sharedPreferencesPowerStatus;
    private SharedPreferences.Editor editPower;
    private Location prevLocation;
    private TextView percentChargeBattarey;
    private BtConnection btConnection;
    private boolean isActive = true;

    public BikeParametersFragment() {
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Для новых устройств
        btConnection = BtConnection.createBtConnection(getActivity());
        locationLiveData = new LocationLiveData(getActivity());
        sharedPreferences = getActivity().getSharedPreferences(Constants.ODO_PREFERENCE, Context.MODE_PRIVATE);
        sharedPreferencesPowerStatus = getActivity().getSharedPreferences(Constants.POWER_PREFERENCE, Context.MODE_PRIVATE);
        SharedPreferences.Editor edit = sharedPreferences.edit();
        editPower = sharedPreferencesPowerStatus.edit();
        locationLiveData.observe(this, location -> {

            //переводим полученную скорость из м/с в км/ч
            long speedKmH = Math.round(location.getSpeed() * 3.6);

            speedView.setText(String.valueOf(speedKmH));

            if (switchPower.isChecked()) {
                if (prevLocation == null) {
                    prevLocation = location;
                }
                float currentKilometrage = sharedPreferences.getFloat(Constants.ODO_PREFERENCE, 0);
                float distance = location.distanceTo(prevLocation);
                if (distance > 1) {
                    currentKilometrage += distance;
                }
                //храним в метрах
                edit.putFloat(Constants.ODO_PREFERENCE, currentKilometrage);
                edit.apply();
                String kilometrage = String.valueOf((int) (currentKilometrage / 1000));
                String outData = "000000";
                outData = outData.substring(kilometrage.length()) + kilometrage;
                kilometrageView.setText(outData);
                prevLocation = location;
            }
        });
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_bike_parameters, container, false);
        switchPower = view.findViewById(R.id.power_bike);
        progressBar = view.findViewById(R.id.progressBar2);
        percentChargeBattarey = view.findViewById(R.id.chargePercentBattarey);
        progressBar.setMax(100);
        progressBar.incrementProgressBy(1);
        MyLocationListener.setUpLocationListener(this.getActivity());
        myLocationListener = new MyLocationListener();
        speedView = view.findViewById(R.id.speedView);
        kilometrageView = view.findViewById(R.id.kilometrage);

        float currentKilometrage = sharedPreferences.getFloat(Constants.ODO_PREFERENCE, 0);
        String kilometrage = String.valueOf((int) (currentKilometrage / 1000));
        String outData = "000000";
        outData = outData.substring(kilometrage.length()) + kilometrage;
        kilometrageView.setText(outData);

        switchPower.setOnClickListener(v -> {
            editPower.putBoolean(Constants.POWER_PREFERENCE, switchPower.isChecked());
            editPower.apply();
            if (this.isActive) {
                if (switchPower.isChecked()) {
                    view.setBackgroundResource(R.drawable.backgrond2);
                    try {
                        btConnection.sendData("1");
                    } catch (NullPointerException ex) {
                        Toast.makeText(getActivity(), "Подключитесь к электроскутеру", Toast.LENGTH_SHORT).show();
                        view.setBackgroundResource(R.drawable.backgrond1);
                        switchPower.setChecked(false);
                    }
                } else {
                    view.setBackgroundResource(R.drawable.backgrond1);
                    try {
                        btConnection.sendData("0");
                    } catch (NullPointerException ex) {
                        Toast.makeText(getActivity(), "Подключитесь к электроскутеру", Toast.LENGTH_SHORT).show();
                    }
                }
            } else {
                Toast.makeText(getActivity(), "Подключитесь к электроскутеру", Toast.LENGTH_SHORT).show();
                switchPower.setChecked(false);
            }
        });

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

    }

    @Override
    public void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
        boolean status = sharedPreferencesPowerStatus.getBoolean(Constants.POWER_PREFERENCE, false);
        switchPower.setChecked(status);
        if (status) {
            this.getView().setBackgroundResource(R.drawable.backgrond2);
        } else {
            this.getView().setBackgroundResource(R.drawable.backgrond1);
        }
    }

    @Override
    public void onStop() {
        EventBus.getDefault().unregister(this);
        editPower.putBoolean(Constants.POWER_PREFERENCE, switchPower.isChecked());
        editPower.apply();
        super.onStop();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(InDataModel inData) {
        progressBar.setProgress(inData.getBatteryСharge());
        String mes = String.valueOf(inData.getBatteryСharge()) + "%";
        percentChargeBattarey.setText(mes);
        Log.d("bileApp", String.valueOf(inData.getBatteryСharge()));

        if (switchPower.isChecked() && inData.isError()) {
            switchPower.performClick();
            this.isActive = false;
        }
        if (!inData.isError()) {
            this.isActive = true;
        }

//        switchPower.setChecked(false);
//        editPower.putBoolean(Constants.POWER_PREFERENCE, false);
//        editPower.apply();
    }
}
