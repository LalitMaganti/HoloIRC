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

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.PopupMenu;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AbsListView;
import android.widget.GridView;

import com.fusionx.lightirc.R;
import com.fusionx.lightirc.adapters.AnimatedServerListAdapter;
import com.fusionx.lightirc.adapters.ServerListAdapter;
import com.fusionx.lightirc.collections.SynchronizedArrayList;
import com.fusionx.lightirc.communication.IRCService;
import com.fusionx.lightirc.communication.MessageSender;
import com.fusionx.lightirc.communication.ServerCommandSender;
import com.fusionx.lightirc.irc.Server;
import com.fusionx.lightirc.irc.ServerConfiguration;
import com.fusionx.lightirc.irc.event.ConnectedEvent;
import com.fusionx.lightirc.irc.event.FinalDisconnectEvent;
import com.fusionx.lightirc.irc.event.RetryPendingDisconnectEvent;
import com.fusionx.lightirc.util.SharedPreferencesUtils;
import com.fusionx.lightirc.util.UIUtils;
import com.squareup.otto.Subscribe;

import java.io.File;
import java.util.ArrayList;

public class ServerListActivity extends ActionBarActivity implements PopupMenu
        .OnMenuItemClickListener, PopupMenu.OnDismissListener,
        ServerListAdapter.BuilderAdapterCallback, AnimatedServerListAdapter.SingleDismissCallback {
    private IRCService mService = null;
    private ServerConfiguration.Builder mBuilder = null;
    private ServerListAdapter mServerCardsAdapter = null;
    private AnimatedServerListAdapter mAnimationAdapter = null;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        setTheme(UIUtils.getThemeInt(this));

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_server_list);

        SharedPreferencesUtils.setUpPreferences(this);
        mServerCardsAdapter = new ServerListAdapter(this,
                new SynchronizedArrayList<ServerConfiguration.Builder>());
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
    }

    @Override
    protected void onStop() {
        super.onStop();

        for (ServerConfiguration.Builder builder : mServerCardsAdapter.getListOfItems()) {
            final MessageSender sender = MessageSender.getSender(builder.getTitle(), true);
            if (sender != null) {
                try {
                    sender.getBus().unregister(this);
                } catch (Exception ex) {
                    // Do nothing - we aren't registered it seems
                    // TODO - fix this properly
                }
            }
        }
        unbindService(mConnection);
        mService = null;
    }

    @Subscribe
    public void onDisconnect(final RetryPendingDisconnectEvent event) {
        mServerCardsAdapter.notifyDataSetChanged();
    }

    @Subscribe
    public void onDisconnect(final FinalDisconnectEvent event) {
        mServerCardsAdapter.notifyDataSetChanged();
    }

    private final ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(final ComponentName className, final IBinder binder) {
            mService = ((IRCService.IRCBinder) binder).getService();
            setUpServerList();

            final GridView listView = (GridView) findViewById(R.id.server_list);
            mAnimationAdapter = new AnimatedServerListAdapter
                    (mServerCardsAdapter, ServerListActivity.this);
            mAnimationAdapter.setAbsListView(listView);
            listView.setAdapter(mAnimationAdapter);
        }

        @Override
        public void onServiceDisconnected(final ComponentName name) {
        }
    };

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
        final Intent intent = new Intent(ServerListActivity.this,
                AppPreferenceActivity.class);
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
        ArrayList<String> serverFiles = SharedPreferencesUtils.getServersFromPreferences(this);
        for (final String file : serverFiles) {
            ServerConfiguration.Builder builder = SharedPreferencesUtils.convertPrefsToBuilder
                    (this, file);
            mServerCardsAdapter.add(builder);
            final MessageSender sender = MessageSender.getSender(builder.getTitle(), true);
            if (sender != null) {
                sender.getBus().register(this);
            }
        }
    }

    @Subscribe
    public void onServerConnected(final ConnectedEvent event) {
        mServerCardsAdapter.notifyDataSetChanged();
    }

    // Connect to server
    public void onCardClick(final View view) {
        final Intent intent = new Intent(ServerListActivity.this, IRCActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra("server", (ServerConfiguration.Builder) view.getTag());
        startActivity(intent);
    }

    // Popup menu
    public void showPopup(final View view) {
        final PopupMenu popup = new PopupMenu(this, view);
        mBuilder = (ServerConfiguration.Builder) view.getTag();
        popup.inflate(R.menu.activity_server_list_popup);

        if (serverIsAvailable(mBuilder.getTitle())) {
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
                break;
            case R.id.activity_server_list_popup_disconnect:
                disconnectFromServer(mBuilder);
                break;
            case R.id.activity_server_list_popup_delete:
                deleteServer(mBuilder);
                break;
            default:
                return false;
        }
        mBuilder = null;
        return true;
    }

    private void disconnectFromServer(final ServerConfiguration.Builder builder) {
        final MessageSender sender = MessageSender.getSender(builder.getTitle(), true);
        if (sender != null) {
            sender.getBus().unregister(this);
        }

        ServerCommandSender.sendDisconnect(mService.getServer(builder.getTitle()), this);
        mService.removeServerFromManager(builder.getTitle());
        mServerCardsAdapter.notifyDataSetChanged();
    }

    private void addNewServer() {
        final Intent intent = new Intent(ServerListActivity.this, ServerPreferenceActivity
                .class);

        intent.putExtra("new", true);
        intent.putExtra("file", "server");
        intent.putStringArrayListExtra("list", mServerCardsAdapter.getListOfTitles(null));
        startActivity(intent);
    }

    private void editServer(final ServerConfiguration.Builder builder) {
        final Intent intent = new Intent(ServerListActivity.this, ServerPreferenceActivity.class);

        intent.putExtra("file", builder.getFile());
        intent.putExtra("server", builder);
        intent.putStringArrayListExtra("list", mServerCardsAdapter.getListOfTitles(builder));
        startActivity(intent);
    }

    private void deleteServer(final ServerConfiguration.Builder builder) {
        mAnimationAdapter.animateDismiss(mServerCardsAdapter.getPosition(builder));
    }

    private boolean serverIsAvailable(final String title) {
        final Server server = getServer(title);
        return mService != null && server != null && !server.getStatus().equals(getString(R
                .string.status_disconnected));
    }

    // ServerListAdapter callbacks
    @Override
    public Server getServer(final String title) {
        return mService.getServer(title);
    }

    @SuppressWarnings("RedundantCast")
    @Override
    public void onDismiss(AbsListView listView, int position) {
        final ServerConfiguration.Builder builder = mServerCardsAdapter.getItem(position);
        final File folder = new File(SharedPreferencesUtils
                .getSharedPreferencesPath(this) + builder.getFile() + ".xml");
        folder.delete();
        ((GridView) listView).setAdapter(null);
        mServerCardsAdapter.remove(builder);
        mAnimationAdapter.notifyDataSetChanged();
        ((GridView) listView).setAdapter(mAnimationAdapter);
    }
}