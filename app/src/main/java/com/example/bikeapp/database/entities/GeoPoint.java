package com.example.bikeapp.database.entities;

import com.j256.ormlite.field.DataType;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import java.util.Date;

//сущность для базы данных
@DatabaseTable(tableName = "geopoints")
public class GeoPoint {
    @DatabaseField(generatedId = true)
    private int id;

    @DatabaseField(dataType = DataType.STRING)
    //dd.mm.yyyy
    private String date;

    @DatabaseField(dataType = DataType.DOUBLE)
    private double latitude;

    @DatabaseField(dataType = DataType.DOUBLE)
    private double longitude;

    public GeoPoint() {
    }

    public GeoPoint(String date, double latitude, double longitude) {
        this.date = date;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public String getDate() {
        return date;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }
}
