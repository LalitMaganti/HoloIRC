package com.fusionx.lightirc.model.db;

import android.provider.BaseColumns;

public final class DatabaseContract {

    // To prevent someone from accidentally instantiating the contract class,
    // give it an empty constructor.
    public DatabaseContract() {
    }

    /* Inner class that defines the server table contents */
    public static abstract class ServerTable implements BaseColumns {

        public static final String TABLE_NAME = "servers";

        // Common columns
        public static final String COLUMN_ID = "_id";

        public static final String COLUMN_TITLE = "title";

        public static final String COLUMN_URL = "url";

        public static final String COLUMN_PORT = "port";

        public static final String COLUMN_SSL = "ssl";

        public static final String COLUMN_SSL_ACCEPT_ALL = "ssl_accept_all";

        public static final String COLUMN_NICK_ONE = "nick_one";

        public static final String COLUMN_NICK_TWO = "nick_two";

        public static final String COLUMN_NICK_THREE = "nick_three";

        public static final String COLUMN_REAL_NAME = "real_name";

        public static final String COLUMN_NICK_CHANGEABLE = "nick_changeable";

        public static final String COLUMN_AUTOJOIN = "autojoin";

        public static final String COLUMN_SERVER_USERNAME = "server_username";

        public static final String COLUMN_SERVER_PASSWORD = "server_password";

        public static final String COLUMN_SASL_USERNAME = "sasl_username";

        public static final String COLUMN_SASL_PASSWORD = "sasl_password";

        public static final String COLUMN_NICK_SERV_PASSWORD = "nick_serv_password";

        // Database creation sql statement
        public static final String TABLE_CREATE = String
                .format("create table %s(%s integer primary key autoincrement, %s text not null, "
                        + "%s text not null, %s integer, %s integer, %s integer, "
                        + "%s text not null, %s text not null, %s text not null, "
                        + "%s text not null, %s integer, %s text not null, %s text not null,"
                        + " %s text not null, %s text not null, %s text not null, "
                        + "%s text not null);",
                        TABLE_NAME, COLUMN_ID, COLUMN_TITLE, COLUMN_URL,
                        COLUMN_PORT, COLUMN_SSL, COLUMN_SSL_ACCEPT_ALL, COLUMN_NICK_ONE,
                        COLUMN_NICK_TWO, COLUMN_NICK_THREE, COLUMN_REAL_NAME,
                        COLUMN_NICK_CHANGEABLE, COLUMN_AUTOJOIN, COLUMN_SERVER_USERNAME,
                        COLUMN_SERVER_PASSWORD, COLUMN_SASL_USERNAME, COLUMN_SASL_PASSWORD,
                        COLUMN_NICK_SERV_PASSWORD);
    }
}