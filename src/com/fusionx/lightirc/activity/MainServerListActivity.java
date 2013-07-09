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
import com.fusionx.lightirc.irc.LightThread;
import com.fusionx.lightirc.misc.PreferenceKeys;
import com.fusionx.lightirc.misc.Utils;
import com.fusionx.lightirc.service.IRCService;
import com.haarman.listviewanimations.swinginadapters.prepared.SwingBottomInAnimationAdapter;
import lombok.AccessLevel;
import lombok.Getter;
import org.pircbotx.Configuration;
import org.pircbotx.PircBotX;
import org.pircbotx.hooks.Listener;
import org.pircbotx.hooks.ListenerAdapter;

import javax.net.SocketFactory;
import javax.net.ssl.SSLSocketFactory;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class MainServerListActivity extends Activity implements PopupMenu.OnMenuItemClickListener,
        PopupMenu.OnDismissListener {
    @Getter(AccessLevel.PUBLIC)
    private IRCService service;
    private ArrayList<Configuration.Builder> mBuilderList;
    private Configuration.Builder mBuilder;
    private BuilderAdapter mServerCardsAdapter;

    private final MainActivityListener listener = new MainActivityListener();

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        setTheme(Utils.getThemeInt(getApplicationContext()));

        setContentView(R.layout.activity_server_list);
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void onResume() {
        if (service == null) {
            final Intent service = new Intent(this, IRCService.class);
            service.putExtra("stop", false);
            startService(service);
            bindService(service, mConnection, 0);
        } else {
            setUpListView();
            setUpServerList();
        }

        super.onResume();
    }

    @Override
    protected void onPause() {
        if (service != null) {
            for (LightThread thread : service.getThreadManager().values()) {
                thread.getBot().getConfiguration().getListenerManager().removeListener(listener);
            }
        }
        unbindService(mConnection);
        service = null;

        super.onPause();
    }

    private final ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(final ComponentName className, final IBinder binder) {
            service = ((IRCService.IRCBinder) binder).getService();
            setUpListView();
            setUpServerList();
            for (LightThread thread : service.getThreadManager().values()) {
                thread.getBot().getConfiguration().getListenerManager().addListener(listener);
            }
        }

        @Override
        public void onServiceDisconnected(final ComponentName name) {
            service = null;
            setUpServerList();
        }
    };

    private class MainActivityListener extends ListenerAdapter<PircBotX> implements Listener<PircBotX> {

    }

    private void setUpListView() {
        final ListView listView = (ListView) findViewById(R.id.server_list);
        mServerCardsAdapter = new BuilderAdapter(service, this);
        final SwingBottomInAnimationAdapter swingBottomInAnimationAdapter
                = new SwingBottomInAnimationAdapter(new ServerCardsAdapter(mServerCardsAdapter));
        swingBottomInAnimationAdapter.setAbsListView(listView);

        listView.setAdapter(swingBottomInAnimationAdapter);
    }

    // Action bar
    @Override
    public boolean onCreateOptionsMenu(final Menu menu) {
        final MenuInflater inflate = getMenuInflater();
        inflate.inflate(R.menu.activity_server_list_ab, menu);
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
        final Intent intent = new Intent(MainServerListActivity.this, SettingsActivity.class);
        startActivity(intent);
    }

    private void addNewServer() {
        final Intent intent = new Intent(MainServerListActivity.this, ServerSettingsActivity.class);
        intent.putExtra("new", true);

        final ArrayList<String> array = getListOfServersFromPreferencesFiles();
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
            globalSettings.edit().putBoolean("firstrun", false).commit();
        }

        setUpServers(getListOfServersFromPreferencesFiles());
        setUpCards();
    }

    private void setUpServers(final ArrayList<String> servers) {
        mBuilderList.clear();
        for (final String server : servers) {
            final SharedPreferences serverSettings = getSharedPreferences(server, MODE_PRIVATE);
            final Configuration.Builder bot = new Configuration.Builder();
            bot.setTitle(serverSettings.getString(PreferenceKeys.Title, ""));
            bot.setServerHostname(serverSettings.getString(PreferenceKeys.URL, ""));
            bot.setServerPort(Integer.parseInt(serverSettings.getString(PreferenceKeys.Port, "6667")));
            bot.setName(serverSettings.getString(PreferenceKeys.Nick, ""));
            bot.setLogin(serverSettings.getString(PreferenceKeys.ServerUserName, "lightirc"));
            bot.setServerPassword(serverSettings.getString(PreferenceKeys.ServerPassword, ""));

            final String nickServPassword = serverSettings.getString(PreferenceKeys.NickServPassword, null);
            if (nickServPassword != null && !nickServPassword.equals("")) {
                bot.setNickservPassword(nickServPassword);
            }

            bot.setAutoNickChange(serverSettings.getBoolean(PreferenceKeys.AutoNickChange, true));

            final boolean ssl = serverSettings.getBoolean(PreferenceKeys.SSL, false);
            if (ssl) {
                bot.setSocketFactory(SSLSocketFactory.getDefault());
            } else {
                bot.setSocketFactory(SocketFactory.getDefault());
            }

            final Set<String> auto = serverSettings.getStringSet(PreferenceKeys.AutoJoin, new HashSet<String>());
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

        e.putString(PreferenceKeys.Title, "Freenode");
        e.putString(PreferenceKeys.URL, "irc.freenode.net");
        e.putString(PreferenceKeys.Port, "6667");
        e.putString(PreferenceKeys.Nick, "LightIRCUser");
        e.putString(PreferenceKeys.ServerUserName, "lightirc");
        e.putBoolean(PreferenceKeys.AutoNickChange, true);
        e.putBoolean(PreferenceKeys.SSL, false);

        final HashSet<String> auto = new HashSet<String>();
        e.putStringSet(PreferenceKeys.AutoJoin, auto);
        e.commit();
    }

    private ArrayList<String> getListOfServersFromPreferencesFiles() {
        final ArrayList<String> array = new ArrayList<String>();
        final File folder = new File(Utils.getSharedPreferencesPath(getApplicationContext()));
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
        final Intent intent = new Intent(MainServerListActivity.this, IRCFragmentActivity.class);

        intent.putExtra("server", (Configuration.Builder) v.getTag());
        service = null;

        startActivity(intent);
    }

    // Popup menu
    public void showPopup(final View view) {
        final PopupMenu popup = new PopupMenu(this, view);
        mBuilder = (Configuration.Builder) view.getTag();
        popup.inflate(R.menu.activity_server_list_popup);

        if (service != null && service.getBot(mBuilder.getTitle()) != null &&
                !(service.getBot(mBuilder.getTitle()).getStatus().equals(getString(R.string.status_disconnected)))) {
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
        final ArrayList<String> servers = getListOfServersFromPreferencesFiles();
        servers.remove(fileName);

        final File folder = new File(Utils.getSharedPreferencesPath(getApplicationContext()) + fileName + ".xml");
        folder.delete();

        setUpServers(servers);
        setUpCards();
    }

    private void disconnectFromServer(final Configuration.Builder builder) {
        service.disconnectFromServer(builder.getTitle());
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