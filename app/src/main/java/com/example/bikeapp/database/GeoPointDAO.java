package com.example.bikeapp.database;

import com.example.bikeapp.database.entities.GeoPoint;
import com.j256.ormlite.dao.BaseDaoImpl;
import com.j256.ormlite.support.ConnectionSource;

import java.sql.SQLException;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class GeoPointDAO extends BaseDaoImpl<GeoPoint, Integer> {
    protected GeoPointDAO(ConnectionSource connectionSource, Class<GeoPoint> dataClass) throws SQLException {
        super(connectionSource, dataClass);
    }

    public List<GeoPoint> getAllPoints() throws SQLException {
        return this.queryForAll();
    }

    public List<GeoPoint> getPointsByDate() throws SQLException {
        Date date = Calendar.getInstance().getTime();
        String dateStr = date.getDate() + "." + date.getMonth() + "." + (date.getYear() + 1900);
        return this.queryForEq("date", dateStr);

    }
}
