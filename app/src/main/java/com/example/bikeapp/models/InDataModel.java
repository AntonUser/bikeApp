package com.example.bikeapp.models;

import java.util.Objects;

public class InDataModel {
    private int batteryСharge;
    private boolean isError;

    public InDataModel(int batteryСharge, boolean isError) {
        this.batteryСharge = batteryСharge;
        this.isError = isError;
    }

    public int getBatteryСharge() {
        return batteryСharge;
    }

    public void setBatteryСharge(int batteryСharge) {
        this.batteryСharge = batteryСharge;
    }

    public boolean isError() {
        return isError;
    }

    public void setError(boolean error) {
        isError = error;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        InDataModel that = (InDataModel) o;
        return batteryСharge == that.batteryСharge &&
                isError == that.isError;
    }

    @Override
    public int hashCode() {
        return Objects.hash(batteryСharge, isError);
    }
}
