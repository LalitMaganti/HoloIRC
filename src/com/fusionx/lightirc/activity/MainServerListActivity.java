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
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.PreferenceActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.PopupMenu;
import com.fima.cardsui.views.CardUI;
import com.fusionx.lightirc.R;
import com.fusionx.lightirc.activity.ServerSettingsActivity.BaseServerSettingFragment;
import com.fusionx.lightirc.cardsui.ServerCard;
import com.fusionx.lightirc.misc.Constants;
import com.fusionx.lightirc.services.IRCService;
import org.pircbotx.Configuration;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class MainServerListActivity extends Activity implements
        PopupMenu.OnMenuItemClickListener, PopupMenu.OnDismissListener {
    private ArrayList<Configuration.Builder> values;
    private IRCService service;
    private void connectToServer(final Configuration.Builder builder) {
        final Intent intent = new Intent(MainServerListActivity.this,
                ServerChannelActivity.class);
        intent.putExtra("server", builder);
        startActivity(intent);
    }

    private final ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(final ComponentName className, final IBinder binder) {
            service = ((IRCService.IRCBinder) binder).getService();
            setUpServerList();
        }
        @Override
        public void onServiceDisconnected(final ComponentName name) {
            service = null;
        }
    };

    private void setUpServerList() {
        final SharedPreferences settings = getSharedPreferences("main", MODE_PRIVATE);
        final boolean firstRun = settings.getBoolean("firstrun", true);
        int noOfServers = settings.getInt("noOfServers", 0);
        values = new ArrayList<Configuration.Builder>();

        if (firstRun) {
            noOfServers = firstRunAdditions();
            Editor e = settings.edit();
            e.putBoolean("firstrun", false);
            e.putInt("noOfServers", noOfServers);
            e.commit();
        }

        for (int i = 0; i < noOfServers; i++) {
            final SharedPreferences serverSettings = getSharedPreferences("server_" + i
                    , MODE_PRIVATE);
            Configuration.Builder bot = new Configuration.Builder();
            bot.setTitle(serverSettings.getString(Constants.Title, ""));
            bot.setServerHostname(serverSettings.getString(Constants.URL, ""));
            bot.setServerPort(Integer.parseInt(serverSettings.getString(Constants.Port, "6667")));
            bot.setName(serverSettings.getString(Constants.Nick, ""));
            bot.setLogin(serverSettings.getString(Constants.ServerUserName, "lightirc"));
            bot.setServerPassword(serverSettings.getString(Constants.ServerPassword, ""));

            final String nickServPassword = settings.getString(Constants
                    .NickServPassword, null);
            if (nickServPassword != null && !nickServPassword.equals("")) {
                bot.setNickservPassword(nickServPassword);
            }

            Set<String> auto = new HashSet<String>();
            auto = settings.getStringSet(Constants.AutoJoin, auto);
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
                ServerCard server;
                if(service.getBot(bot.getTitle()) != null) {
                    server = new ServerCard(bot.getTitle(),
                            service.getBot(bot.getTitle()).getStatus(), values.indexOf(bot));
                } else {
                    server = new ServerCard(bot.getTitle(),
                            "Disconnected", values.indexOf(bot));
                }
                mCardView.addCard(server);
            }

            mCardView.refresh();
        }
    }

    private void editServer(final Configuration.Builder builder) {
        Intent intent = new Intent(MainServerListActivity.this,
                ServerSettingsActivity.class);
        intent.putExtra(PreferenceActivity.EXTRA_SHOW_FRAGMENT,
                BaseServerSettingFragment.class.getName());
        intent.putExtra(PreferenceActivity.EXTRA_NO_HEADERS, true);
        intent.putExtra("indexOfServer", values.indexOf(builder));
        intent.putExtra("server", builder);
        startActivity(intent);
    }

    private int firstRunAdditions() {
        final int noOfServers = 1;
        SharedPreferences settings = getSharedPreferences("server_0", MODE_PRIVATE);
        final Editor e = settings.edit();

        e.putString(Constants.Title, "Freenode");
        e.putString(Constants.URL, "irc.freenode.net");
        e.putString(Constants.Port, "6667");
        e.putString(Constants.Nick, "LightIRCUser");
        e.putString(Constants.ServerUserName, "lightirc");

        HashSet<String> auto = new HashSet<String>();
        e.putStringSet(Constants.AutoJoin, auto);
        e.commit();

        return noOfServers;
    }

    public void onCardClick(final View v) {
        connectToServer(values.get((Integer) v.getTag()));
    }

    private Configuration.Builder builder;
    public void showPopup(final View v) {
        PopupMenu popup = new PopupMenu(this, v);
        builder = values.get((Integer) v.getTag());
        popup.inflate(R.menu.activity_server_list_popup);

        if(service.getBot(builder.getTitle()) != null &&
                service.getBot(builder.getTitle()).getStatus().equals("Connected")) {
            popup.getMenu().getItem(1).setEnabled(false);
        } else {
            popup.getMenu().getItem(0).setEnabled(false);
        }

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
            case R.id.activity_server_list_popup_edit:
                editServer(builder);
                builder = null;
                return true;
            case R.id.activity_server_list_popup_disconnect:
                disconnectFromServer(builder);
                builder = null;
                return true;
            default:
                return false;
        }
    }

    private void disconnectFromServer(Configuration.Builder builder) {
        service.disconnectFromServer(builder.getTitle());
        setUpServerList();
    }

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_server_list);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.activity_server_list_ab, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.activity_server_list_ab_add:
                addNewServer();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void addNewServer() {
        Intent intent = new Intent(MainServerListActivity.this,
                ServerSettingsActivity.class);
        intent.putExtra(PreferenceActivity.EXTRA_SHOW_FRAGMENT,
                BaseServerSettingFragment.class.getName());
        intent.putExtra(PreferenceActivity.EXTRA_NO_HEADERS, true);
        intent.putExtra("new", true);
        intent.putExtra("noOfServers", values.size());
        startActivity(intent);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(service == null) {
            final Intent servic = new Intent(this, IRCService.class);
            servic.putExtra("stop", false);
            startService(servic);
            bindService(servic, mConnection, 0);
        } else {
            setUpServerList();
        }
    }
}