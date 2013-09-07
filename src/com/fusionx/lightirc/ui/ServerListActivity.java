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
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.PopupMenu;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;

import com.fusionx.lightirc.R;
import com.fusionx.lightirc.adapters.ServerListAdapter;
import com.fusionx.lightirc.collections.BuilderList;
import com.fusionx.lightirc.irc.Server;
import com.fusionx.lightirc.irc.ServerConfiguration;
import com.fusionx.lightirc.uiircinterface.IRCBridgeService;
import com.fusionx.lightirc.uiircinterface.ServerCommandSender;
import com.fusionx.lightirc.util.SharedPreferencesUtils;
import com.fusionx.lightirc.util.UIUtils;
import com.github.espiandev.showcaseview.ShowcaseView;
import com.haarman.listviewanimations.BaseAdapterDecorator;
import com.haarman.listviewanimations.swinginadapters.prepared.SwingBottomInAnimationAdapter;

import java.io.File;
import java.util.ArrayList;

public class ServerListActivity extends ActionBarActivity implements PopupMenu
        .OnMenuItemClickListener, PopupMenu.OnDismissListener,
        ServerListAdapter.BuilderAdapterCallback, ShowcaseView.OnShowcaseEventListener {
    private IRCBridgeService mService = null;
    private BuilderList mBuilderList = null;
    private ServerConfiguration.Builder mBuilder = null;
    private ServerListAdapter mServerCardsAdapter = null;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        setTheme(UIUtils.getThemeInt(this));

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_server_list);

        mServerCardsAdapter = new ServerListAdapter(this);
        mBuilderList = new BuilderList();
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (mService == null) {
            final Intent service = new Intent(this, IRCBridgeService.class);
            service.putExtra("stop", false);
            startService(service);
            bindService(service, mConnection, 0);
        } else {
            setUpServerList();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();

        unbindService(mConnection);
        mService = null;
    }

    private final ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(final ComponentName className, final IBinder binder) {
            mService = ((IRCBridgeService.IRCBinder) binder).getService();
            setUpServerList();

            final ListView listView = (ListView) findViewById(R.id.server_list);
            final SwingBottomInAnimationAdapter adapter = new SwingBottomInAnimationAdapter
                    (new ServerCardsAdapter(mServerCardsAdapter));
            adapter.setAbsListView(listView);
            listView.setAdapter(adapter);
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

        setUpServers(SharedPreferencesUtils.getServersFromPreferences(this));
    }

    private void setUpServers(final ArrayList<String> serverFiles) {
        mBuilderList.clear();
        for (final String file : serverFiles) {
            mBuilderList.add(SharedPreferencesUtils.convertPrefsToBuilder(this, file));
        }

        mServerCardsAdapter.clear();
        if (!mBuilderList.isEmpty()) {
            for (final ServerConfiguration.Builder builder : mBuilderList) {
                mServerCardsAdapter.add(builder);
            }
        }
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
                deleteServer(mBuilder.getFile());
                break;
            default:
                return false;
        }
        mBuilder = null;
        return true;
    }

    private void disconnectFromServer(final ServerConfiguration.Builder builder) {
        ServerCommandSender.sendDisconnect(mService.getServer(builder.getTitle()), this);
        mService.removeServerFromManager(builder.getTitle());
        mServerCardsAdapter.notifyDataSetChanged();
    }

    private void addNewServer() {
        final Intent intent = Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB ? new Intent
                (ServerListActivity.this, ServerPreferenceActivityCompat.class) : new Intent
                (ServerListActivity.this, ServerPreferenceActivityHC.class);

        intent.putExtra("new", true);
        intent.putExtra("file", "server");
        intent.putExtra("main", true);
        intent.putStringArrayListExtra("list", mBuilderList.getListOfTitles(null));
        startActivity(intent);
    }

    private void editServer(final ServerConfiguration.Builder builder) {
        final Intent intent = Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB ? new Intent
                (ServerListActivity.this, ServerPreferenceActivityCompat.class) : new Intent
                (ServerListActivity.this, ServerPreferenceActivityHC.class);

        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra("file", builder.getFile());
        intent.putStringArrayListExtra("list", mBuilderList.getListOfTitles(builder));
        intent.putExtra("server", builder);
        intent.putExtra("main", true);
        startActivity(intent);
    }

    private void deleteServer(final String fileName) {
        final ArrayList<String> servers = SharedPreferencesUtils
                .getServersFromPreferences(getApplicationContext());
        servers.remove(fileName);

        final File folder = new File(SharedPreferencesUtils
                .getSharedPreferencesPath(getApplicationContext()) + fileName + ".xml");
        folder.delete();

        setUpServers(servers);
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

    @Override
    public void onShowcaseViewHide(ShowcaseView showcaseView) {

    }

    @Override
    public void onShowcaseViewShow(ShowcaseView showcaseView) {

    }

    private class ServerCardsAdapter extends BaseAdapterDecorator {
        public ServerCardsAdapter(final ServerListAdapter adapter) {
            super(adapter);
        }
    }
}