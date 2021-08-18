package app.holoirc.model.db;

import org.apache.commons.lang3.StringUtils;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import co.fusionx.relay.base.ServerConfiguration;
import co.fusionx.relay.misc.NickStorage;

import static android.provider.BaseColumns._ID;
import static app.holoirc.util.DatabaseUtils.convertStringListToString;
import static app.holoirc.util.DatabaseUtils.convertStringToArray;
import static app.holoirc.util.DatabaseUtils.getIntByName;
import static app.holoirc.util.DatabaseUtils.getStringByName;

public class ServerDatabase extends SQLiteOpenHelper {

    // Database Name
    private static final String DATABASE_NAME = "HoloIRCDB";

    // Database Version
    private static final int DATABASE_VERSION = 1;

    private static ServerDatabase sServerDatabase;

    private final SQLiteDatabase mDatabase;

    private ServerDatabase(final Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);

        mDatabase = getWritableDatabase();
    }

    public static ServerDatabase getInstance(final Context context) {
        if (sServerDatabase == null) {
            sServerDatabase = new ServerDatabase(context);
        }
        return sServerDatabase;
    }

    private static ServerConfiguration.Builder getBuilderFromCursor(final Cursor cursor) {
        final ServerConfiguration.Builder builder = new ServerConfiguration.Builder();

        builder.setId(getIntByName(cursor, _ID));

        // Server connection
        builder.setTitle(getStringByName(cursor, DatabaseContract.ServerTable.COLUMN_TITLE));
        builder.setUrl(getStringByName(cursor, DatabaseContract.ServerTable.COLUMN_URL));
        builder.setPort(getIntByName(cursor, DatabaseContract.ServerTable.COLUMN_PORT));

        // SSL
        builder.setSsl(getIntByName(cursor, DatabaseContract.ServerTable.COLUMN_SSL) > 0);
        builder.setSslAcceptAllCertificates(getIntByName(cursor,
                DatabaseContract.ServerTable.COLUMN_SSL_ACCEPT_ALL) > 0);

        // User settings
        final String firstChoice = getStringByName(cursor, DatabaseContract.ServerTable.COLUMN_NICK_ONE);
        final String secondChoice = getStringByName(cursor, DatabaseContract.ServerTable.COLUMN_NICK_TWO);
        final String thirdChoice = getStringByName(cursor, DatabaseContract.ServerTable.COLUMN_NICK_THREE);
        final NickStorage nickStorage = new NickStorage(firstChoice, secondChoice,
                thirdChoice);
        builder.setNickStorage(nickStorage);
        builder.setRealName(getStringByName(cursor, DatabaseContract.ServerTable.COLUMN_REAL_NAME));
        builder.setNickChangeable(getIntByName(cursor, DatabaseContract.ServerTable.COLUMN_NICK_CHANGEABLE) > 0);

        // Autojoin channels
        final List<String> channels = convertStringToArray(getStringByName(cursor,
                DatabaseContract.ServerTable.COLUMN_AUTOJOIN));
        builder.getAutoJoinChannels().addAll(channels);

        // Server authorisation
        builder.setServerUserName(getStringByName(cursor, DatabaseContract.ServerTable.COLUMN_SERVER_USERNAME));
        builder.setServerPassword(getStringByName(cursor, DatabaseContract.ServerTable.COLUMN_SERVER_PASSWORD));

        // SASL authorisation
        builder.setSaslUsername(getStringByName(cursor, DatabaseContract.ServerTable.COLUMN_SASL_USERNAME));
        builder.setSaslPassword(getStringByName(cursor, DatabaseContract.ServerTable.COLUMN_SASL_PASSWORD));

        // NickServ authorisation
        builder.setNickservPassword(getStringByName(cursor, DatabaseContract.ServerTable.COLUMN_NICK_SERV_PASSWORD));

        return builder;
    }

    @Override
    public void onCreate(final SQLiteDatabase db) {
        db.execSQL(DatabaseContract.ServerTable.TABLE_CREATE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Drop older table if existed
        // db.execSQL("DROP TABLE IF EXISTS " + DatabaseContract.ServerTable.TABLE_NAME);
        // Create tables again
        // onCreate(db);
    }

    public void addServer(final ContentValues values) {
        final long id = mDatabase.insert(DatabaseContract.ServerTable.TABLE_NAME, null, values);
        values.put(_ID, (int) id);
    }

    public void addServer(final ServerConfiguration.Builder builder,
            final List<String> ignoreList) {
        final ContentValues values = getContentValuesFromBuilder(builder, false);

        if (StringUtils.isNotEmpty(builder.getTitle()) && StringUtils
                .isNotEmpty(builder.getUrl())) {
            values.put(DatabaseContract.ServerTable.COLUMN_IGNORE_LIST,
                    convertStringListToString(ignoreList));

            final int id = (int) mDatabase.insert(DatabaseContract.ServerTable.TABLE_NAME, null, values);
            builder.setId(id);
        }
    }

    public List<ServerConfiguration.Builder> getAllBuilders() {
        final List<ServerConfiguration.Builder> builders = new ArrayList<>();
        final String selectQuery = "SELECT * FROM " + DatabaseContract.ServerTable.TABLE_NAME;

        final Cursor cursor = mDatabase.rawQuery(selectQuery, null);
        if (cursor.moveToFirst()) {
            do {
                final ServerConfiguration.Builder builder = getBuilderFromCursor(cursor);
                if (StringUtils.isNotEmpty(builder.getTitle()) && StringUtils
                        .isNotEmpty(builder.getUrl())) {
                    builders.add(builder);
                } else {
                    removeServer(builder.getId());
                }
            } while (cursor.moveToNext());
        }
        return builders;
    }

    public ServerConfiguration.Builder getBuilderByName(final String serverName) {
        final Cursor cursor = mDatabase.query(DatabaseContract.ServerTable.TABLE_NAME, null,
                String.format("%s=?", DatabaseContract.ServerTable.COLUMN_TITLE), new String[]{serverName}, null, null, null);
        return cursor.moveToFirst() ? getBuilderFromCursor(cursor) : null;
    }

    public Collection<String> getIgnoreListByName(final String serverName) {
        final Cursor cursor = mDatabase.query(DatabaseContract.ServerTable.TABLE_NAME, new String[]{DatabaseContract.ServerTable.COLUMN_IGNORE_LIST},
                String.format("%s=?", DatabaseContract.ServerTable.COLUMN_TITLE), new String[]{serverName}, null, null, null);
        cursor.moveToFirst();

        return convertStringToArray(getStringByName(cursor, DatabaseContract.ServerTable.COLUMN_IGNORE_LIST));
    }

    public void updateServer(final ContentValues values) {
        final int id = values.getAsInteger(_ID);
        mDatabase.update(DatabaseContract.ServerTable.TABLE_NAME, values, _ID + "=" + id, null);
    }

    public void updateIgnoreList(final String serverName, final List<String> ignoreList) {
        final ContentValues values = new ContentValues();
        values.put(DatabaseContract.ServerTable.COLUMN_IGNORE_LIST, convertStringListToString(ignoreList));
        mDatabase.update(DatabaseContract.ServerTable.TABLE_NAME, values, DatabaseContract.ServerTable.COLUMN_TITLE + "=?", new String[]{serverName});
    }

    public ContentValues getContentValuesFromBuilder(final ServerConfiguration.Builder builder,
            final boolean id) {
        final ContentValues values = new ContentValues();

        if (id) {
            values.put(_ID, builder.getId());
        }

        // Server connection
        values.put(DatabaseContract.ServerTable.COLUMN_TITLE, builder.getTitle());
        values.put(DatabaseContract.ServerTable.COLUMN_URL, builder.getUrl());
        values.put(DatabaseContract.ServerTable.COLUMN_PORT, builder.getPort());

        // SSL
        values.put(DatabaseContract.ServerTable.COLUMN_SSL, builder.isSsl() ? 1 : 0);
        values.put(DatabaseContract.ServerTable.COLUMN_SSL_ACCEPT_ALL, builder.isSslAcceptAllCertificates() ? 1 : 0);

        // User settings
        final NickStorage storage = builder.getNickStorage();
        values.put(DatabaseContract.ServerTable.COLUMN_NICK_ONE, storage.getFirst());
        values.put(DatabaseContract.ServerTable.COLUMN_NICK_TWO, storage.getNickAtPosition(1));
        values.put(DatabaseContract.ServerTable.COLUMN_NICK_THREE, storage.getNickAtPosition(2));

        values.put(DatabaseContract.ServerTable.COLUMN_REAL_NAME, builder.getRealName());
        values.put(DatabaseContract.ServerTable.COLUMN_NICK_CHANGEABLE, builder.isNickChangeable() ? 1 : 0);

        // Autojoin channels
        values.put(DatabaseContract.ServerTable.COLUMN_AUTOJOIN, convertStringListToString(builder.getAutoJoinChannels()));

        // Server authorisation
        values.put(DatabaseContract.ServerTable.COLUMN_SERVER_USERNAME, builder.getServerUserName());
        values.put(DatabaseContract.ServerTable.COLUMN_SERVER_PASSWORD, builder.getServerPassword());

        // SASL authorisation
        values.put(DatabaseContract.ServerTable.COLUMN_SASL_USERNAME, builder.getSaslUsername());
        values.put(DatabaseContract.ServerTable.COLUMN_SASL_PASSWORD, builder.getSaslPassword());

        // NickServ authorisation
        values.put(DatabaseContract.ServerTable.COLUMN_NICK_SERV_PASSWORD, builder.getNickservPassword());

        return values;
    }

    public void removeServer(final int id) {
        mDatabase.delete(DatabaseContract.ServerTable.TABLE_NAME, _ID + "=" + id, null);
    }
}