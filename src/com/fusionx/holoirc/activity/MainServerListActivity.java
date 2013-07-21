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

package com.fusionx.holoirc.activity;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.IBinder;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.PopupMenu;
import com.fusionx.holoirc.R;
import com.fusionx.holoirc.adapters.BuilderAdapter;
import com.fusionx.holoirc.misc.PreferenceKeys;
import com.fusionx.holoirc.misc.SharedPreferencesUtils;
import com.fusionx.holoirc.misc.Utils;
import com.fusionx.holoirc.service.IRCService;
import com.haarman.listviewanimations.BaseAdapterDecorator;
import com.haarman.listviewanimations.swinginadapters.prepared.SwingBottomInAnimationAdapter;
import org.pircbotx.Configuration;
import org.pircbotx.PircBotX;
import org.pircbotx.hooks.Event;
import org.pircbotx.hooks.Listener;
import org.pircbotx.hooks.ListenerAdapter;
import org.pircbotx.hooks.events.DisconnectEvent;
import org.pircbotx.hooks.events.holoirc.IOExceptionEvent;

import javax.net.SocketFactory;
import javax.net.ssl.SSLSocketFactory;
import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class MainServerListActivity extends Activity implements PopupMenu.OnMenuItemClickListener,
        PopupMenu.OnDismissListener, BuilderAdapter.BuilderAdapterListenerInterface {
    private IRCService mService = null;
    private ArrayList<Configuration.Builder> mBuilderList = null;
    private Configuration.Builder mBuilder = null;
    private BuilderAdapter mServerCardsAdapter = null;
    private final ListListener mListener = new ListListener();

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setTheme(Utils.getThemeInt(getApplicationContext()));
        setContentView(R.layout.activity_server_list);
    }

    @Override
    protected void onStart() {
        if (mService == null) {
            final Intent service = new Intent(this, IRCService.class);
            service.putExtra("stop", false);
            startService(service);
            bindService(service, mConnection, 0);
        } else {
            setUpListView();
            setUpServerList();
        }

        super.onStart();
    }

    @Override
    protected void onStop() {
        unbindService(mConnection);
        mService = null;

        super.onStop();
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
        }
    };

    private void setUpListView() {
        final ListView listView = (ListView) findViewById(R.id.server_list);
        mServerCardsAdapter = new BuilderAdapter(this);
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

        final ArrayList<String> array = SharedPreferencesUtils.getServersFromPreferences(getApplicationContext());
        final Integer in = array.isEmpty() ? 0 : Integer.parseInt(array.get(array.size() - 1).replace("server_", "")) + 1;

        intent.putExtra("file", "server_" + in);
        intent.putExtra("main", true);
        startActivity(intent);
    }

    private void setUpServerList() {
        final SharedPreferences globalSettings = getSharedPreferences("main", MODE_PRIVATE);
        final boolean firstRun = globalSettings.getBoolean("firstrun", true);
        mBuilderList = new ArrayList<>();

        if (firstRun) {
            SharedPreferencesUtils.firstTimeServerSetup(this);
            globalSettings.edit().putBoolean("firstrun", false).commit();
        }

        setUpServers(SharedPreferencesUtils.getServersFromPreferences(getApplicationContext()));
        setUpCards();
    }

    private void setUpServers(final ArrayList<String> servers) {
        mBuilderList.clear();
        for (final String server : servers) {
            final SharedPreferences serverSettings = getSharedPreferences(server, MODE_PRIVATE);
            final Configuration.Builder builder = new Configuration.Builder();
            builder.setTitle(serverSettings.getString(PreferenceKeys.Title, ""));
            builder.setServerHostname(serverSettings.getString(PreferenceKeys.URL, ""));
            builder.setServerPort(Integer.parseInt(serverSettings.getString(PreferenceKeys.Port, "6667")));
            final boolean ssl = serverSettings.getBoolean(PreferenceKeys.SSL, false);
            builder.setSocketFactory(ssl ? SSLSocketFactory.getDefault() : SocketFactory.getDefault());

            builder.setName(serverSettings.getString(PreferenceKeys.Nick, ""));
            builder.setRealname(serverSettings.getString(PreferenceKeys.RealName, "HoloIRC"));
            builder.setAutoNickChange(serverSettings.getBoolean(PreferenceKeys.AutoNickChange, true));

            final Set<String> auto = serverSettings.getStringSet(PreferenceKeys.AutoJoin, new HashSet<String>());
            for (final String channel : auto) {
                builder.addAutoJoinChannel(channel);
            }

            builder.setLogin(serverSettings.getString(PreferenceKeys.ServerUserName, "holoirc"));
            builder.setServerPassword(serverSettings.getString(PreferenceKeys.ServerPassword, ""));

            final String nickServPassword = serverSettings.getString(PreferenceKeys.NickServPassword, null);
            if (nickServPassword != null && !nickServPassword.equals("")) {
                builder.setNickservPassword(nickServPassword);
            }

            builder.setFile(server);
            mBuilderList.add(builder);

            if (serverIsConnected(builder.getTitle())) {
                getBot(builder.getTitle()).getConfiguration().getListenerManager().addListener(mListener);
            }
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

    // Connect to server
    public void onCardClick(final View v) {
        final Intent intent = new Intent(MainServerListActivity.this, IRCFragmentActivity.class);
        intent.putExtra("server", (Configuration.Builder) v.getTag());
        startActivity(intent);
    }

    // Popup menu
    public void showPopup(final View view) {
        final PopupMenu popup = new PopupMenu(this, view);
        mBuilder = (Configuration.Builder) view.getTag();
        popup.inflate(R.menu.activity_server_list_popup);

        if (serverIsConnected(mBuilder.getTitle())) {
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
        final ArrayList<String> servers = SharedPreferencesUtils.getServersFromPreferences(getApplicationContext());
        servers.remove(fileName);

        final File folder = new File(SharedPreferencesUtils.getSharedPreferencesPath(getApplicationContext())
                + fileName + ".xml");
        folder.delete();

        setUpServers(servers);
        setUpCards();
    }

    private void disconnectFromServer(final Configuration.Builder builder) {
        getBot(builder.getTitle()).getConfiguration().getListenerManager().removeListener(mListener);
        mService.disconnectFromServer(builder.getTitle());
        mServerCardsAdapter.notifyDataSetChanged();
    }

    private void editServer(final Configuration.Builder builder) {
        final Intent intent = new Intent(MainServerListActivity.this,
                ServerSettingsActivity.class);
        intent.putExtra("file", builder.getFile());
        intent.putExtra("server", builder);
        intent.putExtra("main", true);
        startActivity(intent);
    }

    private boolean serverIsConnected(final String title) {
        return mService != null && getBot(title) != null &&
                (getBot(title).getStatus().equals(getString(R.string.status_connected)));
    }

    // BuilderAdapter Listener Interface
    @Override
    public PircBotX getBot(final String title) {
        return mService.getBot(title);
    }

    private class ListListener extends ListenerAdapter<PircBotX> implements Listener<PircBotX> {
        @Override
        public void onEvent(final Event<PircBotX> event) throws Exception {
            if (event instanceof IOExceptionEvent)
                onIOException((IOExceptionEvent<PircBotX>) event);
            else
                super.onEvent(event);
        }

        @Override
        public void onDisconnect(final DisconnectEvent<PircBotX> event) {
            event.getBot().setStatus(getString(R.string.status_disconnected));

            mServerCardsAdapter.notifyDataSetChanged();
            event.getBot().getConfiguration().getListenerManager().removeListener(mListener);
        }

        public void onIOException(final IOExceptionEvent<PircBotX> event) {
            event.getBot().setStatus(getString(R.string.status_disconnected));

            mServerCardsAdapter.notifyDataSetChanged();
            event.getBot().getConfiguration().getListenerManager().removeListener(mListener);
        }
    }

    private class ServerCardsAdapter extends BaseAdapterDecorator {
        public ServerCardsAdapter(final BuilderAdapter adapter) {
            super(adapter);
        }
    }
}