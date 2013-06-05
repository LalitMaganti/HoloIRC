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
import com.fusionx.lightirc.misc.Constants;
import com.fusionx.lightirc.service.IRCService;
import com.fusionx.lightirc.uisubclasses.ServerCard;
import org.pircbotx.Configuration;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class MainServerListActivity extends Activity implements PopupMenu.OnMenuItemClickListener,
        PopupMenu.OnDismissListener {
    private ArrayList<Configuration.Builder> values;
    private IRCService service;
    private Configuration.Builder builder;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_server_list);
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

    // Action bar
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

        final ArrayList<String> array = getListOfServersFormPrefsFiles();
        Integer in;
        if(!array.isEmpty()) {
            in = Integer.parseInt(array.get(array.size() - 1).replace("server_", "")) + 1;
        } else {
            in = 0;
        }

        intent.putExtra("file", "server_" + in);
        startActivity(intent);
    }

    private void setUpServerList() {
        final SharedPreferences globalSettings = getSharedPreferences("main", MODE_PRIVATE);
        final boolean firstRun = globalSettings.getBoolean("firstrun", true);
        values = new ArrayList<Configuration.Builder>();

        if (firstRun) {
            firstRunAdditions();
            final Editor e = globalSettings.edit();
            e.putBoolean("firstrun", false);

            e.commit();
        }

        setUpServers(getListOfServersFormPrefsFiles());
        setUpCards();
    }

    private void setUpServers(ArrayList<String> servers) {
        values.clear();
        for (final String server : servers) {
            final SharedPreferences serverSettings = getSharedPreferences(server, MODE_PRIVATE);
            final Configuration.Builder bot = new Configuration.Builder();
            bot.setTitle(serverSettings.getString(Constants.Title, ""));
            bot.setServerHostname(serverSettings.getString(Constants.URL, ""));
            bot.setServerPort(Integer.parseInt(serverSettings.getString(Constants.Port, "6667")));
            bot.setName(serverSettings.getString(Constants.Nick, ""));
            bot.setLogin(serverSettings.getString(Constants.ServerUserName, "lightirc"));
            bot.setServerPassword(serverSettings.getString(Constants.ServerPassword, ""));

            final String nickServPassword = serverSettings.getString(Constants
                    .NickServPassword, null);
            if (nickServPassword != null && !nickServPassword.equals("")) {
                bot.setNickservPassword(nickServPassword);
            }

            Set<String> auto = new HashSet<String>();
            auto = serverSettings.getStringSet(Constants.AutoJoin, auto);
            for (final String channel : auto) {
                bot.addAutoJoinChannel(channel);
            }

            bot.setFile(server);
            values.add(bot);
        }
    }

    private void setUpCards() {
        CardUI mCardView = (CardUI) findViewById(R.id.cardsview);
        mCardView.clearCards();
        if (!values.isEmpty()) {
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

    private void firstRunAdditions() {
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
    }

    private ArrayList<String> getListOfServersFormPrefsFiles() {
        ArrayList<String> array = new ArrayList<String>();
        File folder = new File(getFilesDir().getAbsolutePath().replace("files", "shared_prefs"));
        for(String file : folder.list()) {
            if(file.startsWith("server_")) {
                array.add(file.replace(".xml", ""));
            }
        }
        Collections.sort(array);
        return array;
    }

    // Connect to server
    public void onCardClick(final View v) {
        final Intent intent = new Intent(MainServerListActivity.this,
                ServerChannelActivity.class);
        intent.putExtra("server", values.get((Integer) v.getTag()));
        startActivity(intent);
    }

    // Popup menu
    public void showPopup(final View v) {
        PopupMenu popup = new PopupMenu(this, v);
        builder = values.get((Integer) v.getTag());
        popup.inflate(R.menu.activity_server_list_popup);

        if(service.getBot(builder.getTitle()) != null &&
                service.getBot(builder.getTitle()).getStatus().equals("Connected")) {
            popup.getMenu().getItem(1).setEnabled(false);
            popup.getMenu().getItem(2).setEnabled(false);
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
            case R.id.activity_server_list_popup_delete:
                deleteServer(builder.getFile());
                builder = null;
                return true;
            default:
                return false;
        }
    }

    private void deleteServer(String fileName) {
        final ArrayList<String> servers = getListOfServersFormPrefsFiles();
        servers.remove(fileName);
        File folder = new File(getFilesDir().getAbsolutePath()
                .replace("files", "shared_prefs/") + fileName + ".xml");
        folder.delete();
        setUpServers(servers);
        setUpCards();
    }

    private void disconnectFromServer(Configuration.Builder builder) {
        service.disconnectFromServer(builder.getTitle());
        setUpServerList();
    }

    private void editServer(final Configuration.Builder builder) {
        Intent intent = new Intent(MainServerListActivity.this,
                ServerSettingsActivity.class);
        intent.putExtra(PreferenceActivity.EXTRA_SHOW_FRAGMENT,
                BaseServerSettingFragment.class.getName());
        intent.putExtra(PreferenceActivity.EXTRA_NO_HEADERS, true);
        intent.putExtra("file", builder.getFile());
        intent.putExtra("server", builder);
        startActivity(intent);
    }
}