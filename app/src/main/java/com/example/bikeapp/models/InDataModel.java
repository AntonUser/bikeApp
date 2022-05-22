package com.example.bikeapp.models;

import java.util.Objects;

public class InDataModel {
    private int batteryСharge;

    public InDataModel(int batteryСharge) {
        this.batteryСharge = batteryСharge;
    }

    public int getBatteryСharge() {
        return batteryСharge;
    }

    public void setBatteryСharge(int batteryСharge) {
        this.batteryСharge = batteryСharge;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        InDataModel that = (InDataModel) o;
        return batteryСharge == that.batteryСharge;
    }

    @Override
    public int hashCode() {
        return Objects.hash(batteryСharge);
    }
}
