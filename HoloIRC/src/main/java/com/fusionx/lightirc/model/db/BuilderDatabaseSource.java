package com.fusionx.lightirc.model.db;

import com.fusionx.relay.ServerConfiguration;
import com.fusionx.relay.misc.NickStorage;

import org.apache.commons.lang3.StringUtils;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static com.fusionx.lightirc.model.db.DatabaseContract.ServerTable.COLUMN_AUTOJOIN;
import static com.fusionx.lightirc.model.db.DatabaseContract.ServerTable.COLUMN_IGNORE_LIST;
import static com.fusionx.lightirc.model.db.DatabaseContract.ServerTable.COLUMN_NICK_CHANGEABLE;
import static com.fusionx.lightirc.model.db.DatabaseContract.ServerTable.COLUMN_NICK_ONE;
import static com.fusionx.lightirc.model.db.DatabaseContract.ServerTable.COLUMN_NICK_SERV_PASSWORD;
import static com.fusionx.lightirc.model.db.DatabaseContract.ServerTable.COLUMN_NICK_THREE;
import static com.fusionx.lightirc.model.db.DatabaseContract.ServerTable.COLUMN_NICK_TWO;
import static com.fusionx.lightirc.model.db.DatabaseContract.ServerTable.COLUMN_PORT;
import static com.fusionx.lightirc.model.db.DatabaseContract.ServerTable.COLUMN_REAL_NAME;
import static com.fusionx.lightirc.model.db.DatabaseContract.ServerTable.COLUMN_SASL_PASSWORD;
import static com.fusionx.lightirc.model.db.DatabaseContract.ServerTable.COLUMN_SASL_USERNAME;
import static com.fusionx.lightirc.model.db.DatabaseContract.ServerTable.COLUMN_SERVER_PASSWORD;
import static com.fusionx.lightirc.model.db.DatabaseContract.ServerTable.COLUMN_SERVER_USERNAME;
import static com.fusionx.lightirc.model.db.DatabaseContract.ServerTable.COLUMN_SSL;
import static com.fusionx.lightirc.model.db.DatabaseContract.ServerTable.COLUMN_SSL_ACCEPT_ALL;
import static com.fusionx.lightirc.model.db.DatabaseContract.ServerTable.COLUMN_TITLE;
import static com.fusionx.lightirc.model.db.DatabaseContract.ServerTable.COLUMN_URL;
import static com.fusionx.lightirc.model.db.DatabaseContract.ServerTable.TABLE_NAME;
import static com.fusionx.lightirc.model.db.DatabaseContract.ServerTable._ID;
import static com.fusionx.lightirc.util.DatabaseUtils.convertStringListToString;
import static com.fusionx.lightirc.util.DatabaseUtils.convertStringToArray;
import static com.fusionx.lightirc.util.DatabaseUtils.getIntByName;
import static com.fusionx.lightirc.util.DatabaseUtils.getStringByName;

public class BuilderDatabaseSource {

    private final ServerDatabase mServerDatabase;

    private SQLiteDatabase mDatabase;

    public BuilderDatabaseSource(final Context context) {
        mServerDatabase = new ServerDatabase(context);
    }

    private static ServerConfiguration.Builder getBuilderFromCursor(final Cursor cursor) {
        final ServerConfiguration.Builder builder = new ServerConfiguration.Builder();

        builder.setId(getIntByName(cursor, _ID));

        // Server connection
        builder.setTitle(getStringByName(cursor, COLUMN_TITLE));
        builder.setUrl(getStringByName(cursor, COLUMN_URL));
        builder.setPort(getIntByName(cursor, COLUMN_PORT));

        // SSL
        builder.setSsl(getIntByName(cursor, COLUMN_SSL) > 0);
        builder.setSslAcceptAllCertificates(getIntByName(cursor,
                COLUMN_SSL_ACCEPT_ALL) > 0);

        // User settings
        final String firstChoice = getStringByName(cursor, COLUMN_NICK_ONE);
        final String secondChoice = getStringByName(cursor, COLUMN_NICK_TWO);
        final String thirdChoice = getStringByName(cursor, COLUMN_NICK_THREE);
        final NickStorage nickStorage = new NickStorage(firstChoice, secondChoice,
                thirdChoice);
        builder.setNickStorage(nickStorage);
        builder.setRealName(getStringByName(cursor, COLUMN_REAL_NAME));
        builder.setNickChangeable(getIntByName(cursor, COLUMN_NICK_CHANGEABLE) > 0);

        // Autojoin channels
        final List<String> channels = convertStringToArray(getStringByName(cursor,
                COLUMN_AUTOJOIN));
        builder.getAutoJoinChannels().addAll(channels);

        // Server authorisation
        builder.setServerUserName(getStringByName(cursor, COLUMN_SERVER_USERNAME));
        builder.setServerPassword(getStringByName(cursor, COLUMN_SERVER_PASSWORD));

        // SASL authorisation
        builder.setSaslUsername(getStringByName(cursor, COLUMN_SASL_USERNAME));
        builder.setSaslPassword(getStringByName(cursor, COLUMN_SASL_PASSWORD));

        // NickServ authorisation
        builder.setNickservPassword(getStringByName(cursor, COLUMN_NICK_SERV_PASSWORD));

        return builder;
    }

    public BuilderDatabaseSource open() throws SQLException {
        mDatabase = mServerDatabase.getWritableDatabase();
        return this;
    }

    public void close() {
        mServerDatabase.close();
        mDatabase = null;
    }

    public void addServer(final ContentValues values) {
        final long id = mDatabase.insert(TABLE_NAME, null, values);
        values.put(_ID, (int) id);
    }

    public void addServer(final ServerConfiguration.Builder builder,
            final List<String> ignoreList) {
        final ContentValues values = getContentValuesFromBuilder(builder, false);

        if (StringUtils.isNotEmpty(builder.getTitle()) && StringUtils
                .isNotEmpty(builder.getUrl())) {
            values.put(DatabaseContract.ServerTable.COLUMN_IGNORE_LIST,
                    convertStringListToString(ignoreList));

            final int id = (int) mDatabase.insert(TABLE_NAME, null, values);
            builder.setId(id);
        }
    }

    public List<ServerConfiguration.Builder> getAllBuilders() {
        final List<ServerConfiguration.Builder> builders = new ArrayList<>();
        final String selectQuery = "SELECT * FROM " + TABLE_NAME;

        final Cursor cursor = mDatabase.rawQuery(selectQuery, null);
        if (cursor.moveToFirst()) {
            do {
                final ServerConfiguration.Builder builder = getBuilderFromCursor(cursor);
                builders.add(builder);
            } while (cursor.moveToNext());
        }
        return builders;
    }

    public ServerConfiguration.Builder getBuilderByName(final String serverName) {
        final Cursor cursor = mDatabase.query(TABLE_NAME, null,
                String.format("%s=?", COLUMN_TITLE), new String[]{serverName}, null, null, null);
        return cursor.moveToFirst() ? getBuilderFromCursor(cursor) : null;
    }

    public Collection<String> getIgnoreListByName(final String serverName) {
        final Cursor cursor = mDatabase.query(TABLE_NAME, new String[]{COLUMN_IGNORE_LIST},
                String.format("%s=?", COLUMN_TITLE), new String[]{serverName}, null, null, null);
        cursor.moveToFirst();

        return convertStringToArray(getStringByName(cursor, COLUMN_IGNORE_LIST));
    }

    public void updateServer(final ContentValues values) {
        final int id = values.getAsInteger(_ID);
        mDatabase.update(TABLE_NAME, values, _ID + "=" + id, null);
    }

    public void updateIgnoreList(final String serverName, final List<String> ignoreList) {
        final ContentValues values = new ContentValues();
        values.put(COLUMN_IGNORE_LIST, convertStringListToString(ignoreList));
        mDatabase.update(TABLE_NAME, values, COLUMN_TITLE + "=?", new String[]{serverName});
    }

    public ContentValues getContentValuesFromBuilder(final ServerConfiguration.Builder builder,
            final boolean id) {
        final ContentValues values = new ContentValues();

        if (id) {
            values.put(_ID, builder.getId());
        }

        // Server connection
        values.put(COLUMN_TITLE, builder.getTitle());
        values.put(COLUMN_URL, builder.getUrl());
        values.put(COLUMN_PORT, builder.getPort());

        // SSL
        values.put(COLUMN_SSL, builder.isSsl() ? 1 : 0);
        values.put(COLUMN_SSL_ACCEPT_ALL, builder.isSslAcceptAllCertificates() ? 1 : 0);

        // User settings
        final NickStorage storage = builder.getNickStorage();
        values.put(COLUMN_NICK_ONE, storage.getFirstChoiceNick());
        values.put(COLUMN_NICK_TWO, storage.getSecondChoiceNick());
        values.put(COLUMN_NICK_THREE, storage.getThirdChoiceNick());

        values.put(COLUMN_REAL_NAME, builder.getRealName());
        values.put(COLUMN_NICK_CHANGEABLE, builder.isNickChangeable() ? 1 : 0);

        // Autojoin channels
        values.put(COLUMN_AUTOJOIN, convertStringListToString(builder.getAutoJoinChannels()));

        // Server authorisation
        values.put(COLUMN_SERVER_USERNAME, builder.getServerUserName());
        values.put(COLUMN_SERVER_PASSWORD, builder.getServerPassword());

        // SASL authorisation
        values.put(COLUMN_SASL_USERNAME, builder.getSaslUsername());
        values.put(COLUMN_SASL_PASSWORD, builder.getSaslPassword());

        // NickServ authorisation
        values.put(COLUMN_NICK_SERV_PASSWORD, builder.getNickservPassword());

        return values;
    }

    public void removeServer(final int id) {
        mDatabase.delete(TABLE_NAME, _ID + "=" + id, null);
    }
}