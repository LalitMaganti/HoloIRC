package com.fusionx.lightirc.model.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class ServerDatabase extends SQLiteOpenHelper {

    // Database Name
    private static final String DATABASE_NAME = "HoloIRCDB";

    // Database Version
    private static final int DATABASE_VERSION = 1;

    public ServerDatabase(final Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(final SQLiteDatabase db) {
        db.execSQL(DatabaseContract.ServerTable.TABLE_CREATE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Drop older table if existed
        db.execSQL("DROP TABLE IF EXISTS " + DatabaseContract.ServerTable.TABLE_NAME);

        // Create tables again
        onCreate(db);
    }
}