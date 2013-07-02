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
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.PopupMenu;
import com.fusionx.lightirc.R;
import com.fusionx.lightirc.adapters.BuilderAdapter;
import com.fusionx.lightirc.adapters.ServerCardsAdapter;
import com.fusionx.lightirc.misc.Constants;
import com.fusionx.lightirc.misc.Utils;
import com.fusionx.lightirc.service.IRCService;
import com.haarman.listviewanimations.swinginadapters.prepared.SwingBottomInAnimationAdapter;
import org.pircbotx.Configuration;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class MainServerListActivity extends Activity implements PopupMenu.OnMenuItemClickListener,
        PopupMenu.OnDismissListener {
    private ArrayList<Configuration.Builder> mBuilderList;
    private IRCService mService;
    private Configuration.Builder mBuilder;
    private BuilderAdapter mServerCardsAdapter;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTheme(Utils.getThemeInt(getApplicationContext()));

        setContentView(R.layout.activity_server_list);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mService == null) {
            final Intent service = new Intent(this, IRCService.class);
            service.putExtra("stop", false);
            startService(service);
            bindService(service, mConnection, 0);
        } else {
            setUpListView();
            setUpServerList();
        }
    }

    private final ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(final ComponentName className, final IBinder binder) {
            mService = ((IRCService.IRCBinder) binder).getService();
            setUpListView();
            setUpServerList();
        }

        @Override
        public void onServiceDisconnected(final ComponentName name) {
            mService = null;
        }
    };

    private void setUpListView() {
        final ListView listView = (ListView) findViewById(R.id.server_list);
        mServerCardsAdapter = new BuilderAdapter(mService, this);
        SwingBottomInAnimationAdapter swingBottomInAnimationAdapter
                = new SwingBottomInAnimationAdapter(new ServerCardsAdapter(mServerCardsAdapter));
        swingBottomInAnimationAdapter.setAbsListView(listView);

        listView.setAdapter(swingBottomInAnimationAdapter);
    }

    // Action bar
    @Override
    public boolean onCreateOptionsMenu(final Menu menu) {
        final MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.activity_server_list_ab, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        switch (item.getItemId()) {
            case R.id.activity_server_list_ab_settings:
                displaySettings();
                return true;
            case R.id.activity_server_list_ab_add:
                addNewServer();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void displaySettings() {
        Intent intent = new Intent(MainServerListActivity.this, SettingsActivity.class);
        startActivity(intent);
    }

    private void addNewServer() {
        Intent intent = new Intent(MainServerListActivity.this, ServerSettingsActivity.class);
        intent.putExtra("new", true);

        final ArrayList<String> array = getListOfServersFromPrefsFiles();
        Integer in;
        if (!array.isEmpty()) {
            in = Integer.parseInt(array.get(array.size() - 1).replace("server_", "")) + 1;
        } else {
            in = 0;
        }

        intent.putExtra("file", "server_" + in);
        intent.putExtra("main", true);
        startActivity(intent);
    }

    private void setUpServerList() {
        final SharedPreferences globalSettings = getSharedPreferences("main", MODE_PRIVATE);
        final boolean firstRun = globalSettings.getBoolean("firstrun", true);
        mBuilderList = new ArrayList<Configuration.Builder>();

        if (firstRun) {
            firstRunAdditions();
            final Editor e = globalSettings.edit();
            e.putBoolean("firstrun", false);

            e.commit();
        }

        setUpServers(getListOfServersFromPrefsFiles());
        setUpCards();
    }

    private void setUpServers(final ArrayList<String> servers) {
        mBuilderList.clear();
        for (final String server : servers) {
            final SharedPreferences serverSettings = getSharedPreferences(server, MODE_PRIVATE);
            final Configuration.Builder bot = new Configuration.Builder();
            bot.setTitle(serverSettings.getString(Constants.Title, ""));
            bot.setServerHostname(serverSettings.getString(Constants.URL, ""));
            bot.setServerPort(Integer.parseInt(serverSettings.getString(Constants.Port, "6667")));
            bot.setName(serverSettings.getString(Constants.Nick, ""));
            bot.setLogin(serverSettings.getString(Constants.ServerUserName, "lightirc"));
            bot.setServerPassword(serverSettings.getString(Constants.ServerPassword, ""));
            bot.setAutoNickChange(serverSettings.getBoolean(Constants.AutoNickChange, true));

            final String nickServPassword = serverSettings.getString(Constants
                    .NickServPassword, null);
            if (nickServPassword != null && !nickServPassword.equals("")) {
                bot.setNickservPassword(nickServPassword);
            }

            final Set<String> auto = serverSettings.getStringSet(Constants.AutoJoin, new HashSet<String>());
            for (final String channel : auto) {
                bot.addAutoJoinChannel(channel);
            }

            bot.setFile(server);
            mBuilderList.add(bot);
        }
    }

    private void setUpCards() {
        mServerCardsAdapter.clear();
        if (!mBuilderList.isEmpty()) {
            for (final Configuration.Builder bot : mBuilderList) {
                mServerCardsAdapter.add(bot);
            }
        }
    }

    private void firstRunAdditions() {
        final SharedPreferences settings = getSharedPreferences("server_0", MODE_PRIVATE);
        final Editor e = settings.edit();

        e.putString(Constants.Title, "Freenode");
        e.putString(Constants.URL, "irc.freenode.net");
        e.putString(Constants.Port, "6667");
        e.putString(Constants.Nick, "LightIRCUser");
        e.putString(Constants.ServerUserName, "lightirc");
        e.putBoolean(Constants.AutoNickChange, true);

        final HashSet<String> auto = new HashSet<String>();
        e.putStringSet(Constants.AutoJoin, auto);
        e.commit();
    }

    private ArrayList<String> getListOfServersFromPrefsFiles() {
        final ArrayList<String> array = new ArrayList<String>();
        final File folder = new File(getFilesDir().getAbsolutePath().replace("files", "shared_prefs"));
        for (final String file : folder.list()) {
            if (file.startsWith("server_")) {
                array.add(file.replace(".xml", ""));
            }
        }
        Collections.sort(array);
        return array;
    }

    // Connect to server
    public void onCardClick(final View v) {
        final Intent intent = new Intent(MainServerListActivity.this,
                IRCFragmentActivity.class);
        intent.putExtra("server", (Configuration.Builder) v.getTag());
        startActivity(intent);
    }

    // Popup menu
    public void showPopup(final View v) {
        final PopupMenu popup = new PopupMenu(this, v);
        mBuilder = (Configuration.Builder) v.getTag();
        popup.inflate(R.menu.activity_server_list_popup);

        if (mService != null && mService.getBot(mBuilder.getTitle()) != null &&
                (mService.getBot(mBuilder.getTitle()).getStatus().equals("Connected") ||
                        mService.getBot(mBuilder.getTitle()).getStatus().equals("Connecting"))) {
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
    public void onDismiss(final PopupMenu popupMenu) {
        mBuilder = null;
    }

    @Override
    public boolean onMenuItemClick(final MenuItem menuItem) {
        switch (menuItem.getItemId()) {
            case R.id.activity_server_list_popup_edit:
                editServer(mBuilder);
                mBuilder = null;
                return true;
            case R.id.activity_server_list_popup_disconnect:
                disconnectFromServer(mBuilder);
                mBuilder = null;
                return true;
            case R.id.activity_server_list_popup_delete:
                deleteServer(mBuilder.getFile());
                mBuilder = null;
                return true;
            default:
                return false;
        }
    }

    private void deleteServer(final String fileName) {
        final ArrayList<String> servers = getListOfServersFromPrefsFiles();
        servers.remove(fileName);
        final File folder = new File(getFilesDir().getAbsolutePath().
                replace("files", "shared_prefs/") + fileName + ".xml");
        folder.delete();
        setUpServers(servers);
        setUpCards();
    }

    private void disconnectFromServer(final Configuration.Builder builder) {
        mService.disconnectFromServer(builder.getTitle());
        setUpCards();
    }

    private void editServer(final Configuration.Builder builder) {
        final Intent intent = new Intent(MainServerListActivity.this,
                ServerSettingsActivity.class);
        intent.putExtra("file", builder.getFile());
        intent.putExtra("server", builder);
        intent.putExtra("main", true);
        startActivity(intent);
    }
}