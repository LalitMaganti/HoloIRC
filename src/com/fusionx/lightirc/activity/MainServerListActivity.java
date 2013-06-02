/*
    LightIRC - an IRC client for Android

    Copyright 2013 Lalit Maganti

    This file is part of LightIRC.

    LightIRC is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    LightIRC is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with LightIRC. If not, see <http://www.gnu.org/licenses/>.
 */

package com.fusionx.lightirc.activity;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.view.MenuItem;
import android.view.View;
import android.widget.PopupMenu;
import com.fima.cardsui.views.CardUI;
import com.fusionx.lightirc.R;
import com.fusionx.lightirc.activity.ServerSettingsActivity.BaseServerSettingFragment;
import com.fusionx.lightirc.cardsui.ServerCard;
import com.fusionx.lightirc.misc.Constants;
import org.pircbotx.Configuration;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class MainServerListActivity extends Activity implements
        PopupMenu.OnMenuItemClickListener, PopupMenu.OnDismissListener {
    private void connectToServer(final Configuration.Builder builder) {
        final Intent intent = new Intent(MainServerListActivity.this,
                ServerChannelActivity.class);
        intent.putExtra("server", builder);
        startActivity(intent);
    }

    private void editServer(final Configuration.Builder builder) {
        Intent intent = new Intent(MainServerListActivity.this,
                ServerSettingsActivity.class);
        intent.putExtra(PreferenceActivity.EXTRA_SHOW_FRAGMENT,
                BaseServerSettingFragment.class.getName());
        intent.putExtra(PreferenceActivity.EXTRA_NO_HEADERS, true);
        //TODO - not always 0
        intent.putExtra("indexOfServer", 0);
        intent.putExtra("server", builder);
        startActivity(intent);
    }

    private void getSetServerList() {
        final SharedPreferences settings = getSharedPreferences("main", 0);
        final boolean firstRun = settings.getBoolean("firstrun", true);
        int noOfServers = settings.getInt("noOfServers", 0);
        final ArrayList<Configuration.Builder> values = new ArrayList<Configuration.Builder>();

        if (firstRun) {
            noOfServers = firstRunAdditions(settings);
        }

        for (int i = 0; i < noOfServers; i++) {
            Configuration.Builder bot = new Configuration.Builder();
            bot.setTitle(settings.getString(Constants.titlePrefPrefix + i, ""));
            bot.setServerHostname(settings.getString(Constants.urlPrefPrefix + i, ""));
            bot.setServerPort(settings.getInt(Constants.serverPortPrefPrefix + i, 6667));
            bot.setName(settings.getString(Constants.nickPrefPrefix + i, ""));
            bot.setLogin(settings.getString(Constants
                    .serverUsernamePrefPrefix + i, "lightirc"));
            bot.setServerPassword(settings.getString(Constants
                    .serverPasswordPrefPrefix + i, ""));

            final String nickServPassword = settings.getString(Constants
                    .serverNickServPasswordPrefPrefix + i, null);
            if (nickServPassword != null && !nickServPassword.equals("")) {
                bot.setNickservPassword(nickServPassword);
            }

            Set<String> auto = new HashSet<String>();
            auto = settings.getStringSet(Constants.autoJoinPrefPrefix + i, auto);
            for (final String channel : auto) {
                bot.addAutoJoinChannel(channel);
            }
            values.add(bot);
        }

        if (!values.isEmpty()) {
            CardUI mCardView = (CardUI) findViewById(R.id.cardsview);
            mCardView.clearCards();
            mCardView.setSwipeable(false);
            for (Configuration.Builder bot : values) {
                ServerCard server = new ServerCard(bot.getTitle(),
                        bot.getServerHostname(), bot);
                mCardView.addCard(server);
            }

            mCardView.refresh();
        }
    }

    private int firstRunAdditions(final SharedPreferences settings) {
        final int noOfServers = 1;
        final Editor e = settings.edit();

        e.putBoolean("firstrun", false);
        e.putInt("noOfServers", noOfServers);

        e.putString(Constants.titlePrefPrefix + "0", "Freenode");
        e.putString(Constants.urlPrefPrefix + "0", "irc.freenode.net");
        e.putString(Constants.nickPrefPrefix + "0", "LightIRCUser");

        HashSet<String> auto = new HashSet<String>();
        e.putStringSet(Constants.autoJoinPrefPrefix + "0", auto);
        e.commit();

        return noOfServers;
    }

    public void onCardClick(final View v) {
        connectToServer((Configuration.Builder) v.getTag());
    }


    private Configuration.Builder builder;
    public void showPopup(final View v) {
        PopupMenu popup = new PopupMenu(this, v);
        builder = (Configuration.Builder) v.getTag();
        popup.inflate(R.menu.activity_server_list_cab);
        popup.setOnMenuItemClickListener(this);
        popup.setOnDismissListener(this);
        popup.show();
    }


    @Override
    public void onDismiss(PopupMenu popupMenu) {
        builder = null;
    }

    @Override
    public boolean onMenuItemClick(final MenuItem menuItem) {
        switch (menuItem.getItemId()) {
            case R.id.activity_server_list_cab_edit:
                editServer(builder);
                builder = null;
                return true;
            case R.id.activity_server_list_cab_connect:
                connectToServer(builder);
                builder = null;
                return true;
            default:
                return false;
        }
    }

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_server_list);
    }

    @Override
    protected void onResume() {
        super.onResume();
        getSetServerList();
    }
}