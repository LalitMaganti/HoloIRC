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

package com.fusionx.lightirc.ui;

import com.fusionx.lightirc.R;
import com.fusionx.lightirc.adapters.AnimatedServerListAdapter;
import com.fusionx.lightirc.adapters.ServerListAdapter;
import com.fusionx.lightirc.communication.IRCService;
import com.fusionx.lightirc.misc.AppPreferences;
import com.fusionx.lightirc.ui.widget.ServerCard;
import com.fusionx.lightirc.ui.widget.ServerCardInterface;
import com.fusionx.lightirc.util.SharedPreferencesUtils;
import com.fusionx.lightirc.util.UIUtils;
import com.fusionx.relay.Server;
import com.fusionx.relay.ServerConfiguration;
import com.fusionx.relay.ServerStatus;
import com.fusionx.relay.event.server.ConnectEvent;
import com.fusionx.relay.event.server.DisconnectEvent;
import com.squareup.otto.Subscribe;

import android.annotation.TargetApi;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.AbsListView;
import android.widget.GridView;

import java.util.ArrayList;
import java.util.Collection;

public class ServerListActivity extends ActionBarActivity implements ServerListAdapter.Callbacks,
        AnimatedServerListAdapter.SingleDismissCallback, ServerCard.Callbacks {

    private final ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(final ComponentName className, final IBinder binder) {
            mService = ((IRCService.IRCBinder) binder).getService();
            setUpServerList();

            final GridView listView = (GridView) findViewById(R.id.server_list);
            mAnimationAdapter = new AnimatedServerListAdapter(mServerCardsAdapter,
                    ServerListActivity.this);
            mAnimationAdapter.setAbsListView(listView);
            listView.setAdapter(mAnimationAdapter);
        }

        @Override
        public void onServiceDisconnected(final ComponentName name) {
            mServerCardsAdapter.notifyDataSetChanged();
        }
    };

    private IRCService mService = null;

    private ServerListAdapter mServerCardsAdapter = null;

    private AnimatedServerListAdapter mAnimationAdapter = null;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        AppPreferences.setUpPreferences(this);
        setTheme(UIUtils.getThemeInt());

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_server_list);

        mServerCardsAdapter = new ServerListAdapter(this, new ArrayList<ServerCardInterface>());
    }

    @Override
    protected void onStart() {
        super.onStart();

        final Intent service = new Intent(this, IRCService.class);
        service.putExtra("stop", false);
        startService(service);
        bindService(service, mConnection, 0);
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (mService != null) {
            setUpServerList();
        }

        if (mAnimationAdapter != null) {
            mAnimationAdapter.reset();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();

        for (ServerCardInterface builder : mServerCardsAdapter.getListOfItems()) {
            if (builder.getTitle() != null) {
                final Server sender = getServer(builder.getTitle());
                if (sender != null) {
                    try {
                        sender.getServerEventBus().unregister(this);
                    } catch (Exception ex) {
                        // Do nothing - we aren't registered it seems
                        // TODO - fix this properly
                    }
                }
            }
        }
        unbindService(mConnection);
        mService = null;
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
        final Intent intent = new Intent(this, AppPreferenceActivity.class);
        intent.putExtra("connectedServers", mServerCardsAdapter.getNumberOfConnectedServers());
        startActivity(intent);
    }

    private void setUpServerList() {
        final SharedPreferences globalSettings = getSharedPreferences("main", MODE_PRIVATE);
        final boolean firstRun = globalSettings.getBoolean("firstrun", true);

        if (firstRun) {
            SharedPreferencesUtils.firstTimeServerSetup(this);
            globalSettings.edit().putBoolean("firstrun", false).commit();
        }

        mServerCardsAdapter.clear();
        Collection<String> serverFiles = SharedPreferencesUtils.getServersFromPreferences(this);
        for (final String file : serverFiles) {
            ServerConfiguration.Builder builder = SharedPreferencesUtils.convertPrefsToBuilder
                    (this, file);
            final ServerCard card = new ServerCard(this, builder, this);
            mServerCardsAdapter.add(card);
            final Server sender = getServer(builder.getTitle());
            if (sender != null) {
                sender.getServerEventBus().register(this);
            }
        }
    }

    @Override
    public void disconnectFromServer(final ServerCard builder) {
        final Server server = getServer(builder.getTitle());
        if (server != null) {
            server.getServerEventBus().unregister(this);
            server.getServerCallBus().sendDisconnect();
        }

        mService.disconnect(builder.getTitle());
        mServerCardsAdapter.notifyDataSetChanged();
    }

    @Override
    public ArrayList<String> getServerTitles(ServerCard card) {
        return mServerCardsAdapter.getListOfTitles(card);
    }

    private void addNewServer() {
        final Intent intent = new Intent(ServerListActivity.this, ServerPreferenceActivity.class);

        intent.putExtra("new", true);
        intent.putExtra("file", "server");
        intent.putStringArrayListExtra("list", mServerCardsAdapter.getListOfTitles(null));
        startActivity(intent);
    }

    @Override
    public void deleteServer(final ServerCard builder) {
        mAnimationAdapter.animateDismiss(mServerCardsAdapter.getListOfItems().indexOf(builder));
    }

    @Override
    public boolean isServerAvailable(final String title) {
        final Server server = getServer(title);
        return mService != null && server != null && server.getStatus() != ServerStatus
                .DISCONNECTED;
    }

    // ServerListAdapter callbacks
    @Override
    public Server getServer(final String title) {
        return mService != null ? mService.getServerIfExists(title) : null;
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    @Override
    public void onDismiss(final AbsListView listView, int position) {
        final ServerCardInterface builder = mServerCardsAdapter.getItem(position);
        builder.onCardDismiss();

        listView.setAdapter(null);
        mServerCardsAdapter.remove(builder);
        mAnimationAdapter.notifyDataSetChanged();
        listView.setAdapter(mAnimationAdapter);
    }

    // Subscribe events
    @Subscribe
    public void onDisconnect(final DisconnectEvent event) {
        mServerCardsAdapter.notifyDataSetChanged();
    }

    @Subscribe
    public void onServerConnected(final ConnectEvent event) {
        mServerCardsAdapter.notifyDataSetChanged();
    }
}