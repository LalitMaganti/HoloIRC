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

        public static final String COLUMN_TITLE = "pref_title";

        public static final String COLUMN_URL = "pref_url";

        public static final String COLUMN_PORT = "pref_port";

        public static final String COLUMN_SSL = "pref_ssl";

        public static final String COLUMN_SSL_ACCEPT_ALL = "pref_ssl_accept_all_connections";

        public static final String COLUMN_NICK_ONE = "pref_nick";

        public static final String COLUMN_NICK_TWO = "pref_second_nick";

        public static final String COLUMN_NICK_THREE = "pref_third_nick";

        public static final String COLUMN_REAL_NAME = "pref_realname";

        public static final String COLUMN_NICK_CHANGEABLE = "pref_auto_nick";

        public static final String COLUMN_AUTOJOIN = "pref_autojoin";

        public static final String COLUMN_SERVER_USERNAME = "pref_login_username";

        public static final String COLUMN_SERVER_PASSWORD = "pref_login_password";

        public static final String COLUMN_SASL_USERNAME = "pref_sasl_username";

        public static final String COLUMN_SASL_PASSWORD = "pref_sasl_password";

        public static final String COLUMN_NICK_SERV_PASSWORD = "pref_nickserv_password";

        // Database creation sql statement
        public static final String TABLE_CREATE = String
                .format("create table %s(%s integer primary key autoincrement, %s text not null, "
                                + "%s text not null, %s integer, %s integer, %s integer, "
                                + "%s text not null, %s text not null, %s text not null, "
                                + "%s text not null, %s integer, %s text not null, %s text not null,"
                                + " %s text not null, %s text not null, %s text not null, "
                                + "%s text not null);",
                        TABLE_NAME, ServerTable._ID, COLUMN_TITLE, COLUMN_URL,
                        COLUMN_PORT, COLUMN_SSL, COLUMN_SSL_ACCEPT_ALL, COLUMN_NICK_ONE,
                        COLUMN_NICK_TWO, COLUMN_NICK_THREE, COLUMN_REAL_NAME,
                        COLUMN_NICK_CHANGEABLE, COLUMN_AUTOJOIN, COLUMN_SERVER_USERNAME,
                        COLUMN_SERVER_PASSWORD, COLUMN_SASL_USERNAME, COLUMN_SASL_PASSWORD,
                        COLUMN_NICK_SERV_PASSWORD
                );
    }
}