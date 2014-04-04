package com.fusionx.lightirc.util;

import android.database.Cursor;

public class DatabaseUtils {


    public static String getStringByName(final Cursor cursor, final String name) {
        return cursor.getString(cursor.getColumnIndex(name));
    }

    public static int getIntByName(final Cursor cursor, final String name) {
        return cursor.getInt(cursor.getColumnIndex(name));
    }
}