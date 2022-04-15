package com.example.bikeapp.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.example.bikeapp.database.entities.GeoPoint;
import com.j256.ormlite.android.apptools.OrmLiteSqliteOpenHelper;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;

import java.sql.SQLException;

public class DatabaseHelper extends OrmLiteSqliteOpenHelper {
    //имя файла базы данных который будет храниться в /data/data/APPNAME/DATABASE_NAME.db
    private static final String DATABASE_NAME = "bikeapp.db";

    //с каждым увеличением версии, при нахождении в устройстве БД с предыдущей версией будет выполнен метод onUpgrade();
    private static final int DATABASE_VERSION = 1;

    private GeoPointDAO geoPointDAO = null;

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase database, ConnectionSource connectionSource) {
        try {
            TableUtils.createTable(connectionSource, GeoPoint.class);
        } catch (SQLException throwables) {
            Log.e("bikeApp", "error creating DB " + DATABASE_NAME);
            throwables.printStackTrace();
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase database, ConnectionSource connectionSource, int oldVersion, int newVersion) {

    }

    public GeoPointDAO getGeoPointDAO() throws SQLException {
        if (geoPointDAO == null) {
            geoPointDAO = new GeoPointDAO(connectionSource, GeoPoint.class);
        }
        return geoPointDAO;
    }

    @Override
    public void close() {
        super.close();
        geoPointDAO = null;
    }
}
