package com.fusionx.lightirc.util;

import org.apache.commons.lang3.StringUtils;

import android.database.Cursor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class DatabaseUtils {

    private static final String strSeparator = "__,__";

    public static String convertStringListToString(final List<String> list) {
        final StringBuilder builder = new StringBuilder();
        for (int i = 0; i < list.size(); i++) {
            builder.append(list.get(i));
            if (i < list.size() - 1) {
                builder.append(strSeparator);
            }
        }
        return builder.toString();
    }

    public static List<String> convertStringToArray(final String str) {
        if (StringUtils.isEmpty(str)) {
            return new ArrayList<>();
        }
        return Arrays.asList(str.split(strSeparator));
    }

    public static String getStringByName(final Cursor cursor, final String name) {
        return cursor.getString(cursor.getColumnIndex(name));
    }

    public static int getIntByName(final Cursor cursor, final String name) {
        return cursor.getInt(cursor.getColumnIndex(name));
    }
}