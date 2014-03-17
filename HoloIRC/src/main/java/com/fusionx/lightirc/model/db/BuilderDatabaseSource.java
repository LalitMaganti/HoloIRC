package com.fusionx.lightirc.model.db;

import com.fusionx.relay.ServerConfiguration;
import com.fusionx.relay.misc.NickStorage;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.fusionx.lightirc.model.db.DatabaseContract.ServerTable;

public class BuilderDatabaseSource {

    public static String strSeparator = "__,__";

    private final ServerDatabase mServerDatabase;

    private SQLiteDatabase mDatabase;

    public BuilderDatabaseSource(final Context context) {
        mServerDatabase = new ServerDatabase(context);
    }

    private static String convertArrayToString(final List<String> list) {
        String str = "";
        for (int i = 0; i < list.size(); i++) {
            str = str + list.get(i);
            if (i < list.size() - 1) {
                str = str + strSeparator;
            }
        }
        return str;
    }

    private static List<String> convertStringToArray(final String str) {
        return Arrays.asList(str.split(strSeparator));
    }

    public void open() throws SQLException {
        mDatabase = mServerDatabase.getWritableDatabase();
    }

    public void close() {
        mServerDatabase.close();
        mDatabase = null;
    }

    public List<ServerConfiguration.Builder> getAllBuilders() {
        final List<ServerConfiguration.Builder> builders = new ArrayList<>();
        // Select All Query
        final String selectQuery = "SELECT  * FROM " + ServerTable.TABLE_NAME;

        final Cursor cursor = mDatabase.rawQuery(selectQuery, null);

        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {
                final ServerConfiguration.Builder builder = new ServerConfiguration.Builder();

                builder.setId(cursor.getInt(0));

                // Server connection
                builder.setTitle(cursor.getString(1));
                builder.setUrl(cursor.getString(2));
                builder.setPort(cursor.getInt(3));

                // SSL
                builder.setSsl(cursor.getInt(4) > 0);
                builder.setSslAcceptAllCertificates(cursor.getInt(5) > 0);

                // User settings
                final String firstChoice = cursor.getString(6);
                final String secondChoice = cursor.getString(7);
                final String thirdChoice = cursor.getString(8);
                final NickStorage nickStorage = new NickStorage(firstChoice, secondChoice,
                        thirdChoice);
                builder.setNickStorage(nickStorage);
                builder.setRealName(cursor.getString(9));
                builder.setNickChangeable(cursor.getInt(10) > 0);

                // Autojoin channels
                builder.getAutoJoinChannels().addAll(convertStringToArray(cursor.getString(11)));

                // Server authorisation
                builder.setServerUserName(cursor.getString(12));
                builder.setServerPassword(cursor.getString(13));

                // SASL authorisation
                builder.setSaslUsername(cursor.getString(14));
                builder.setSaslPassword(cursor.getString(15));

                // NickServ authorisation
                builder.setNickservPassword(cursor.getString(16));

                // Adding contact to list
                builders.add(builder);
            } while (cursor.moveToNext());
        }

        // return contact list
        return builders;
    }

    public void updateServer(final ContentValues values) {
        final int id = values.getAsInteger(ServerTable._ID);
        mDatabase.update(ServerTable.TABLE_NAME, values, ServerTable._ID + "=" + id, null);
    }

    public void addServer(final ServerConfiguration.Builder builder) {
        final ContentValues values = getContentValuesFromBuilder(builder, false);
        final int id = (int) mDatabase.insert(ServerTable.TABLE_NAME, null, values);
        builder.setId(id);
    }

    public ContentValues getContentValuesFromBuilder(final ServerConfiguration.Builder builder,
            final boolean id) {
        final ContentValues values = new ContentValues();

        if (id) {
            values.put(DatabaseContract.ServerTable._ID, builder.getId());
        }

        // Server connection
        values.put(ServerTable.COLUMN_TITLE, builder.getTitle());
        values.put(ServerTable.COLUMN_URL, builder.getUrl());
        values.put(ServerTable.COLUMN_PORT, builder.getPort());

        // SSL
        values.put(ServerTable.COLUMN_SSL, builder.isSsl() ? 1 : 0);
        values.put(ServerTable.COLUMN_SSL_ACCEPT_ALL, builder.isSslAcceptAllCertificates() ? 1 : 0);

        // User settings
        final NickStorage storage = builder.getNickStorage();
        values.put(ServerTable.COLUMN_NICK_ONE, storage.getFirstChoiceNick());
        values.put(ServerTable.COLUMN_NICK_TWO, storage.getSecondChoiceNick());
        values.put(ServerTable.COLUMN_NICK_THREE, storage.getThirdChoiceNick());

        values.put(ServerTable.COLUMN_REAL_NAME, builder.getRealName());
        values.put(ServerTable.COLUMN_NICK_CHANGEABLE, builder.isNickChangeable() ? 1 : 0);

        // Autojoin channels
        values.put(ServerTable.COLUMN_AUTOJOIN,
                convertArrayToString(builder.getAutoJoinChannels()));

        // Server authorisation
        values.put(ServerTable.COLUMN_SERVER_USERNAME, builder.getServerUserName());
        values.put(ServerTable.COLUMN_SERVER_PASSWORD, builder.getServerPassword());

        // SASL authorisation
        values.put(ServerTable.COLUMN_SASL_USERNAME, builder.getSaslUsername());
        values.put(ServerTable.COLUMN_SASL_PASSWORD, builder.getSaslPassword());

        // NickServ authorisation
        values.put(ServerTable.COLUMN_NICK_SERV_PASSWORD, builder.getNickservPassword());

        return values;
    }

    public void removeServer(ContentValues values) {
        final int id = values.getAsInteger(ServerTable._ID);
        mDatabase.delete(ServerTable.TABLE_NAME, ServerTable._ID + "=" + id, null);
    }
}