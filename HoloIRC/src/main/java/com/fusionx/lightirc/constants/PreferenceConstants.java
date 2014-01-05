/*
    HoloIRC - an IRC client for Android

    Copyright 2013 Lalit Maganti

    This file is part of HoloIRC.

    HoloIRC is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    HoloIRC is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with HoloIRC. If not, see <http://www.gnu.org/licenses/>.
 */

package com.fusionx.lightirc.constants;

/**
 * Contains the keys for retrieving settings from the SharedPreferences
 *
 * @author Lalit Maganti
 */
public class PreferenceConstants {

    // Server settings
    public final static String PREF_IGNORE_LIST = "pref_ignore_list";

    public final static String PREF_TITLE = "pref_title";

    public final static String PREF_URL = "pref_url";

    public final static String PREF_PORT = "pref_port";

    public final static String PREF_SSL = "pref_ssl";

    public final static String PREF_SSL_ACCEPT_ALL_CONNECTIONS = "pref_ssl_accept_all_connections";

    public final static String PREF_NICK = "pref_nick";

    public final static String PREF_SECOND_NICK = "pref_second_nick";

    public final static String PREF_THIRD_NICK = "pref_third_nick";

    public final static String PREF_REALNAME = "pref_realname";

    public final static String PREF_AUTO_NICK = "pref_auto_nick";

    public final static String PREF_AUTOJOIN = "pref_autojoin";

    public final static String PREF_LOGIN_USERNAME = "pref_login_username";

    public final static String PREF_LOGIN_PASSWORD = "pref_login_password";

    public final static String PREF_SASL_USERNAME = "pref_sasl_username";

    public final static String PREF_SASL_PASSWORD = "pref_sasl_password";

    public final static String PREF_NICKSERV_PASSWORD = "pref_nickserv_password";

    // Appearance Settings
    public final static String FRAGMENT_SETTINGS_THEME = "fragment_settings_theme";

    public final static String PREF_HIGHLIGHT_WHOLE_LINE = "pref_highlight_whole_line";

    // Server channel settings
    public final static String PREF_RECONNECT_TRIES = "pref_reconnect_tries";

    public final static String PREF_MOTD = "pref_motd";

    public final static String PREF_TIMESTAMPS = "pref_timestamps";

    public final static String PREF_QUIT_REASON = "pref_quit_reason";

    public final static String PREF_HIDE_MESSAGES = "pref_hide_messages";

    public final static String PREF_PART_REASON = "pref_part_reason";

    // Default user profile
    public final static String PREF_DEFAULT_FIRST_NICK = "pref_default_first_nick";

    public final static String PREF_DEFAULT_SECOND_NICK = "pref_default_second_nick";

    public final static String PREF_DEFAULT_THIRD_NICK = "pref_default_third_nick";

    public final static String PREF_DEFAULT_REALNAME = "pref_default_realname";

    public final static String PREF_DEFAULT_AUTO_NICK = "pref_default_auto_nick";

    // About settings
    public final static String PREF_APP_VERSION = "pref_app_version";

    public final static String PREF_SOURCE = "pref_source";
}