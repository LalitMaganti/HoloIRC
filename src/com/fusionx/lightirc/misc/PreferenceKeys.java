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

package com.fusionx.lightirc.misc;

/**
 * Contains the keys for retrieving settings from the SharedPreferences
 *
 * @author Lalit Maganti
 */
public class PreferenceKeys {

    // Server settings
    public final static String IgnoreList = "pref_ignore_list";

    public final static String Title = "pref_title";
    public final static String URL = "pref_url";
    public final static String Port = "pref_port";
    public final static String SSL = "pref_ssl";

    public final static String FirstNick = "pref_nick";
    public final static String SecondNick = "pref_second_nick";
    public final static String ThirdNick = "pref_third_nick";
    public final static String RealName = "pref_realname";
    public final static String AutoNickChange = "pref_auto_nick";

    public final static String AutoJoin = "pref_autojoin";

    public final static String ServerUserName = "pref_login_username";
    public final static String ServerPassword = "pref_login_password";

    public final static String SaslUsername = "pref_sasl_username";
    public final static String SaslPassword = "pref_sasl_password";

    public final static String NickServPassword = "pref_nickserv_password";

    // Appearance Settings
    public final static String Theme = "fragment_settings_theme";

    // Server channel settings
    public final static String ReconnectTries = "pref_reconnect_tries";
    public final static String Motd = "pref_motd";
    public final static String QuitReason = "pref_quit_reason";

    public final static String HideMessages = "pref_hide_messages";
    public final static String PartReason = "pref_part_reason";

    // Default user profile
    public final static String DefaultFirstNick = "pref_default_first_nick";
    public final static String DefaultSecondNick = "pref_default_second_nick";
    public final static String DefaultThirdNick = "pref_default_third_nick";

    public final static String DefaultRealName = "pref_default_realname";
    public final static String DefaultAutoNickChange = "pref_default_auto_nick";

    // About settings
    public final static String AppVersion = "pref_app_version";
}